package Threads;

import org.lwjgl.util.vector.Vector3f;

import Models.TexturedModel;
import TrafficLogic.Vehicle;

public class ThreadVehicle extends Vehicle implements Runnable
{
	protected int timeToken = -1;
	protected boolean executedOnce = false;

	public ThreadVehicle(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, float alpha, Vector3f colorId)
	{
		super(model, position, rotX, rotY, rotZ, scale, alpha, colorId);
	}

	@Override
	public void run()
	{
		while (!terminateVehicle) //Hasta la vista baby
		{
			waitToAct();

			if (!terminateVehicle) executeJob();//Vehicle may be awaken when rebuildRoad or resetTraffic, dont execute job in that case, just exit
			else removeVehicleFromRoad();

			executedOnce = true;
			Executor.imDoneVehicle();
		}
	}

	private void executeJob()
	{
		switch (Monitor.getSubPhase())
		{
			case 0:

				this.setOnIntersectionFlag();

				this.loadNextVehicleData();

				this.waitTurn();

				//Compute next state
				this.computeNextStatus();

				this.personalTimer.countDown();//Signal child vehicles to go

				//if (this.done) already true here if true

				break;

			case 1:
				//Apply next vehicle state: put vehicle in calculated position and update data structures
				this.updateBlockIdToVehicleMap();

				this.lengthOfBlockPathTraveled = this.tempPathTraveled;//TODO wtf is this

				this.setPosition(this.nextPosition);

				this.personalTimer.reset();

				this.updateValuesInGUI();

				this.intersectionToken = false;
				this.managedByIntManager = false;

				break;

			case 2:
				//Remove vehicle from road				

				if (this.done)
				{
					removeVehicleFromRoad();
					this.terminateVehicle = true;
				}

				break;
		}
	}

	private void waitToAct()
	{
		while ((Monitor.getPhase() != 2 && !terminateVehicle) || (executedOnce && !terminateVehicle) || (Monitor.getPhase() == 0 && Monitor.getPhase() == 2))
		{//See truth table file for expression understanding
			try
			{
				synchronized (Monitor.getMonitor())
				{
					Monitor.getMonitor().wait();

					//If I've already executed, check if roadPhase has changed
					if (executedOnce) if (timeToken != Executor.getTimeToken()) executedOnce = false;
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		timeToken = Executor.getTimeToken();
	}
}