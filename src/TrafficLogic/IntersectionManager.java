package TrafficLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import Guis.Gui_IntersectionStatus;
import Render_engine.EngineData;
import Utility.ResettableCountDownLatch;
import Utility_objects.VehicleWaitingList;

public class IntersectionManager
{//The field visibility is not specified, see here why: //https://stackoverflow.com/questions/215497/in-java-difference-between-package-private-public-protected-and-private

	protected boolean mainDirTransit = false; //True when transition happened on main dir in last cycle
	protected int currentMainDir = -1;
	protected int transits = 0;
	protected IncomingVehicle lastVehicleWithToken = null; //Used for debugging and showoff in intManager gui

	//Intersection manager infos
	protected volatile boolean terminate = false; //Set true when rebuilding road
	protected int blockId;
	protected RoadBlock roadBlock = null;
	protected int[] positionInGrid; //Position of intersection manager

	protected int[] focusDirections; //Directions in which the int manager has to "watch" for vehicles
	protected String distanceOrder = "";
	protected boolean isIntersectionFree = true; //True when there are no vehicles on intersection block
	protected ResettableCountDownLatch exitingVehicleLoading = new ResettableCountDownLatch(1);

	protected IncomingVehicle northVehicle = new IncomingVehicle();
	protected IncomingVehicle southVehicle = new IncomingVehicle();
	protected IncomingVehicle eastVehicle = new IncomingVehicle();
	protected IncomingVehicle westVehicle = new IncomingVehicle();

	protected Vehicle northExtVehicle = null;
	protected Vehicle southExtVehicle = null;
	protected Vehicle eastExtVehicle = null;
	protected Vehicle westExtVehicle = null;

	protected HashMap<Integer, IncomingVehicle> dirToVehicleMap = new HashMap<Integer, IncomingVehicle>(); //Maps NSEW bits to nortVehicle, southVehicle, .....
	protected ArrayList<IncomingVehicle> incomingVehicles = new ArrayList<IncomingVehicle>();
	protected ArrayList<String> directionReservations = new ArrayList<String>();
	protected VehicleWaitingList waitingList;// [waiting time for dir 0, ... , ... , waiting time for dir 3]
	protected ConcurrentLinkedDeque<Vehicle> currentBlockVQueue;

	protected ArrayList<IncomingVehicle> tempArray = new ArrayList<IncomingVehicle>();
	protected int tempDir;

	public IntersectionManager(int blockId, int[] temp)
	{
		this.blockId = blockId;
		this.positionInGrid = RoadData.getBlockCoordsFromId(this.blockId);
		this.roadBlock = RoadData.road.get(this.positionInGrid[1]).get(this.positionInGrid[0]);

		this.focusDirections = temp.clone();//TODO clone or also safe to use the same obj
		this.waitingList = new VehicleWaitingList(focusDirections);

		//Init vehicle map
		dirToVehicleMap.put(0, northVehicle);
		dirToVehicleMap.put(1, southVehicle);
		dirToVehicleMap.put(2, eastVehicle);
		dirToVehicleMap.put(3, westVehicle);
	}

	protected void solveIntersection()
	{//Where the magic happens

		if (this.currentMainDir != -1)
		{
			if (EngineData.lastSelectedIntManager == this) System.out.println("##################################");
			//Solve main dir
			this.solveMainDir();

			if (EngineData.lastSelectedIntManager == this) System.out.println("The rest:");
			//Solve the rest of vehicles: For each vehicle, closest to farthest from this intersection
			for (IncomingVehicle v : this.incomingVehicles)
			{
				if (v != this.dirToVehicleMap.get(this.currentMainDir))
				{//Skip main dir vehicle, already solved above

					if (RoadData.solveMode == 1)
					{//Basic solve

						this.solveBasic(v);
					}
					else //(RoadData.solveMode == 2)
					{//Advanced solve
						this.solveAdvanced(v);
					}
				}
			}
		}
	}

	protected void solveMainDir()
	{
		if (EngineData.lastSelectedIntManager == this) System.out.println("Main dir:");
		if (RoadData.solveMode == 1) this.solveBasic(this.dirToVehicleMap.get(this.currentMainDir));
		else this.solveAdvanced(this.dirToVehicleMap.get(this.currentMainDir));

		//this.incomingVehicles.remove(this.dirToVehicleMap.get(this.currentMainDir));
	}

