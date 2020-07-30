package TrafficLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.lwjgl.util.vector.Vector3f;

import Entities.Entity;
import Exceptions.NoSolutionToPathFindingProcess;
import Guis.Gui_MainWindow;
import Models.TexturedModel;
import PathFinder.PathFinder;
import PathFinder.ProximitySensor;
import Render_engine.EngineData;
import Threads.Executor;
import Threads.Monitor;
import Threads.ThreadIntersectionManager;

public class RoadManager
{
	private static float blockSideLength = 1f;

	private static ArrayList<Integer> startBlocks = new ArrayList<Integer>();
	private static ArrayList<Integer> endBlocks = new ArrayList<Integer>();

	private static Random r = new Random();

	public static void initializeRoad()
	{
		Gui_MainWindow.setProgressBar("Initializing Road", 50);
		RoadData.road = new ArrayList<ArrayList<RoadBlock>>();
		RoadData.initializeRoadDataStructures();

		//Loads path obj to later be used
		preLoadPathObjects();
		Gui_MainWindow.showProgressBar(false);
	}

	public static void buildRoadGrid()
	{
		RoadData.proximityMap = new ProximitySensor[RoadData.gridHeight][RoadData.gridWidth];

		//Init bitMap
		RoadData.bitMap = new int[RoadData.gridHeight][RoadData.gridWidth];

		//Generate obstacles
		NoiseGenerator.generateNoise(RoadData.obstaclesAmount, true);

		computeRoutes();

		loadIntManagersAndRoadModels();

		initRoadBlockToVehicleMap();

		//No need to keep all this stuff in memory after the map has been loaded
		RoadData.proximityMap = null;

		RoadData.isRebuilding.countDown();
	}

	private static int getFrequency(int[] array)
	{//Count how many 1's this array contains
		int cont = 0;

		for (int element : array)
			if (element == 1) cont++;

		return cont;
	}

	private static void initRoadBlockToVehicleMap()
	{//Creates the correct structure to later put vehicles in

		ArrayList<Integer> previousBlockIds = new ArrayList<Integer>();

		previousBlockIds.addAll(RoadData.blockIdToVehicles.keySet());

		ArrayList<Integer> currentBlockIds = new ArrayList<Integer>();

		ConcurrentHashMap<String, ConcurrentLinkedDeque<Vehicle>> temp;
		String directionValues;
		String[] currentRouteDirValues;
		int blockIndex = 0; //Current position index in route

		//For each route
		for (int routeIndex : RoadData.routeToBlocksId.keySet())
		{
			currentRouteDirValues = RoadData.routeIndexToDirectionValues.get(routeIndex);

			//For each block of current route
			for (int blockId : RoadData.routeToBlocksId.get(routeIndex))
			{
				//If there is no info for the current block
				if (!RoadData.blockIdToVehicles.containsKey(blockId))
				{//Add keys and relative new linked lists

					directionValues = currentRouteDirValues[blockIndex];

					temp = new ConcurrentHashMap<String, ConcurrentLinkedDeque<Vehicle>>();
					temp.put(directionValues, new ConcurrentLinkedDeque<Vehicle>());

					RoadData.blockIdToVehicles.put(blockId, temp);
				}
				else
				{//Delete the older direction data, add the new

					//If first time "visiting block"
					if (!currentBlockIds.contains(blockId))
					{
						//Clear block's map
						RoadData.blockIdToVehicles.get(blockId).clear();
					}

					//Add new values if not already present
					directionValues = currentRouteDirValues[blockIndex];

					if (!RoadData.blockIdToVehicles.get(blockId).containsKey(directionValues))
					{
						RoadData.blockIdToVehicles.get(blockId).put(directionValues, new ConcurrentLinkedDeque<Vehicle>());
					}
				}

				blockIndex++;

				currentBlockIds.add(blockId);
			}

			blockIndex = 0;
		}

		//Removes other lists
		for (int oldBlockId : previousBlockIds)
		{
			if (!currentBlockIds.contains(oldBlockId))
			{
				RoadData.blockIdToVehicles.remove(oldBlockId);
			}
		}
	}

