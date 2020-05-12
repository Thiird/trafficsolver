package Main;

import java.io.IOException;

import javax.swing.UnsupportedLookAndFeelException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import Display.DisplayManager;
import Entities.Light;
import Guis.Gui_ControlPanel;
import Guis.Gui_MainWindow;
import Render_engine.Camera;
import Render_engine.EngineData;
import Render_engine.MasterRenderer;
import Render_engine.PBOManager;
import Threads.Executor;
import Threads.KeyboardListener;
import Threads.Monitor;
import TrafficLogic.RoadData;
import TrafficLogic.RoadManager;
import TrafficLogic.VehiclesManager;

public class Main
{
	//Values used for highlight fade in-out
	public static final float transparencyPeriod = 1500f;
	public static int nOfSeconds = 1;
	private static long start = System.currentTimeMillis();
	private static float overlayLimit = 0.6f;
	public static boolean goUpOrDown = true; //True when going up, false down

	static DisplayManager display = null;

	//Entity selection
	boolean leftMousePressed = false;
	Vector2f lastMousePos = new Vector2f();
	int[] coords = new int[2]; //Selected block coords in grid

	public static void main(String[] args)
	{
		EngineData.initializeSettings();
		EngineData.updateGeneralVariables();
		EngineData.updateTrafficVariables();

		// Init Scene Objects
		try
		{
			display = new DisplayManager();
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | LWJGLException e1)
		{
			e1.printStackTrace();
		}

		//MasterRenderer renderer = new MasterRenderer(new Vector3f(0f, 1f, 0.37f)); //Green screen
		MasterRenderer renderer = new MasterRenderer(new Vector3f(0.5f, 0.5f, 0.5f));

		// Scene set-up
		Light light = new Light(new Vector3f(-2000, 2000, 2000), new Vector3f(1, 1, 1));
		Camera camera = new Camera(new Vector3f(10f, 10f, 10f));

		//View Matrix
		Matrix4f viewMat = null;

		PBOManager PBOManager = new PBOManager();

		EngineData.directionsGizmo = EngineData.loadEntity("others/directions_gizmo", "dirGizmoTexture", new Vector3f(0f, 4f, 0f), 0f, 0f, 0f, 1f, new Vector3f(0, 0, 255), false, 0f, 4);
		EngineData.initShowreelScene();

		RoadData.computeCollisionOccurences();
		RoadManager.initializeRoad();
		RoadManager.buildRoadGrid();

		new Thread(new KeyboardListener()).start();
		new Thread(new Executor()).start();

		Gui_MainWindow.controlPanel.setVisible(true);

		// Main rendering loop
		while (!Display.isCloseRequested())
		{
			try
			{
				// Capturing inputs to move the camera
				viewMat = camera.move();

				//Render to id FBO (for obj picking)
				MasterRenderer.renderIdView = true;
				renderer.renderScene(light, viewMat, camera.getPosition());
				PBOManager.readPixelData();
				MasterRenderer.renderIdView = false;

				//Render to normal window
				renderer.renderScene(light, viewMat, camera.getPosition());

				DisplayManager.update();

				CheckForVehicleDeletionRequest();
				CheckForRoadRebuildRequests();
				CheckForEntityLoadRequests();
				ChangeWindowRez();
				UpdateTransparencyValues();
				Gui_ControlPanel.changeState();
			}
			catch (LWJGLException | IOException | InterruptedException e)
			{
				e.printStackTrace();
			}

		}

		RoadData.quitApplication = true;

		//Let Executor and KeyboardListener thread to quit (not necessary but still who cares about waiting 5ms after user has pressed on close button)
		renderer.cleanUp();

		// Delete all VAOs, VBOs and Textures
		EngineData.cleanUp();

		PBOManager.cleanUp();

		// Close the display
		DisplayManager.closeDisplay();
		System.exit(0);
	}

	private static void CheckForVehicleDeletionRequest()
	{//Calling this here in main method because otherwise sooner of later renderer will try to render deleted entities, causing nullPointerException or concurrentModificationException
		//on toRenderEntities arraylist

		if (Executor.vehicleRemoval.getCount() != 0)
		{
			EngineData.removeDeadVehiclesFromRoad();
			Executor.vehicleRemoval.countDown();
		}
	}

	private static void ChangeWindowRez() throws LWJGLException, InterruptedException
	{//Checks if graphics settings have changed

		//If window size has changed, update size, then continue simulating
		if (Gui_ControlPanel.windowComboBox.getSelectedIndex() != DisplayManager.currentDisplayModeIndex)
		{
			DisplayManager.updateWindowResolution();
		}
	}

	private static void CheckForEntityLoadRequests() throws LWJGLException, InterruptedException
	{
		if (Executor.vehicleCreation.getCount() == 1)
		{
			VehiclesManager.createVehicles(EngineData.nOfVehiclesToLoad.get());

			EngineData.nOfVehiclesToLoad.set(0);

			//Done loading vehicles
			Executor.vehicleCreation.countDown();
		}
	}

	private static void CheckForRoadRebuildRequests() throws LWJGLException, InterruptedException
	{
		if (Monitor.getPhase() == 0)
		{
			if (RoadData.isRebuilding.getCount() == 1)
			{
				RoadManager.rebuildRoad();
				RoadData.isRebuilding.countDown();
			}
		}
	}

	private static void UpdateTransparencyValues()
	{//Alpha values used to blend the overlay color of the selected Entity with its base color in a fade out/in fascion

		//Update value
		if (goUpOrDown)
		{
			Executor.transparencyValue += (overlayLimit / nOfSeconds) * (((System.currentTimeMillis() - start) / (transparencyPeriod / 2)));
		}
		else
		{
			Executor.transparencyValue -= (overlayLimit / nOfSeconds) * (((System.currentTimeMillis() - start) / (transparencyPeriod / 2)));
		}

		//Clamp value and invert effect
		if (Executor.transparencyValue > overlayLimit)
		{
			Executor.transparencyValue = overlayLimit;
			goUpOrDown = false;
		}
		else if (Executor.transparencyValue < 0f)
		{
			Executor.transparencyValue = 0f;
			goUpOrDown = true;
		}

		start = System.currentTimeMillis();
	}
}