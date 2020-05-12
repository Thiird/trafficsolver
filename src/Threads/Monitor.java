package Threads;

import java.util.concurrent.atomic.AtomicInteger;

import Guis.Gui_ExecutionVisualization;

public class Monitor
{
	private static volatile Monitor monitorInstance = null;
	public static int phase = 5;
	private static int subPhase = 0;

	private Monitor()
	{
		//Singleton design pattern
		//Thx Pera :D
	}

	public static synchronized Monitor getMonitor()
	{//Returns reference to this object

		if (monitorInstance == null) monitorInstance = new Monitor();

		return monitorInstance;
	}

	public static void release(AtomicInteger cont)
	{
		while (cont.get() != 0)
		{//Till there are threads to execute, keep releasing

			synchronized (getMonitor())
			{
				getMonitor().notifyAll();
			}
		}
	}

	public static synchronized void printToConsole(String s)
	{
		System.out.println(s);
	}

	public synchronized static int getPhase()
	{
		return phase;
	}

	public synchronized static int getSubPhase()
	{
		return subPhase;
	}

	public static void nextPhase()
	{
		synchronized (getMonitor())
		{
			switch (phase)
			{
				case 0:
					//Rebuild road
					phase++;
					break;
				case 1:
					//Intersection management
					phase++;
					break;
				case 2:
					//Vehicles have three subPhases
					if (subPhase == 0 || subPhase == 1) subPhase++;
					else
					{
						subPhase = 0;
						phase++;
					}
					break;
				case 3:
					//Intersections detect vehicle transitions
					phase++;
					break;
				case 4:
					phase++;
					break;
				case 5:
					//Remove Dead vehicles
					phase = 6;
					break;
				case 6:
					//Grid moving
					phase = 0;
					break;
			}

			Gui_ExecutionVisualization.setRoadPhase(phase);
			Gui_ExecutionVisualization.setRoadSubPhase(subPhase);
		}
	}

	public static void setSubPhase(int n)
	{
		subPhase = n;
	}
}