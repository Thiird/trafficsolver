package TrafficLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.util.vector.Vector2f;

import PathFinder.ProximitySensor;
import Utility.ResettableCountDownLatch;
import Utility_objects.XmlLoader;

public class RoadData
{
	//True user has pressed button to rebuild road
	public static volatile boolean toRebuild = false;
	public static volatile ResettableCountDownLatch isRebuilding = new ResettableCountDownLatch(1);

	public static volatile boolean showVehiclesColors = false;

	//True user changes solving mode
	public static volatile boolean toResetTraffic = false;

	public static volatile int transitsPerDirLimit = 3;

	//Data from XML files
	public static HashMap<String, String[]> bitMaskingData = null;
	public static HashMap<String, String> bitMasksToIndex = null;
	public static HashMap<String, String[]> pathsConfigs = null;
	public static HashMap<String, ArrayList<String>> blocksData = null;
	public static HashMap<String, String> directionsToPathsIndices = null;
	public static HashMap<String, String[]> pathIndexToPathInfo = null;
	public static HashMap<String, Path> pathsIndicesPathObj = null;
	public static HashMap<String, float[]> collisionData = null;

	//Maps routes indices to list of roadBlocks which make that route
	public static HashMap<Integer, int[]> routeToBlocksId = new HashMap<Integer, int[]>();

	//Maps routes indices to list of pathsConfigurations to use to go through the current route -- {1 : ["1,180,0","0,90,1",...]}
	public static HashMap<Integer, String[]> routeIndexToConfigId = new HashMap<Integer, String[]>();

	//Maps routes indices to list of directional values to use to go through the current route -- {1 : ["10","10",...]}
	public static HashMap<Integer, String[]> routeIndexToDirectionValues = new HashMap<Integer, String[]>();
	public static volatile ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentLinkedDeque<Vehicle>>> blockIdToVehicles = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentLinkedDeque<Vehicle>>>();
	public static ProximitySensor[][] proximityMap;
	public static HashMap<String, ArrayList<String>> collisionOccurences = new HashMap<String, ArrayList<String>>();

	public static Vector2f mapCenter = new Vector2f(0f, 0f);
	public static int[][] bitMap;
	public static ArrayList<ArrayList<RoadBlock>> road;
	public static ArrayList<ArrayList<IntersectionManager>> IntersectionManagerGrid;
	public static volatile int intersectionManagersCounter = 0;//N of int managers in road

	public static int gridWidth;
	public static int gridHeight;

	public static volatile float roadsAmount;
	public static volatile float obstaclesAmount;

	public static int vehiclesPerMinute;

	public static final float targetChasingDistance = 0.45f;
	public static volatile float cruiseVelocity = 0.01f;

	public static volatile int solveMode = 0;

	public static final float chasingFactor = 3f;//Vehicle following another vehicle must travel @ nextVehicleVelocity * this var velocity 
	public static float chasingVelocity = cruiseVelocity * chasingFactor;
	public static final float switchMainDirMinDist = 1f;

	public static float roadBlocksOffset;
	public static final int maxIntersectionSearchDist = 3;//In block unit

	public static final float mapHeight = 0.5f;//Half roadblock width
	public static final float pathHeight = 1.04f;
	public static final float vehicleHeight = 0.985f;

	public static int blockIndex = 0;//Used for RoadBlock's id
	public static int lastBlockIndex = 0;
	public static int routesIndex = 0;
	public static int obstaclesCounter = 0;
	public static volatile int vehiclesCreated = 0;
	public static volatile int solvedVehicles = 0;

	public static volatile LinkedList<Thread> newVehiclesThreads = new LinkedList<Thread>();
	public static volatile ConcurrentLinkedQueue<Vehicle> activeVehicles = new ConcurrentLinkedQueue<Vehicle>();
	public static volatile ConcurrentLinkedQueue<Vehicle> deadVehicles = new ConcurrentLinkedQueue<Vehicle>();

	public static boolean quitApplication = false;

	public static void initializeRoadDataStructures()
	{
		//Load datas from xml files
		bitMaskingData = XmlLoader.loadBitMasks();
		bitMasksToIndex = XmlLoader.loadBitMaskinToIndex();
		blocksData = XmlLoader.loadBlocksData();
		directionsToPathsIndices = XmlLoader.LoadDirectionsToPathsIndices();
		pathIndexToPathInfo = XmlLoader.LoadPathIndexToPathInfo();
		pathsConfigs = XmlLoader.LoadPathsConfigurations();
		collisionData = XmlLoader.LoadCollisionData();

		pathsIndicesPathObj = new HashMap<String, Path>();
	}

	public static int innerToOuterId(int id)
	{
		//innerCoords = { id % (w - 2), id / (w - 2) }

		return getBlockIdFromCoords((id % (RoadData.gridWidth - 2)) + 1, (id / (RoadData.gridWidth - 2)) + 1);
	}

	public static int getBlockIdFromCoords(int x, int y)
	{//Returns id of block in grid based on coordinates
		return RoadData.gridWidth * y + x; //Simple uh? :D
	}

	public static int[] getBlockCoordsFromId(int id)
	{//Returns coordinates of block in grid based on id
		return new int[] { id % RoadData.gridWidth, id / RoadData.gridWidth };
	}

	public static void computeCollisionOccurences()
	{
		int src1 = 0, dest1 = 0, src2 = 0, dest2 = 0;

		String collisionData = "NNNNNYNYNYYYNNYNYYNYYYYYNYY";
		String lastAdded = "";
		int iterationCounter = 0;

		//a and b control the first two numbers
		for (int a = 0; a != 4; a++)
		{
			dest1 = goToRight(src1);

			for (int b = 0; b != 3; b++)
			{
				src2 = goToRight(src1);

				//c and d control the last two numbers
				for (int c = 0; c != 3; c++)
				{
					dest2 = goToRight(src2);

					for (int d = 0; d != 3; d++)
					{
						if (iterationCounter == 27)
						{
							//System.out.println();
							iterationCounter = 0;
						}

						if ((collisionData.charAt(iterationCounter)) == 'Y')
						{
							if (!lastAdded.equals(Integer.toString(src1) + Integer.toString(dest1)))
							{
								lastAdded = Integer.toString(src1) + Integer.toString(dest1);
								collisionOccurences.put(lastAdded, new ArrayList<String>());
							}

							collisionOccurences.get(lastAdded).add(Integer.toString(src2) + Integer.toString(dest2));
							//System.out.println(Integer.toString(src1) + Integer.toString(dest1) + " - " + Integer.toString(src2) + Integer.toString(dest2) + " Y");
						}
						else
						{
							//System.out.println(Integer.toString(src1) + Integer.toString(dest1) + " - " + Integer.toString(src2) + Integer.toString(dest2) + " N");
						}

						iterationCounter++;

						dest2 = goToRight(dest2);
					}

					src2 = goToRight(src2);
				}

				//System.out.println("--------------");

				dest1 = goToRight(dest1);
			}

			//System.out.println("######################");

			src1 = goToRight(src1);
		}
	}

	public static int goToRight(int index)
	{
		if (index == 0) return 3;
		else if (index == 3) return 1;
		else if (index == 1) return 2;

		//Else if index == 2
		return 0;

	}
}