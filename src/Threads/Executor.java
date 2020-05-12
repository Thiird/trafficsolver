package Threads;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.LWJGLException;

import Guis.Gui_ControlPanel;
import Guis.Gui_MainWindow;
import Guis.Gui_RoadStatus;
import Render_engine.EngineData;
import TrafficLogic.IntersectionManager;
import TrafficLogic.RoadData;
import TrafficLogic.RoadManager;
import TrafficLogic.VehiclesManager;
import Utility.ResettableCountDownLatch;

public class Executor implements Runnable
{
	private static float previousBlockOffset;
	public static ResettableCountDownLatch vehicleCreation = new ResettableCountDownLatch(1);
	public static ResettableCountDownLatch vehicleRemoval = new ResettableCountDownLatch(1);
	public static boolean cycleCompleted = false; //True when all 4 phases are executed 
	public static volatile boolean runSimulation = false; //True when simulation is running, false when it is paused
	public static volatile boolean showColors = false;
	public static volatile int solverDelay = 0;

	public static float transparencyValue = 0.0f;
	public static int cont = 0;
	public static volatile AtomicInteger toRunVehicles = new AtomicInteger(0);
	public static volatile AtomicInteger toRunIntManagers = new AtomicInteger(0);

	//Creation of vehicle
	private static long refTime;
	private static float timeElapsed;

	public static AtomicInteger timeToken = new AtomicInteger(0);

	public void run()
	{
		vehicleCreation.countDown();
		vehicleRemoval.countDown();

		while (!RoadData.quitApplication)
		{
			if (runSimulation || !(cycleCompleted))
			{
				cycleCompleted = false;

				synchronized (Monitor.getMonitor())
				{
					getTimeTokentAndIncrement();
					Monitor.nextPhase();
					prepareNofThreadsToRun();
					//Monitor.printToConsole("============================================== PHASE " + Monitor.getPhase() + " - " + Monitor.getSubPhase());
				}

				if (cont == 0) refTime = System.nanoTime();

				try
				{
					switch (Monitor.getPhase())
					{
						case 0:
							checkForRoadOrTrafficReset();

							break;
						case 1:
							//Solve intersections
							//Monitor.printToConsole("==============================================");
							Monitor.release(toRunIntManagers);

							break;
						case 2:
							//Run Vehicles
							Monitor.release(toRunVehicles);

							break;
						case 3:
							//Detect transitions
							Monitor.release(toRunIntManagers);

							break;
						case 4:
							timeElapsed += (System.nanoTime() - refTime) / 1000000000f;
							//Monitor.printToConsole(String.format("Elapsed time is %.7f\n", timeElapsed));
							if (createVehicles(timeElapsed))
							{
								vehicleCreation.reset();
								vehicleCreation.await();
								timeElapsed = 0;
							}
							refTime = System.nanoTime();

							break;
						case 5:
							checkForDeadVehicles();

							break;
						case 6:
							moveGrid();
							cycleCompleted = true;

							break;
					}

					Thread.sleep(solverDelay);
				}
				catch (InterruptedException | LWJGLException e)
				{
					e.printStackTrace();
				}

				timeElapsed += (System.nanoTime() - refTime) / 1000000000f;
			}

			updateGuisValues();
		}

		deleteRoad();
	}

	private static void deleteRoad()
	{//Terminates vehicles and intManagers

		VehiclesManager.clearTraffic();

		RoadManager.clearIntersections();
	}

	private static void prepareNofThreadsToRun()
	{
		switch (Monitor.getPhase())
		{
			case 1:
				toRunIntManagers.set(RoadData.intersectionManagersCounter);
				break;
			case 2:
				toRunVehicles.set(RoadData.activeVehicles.size());
				break;
			case 3:
				toRunIntManagers.set(RoadData.intersectionManagersCounter);
				break;
		}
	}