	protected void solveBasic(IncomingVehicle v)
	{
		if (!this.isIntersectionFree) this.checkInternalBasic(v);
		else
		{
			if (!this.directionReservations.contains(v.getDirectionInIntersection()))
			{
				this.reserveVDirections(v);
				assignToken(v);
			}
		}
	}

	protected void checkInternalBasic(IncomingVehicle v)
	{//Checks for collision inside of intersection - Basic
		boolean freeToGo = true;

		//External check
		if (this.directionReservations.contains(v.getDirectionInIntersection()))
		{//Direction is already reserved

			if (v.getVehicle().getIsReadyToCross() && !v.getVehicle().isBlockedByUser()) this.waitingList.waits(v.getIntEnterDir());

			freeToGo = false;
		}
		else//Internal check
		{
			//For each direction with which the current vehicle can intersect
			for (String dirToCheck : RoadData.collisionOccurences.get(v.getDirectionInIntersection()))
			{
				//If that is a possible route in current road
				if (RoadData.blockIdToVehicles.get(this.blockId).containsKey(dirToCheck))
				{
					//If there are vehicles, no-go situation
					if (RoadData.blockIdToVehicles.get(this.blockId).get(dirToCheck).size() != 0)
					{
						if (v.getVehicle().getIsReadyToCross())
						{
							this.waitingList.waits(v.getIntEnterDir());
							this.reserveVDirections(v);
						}

						freeToGo = false;
						break;
					}
				}
			}
		}

		if (freeToGo) assignToken(v);
	}

	protected void solveAdvanced(IncomingVehicle v)
	{
		if (EngineData.lastSelectedIntManager == this) System.out.println("========================");
		if (EngineData.lastSelectedIntManager == this) System.out.println("Solving: " + v.getVehicle());
		if (!this.isIntersectionFree) this.checkInternalAdvanced(v);
		else
		{
			if (!this.directionReservations.contains(v.getDirectionInIntersection()))
			{
				this.reserveVDirections(v);
				assignToken(v);
			}
		}
	}