	public static void computeRoutes()
	{//Calculates all the paths

		//Remap value from 0-1 to 0-0.5
		//noiseAmount = low2 + (value - low1) * (high2 - low2) / (high1 - low1);
		//roadAmount = 0f + (roadAmount - 0f) * (1f - 0f) / (1f - 0f);

		int xStart = 0, yStart = 0;
		int xEnd = 0, yEnd = 0;
		int maxRoutes = 0;

		boolean manualRoads = false;

		if (manualRoads)
		{
			calculateRoute(2, 0, 2, RoadData.gridHeight - 2 - 1, 1);
			/*calculateRoute(5, RoadData.gridHeight - 2 - 1, 5, 0, 0);
			calculateRoute(5, 0, 5, RoadData.gridHeight - 2 - 1, 1);
			
			calculateRoute(0, 5, RoadData.gridWidth - 2 - 1, 5, 3);
			calculateRoute(RoadData.gridWidth - 2 - 1, 5, 0, 5, 2);*/
		}
		else
		{
			//Calculate routes from N side
			computeRoadEdgeRoutes(xStart, yStart, xEnd, yEnd, maxRoutes, 0);

			//Calculate routes from S side
			computeRoadEdgeRoutes(xStart, yStart, xEnd, yEnd, maxRoutes, 1);

			//Calculate routes from E side
			computeRoadEdgeRoutes(xStart, yStart, xEnd, yEnd, maxRoutes, 2);

			//Calculate routes from W side
			computeRoadEdgeRoutes(xStart, yStart, xEnd, yEnd, maxRoutes, 3);
		}
	}

	private static void computeRoadEdgeRoutes(int xStart, int yStart, int xEnd, int yEnd, int maxRoutes, int roadStartSide)
	{
		int innerWidth = RoadData.gridWidth - 2;
		int innerHeight = RoadData.gridHeight - 2;

		if (roadStartSide == 0 || roadStartSide == 1) maxRoutes = computeMaxRoutes(innerWidth);
		else maxRoutes = computeMaxRoutes(innerHeight);

		//maxRoutes = 2;

		//Calculate routes
		for (int i = 1; i < maxRoutes; i++)
		{
			switch (roadStartSide)
			{
				case 0:

					xStart = getRandomValue(startBlocks, innerWidth - 1);
					yStart = innerHeight - 1;

					xEnd = getRandomValue(endBlocks, innerWidth - 1);
					yEnd = 0;

					break;
				case 1:

					xStart = getRandomValue(startBlocks, innerWidth - 1);
					yStart = 0;

					xEnd = getRandomValue(endBlocks, innerWidth - 1);
					yEnd = innerHeight - 1;
					break;

				case 2:

					xStart = innerWidth - 1;
					yStart = getRandomValue(startBlocks, innerHeight - 1);

					xEnd = 0;
					yEnd = getRandomValue(endBlocks, innerHeight - 1);

					break;

				case 3:

					xStart = 0;
					yStart = getRandomValue(startBlocks, innerHeight - 1);

					xEnd = innerWidth - 1;
					yEnd = getRandomValue(endBlocks, innerHeight - 1);

					break;
			}

			calculateRoute(xStart, yStart, xEnd, yEnd, roadStartSide);
			Gui_MainWindow.setProgressBar("Computing roads...  " + i + " out of " + maxRoutes + ", side: " + roadStartSide, -1);

			startBlocks.clear();
			endBlocks.clear();
		}
	}

	private static int computeMaxRoutes(int dimension)
	{//Returns the max number of routes that can be created in the given dimension

		int maxRoutes;

		if (dimension % 2 == 0) maxRoutes = dimension / 2;
		else maxRoutes = (int) ((dimension - 2) / 2);

		maxRoutes = (int) (maxRoutes * ((float) EngineData.currentSettings.get("roadsAmount")[0]));

		//Clamp value
		if (maxRoutes < 2) maxRoutes = 2;

		return maxRoutes;
	}

	private static void calculateRoute(int xStart, int yStart, int xEnd, int yEnd, int roadStartSide)
	{//Updates the routes hashmap with a new route, given source-dest block indexes		

		//Calculate route
		ArrayList<Integer> route = null;

		try
		{
			route = PathFinder.findPath(xStart, yStart, xEnd, yEnd);
		}
		catch (NoSolutionToPathFindingProcess e)
		{
			e.printStackTrace();
		}

		//Change id's to outerGrid space
		for (int i = 0; i < route.size(); i++)
		{
			route.set(i, RoadData.innerToOuterId(route.get(i)));
		}

		addStartEndBlocks(route, roadStartSide);

		int[] tempPos;
		for (int i = 0; i < route.size(); i++)
		{
			//Update bitMap
			tempPos = RoadData.getBlockCoordsFromId(route.get(i));

			//Add route to bitMap, -2 means that block is part of the just calculated route
			RoadData.bitMap[tempPos[1]][tempPos[0]] = -2;
		}

		int[] tempRoute = intArrayListToArray(route);

		RoadData.routeToBlocksId.put(RoadData.routesIndex, tempRoute);

		RoadData.routeIndexToConfigId.put(RoadData.routesIndex, new String[tempRoute.length]);

		RoadData.routeIndexToDirectionValues.put(RoadData.routesIndex, new String[tempRoute.length]);

		RoadData.routesIndex++;

		updateMaps(tempRoute);
	}

