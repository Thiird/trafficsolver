package Utility_objects;

import java.util.Arrays;

public class VehicleWaitingList
{
	private int[] waitingList = new int[4];
	private int temp;
	private int dir;

	public VehicleWaitingList(int[] activeDirections)
	{
		waitingList[0] = activeDirections[0] == 0 ? -1 : 0;
		waitingList[1] = activeDirections[1] == 0 ? -1 : 0;
		waitingList[2] = activeDirections[2] == 0 ? -1 : 0;
		waitingList[3] = activeDirections[3] == 0 ? -1 : 0;
	}

	public void clear()
	{
		waitingList[0] = waitingList[0] == -1 ? -1 : 0;
		waitingList[1] = waitingList[1] == -1 ? -1 : 0;
		waitingList[2] = waitingList[2] == -1 ? -1 : 0;
		waitingList[3] = waitingList[3] == -1 ? -1 : 0;
	}

	public int getLongestWaitingDir()
	{//Returns the direction that has been waiting for the most time

		//Returns -1 if list is empty
		if (waitingList[0] <= 0 && waitingList[1] <= 0 && waitingList[2] <= 0 && waitingList[3] <= 0) return -1;

		//Search dir
		temp = waitingList[0];
		dir = 0;

		for (int i = 1; i < waitingList.length; i++)
		{
			if (waitingList[i] > temp)
			{
				temp = waitingList[i];
				dir = i;
			}
		}

		return dir;
	}

	public void waits(int dir)
	{
		waitingList[dir]++;
	}

	public void go(int dir)
	{//Reset counter, vehicle has received permission to enter intersection
		waitingList[dir] = 0;
	}

	@Override
	public String toString()
	{
		return Arrays.toString(waitingList);
	}
}