	protected void checkInternalAdvanced(IncomingVehicle v)
	{//Checks for collision inside of intersection - Advanced
		boolean freeToGo = true;
		String collisionPaths = "";

		//External check
		if (this.directionReservations.contains(v.getDirectionInIntersection()))
		{//Direction is already reserved

			if (v.getVehicle().getIsReadyToCross() && !v.getVehicle().isBlockedByUser()) this.waitingList.waits(v.getIntEnterDir());

			freeToGo = false;
		}
		else//Internal check
		{
			//For each direction with which the current vehicle can intersect
			for (String dirToCheck : RoadData.collisionOccurences.get(v.getDirectionInIntersection()))
			{
				//If that is a possible route in current road
				if (RoadData.blockIdToVehicles.get(this.blockId).containsKey(dirToCheck))
				{
					//If there are vehicles, no-go situation
					if (RoadData.blockIdToVehicles.get(this.blockId).get(dirToCheck).size() != 0)
					{
						if (RoadData.blockIdToVehicles.get(this.blockId).get(dirToCheck).size() == 1)
						{
							collisionPaths = rotateTillCorrect(v.getDirectionInIntersection() + dirToCheck);
							if (RoadData.blockIdToVehicles.get(this.blockId).get(dirToCheck).getLast().getLengthOfPathTravelled() <= RoadData.collisionData.get(collisionPaths)[1] + 0.3f)
							{
								if (v.getVehicle().getIsReadyToCross())
								{
									this.waitingList.waits(v.getIntEnterDir());
									this.reserveVDirections(v);
								}

								freeToGo = false;
								break;
							}
						}
						else
						{
							if (v.getVehicle().getIsReadyToCross())
							{
								this.waitingList.waits(v.getIntEnterDir());
								this.reserveVDirections(v);
							}

							freeToGo = false;
							break;
						}

					}
				}
			}
		}

		if (freeToGo) assignToken(v);

		/*ConcurrentHashMap<String, ConcurrentLinkedDeque<Vehicle>> intersectionData = RoadData.blockIdToVehicles.get(this.blockId);
		String vDirInIntersection = v.getDirectionInIntersection();
		int collPathWithVehicles = 0; //N° of collision paths that have at are not empty
		String dir = "";
		String collisionPaths = "";
		boolean freeToGo = true;
		
		//Count how many collision paths containing vehicles there are, if > 1, cant handle, stop vehicle
		//For each direction with which the current vehicle can intersect
		//External check
		if (this.directionReservations.contains(v.getDirectionInIntersection()))
		{//Direction is already reserved
		
			if (v.getVehicle().getIsReadyToCross() && !v.getVehicle().isBlockedByUser()) this.waitingList.waits(v.getIntEnterDir());
		
			freeToGo = false;
		}
		else//Internal check
		{
			if (EngineData.lastSelectedIntManager == this) System.out.println("Dirs to check: " + RoadData.collisionOccurences.get(vDirInIntersection));
			for (String dirToCheck : RoadData.collisionOccurences.get(vDirInIntersection))
			{
				if (EngineData.lastSelectedIntManager == this) System.out.println("Checking: " + dirToCheck);
				//If that is an active route in current road and there is one or more vehicles
				if (RoadData.blockIdToVehicles.get(this.blockId).containsKey(dirToCheck) && RoadData.blockIdToVehicles.get(this.blockId).get(dirToCheck).size() != 0)
				{
					dir = dirToCheck;
					collPathWithVehicles++;
					if (EngineData.lastSelectedIntManager == this) System.out.println("dir: " + dir + " counter: " + collPathWithVehicles);
					if (collPathWithVehicles > 1)
					{//There are two or more directions with which I will intersect that contain vehicles, max I can manage is one
						//Use Basic solve mode
						if (EngineData.lastSelectedIntManager == this) System.out.println("Greater than 1");
						freeToGo = false;
						break;
					}
				}
			}
		
			if (EngineData.lastSelectedIntManager == this) System.out.println("collPathContainingVehicles after loop: " + collPathWithVehicles);
		
			if (collPathWithVehicles == 1)
			{//Advanced Mode
				if (EngineData.lastSelectedIntManager == this) System.out.println("collPathContainingVehicles == 1 ");
				collisionPaths = rotateTillRight(vDirInIntersection + dir);
				if (EngineData.lastSelectedIntManager == this)
					System.out.println(intersectionData.get(dir).getLast() + " has travelled: " + intersectionData.get(dir).getLast().getLengthOfPathTravelled());
				if (intersectionData.get(dir).getLast().getLengthOfPathTravelled() <= RoadData.collisionData.get(collisionPaths)[1] + 0.3f)
				{
					if (EngineData.lastSelectedIntManager == this) System.out.println("NOT FREE TO GO");
					//assignToken(v);
					if (v.getVehicle().getIsReadyToCross())
					{
						this.waitingList.waits(v.getIntEnterDir());
						this.reserveVDirections(v);
					}
					freeToGo = false;
				}
			}
		}
		
		if (freeToGo) assignToken(v);*/
	}

	protected void reserveVDirections(IncomingVehicle v)
	{
		//Dir is not reserved, so resever it for current v
		this.directionReservations.add(v.getDirectionInIntersection());
		this.directionReservations.addAll(RoadData.collisionOccurences.get(v.getDirectionInIntersection()));
	}

	protected void assignToken(IncomingVehicle v)
	{//Assigns token to given vehicle

		if (v.getIntEnterDir() == this.currentMainDir)
		{
			if (EngineData.lastSelectedIntManager == this) System.out.println(v.getVehicle() + " is main dir");
			this.lastVehicleWithToken = v;
		}

		if (EngineData.lastSelectedIntManager == this) System.out.println("Assigning token to: " + v.getVehicle());
		this.waitingList.go(v.getIntEnterDir());

		v.getVehicle().giveToken();
		//v.getVehicle().setOverlayColor(new Vector3f(255, 154, 25));
	}

	protected String rotateTillCorrect(String input)
	{//Rotates clock-wise
		int[] array = new int[4];

		//Create array
		for (int i = 0; i < input.length(); i++)
		{
			array[i] = Character.getNumericValue(input.charAt(i));
		}

		//Rotate till first number is 1  
		while (array[0] != 0)
		{
			for (int i = 0; i < input.length(); i++)
			{
				switch (array[i])
				{
					case 0:
						array[i] = 2;
						break;
					case 1:
						array[i] = 3;
						break;
					case 2:
						array[i] = 1;
						break;
					case 3: //case 3
						array[i] = 0;
						break;
				}
			}
		}

		return (Arrays.toString(array).replace(", ", "").replace("[", "").replace("]", ""));
	}