	private static void addStartEndBlocks(ArrayList<Integer> route, int roadStartSide)
	{
		//Add new start and end block
		int[] elem1;

		elem1 = RoadData.getBlockCoordsFromId(route.get(0));
		route.add(0, nextBlock(elem1, roadStartSide));

		elem1 = RoadData.getBlockCoordsFromId(route.get(route.size() - 1));
		route.add(nextBlock(elem1, roadStartSide));
	}

	private static int nextBlock(int[] block, int roadStartSide)
	{//when I do elem[x] == 1 its like checking for elem[x] == 0, but I have to use 1 because I still have
		//to add first and last block of route, so the first and last block as of now are on row 1/column 1, and
		//I add one on row 0/columns 0

		if (block[0] == 1 && block[1] == 1)
		{//Check left lower corner

			if (roadStartSide == 0 || roadStartSide == 1)
			{
				return RoadData.getBlockIdFromCoords(block[0], block[1] - 1);
			}
			else if (roadStartSide == 2 || roadStartSide == 3)
			{
				return RoadData.getBlockIdFromCoords(block[0] - 1, block[1]);
			}
		}
		else if (block[0] == RoadData.gridWidth - 2 && block[1] == 1)
		{//Check right lower corner

			if (roadStartSide == 0 || roadStartSide == 1)
			{
				return RoadData.getBlockIdFromCoords(block[0], block[1] - 1);
			}
			else if (roadStartSide == 2 || roadStartSide == 3)
			{
				return RoadData.getBlockIdFromCoords(block[0], block[1] + 1);
			}
		}
		else if (block[0] == RoadData.gridWidth - 2 && block[1] == RoadData.gridHeight - 2)
		{//Check right upper corner

			if (roadStartSide == 0 || roadStartSide == 1)
			{
				return RoadData.getBlockIdFromCoords(block[0], block[1] + 1);
			}
			else if (roadStartSide == 2 || roadStartSide == 3)
			{
				return RoadData.getBlockIdFromCoords(block[0] + 1, block[1]);
			}
		}
		else if (block[0] == 1 && block[1] == RoadData.gridHeight - 2)
		{//Check left upper corner

			if (roadStartSide == 0 || roadStartSide == 1)
			{
				return RoadData.getBlockIdFromCoords(block[0], block[1] + 1);
			}
			else if (roadStartSide == 2 || roadStartSide == 3)
			{
				return RoadData.getBlockIdFromCoords(block[0] - 1, block[1]);
			}
		}
		else
		{//Not in any corner

			if (block[0] == 1)
			{
				//System.out.println("A");
				return RoadData.getBlockIdFromCoords(block[0] - 1, block[1]);

			}
			if (block[0] == RoadData.gridWidth - 2)
			{
				//System.out.println("B");
				return RoadData.getBlockIdFromCoords(block[0] + 1, block[1]);
			}

			if (block[1] == 1)
			{
				//System.out.println("C");
				return RoadData.getBlockIdFromCoords(block[0], block[1] - 1);
			}
			if (block[1] == RoadData.gridHeight - 2)
			{
				//System.out.println("D");
				return RoadData.getBlockIdFromCoords(block[0], block[1] + 1);
			}
		}

		return -1;
	}