	public static int getTimeTokentAndIncrement()
	{
		synchronized (Monitor.getMonitor())
		{
			if (timeToken.get() == 99999)
			{
				timeToken.set(0);
				return timeToken.get();
			}
			else
			{
				return timeToken.getAndIncrement();
			}
		}
	}

	public static void imDoneIntersection()
	{
		toRunIntManagers.decrementAndGet();
	}

	public static void imDoneVehicle()
	{
		toRunVehicles.decrementAndGet();
	}

	public static int getTimeToken()
	{
		synchronized (Monitor.getMonitor())
		{
			return timeToken.get();
		}
	}

	private static void updateGuisValues()
	{
		//Vehicles updates themselves, they are running threads

		//Update roadblock gui
		if (EngineData.lastSelectedRoadBlock != null) EngineData.lastSelectedRoadBlock.updateValuesInGUI();

		//Update intManager gui
		if (EngineData.lastSelectedIntManager != null) EngineData.lastSelectedIntManager.updateValuesInGUI();

		//Update road
		if (Gui_RoadStatus.mainFrame.isVisible())
		{
			Gui_RoadStatus.roadGridDimensions.setText(RoadData.gridWidth + " * " + RoadData.gridHeight);
			Gui_RoadStatus.nOfRoutes.setText(Integer.toString(RoadData.routesIndex));
			Gui_RoadStatus.nOfRoadBlocks.setText(Integer.toString(RoadData.gridWidth * RoadData.gridHeight));
			Gui_RoadStatus.nOfObstacleBlocks.setText(Integer.toString(RoadData.obstaclesCounter));
			Gui_RoadStatus.nOfIntersections.setText(Integer.toString(RoadData.intersectionManagersCounter));
			Gui_RoadStatus.nOfActiveVehicles.setText(Integer.toString(RoadData.activeVehicles.size()));
			Gui_RoadStatus.nOfSolvedVehicles.setText(Integer.toString(RoadData.solvedVehicles));
		}
	}

	private static void checkForRoadOrTrafficReset() throws InterruptedException
	{
		if (RoadData.toRebuild)
		{
			Gui_MainWindow.showProgressBar(true);
			Gui_MainWindow.setProgressBar("Initializing...", 1);

			Gui_MainWindow.setProgressBar("Pausing renderer...", 5);

			//Wait until Main thread has rebuilt road
			RoadData.isRebuilding.reset();
			RoadData.isRebuilding.await();

			RoadData.toRebuild = false;

			Gui_MainWindow.setProgressBar("Road rebuilt!", 100);
			Gui_MainWindow.showProgressBar(false);
			Gui_ControlPanel.btnReBuildRoad.setEnabled(true);

		}

		if (RoadData.toResetTraffic)
		{
			VehiclesManager.clearTraffic();
			VehiclesManager.restart();

			//Reset all intersection data
			resetIntersections();

			RoadData.toResetTraffic = false;

			Gui_ControlPanel.btnResetTraffic.setEnabled(true);
		}
	}

	private static void resetIntersections()
	{//Reset all intersection vehicle data

		for (ArrayList<IntersectionManager> row : RoadData.IntersectionManagerGrid)
		{
			for (IntersectionManager intManager : row)
			{
				if (intManager != null) intManager.resetIntersection();
			}
		}
	}

	private static boolean createVehicles(float elapsedTime) throws LWJGLException, InterruptedException
	{
		return VehiclesManager.checkForVehicleCreation(elapsedTime);
	}

	public static void checkForDeadVehicles()
	{
		if (!RoadData.deadVehicles.isEmpty())
		{
			vehicleRemoval.reset();

			try
			{
				vehicleRemoval.await();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private static void moveGrid()
	{
		//If offset has changed
		if (RoadData.roadBlocksOffset != previousBlockOffset)
		{
			RoadManager.setRoadBlocksPositions();

			previousBlockOffset = RoadData.roadBlocksOffset;
		}
	}

	public static void resetVehicleTimer()
	{
		timeElapsed = 0;
	}
}