	protected void checkExternalAdvanced(IncomingVehicle v, IncomingVehicle nextV)
	{//Checks for collision outside of intersection - Advanced
		//The only collision I have to check for outside the intersection is the (possible) one against nextV

		if (nextV != null)
		{//Check for collision

			//For each direction with which the current vehicle can intersect

			if (RoadData.collisionOccurences.get(v.getDirectionInIntersection()).contains(nextV.getDirectionInIntersection()))
			{//Vehicles are on a collison path

			}
		}
		else
		{
			//This is last vehicle going into intersection
			//Its vel should already have been corrected from iteration of this method on previous vehicle
		}
	}

	protected void sortIncomingVehiclesByDistance()
	{//Sorts vehicles such that vehicleList = [closest to intersection,....,....,...,farthest from intersection]

		IncomingVehicle v;//TODO maybe create a tempV var as class field and use that instead of creating a var here everytime
		boolean madeASwap = true;
		int index;

		while (madeASwap)
		{
			madeASwap = false;

			for (index = 0; index < this.incomingVehicles.size() - 1; index++)
			{
				if (this.incomingVehicles.get(index).getDistFromIntersection() > this.incomingVehicles.get(index + 1).getDistFromIntersection())
				{
					madeASwap = true;
					v = this.incomingVehicles.get(index);
					this.incomingVehicles.set(index, this.incomingVehicles.get(index + 1));
					this.incomingVehicles.set(index + 1, v);
				}
			}
		}

		//this.printIncomingVehicleList();
	}

	protected void printIncomingVehicleList()
	{
		System.out.print("Vehicles sorted as: ");
		for (IncomingVehicle v : this.incomingVehicles)
		{
			System.out.print(v.getVehicle() + "-");
		}

		System.out.println();
	}

	protected boolean searchForVehicle(int currSearchDir)
	{//Searches for a vehicle, starting from current position, going backwards on road from given direction

		boolean foundVehicle = false;
		int inspectedBlocks = 0;
		int[] currentBlockCoords = new int[] { this.positionInGrid[0], this.positionInGrid[1] };
		int currentBlockId = -1;
		Vehicle tempV;
		float distanceFromV = 0f;
		boolean foundOneGoodDirection = false;
		int initialDir = currSearchDir;
		IntersectionManager intM;

		//Select starting block
		this.goToNextBlock(currSearchDir, currentBlockCoords);

		//Start searching
		mainSearchLoop: while (true)
		{
			//Search radius is RoadData.maxIntersectionSearchDist
			if (inspectedBlocks < RoadData.maxIntersectionSearchDist)
			{
				//Check if not out of grid
				if (currentBlockCoords[0] < 0 || currentBlockCoords[0] >= RoadData.gridWidth || currentBlockCoords[1] < 0 || currentBlockCoords[1] >= RoadData.gridHeight)
				{//Out of bounds
					break;
				}

				currentBlockId = RoadData.getBlockIdFromCoords(currentBlockCoords[0], currentBlockCoords[1]);

				//If current block does not hold an intersection
				if (RoadData.IntersectionManagerGrid.get(currentBlockCoords[1]).get(currentBlockCoords[0]) == null)
				{//Dealing with a straight road or turn

					for (String dirValue : RoadData.blockIdToVehicles.get(currentBlockId).keySet())
					{//There should be either 1 or 2 directions (looking at either a straight or turn block)

						//Looking only @ dir values pointing @ int manager
						if (dirValue.substring(1).equals(Integer.toString(this.getOppositeDir(currSearchDir))))
						{
							foundOneGoodDirection = true;
							currSearchDir = Character.getNumericValue(dirValue.charAt(0));
							tempV = RoadData.blockIdToVehicles.get(currentBlockId).get(dirValue).peekLast(); //First vehicle looking from int manager

							if (tempV != null)
							{
								if (!tempV.isBlockedByUser())
								{
									distanceFromV += tempV.getRemainingToTravelOfBlock();
									this.assignVehicleToDir(initialDir, tempV, distanceFromV);
									foundVehicle = true;
								}

								break mainSearchLoop;
							}
							else distanceFromV += Float.parseFloat(RoadData.pathIndexToPathInfo.get(RoadData.directionsToPathsIndices.get(dirValue))[1]);

						}
						//else direction is pointing away from int manager
					}
					if (!foundOneGoodDirection) break;

					//Go to next block
					this.goToNextBlock(currSearchDir, currentBlockCoords);

					inspectedBlocks++;
				}
				else
				{//Theres an intersection, Compare vehicles from different directions to find the nearest one to the current block
					intM = RoadData.IntersectionManagerGrid.get(currentBlockCoords[1]).get(currentBlockCoords[0]);

					try
					{//Before asking for his exiting vehicle, wait to have loaded them
						intM.exitingVehicleLoading.await();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}

					tempV = this.getIncomingVehicleFromIntManager(intM, currSearchDir);
					if (tempV != null)
					{
						if (!tempV.isBlockedByUser())
						{
							distanceFromV += tempV.getRemainingToTravelOfBlock();
							foundVehicle = true;
							this.assignVehicleToDir(initialDir, tempV, distanceFromV);
						}
					}

					//Interrupt search at this block cause cant know in which dir continue to look
					break mainSearchLoop;
				}
			}
			else break mainSearchLoop;
		}

		return foundVehicle;
	}