	private static void updateMaps(int[] routeToAdd)
	{
		int[] comingFromCoords = new int[2];
		int[] goingToCoords = new int[2];
		int[] proximityArray = new int[4];//NSEW
		int[] tempArray = new int[4];//NSEW
		int[] blockPos;
		boolean[] tempBool = new boolean[4];
		ProximitySensor tempSensor;
		String index = null;

		//For each block index in the route
		for (int currentBlockId : routeToAdd)
		{
			//Load block position
			blockPos = RoadData.getBlockCoordsFromId(currentBlockId);

			//Load relative proximitySensor (if there is one)
			tempSensor = RoadData.proximityMap[blockPos[1]][blockPos[0]];

			//If there is a sensor in the current position
			if (tempSensor != null)
			{
				tempSensor.getProximityValues(proximityArray);

				getProximityBasedOnBits(blockPos[0], blockPos[1], tempArray);
				matchProximityValues(tempArray, proximityArray);

				//Maybe check if it has 0 in them, and if so check for startEnd
				checkStartEndBlocks(routeToAdd, currentBlockId, blockPos[0], blockPos[1], tempArray);
				matchProximityValues(tempArray, proximityArray);

				//Update sensor's proximity values
				tempSensor.setProximities(proximityArray);
			}
			else
			{//else add a sensor with correct settings

				//Get proximity from -2's
				getProximityBasedOnBits(blockPos[0], blockPos[1], proximityArray);

				checkStartEndBlocks(routeToAdd, currentBlockId, blockPos[0], blockPos[1], proximityArray);

				//Get proximity from sensors(if there is any), by checking only the 0's spots leaved by getProximityBasedOnBits
				getProximityBasedOnSensors(blockPos[0], blockPos[1], proximityArray);

				binaryToBoolean(proximityArray, tempBool);

				tempSensor = new ProximitySensor(tempBool[0], tempBool[1], tempBool[2], tempBool[3]);

				//Add proximitySensor in current position
				RoadData.proximityMap[blockPos[1]][blockPos[0]] = tempSensor;
			}
		}

		//Update bitMap value for each block in the route
		for (int blockIndex = 0; blockIndex < routeToAdd.length; blockIndex++)
		{
			//Load block position
			blockPos = RoadData.getBlockCoordsFromId(routeToAdd[blockIndex]);

			//-Load comingFrom-goingTo values
			//If current Block is first one in route
			if (blockIndex == 0)
			{
				comingFromCoords[0] = -1;
				comingFromCoords[1] = -1;
			}
			else
			{
				comingFromCoords = RoadData.getBlockCoordsFromId(routeToAdd[blockIndex - 1]);
			}
			//If current Block is last one in route
			if (blockIndex == routeToAdd.length - 1)
			{
				goingToCoords[0] = -1;
				goingToCoords[1] = -1;
			}
			else
			{
				goingToCoords = RoadData.getBlockCoordsFromId(routeToAdd[blockIndex + 1]);
			}

			//Load relative proximitySensor (if there is one)
			tempSensor = RoadData.proximityMap[blockPos[1]][blockPos[0]];

			//Update bitMap value
			updateBitMapValue(blockIndex, blockPos, comingFromCoords, goingToCoords, tempSensor, proximityArray, index);
		}
	}

	private static void updateBitMapValue(int blockIndex, int[] currentPos, int[] comingFromCoords, int[] goingToCoords, ProximitySensor tempSensor, int[] proximityArray, String index)
	{
		tempSensor.getProximityValues(proximityArray);

		String comingFromValue = Integer.toString(getComingFromValues(currentPos, comingFromCoords));
		String goingToValue = Integer.toString(getGoingToValues(currentPos, goingToCoords));

		//index = "proximityValues:comingFromValue:gointToValue"
		index = RoadData.bitMasksToIndex.get(Integer.toString(proximityArray[0]) + Integer.toString(proximityArray[1]) + Integer.toString(proximityArray[2]) + Integer.toString(proximityArray[3]) + ":"
				+ comingFromValue + ":" + goingToValue);

		//Set value into bitMap
		RoadData.bitMap[currentPos[1]][currentPos[0]] = Integer.parseInt(index);

		RoadData.routeIndexToConfigId.get(RoadData.routesIndex - 1)[blockIndex] = index;

		RoadData.routeIndexToDirectionValues.get(RoadData.routesIndex - 1)[blockIndex] = comingFromValue + goingToValue;
	}

	private static int getRandomValue(ArrayList<Integer> array, int innerDimension)
	{
		int n;
		//r.nextInt((max - min) + 1) + min;
		n = r.nextInt((innerDimension - 1) + 1) + 0;
		n++;

		while (array.contains(n))
		{
			n = r.nextInt((innerDimension - 1) + 1) + 0;
			n++;
		}

		array.add(n);

		return n;
	}

	private static int getGoingToValues(int[] currentPos, int[] gointToValues)
	{
		if (gointToValues[0] == -1)//also means gointToValues[1] == -1
		{//Current block is the first or last in route

			//If current block is on first row
			if (currentPos[1] == 0)
			{
				return 1;//value is S
			}
			//If current block is on last row
			else if (currentPos[1] == RoadData.gridHeight - 1)
			{
				return 0;//value is N
			}
			//If current block is on first column
			else if (currentPos[0] == 0)
			{
				return 3;//value is W
			}
			//If current block is on last column
			else
			{
				return 2;//values is E
			}
		}
		else
		{
			//Current block is neither the first or last in route
			if (gointToValues[0] == currentPos[0])//Checking x value
			{
				if (gointToValues[1] > currentPos[1])//Checking y value
				{
					return 0;//value is N
				}
				else if (gointToValues[1] < currentPos[1])
				{
					return 1;//value is S
				}
			}
			else
			{
				if (gointToValues[0] > currentPos[0])
				{
					return 2;//value is E
				}
				else
				{
					return 3;//value is W
				}
			}
		}

		return -1;
	}

