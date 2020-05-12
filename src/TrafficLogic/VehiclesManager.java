package TrafficLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.util.vector.Vector3f;

import Entities.Entity;
import Models.TexturedModel;
import Render_engine.EngineData;
import Threads.Executor;
import Threads.Monitor;
import Threads.ThreadVehicle;

public class VehiclesManager
{
	private static Random r = new Random();

	public static long creationReferenceTime = -10;
	public static long refTime;
	public static float vehiclesSpawnInterval = -1f; //Seconds between vehicles creation

	//Temp vars
	private static int choosenRoute;
	private static ThreadVehicle v;
	private static Vehicle tempV;
	private static TexturedModel tempTm;
	private static HashMap<TexturedModel, ArrayList<Entity>> usedVehicleModels = new HashMap<TexturedModel, ArrayList<Entity>>();

	public static boolean checkForVehicleCreation(float elapsedTime) throws LWJGLException, InterruptedException
	{//Checks how many vehicles must be created in the elapsed time
		//Returns true when vehicles are created

		//System.out.printf("Elapsed time is %.5f\n", elapsedTime);
		//System.out.println("vehicle spawn interval: " + vehiclesSpawnInterval);
		//System.out.println(
		//		vehiclesSpawnInterval + " > " + 0 + " : " + (vehiclesSpawnInterval > 0) + " --- " + elapsedTime + " > " + vehiclesSpawnInterval + " : " + (elapsedTime > vehiclesSpawnInterval));

		//Is -1 when vehiclesPerMinute in GUI is 0
		if (vehiclesSpawnInterval > 0)
		{
			//Get total amount of vehicles to create in elapsed time (usually 1)
			if (elapsedTime >= vehiclesSpawnInterval) //For creating just n number of vehicles apply this ' && RoadData.vehiclesCreated < n '
			{
				//Notify the Engine to create n vehicles
				EngineData.nOfVehiclesToLoad.getAndAdd((int) (elapsedTime / vehiclesSpawnInterval));

				return true;
			}
		}

		return false;
	}

	public static void updateSpawnIntervalValue()
	{
		if (RoadData.vehiclesPerMinute == 0)
		{
			vehiclesSpawnInterval = -1;
		}
		else
		{
			vehiclesSpawnInterval = (60f / RoadData.vehiclesPerMinute);
		}
	}

	public static void createVehicles(int toCreate)
	{
		int vehiclePlacingAttempt = 0;

		while (toCreate > 0)
		{
			//Route to add vehicle
			choosenRoute = r.nextInt(((RoadData.routesIndex - 1) - 0) + 1) + 0;//r.nextInt((max - min) + 1) + min;

			if (canPlaceVehicle(choosenRoute))
			{
				createVehicle(choosenRoute);
				RoadData.vehiclesCreated++;

				toCreate--;
			}
			else
			{
				vehiclePlacingAttempt++;
			}

			//If the number of attempts at placing a vehicle is equal to 2 * the number of routes, probably all the starting blocks are full 
			if (vehiclePlacingAttempt == 2 * RoadData.routesIndex)
			{
				break;
			}
		}
	}

	private static boolean canPlaceVehicle(int choosenRoute)
	{
		int tempBlockId = RoadData.routeToBlocksId.get(choosenRoute)[0];

		//If first block is empty
		for (String tempDirValue : RoadData.blockIdToVehicles.get(tempBlockId).keySet())
		{
			if (!RoadData.blockIdToVehicles.get(tempBlockId).get(tempDirValue).isEmpty()) return false;
		}

		return true;
	}

	public static void restart()
	{
		//TODO ARE THEY CORRECT in gui? (nope with lots of vehicles, solved vehicle remains the same if gui is closed when rebuilding)
		RoadData.vehiclesCreated = 0;
		RoadData.solvedVehicles = 0;
	}

	private static Vehicle findFirstVehicleOnRoute(int routeIndex)
	{
		int tempBlockId;

		for (int i = 0; i < RoadData.routeToBlocksId.get(routeIndex).length; i++)
		{
			tempBlockId = RoadData.routeToBlocksId.get(routeIndex)[i];

			String tempDirValue = RoadData.routeIndexToDirectionValues.get(routeIndex)[i];

			//If there are vehicles on the same direction
			if (RoadData.blockIdToVehicles.get(tempBlockId).get(tempDirValue).size() != 0)
			{
				//HEC
				return RoadData.blockIdToVehicles.get(tempBlockId).get(tempDirValue).getFirst();
			}
		}

		return null;
	}

	public static void createVehicle(int routeId)
	{// r.nextInt((max - min) + 1) + min;

		if ((r.nextInt((4000000 - 0) + 1) + 0) == 3051999)//Muahahahaha
		{
			v = EngineData.loadVehicle("others/car", "cool_car", new Vector3f(0, 0, 0), 0, 0, 0, 1f, new Vector3f(0, 0, 0), 0, 0);
		}
		else
		{
			v = EngineData.loadVehicle("others/car", "car_" + (r.nextInt((5 - 1) + 1) + 1), new Vector3f(0, 0, 0), 0, 0, 0, 1f, new Vector3f(0, 0, 0), 0, 0);
		}

		v.initialize(routeId);

		new Thread(v).start();
	}

	public static void clearTraffic()
	{//Loads texturedModels and relative entities used by the vehicles to then delete them

		//SET THIS BEFORE TERMINATING VEHICLES, SEE RoadManager.clearIntersection()
		Executor.toRunVehicles.set(RoadData.activeVehicles.size());

		Iterator<Vehicle> iterator = RoadData.activeVehicles.iterator();

		while (iterator.hasNext())
		{
			tempV = iterator.next();

			if (tempV != null)
			{
				tempV.terminateVehicle();
			}
		}
		//Release vehicles threads so they can quit
		Monitor.release(Executor.toRunVehicles);

		//Load Entities to delete
		for (int blockId : RoadData.blockIdToVehicles.keySet())
		{
			for (String directionValues : RoadData.blockIdToVehicles.get(blockId).keySet())
			{
				if (RoadData.blockIdToVehicles.get(blockId).get(directionValues).size() != 0)
				{
					iterator = RoadData.blockIdToVehicles.get(blockId).get(directionValues).iterator();

					while (iterator.hasNext())
					{
						tempV = iterator.next();

						tempTm = tempV.getModel();

						if (!usedVehicleModels.containsKey(tempTm))
						{
							usedVehicleModels.put(tempTm, new ArrayList<Entity>(Arrays.asList(tempV)));
						}
						else
						{
							usedVehicleModels.get(tempTm).add(tempV);
						}
					}

					iterator = null;
				}
			}
		}

		//Call DataManager to delete entities
		for (TexturedModel toDel : usedVehicleModels.keySet())
		{
			EngineData.removeEntities(toDel, usedVehicleModels.get(toDel));
		}
	}
}