	public Vehicle getExitingVehicleFromDirection(int direction)
	{//Returns which vehicle is exiting from this int manager on the given direction

		if (direction == 0)
		{
			return this.northExtVehicle;
		}
		else if (direction == 1)
		{
			return this.southExtVehicle;
		}
		else if (direction == 2)
		{
			return this.eastExtVehicle;
		}
		else //direction == 3
		{
			return this.westExtVehicle;
		}
	}

	private Vehicle getIncomingVehicleFromIntManager(IntersectionManager intM, int direction)
	{
		if (direction == 0) return intM.getExitingVehicleFromDirection(1);
		else if (direction == 1) return intM.getExitingVehicleFromDirection(0);
		else if (direction == 2) return intM.getExitingVehicleFromDirection(3);
		else return intM.getExitingVehicleFromDirection(2); //direction == 3
	}

	private void goToNextBlock(int nextDirection, int[] currentBlockCoords)
	{//Loads next block's coords into given array

		//Checking North value
		if (nextDirection == 0)
		{//Go down
			currentBlockCoords[1]++;
		}
		//Checking South value
		else if (nextDirection == 1)
		{//Go up
			currentBlockCoords[1]--;
		}
		//Checking East value
		else if (nextDirection == 2)
		{//Go left
			currentBlockCoords[0]++;
		}
		//Checking West value
		else
		{//Go right
			currentBlockCoords[0]--;
		}
	}

	private void assignVehicleToDir(int direction, Vehicle v, float dist)
	{
		v.setManagedByIntManager(true);

		if (direction == 0)
		{
			this.northVehicle.setVehicle(v);
			this.northVehicle.setDistFromIntersection(dist);
			this.northVehicle.setDirection(v.getNextIntDirValue(this.blockId));

			this.incomingVehicles.add(this.northVehicle);
		}
		else if (direction == 1)
		{
			this.southVehicle.setVehicle(v);
			this.southVehicle.setDistFromIntersection(dist);
			this.southVehicle.setDirection(v.getNextIntDirValue(this.blockId));

			this.incomingVehicles.add(this.southVehicle);
		}
		else if (direction == 2)
		{
			this.eastVehicle.setVehicle(v);
			this.eastVehicle.setDistFromIntersection(dist);
			this.eastVehicle.setDirection(v.getNextIntDirValue(this.blockId));

			this.incomingVehicles.add(this.eastVehicle);
		}
		else //direction == 3
		{
			this.westVehicle.setVehicle(v);
			this.westVehicle.setDistFromIntersection(dist);
			this.westVehicle.setDirection(v.getNextIntDirValue(this.blockId));

			this.incomingVehicles.add(this.westVehicle);
		}
	}