	private static int getComingFromValues(int[] currentPos, int[] comingFromValue)
	{
		if (comingFromValue[0] == -1)//also means comingFromValue[1] == -1
		{//Current block is the first or last in route

			//If current block is on first column
			if (currentPos[0] == 0)
			{
				return 3;//value is W
			}
			//If current block is on first row
			else if (currentPos[1] == 0)
			{
				return 1;//value is S
			}
			//If current block is on last column
			else if (currentPos[0] == RoadData.gridWidth - 1)
			{
				return 2;//value is E
			}
			else//current block is on last row
			{
				return 0;
			}
		}
		else
		{
			//Current block is neither the first or last in route
			if (comingFromValue[0] == currentPos[0])//Checking x value
			{
				if (comingFromValue[1] > currentPos[1])//Checking y value
				{
					return 0;//value is N
				}
				else if (comingFromValue[1] < currentPos[1])
				{
					return 1;//value is S
				}
			}
			else
			{
				if (comingFromValue[0] > currentPos[0])
				{
					return 2;//value is E
				}
				else
				{
					return 3;//value is W
				}
			}
		}

		return -1;
	}

	private static void loadIntManagersAndRoadModels()
	{//Loads intersection managers and road blocks given value in bitMap

		int[] temp = new int[4];
		int frequency = 0;
		boolean isIntersection = false; //True when a roadblock represents an intersection

		ThreadIntersectionManager tempIntManager = null;

		//Init Map with all nulls
		RoadData.IntersectionManagerGrid = new ArrayList<ArrayList<IntersectionManager>>(); //TODO use clear() ?
		//Collections.nCopies(RoadData.gridHeight, new ArrayList<IntersectionManager>(Collections.nCopies(RoadData.gridWidth, null)))

		Vector3f initialCoords = initialBlockCoords();
		Vector3f blockPosition = new Vector3f(initialCoords.x, RoadData.mapHeight, initialCoords.y);
		ArrayList<Object> routes = new ArrayList<Object>();

		for (int y = 0; y < RoadData.gridHeight; y++) //For each row
		{
			RoadData.IntersectionManagerGrid.add(y, new ArrayList<IntersectionManager>());
			RoadData.road.add(y, new ArrayList<RoadBlock>());

			for (int x = 0; x < RoadData.gridWidth; x++) //For each column
			{
				isIntersection = false;

				//Create intersection manager -------------------------------------------
				if (RoadData.proximityMap[y][x] != null)
				{
					RoadData.proximityMap[y][x].getProximityValues(temp);

					frequency = getFrequency(temp);

					//3-way intersection
					if (frequency >= 3) isIntersection = true;
				}

				//Create road block -------------------------------------------
				blockPosition.setX(initialCoords.x + (x * blockSideLength) + (RoadData.roadBlocksOffset * x));

				blockPosition.setZ(initialCoords.z - (y * blockSideLength) - (RoadData.roadBlocksOffset * y));

				RoadData.road.get(y).add(x, loadRoadBlock(x, y, isIntersection, new Vector3f(blockPosition), RoadData.blockIndex, routes));

				RoadData.blockIndex++;

				if (isIntersection)
				{
					tempIntManager = new ThreadIntersectionManager(RoadData.getBlockIdFromCoords(x, y), temp);

					new Thread(tempIntManager).start();

					RoadData.IntersectionManagerGrid.get(y).add(x, tempIntManager);

					RoadData.intersectionManagersCounter++;
				}
				else
				{
					RoadData.IntersectionManagerGrid.get(y).add(x, null);
				}
			}
		}

		RoadData.lastBlockIndex = RoadData.blockIndex;
	}

	private static void preLoadPathObjects()
	{//Loads the three types of path objects

		for (String pathIndex : RoadData.pathIndexToPathInfo.keySet())
		{
			RoadData.pathsIndicesPathObj.put(pathIndex,
					EngineData.loadPath(RoadData.pathIndexToPathInfo.get(pathIndex)[0], null, Integer.parseInt(pathIndex), new Vector3f(0, 0, 0), 0, 0, 0, 1, null, 1f, 4));
		}
	}

