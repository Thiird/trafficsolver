package Threads;

import TrafficLogic.IncomingVehicle;
import TrafficLogic.IntersectionManager;
import TrafficLogic.RoadData;
import TrafficLogic.Vehicle;

public class ThreadIntersectionManager extends IntersectionManager implements Runnable
{
	protected int timeToken = -1;
	protected boolean executedOnce = false;

	public ThreadIntersectionManager(int blockId, int[] temp)
	{
		super(blockId, temp);
	}

	@Override
	public void run()
	{
		while (!terminate)
		{
			waitToAct();

			if (!terminate) executeJob();

			executedOnce = true;
			Executor.imDoneIntersection();
		}
	}

	private void executeJob()
	{
		switch (Monitor.getPhase())
		{
			case 1:
				this.incomingVehicles.clear();

				this.resetVehicleData();

				this.loadExitingVehicles();
				this.loadIncomingVehicles();

				this.sortIncomingVehiclesByDistance();

				this.intersectionIsClear();

				this.manageMainDirAndTransitLimit();

				if (RoadData.solveMode != 0) this.solveIntersection();//MAGICCC!|!!|11\\1!
				else
				{
					for (IncomingVehicle v : this.incomingVehicles)
						v.getVehicle().giveToken();
				}

				this.directionReservations.clear();

				break;

			case 3:
				this.exitingVehicleLoading.reset();

				this.detectTransit();

				this.updateValuesInGUI();
				break;
		}
	}

	private void manageMainDirAndTransitLimit()
	{//Updates mainDir (if necessary) and checks for transitsLimit

		if (this.incomingVehicles.size() == 0)
		{//No vehicles: reset main dir and transit count

			this.currentMainDir = -1;
			this.transits = 0;
			this.waitingList.clear();
		}
		else
		{//this.vehiclesList.size() >= 1

			if (this.mainDirTransit)
			{//if transit happened on main dir, need to check if there are other vehicles behind it,
				//yes? keep direction
				//no? change dir to most waiting one or next in distance if no waitings yet, reset counter
				//System.out.println("B");
				if (transits == RoadData.transitsPerDirLimit)
				{
					//	System.out.println("C");
					transits = 0;
					this.currentMainDir = this.getNextMainDir();
				}
				else if (!this.incomingVehicles.contains(this.dirToVehicleMap.get(this.currentMainDir)))
				{//If after main dir transit, there are no more vehicle on main dir, change dir
					//	System.out.println("D");
					transits = 0;
					this.currentMainDir = getNextMainDir();
				}
			}
			else
			{//Update main dir

				if (currentMainDir == -1 || dirToVehicleMap.get(this.currentMainDir).getVehicle() == null)
				{//No active dir? set one OR when user stops main dir vehicle

					this.currentMainDir = this.getNextMainDir();
				}
				else
				{
					if (dirToVehicleMap.get(this.currentMainDir).getVehicle().isNextVehicleBlocked())
					{//If there is an active dir, check if not moving because user blocked the vehicle before it, which has already entered intersection

						this.transits = 0;
						this.currentMainDir = this.getNextMainDir();
					}
					else if (dirToVehicleMap.get(this.currentMainDir).getDistFromIntersection() > 1.5f)
					{
						if (this.incomingVehicles.get(0) != dirToVehicleMap.get(this.currentMainDir))
						{
							this.transits = 0;
							this.currentMainDir = this.incomingVehicles.get(0).getIntEnterDir();
						}
					}

				}
			}
		}
	}

	private int getNextMainDir()
	{//Called when mainDir has to be changed

		this.transits = 0;

		//Returns longest waiting vehicle (1), if there are waiting vehicle, otherwise
		//returns closest, non-userBlocked, vehicle to intersection (2)

		//1
		tempDir = this.waitingList.getLongestWaitingDir();
		if (tempDir != -1)
		{
			this.waitingList.go(tempDir);
			return tempDir;
		}

		//2
		IncomingVehicle v;

		for (int i = 0; i < this.incomingVehicles.size(); i++)
		{
			v = this.incomingVehicles.get(i);

			if (v != dirToVehicleMap.get(this.currentMainDir) && !v.getVehicle().isBlockedByUser()) return v.getIntEnterDir();
		}

		//Reaches here if no more vehicles on main dir and all other incomingVehicles are blocked by user
		return -1;
	}

	private void detectTransit()
	{ //Detect vehicle transition on intersection

		this.mainDirTransit = false;

		if (lastVehicleWithToken != null)
		{
			//Load last vehicle to enter intersection in current dir
			Vehicle v = RoadData.blockIdToVehicles.get(this.blockId).get(lastVehicleWithToken.getDirectionInIntersection()).peekFirst();

			if (v != null && this.lastVehicleWithToken.getVehicle() == v)
			{
				transits++;
				this.mainDirTransit = true;
			}
		}
	}

	private void waitToAct()
	{
		while ((Monitor.getPhase() == 1 && Monitor.getPhase() == 3) || (Monitor.getPhase() == 1 && terminate) || (Monitor.getPhase() == 3 && terminate) || (Monitor.getPhase() == 0 && !terminate)
				|| (Monitor.getPhase() != 0 && executedOnce) || (Monitor.getPhase() != 0 && Monitor.getPhase() != 1 && Monitor.getPhase() != 3))
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