	protected void resetVehicleData()
	{
		this.lastVehicleWithToken = null;

		//North vehicle
		this.northVehicle.setVehicle(null);
		this.northExtVehicle = null;

		//South vehicle
		this.southVehicle.setVehicle(null);
		this.southExtVehicle = null;

		//East vehicle
		this.eastVehicle.setVehicle(null);
		this.eastExtVehicle = null;

		//West vehicle
		this.westVehicle.setVehicle(null);
		this.westExtVehicle = null;
	}

	public void resetIntersection()
	{
		//resetVehicleData();

		mainDirTransit = false; //True when transition happened on main dir in last cycle
		currentMainDir = -1;
		transits = 0;
		lastVehicleWithToken = null; //Used for debugging and showoff in intManager gui
		isIntersectionFree = true;
		northExtVehicle = null;
		southExtVehicle = null;
		eastExtVehicle = null;
		westExtVehicle = null;
		waitingList.clear();
	}

	protected void saveExitingVehicleReference(int direction, Vehicle v)
	{
		if (v != null)
		{
			if (direction == 0) this.northExtVehicle = v;
			else if (direction == 1) this.southExtVehicle = v;
			else if (direction == 2) this.eastExtVehicle = v;
			else this.westExtVehicle = v; //direction == 3
		}
	}

	private int getOppositeDir(int inputDir)
	{
		if (inputDir == 0) return 1;
		else if (inputDir == 1) return 0;
		else if (inputDir == 2) return 3;
		else return 2;// inputDir == 3
	}

	protected void intersectionIsClear()
	{
		this.isIntersectionFree = false;

		ConcurrentHashMap<String, ConcurrentLinkedDeque<Vehicle>> temp = RoadData.blockIdToVehicles.get(this.blockId);

		for (String dir : temp.keySet())
		{
			if (temp.get(dir).size() != 0) return;
		}

		this.isIntersectionFree = true;
	}

	protected void loadExitingVehicles()
	{//Loads references of vehicles exiting intersection for each direction

		Vehicle tempV;

		//For each direction coming to this intersection
		for (int dir = 0; dir < 4; dir++)
		{
			if (this.focusDirections[dir] == 1)
			{//If active direction

				for (String dirValue : RoadData.blockIdToVehicles.get(this.blockId).keySet())
				{
					if (dirValue.substring(1).equals(Integer.toString(dir)))
					{
						if (RoadData.blockIdToVehicles.get(this.blockId).get(dirValue).size() != 0)
						{
							tempV = RoadData.blockIdToVehicles.get(this.blockId).get(dirValue).peekLast();

							this.saveExitingVehicleReference(dir, tempV);

							break;//TODO, potrebbero esserci più veicoli in uno stesso incrocio uscenti in una stessa direzione?
									//Se si, qui al posto di uscire subito, devo confrontarli e vedere quale è più vicino ad uscire
						}
					}
				}
			}
			else
			{
				this.saveExitingVehicleReference(dir, null);//TODO I need this?
			}
		}
		this.exitingVehicleLoading.countDown();
	}

	public int getBlockId()
	{
		return this.blockId;
	}

	protected void loadIncomingVehicles()
	{//Loads references of vehicles coming to intersection for each direction

		//For each direction coming to this intersection
		for (int dir = 0; dir < 4; dir++)
		{
			//If there is a road incoming from current direction
			if (this.focusDirections[dir] == 1)
			{
				//[0,1,2,3] = [N,S,E,W]
				//BackPropagate search until find a vehicle or another intersection or reached bound of grid
				this.searchForVehicle(dir);
			}
		}
	}

	public void terminate()
	{
		terminate = true;
	}

	public void updateValuesInGUI()
	{
		if (EngineData.selectedEntity == this.roadBlock || EngineData.lastSelectedIntManager == this)
		{
			this.roadBlock.loadBlockInformations();

			this.loadArrivalOrder();

			Gui_IntersectionStatus.intersectionManagerName.setText(this.toString());
			Gui_IntersectionStatus.IntersectionManagerPosition.setText(Arrays.toString(this.positionInGrid));
			Gui_IntersectionStatus.roadBlock.setText(RoadData.road.get(this.positionInGrid[1]).get(this.positionInGrid[0]).toString().split("\\.")[1] + " with ID: " + this.blockId);
			Gui_IntersectionStatus.totNOfVehicles.setText(Integer.toString(this.roadBlock.getTotNOfVehicles()));
			Gui_IntersectionStatus.arrivalOrderDistance.setText(this.distanceOrder);
			Gui_IntersectionStatus.isIntersectionFree.setText(Boolean.toString(this.isIntersectionFree));
			Gui_IntersectionStatus.transits.setText(this.currentMainDir + " | " + this.transits);
			Gui_IntersectionStatus.incomingTraffic.setText(this.buildIncomingTrafficSummary());
			Gui_IntersectionStatus.exitingTraffic.setText(this.buildExitingTrafficSummary());
		}
	}