	private static RoadBlock loadRoadBlock(int x, int y, boolean intersection, Vector3f blockPos, int blockId, ArrayList<Object> routes)
	{//Returns roadBlock with proper settings based on current mapIteration

		RoadBlock tempEntity;
		String[] maskingData;
		ArrayList<String> data;
		String randomBuildingValue;
		int typeOfRoadBlock;

		//If the current block is an "active" one 
		if (RoadData.bitMap[y][x] != -1)
		{//Load current block data - Example: data = [blockName(used for model and texture),blockDescritpion, route1, route2,....]

			maskingData = RoadData.bitMaskingData.get(Integer.toString(RoadData.bitMap[y][x]));
			data = RoadData.blocksData.get(maskingData[0]);

			//Used for 3D obj picking
			if (!intersection)
			{
				typeOfRoadBlock = getTypeOfRoadBlockIndex(data.get(1));
			}
			else
			{//Intersection
				typeOfRoadBlock = 3;
			}

			tempEntity = EngineData.loadRoadBlock("roadModels/roadBlocks/" + data.get(0), x, y, "roadBlocks/" + data.get(0), blockPos, 0f, Float.parseFloat(maskingData[1]), 0f, 1f,
					new Vector3f(255, 0, 0), 0f, data.get(1), typeOfRoadBlock, blockId);

			routes.clear();

			return tempEntity;
		}
		else//If its not active just load the null block
		{
			RoadData.obstaclesCounter++;
			randomBuildingValue = Integer.toString(r.nextInt(((4 - 1) - 1) + 1) + 1);

			tempEntity = EngineData.loadRoadBlock("roadModels/roadBlocks/building_" + randomBuildingValue, x, y, "roadBlocks/building_" + randomBuildingValue, blockPos, 0f, 0f, 0f, 1f,
					new Vector3f(255, 255, 0), 0f, "tomare", 1, blockId);

			return tempEntity;
		}
	}

	private static int getTypeOfRoadBlockIndex(String description)
	{//Used in the picking system
		if (description.equals("Obstacle") || description.equals("Not road"))
		{
			return 2;
		}
		else
		{
			return 1;
		}
	}

	public static void rebuildRoad() throws InterruptedException
	{//Cleans current status and builds another road based on the old parameters

		Gui_MainWindow.setProgressBar("Clearing all that traffic...", 15);

		VehiclesManager.clearTraffic();

		Gui_MainWindow.setProgressBar("Deleting intersections...", 25);

		clearIntersections();

		Gui_MainWindow.setProgressBar("Clearing road models...", 40);

		clearRoad();

		Gui_MainWindow.setProgressBar("Restarting Vehicle Manager...", 50);

		VehiclesManager.restart();

		Gui_MainWindow.setProgressBar("Restarting Intersection Manager...", 65);

		Gui_MainWindow.setProgressBar("Resetting indices...", 75);

		//Reset indexes
		RoadData.blockIndex = 0;
		RoadData.routesIndex = 0;
		RoadData.obstaclesCounter = 0;
		RoadData.intersectionManagersCounter = 0;
		EngineData.entityCounter = 1;

		Gui_MainWindow.setProgressBar("Clearing data structures...", 87);

		//Clean road data
		RoadData.routeToBlocksId.clear();
		RoadData.bitMap = null;
		RoadData.IntersectionManagerGrid = null;
		RoadData.blockIdToVehicles.clear();
		RoadData.road = new ArrayList<ArrayList<RoadBlock>>();

		Gui_MainWindow.setProgressBar("Computing roads...", 95);

		EngineData.updateGeneralVariables();

		//Rebuild road
		buildRoadGrid();

		EngineData.resetSelections();
	}

	public static void clearIntersections()
	{//Makes all the intersection manager threads quit, then wait to make sure they have quit

		//SUPER MEGA IMPORTANT THREADING LESSON, YOU MUST SET THIS MF BEFORE TERMINATING THE INT MANAGERS
		//BECAUSE WHEN U TERMINATE THEM, EVEN BEFORE YOU FINISHED THE FOR LOOP, SOME OF THEM WILL EXECUTE AND DECREMENT 
		//EXECUTOR.TORUNINTMANAGERS WHEN ITS STILL 0, SO METHOD Monitor.Release  WILL NEVER FINISH, BECAUSE CONT WILL ALWAYS BE != 0
		//SAME GOES FOR clearTraffic() METHOD
		Executor.toRunIntManagers.set(RoadData.intersectionManagersCounter);

		for (ArrayList<IntersectionManager> row : RoadData.IntersectionManagerGrid)
		{
			for (IntersectionManager intManager : row)
			{
				if (intManager != null) intManager.terminate();
			}
		}

		//Release int. managers threads so they can quit 
		Monitor.release(Executor.toRunIntManagers);
	}

	private static void clearRoad()
	{//Loads texturedModels and relative entities used in the road and deletes them

		TexturedModel tempTm;
		Entity tempEntity;

		HashMap<TexturedModel, ArrayList<Entity>> usedRoadBlocks = new HashMap<TexturedModel, ArrayList<Entity>>();

		//Load Entities to delete
		for (int y = 0; y < RoadData.gridHeight; y++) //For each row
		{
			for (int x = 0; x < RoadData.gridWidth; x++) //For each column
			{
				//Temp block
				tempEntity = RoadData.road.get(y).get(x);

				tempTm = tempEntity.getModel();

				if (!usedRoadBlocks.containsKey(tempTm))
				{
					usedRoadBlocks.put(tempTm, new ArrayList<Entity>(Arrays.asList(tempEntity)));
				}
				else
				{
					usedRoadBlocks.get(tempTm).add(tempEntity);
				}
			}
		}

		//Call DataManager to delete entities
		for (TexturedModel toDel : usedRoadBlocks.keySet())
		{
			EngineData.removeEntities(toDel, usedRoadBlocks.get(toDel));
		}
	}

