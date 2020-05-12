package Threads;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import Guis.Gui_ControlPanel;
import Guis.Gui_MainWindow;
import Guis.Gui_VehicleStatus;
import Render_engine.Camera;
import Render_engine.EngineData;
import TrafficLogic.RoadBlock;
import TrafficLogic.RoadData;
import TrafficLogic.Vehicle;

public class KeyboardListener implements Runnable
{
	private static boolean justBuilded = false;
	private static boolean justCreatedVehicles = false;
	private static boolean justExecutedSimStep = false;
	private static boolean justStartedSim = false;
	private static boolean justStopped = false;
	private static boolean justResetTraffic = false;
	private static boolean alreadyCliked = false;
	private static boolean clikedFlag = false;
	private static boolean doubleClicked = false;
	static boolean leftMousePressed = false;
	static Vector2f lastMousePos = new Vector2f();
	static int[] coords = new int[2]; //Selected block coords in grid
	private static long timeSinceLastMousePress = System.currentTimeMillis();

	@Override
	public void run()
	{
		while (!RoadData.quitApplication)
		{
			listenForInput();
		}
	}

	public static void listenForInput()
	{
		doubleClicked = false;

		if (Mouse.isButtonDown(0))
		{
			if (!clikedFlag)//Mouse.hasClicked()
			{
				if (alreadyCliked)
				{//Second click

					if (-(timeSinceLastMousePress - System.currentTimeMillis()) <= 260)
					{//If second click was fast enough
						//System.out.print("DOUBLE CLICK MUDAFUKA");
						doubleClicked = true;
					}

					alreadyCliked = false;
				}
				else
				{//First click
					timeSinceLastMousePress = System.currentTimeMillis();
					alreadyCliked = true;
				}
				clikedFlag = true;
			}
		}
		else clikedFlag = false;

		//Simulation manual stepping
		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
		{
			if (!justExecutedSimStep && !Executor.runSimulation)
			{
				Executor.cycleCompleted = false;
				justExecutedSimStep = true;
			}
		}
		else justExecutedSimStep = false;

		//Manually pause play simulation
		if (Keyboard.isKeyDown(Keyboard.KEY_P))
		{
			if (!justStartedSim)
			{
				Gui_ControlPanel.simButton.doClick();

				justStartedSim = true;
			}
		}
		else justStartedSim = false;

		//Entity actions
		if (EngineData.selectedEntity != null && EngineData.selectedEntityType == 0)
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_S))
			{
				if (!justStopped)
				{
					((Vehicle) EngineData.selectedEntity).invertVehicleStatus();

					justStopped = true;
				}
			}
			else justStopped = false;

		}

		//Road rebuild
		if (Keyboard.isKeyDown(Keyboard.KEY_R))
		{
			if (!justBuilded)
			{
				if (!RoadData.toRebuild) Gui_ControlPanel.btnReBuildRoad.doClick();
				justBuilded = true;
			}
		}
		else justBuilded = false;

		//Traffic reset
		if (Keyboard.isKeyDown(Keyboard.KEY_T))
		{
			if (!justResetTraffic)
			{
				if (!RoadData.toRebuild) Gui_ControlPanel.btnResetTraffic.doClick();
				justResetTraffic = true;
			}
		}
		else justResetTraffic = false;

		//Add vehicle
		if (Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			if (Executor.runSimulation)
			{
				if (!justCreatedVehicles)
				{
					//Notify the Engine to create a vehicle
					EngineData.nOfVehiclesToLoad.getAndAdd(1);
					Executor.vehicleCreation.reset();
				}

				justCreatedVehicles = true;
			}
		}
		else justCreatedVehicles = false;

		//Entity selection
		detectEntitySelection();
	}

	private static void detectEntitySelection()
	{//Establishing if user has clicked on an entity

		if (Mouse.isButtonDown(0))
		{
			if (!Mouse.getEventButtonState() && !leftMousePressed)
			{
				lastMousePos.set(Mouse.getX(), Mouse.getY());
				leftMousePressed = true;
			}
		}
		else
		{
			if (leftMousePressed)
			{
				if (lastMousePos.getX() == Mouse.getX() && lastMousePos.getY() == Mouse.getY())
				{
					manageSelectedEntity();
				}

				leftMousePressed = false;
			}
		}
	}

	private static void manageSelectedEntity()
	{//Selecting or deselecting clicked entity

		if (EngineData.hoveringEntity != null)//TODO do I need this?
		{
			if (EngineData.selectedEntity != null)//TODO this too?
			{//If an entity is already in focus

				if (EngineData.selectedEntity == EngineData.hoveringEntity)
				{//Clicked on already selected entity
					deselectEntity();

				}
				else
				{//First time selecting entity
					selectEntity();
				}

				if (doubleClicked)
				{
					Camera.adjustCamToSelectedEntity();
				}
			}
			else
			{
				selectEntity();
			}
		}
	}

	private static void selectEntity()
	{
		if (EngineData.selectedEntity != null) EngineData.selectedEntity.setAlpha(0);

		try
		{
			EngineData.selectedEntity = EngineData.hoveringEntity;
			EngineData.selectedEntityType = EngineData.hoveringEntityType;

			EngineData.selectedEntity.setAlpha(Executor.transparencyValue);

			if (EngineData.selectedEntityType == 0)
			{
				EngineData.lastSelectedVehicle = (Vehicle) EngineData.selectedEntity;
				Gui_MainWindow.vehicleStatus.setVisible(true);
				EngineData.lastSelectedVehicle.updateValuesInGUI(); //TODO HEC
			}
			else if (EngineData.selectedEntityType == 1)
			{
				EngineData.lastSelectedRoadBlock = (RoadBlock) EngineData.selectedEntity;
				Gui_MainWindow.roadBlockStatus.setVisible(true);
				EngineData.lastSelectedRoadBlock.updateValuesInGUI();
			}
			else if (EngineData.selectedEntityType == 3)
			{
				coords = RoadData.getBlockCoordsFromId(((RoadBlock) EngineData.selectedEntity).getId());

				EngineData.lastSelectedIntManager = RoadData.IntersectionManagerGrid.get(coords[1]).get(coords[0]);
				Gui_MainWindow.intersectionStatus.setVisible(true);
				EngineData.lastSelectedIntManager.updateValuesInGUI();
			}
		}
		catch (Exception e)
		{
			//LastSelectedEntity may be null because entity selection/deselection is not synchronized
		}
	}

	public static void deselectEntity()
	{
		if (EngineData.selectedEntity != null)
		{//Will be null when called on gui close

			if (EngineData.selectedEntityType == 0)
			{
				EngineData.lastSelectedVehicle = null;
				Gui_MainWindow.vehicleStatus.setVisible(false);
				Gui_VehicleStatus.resetValues();
			}
			else if (EngineData.selectedEntityType == 1)
			{
				EngineData.lastSelectedRoadBlock = null;
				Gui_MainWindow.roadBlockStatus.setVisible(false);
				Gui_MainWindow.roadBlockStatus.resetValues();
			}
			else if (EngineData.selectedEntityType == 3)
			{
				EngineData.lastSelectedIntManager = null;
				Gui_MainWindow.intersectionStatus.setVisible(false);
				Gui_MainWindow.intersectionStatus.resetValues();
			}

			EngineData.selectedEntityType = -1;
			EngineData.selectedEntity.setAlpha(0f);
			EngineData.selectedEntity = null;
		}
	}
}