	public void loadArrivalOrder()
	{
		this.distanceOrder = "[";

		for (IncomingVehicle v : this.incomingVehicles)
		{
			for (int dir : this.dirToVehicleMap.keySet())
			{
				if (v == this.dirToVehicleMap.get(dir))
				{
					if (dir == 0)
					{
						this.distanceOrder += "N";
					}
					else if (dir == 1)
					{
						this.distanceOrder += "S";
					}
					else if (dir == 2)
					{
						this.distanceOrder += "E";
					}
					else //dir == 3
					{
						this.distanceOrder += "W";
					}
				}
			}
		}

		this.distanceOrder += "]";
	}

	private String buildIncomingTrafficSummary()
	{
		String message = "<html><body>";

		if (this.northVehicle.getVehicle() != null)
		{
			if (this.lastVehicleWithToken == this.northVehicle) message += "<font color=\"red\"> N</font>: ";
			else message += "N: ";

			message += (this.northVehicle.getVehicle()).toString().substring(8) + " - " + this.northVehicle.getDistFromIntersection() + "<br>";
		}
		else
		{
			if (this.focusDirections[0] == 0) message += "N: not an active direction<br>";
			else message += "N: null <br>";
		}

		if (this.southVehicle.getVehicle() != null)
		{
			if (this.lastVehicleWithToken == this.southVehicle) message += "<font color=\"red\"> S</font>: ";
			else message += "S: ";

			message += (this.southVehicle.getVehicle()).toString().substring(8) + " - " + this.southVehicle.getDistFromIntersection() + "<br>";
		}
		else
		{
			if (this.focusDirections[1] == 0) message += "S: not an active direction<br>";
			else message += "S: null <br>";
		}

		if (this.eastVehicle.getVehicle() != null)
		{
			if (this.lastVehicleWithToken == this.eastVehicle) message += "<font color=\"red\"> E</font>: ";
			else message += "E: ";

			message += (this.eastVehicle.getVehicle()).toString().substring(8) + " - " + this.eastVehicle.getDistFromIntersection() + "<br>";
		}
		else
		{
			if (this.focusDirections[2] == 0) message += "E: not an active direction<br>";
			else message += "E: null <br>";

		}

		if (this.westVehicle.getVehicle() != null)
		{
			if (this.lastVehicleWithToken == this.westVehicle) message += "<font color=\"red\">W</font>: ";
			else message += "W: ";

			message += (this.westVehicle.getVehicle()).toString().substring(8) + " - " + this.westVehicle.getDistFromIntersection() + "<br>";
		}
		else
		{
			if (this.focusDirections[3] == 0) message += "W: not an active direction<br>";
			else message += "W: null <br>";
		}

		message += "</body></html>";

		return message;
	}

	private String buildExitingTrafficSummary()
	{
		String message = "<html><body>";

		if (this.northExtVehicle != null)
		{
			message += "N: " + this.northExtVehicle.toString().substring(8) + "<br>";
		}
		else
		{
			message += "N: null <br>";
		}

		if (this.southExtVehicle != null)
		{
			message += "S: " + this.southExtVehicle.toString().substring(8) + "<br>";
		}
		else
		{
			message += "S: null <br>";
		}

		if (this.eastExtVehicle != null)
		{
			message += "E: " + this.eastExtVehicle.toString().substring(8) + "<br>";
		}
		else
		{
			message += "E: null <br>";
		}

		if (this.westExtVehicle != null)
		{
			message += "W: " + this.westExtVehicle.toString().substring(8) + "<br>";
		}
		else
		{
			message += "W: null <br>";
		}

		message += "</body></html>";

		return message;
	}
}