	private static int getRandomYValue(ArrayList<Integer> array, int max, boolean uniqueValues, Random r)
	{
		int x;

		x = r.nextInt(((max - 1) - 0) + 1) + 0;
		x++;

		while (array.contains(x))
		{
			x = r.nextInt(((max - 1) - 0) + 1) + 0;
			x++;
		}

		array.add(x);

		return x;
	}

	private static void getProximityBasedOnBits(int x, int y, int[] proximityValues)
	{
		if (y < RoadData.gridHeight - 1)
		{
			if (RoadData.bitMap[y + 1][x] == -2)
			{
				proximityValues[0] = 1;
			}
			else
			{
				proximityValues[0] = 0;
			}
		}
		else
		{
			proximityValues[0] = 0;
		}

		if (y > 0)
		{
			if (RoadData.bitMap[y - 1][x] == -2)
			{
				proximityValues[1] = 1;
			}
			else
			{
				proximityValues[1] = 0;
			}
		}
		else
		{
			proximityValues[1] = 0;
		}

		if (x < RoadData.gridWidth - 1)
		{
			if (RoadData.bitMap[y][x + 1] == -2)
			{
				proximityValues[2] = 1;
			}
			else
			{
				proximityValues[2] = 0;
			}
		}
		else
		{
			proximityValues[2] = 0;
		}

		if (x > 0)
		{
			if (RoadData.bitMap[y][x - 1] == -2)
			{
				proximityValues[3] = 1;
			}
			else
			{
				proximityValues[3] = 0;
			}
		}
		else
		{
			proximityValues[3] = 0;
		}
	}

	private static void getProximityBasedOnSensors(int x, int y, int[] proximityValues)
	{//Keep the 'proximityValues[x] == 0' so that if that bit its already 1, it doesn't execute the inner if statement

		if (y < RoadData.gridHeight - 1 && proximityValues[0] == 0)
		{
			if (RoadData.proximityMap[y + 1][x] != null && RoadData.proximityMap[y + 1][x].facesSouth())
			{
				proximityValues[0] = 1;
			}
		}

		if (y > 0 && proximityValues[1] == 0)
		{
			if (RoadData.proximityMap[y - 1][x] != null && RoadData.proximityMap[y - 1][x].facesNorth())
			{
				proximityValues[1] = 1;
			}
		}

		if (x > 0 && proximityValues[3] == 0)
		{
			if (RoadData.proximityMap[y][x - 1] != null && RoadData.proximityMap[y][x - 1].facesEast())
			{
				proximityValues[3] = 1;
			}
		}

		if (x < RoadData.gridWidth - 1 && proximityValues[2] == 0)
		{
			if (RoadData.proximityMap[y][x + 1] != null && RoadData.proximityMap[y][x + 1].facesWest())
			{
				proximityValues[2] = 1;
			}
		}
	}

	private static void binaryToBoolean(int[] binaryArray, boolean[] boolArray)
	{
		/* Used to do it like this x'D
		if (binaryArray[0] == 1)
		{
			boolArray[0] = true;
		}
		else
		{
			boolArray[0] = false;
		}*/

		boolArray[0] = binaryArray[0] == 1;
		boolArray[1] = binaryArray[1] == 1;
		boolArray[2] = binaryArray[2] == 1;
		boolArray[3] = binaryArray[3] == 1;
	}

	private static void matchProximityValues(int[] src, int[] dest)
	{//Makes the second arrays proximity values equal to the first one

		for (int i = 0; i < src.length; i++)
			if (dest[i] == 0 && src[i] == 1) dest[i] = 1;
	}

	private static void checkStartEndBlocks(int[] currentRoute, int currentBlock, int x, int y, int[] proximityArray)
	{
		//If current route block is either the first or last on the route, set 1
		if (currentRoute[0] == currentBlock || currentRoute[currentRoute.length - 1] == currentBlock)
		{
			//If first row
			if (y == 0 && x > 0 && x < RoadData.gridWidth - 1)
			{
				proximityArray[1] = 1;
			}

			//If first column
			if (x == 0 && y > 0 && y < RoadData.gridHeight - 1)
			{
				proximityArray[3] = 1;
			}

			//If last row
			if (y == RoadData.gridHeight - 1 && x > 0 && x < RoadData.gridWidth - 1)
			{
				proximityArray[0] = 1;
			}

			//If last column
			if (x == RoadData.gridWidth - 1 && y > 0 && y < RoadData.gridHeight - 1)
			{
				proximityArray[2] = 1;
			}
		}
	}

	private static Vector3f initialBlockCoords()
	{
		//Position values
		float xEven = (RoadData.mapCenter.x - ((1f * ((RoadData.gridWidth - 1f) / 2f))));
		float yEven = (RoadData.mapCenter.y - ((1f * (RoadData.gridHeight / 2f)) - 1f / 2f));

		float xOdd = (RoadData.mapCenter.x - ((1f * (RoadData.gridWidth / 2f)) - 1f / 2f));
		float yOdd = (RoadData.mapCenter.y - ((1f * ((RoadData.gridHeight - 1f) / 2f))));

		//Offset values;
		float xEvenOffset = (RoadData.roadBlocksOffset * (((RoadData.gridWidth / 2f) - 1f))) + (RoadData.roadBlocksOffset / 2f);
		float yEvenOffset = (RoadData.roadBlocksOffset * (((RoadData.gridHeight / 2f) - 1f))) + (RoadData.roadBlocksOffset / 2f);

		float xOddOffset = RoadData.roadBlocksOffset * ((RoadData.gridWidth - 1f) / 2f);
		float yOddOffset = RoadData.roadBlocksOffset * ((RoadData.gridHeight - 1f) / 2f);

		Vector3f tempVec = new Vector3f();

		tempVec.setY(RoadData.mapHeight);

		if (RoadData.gridWidth % 2 == 0) tempVec.setX(xEven - xEvenOffset);
		else tempVec.setX(xOdd - xOddOffset);

		if (RoadData.gridHeight % 2 == 0) tempVec.setZ(-(yEven - yEvenOffset));
		else tempVec.setZ(-(yOdd - yOddOffset));

		return tempVec;
	}

	public static void setRoadBlocksPositions()
	{//Re-positions roadblocks based on current offset/mapHeight/blockSideLength settings
		//Useful for animation/debugging/showoff

		int blockId;

		Vector3f oldPos;
		Vector3f newPos;

		Vector3f offset = new Vector3f();//Offset to apply to roadBlock's vehicles to make them stick to it

		Vector3f newVehiclePos = new Vector3f();

		Vector3f initialCoords = initialBlockCoords();

		if (RoadData.road != null)//This is needed when method called in controlPanel class
		{
			for (int y = 0; y < RoadData.gridHeight; y++) //For each row
			{
				for (int x = 0; x < RoadData.gridWidth; x++) //For each column
				{
					//Register block position
					oldPos = RoadData.road.get(y).get(x).getPosition();

					//Calculate new position
					newPos = new Vector3f(initialCoords.x + (x * blockSideLength) + (RoadData.roadBlocksOffset * x), initialCoords.y,
							initialCoords.z - (y * blockSideLength) - (RoadData.roadBlocksOffset * y));

					//Calculate offset
					Vector3f.sub(newPos, oldPos, offset);

					//Move block to new position
					RoadData.road.get(y).get(x).setPosition(newPos);

					//Move roadBlock components
					blockId = RoadData.getBlockIdFromCoords(x, y);

					//Move vehicles TODO (try to optimize, now I cant see if there are vehicles, so it executes all the times)
					if (RoadData.blockIdToVehicles.containsKey(blockId))
					{//Current block is a road block

						Iterator<Vehicle> iterator;
						Vehicle tempV;

						for (String directionValues : RoadData.blockIdToVehicles.get(blockId).keySet())
						{
							iterator = RoadData.blockIdToVehicles.get(blockId).get(directionValues).iterator();

							while (iterator.hasNext())
							{
								tempV = iterator.next();

								if (tempV != null)
								{
									Vector3f.add(tempV.getPosition(), offset, newVehiclePos);

									tempV.setPosition(newVehiclePos.x, newVehiclePos.y, newVehiclePos.z);
									tempV.movePath();
								}
							}
						}
					}
				}
			}
		}
	}

	private static int[] intArrayListToArray(ArrayList<Integer> array)
	{
		int[] temp = new int[array.size()];

		for (int i = 0; i < array.size(); i++)
		{
			temp[i] = array.get(i);
		}
		return temp;
	}

	public static ArrayList<Integer> arrayToArrayList(int[] array)
	{
		ArrayList<Integer> temp = new ArrayList<Integer>();

		for (int i = 0; i < array.length; i++)
		{
			temp.add(array[i]);
		}

		return temp;
	}
}