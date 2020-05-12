package TrafficLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import Entities.Entity;
import Guis.Gui_VehicleStatus;
import Models.TexturedModel;
import Render_engine.EngineData;
import Utility.MathUtils;
import Utility.ResettableCountDownLatch;

public class Vehicle extends Entity
{
	protected Vector3f nextPosition = new Vector3f();

	//Statistic infos
	private float distanceTravelled = 0f;
	private int crossedBlocks = -1; //<--- can calculate percentage based on this

	//Moving infos
	private float velocity = RoadData.cruiseVelocity; //Current vehicle velocity - meters/seconds
	private Vector3f aim = new Vector3f(0, 0, -1);
	private Vector3f perpendicularAim = new Vector3f(-1, 0, 0); //Vector perpendicular to the aim vector, used to steer
	private int currentPointOnPath = 0;
	private float distFromNextVehicle = 0f;
	private float incrementStep;
	private float currentEdgeLength;
	private float lengthOfEdgeTravelled = 0f;
	protected float lengthOfBlockPathTraveled = 0f;
	protected float distDifference; //Distance difference from nextVehicle
	protected boolean incrementedAlready = false;

	public Vehicle nextVehicle;
	public IntersectionManager nextIntManager;

	//Flags
	private boolean blockedByUser = false; //User can set this to false by clicking on car and hit the S key
	private boolean justBorn = true;
	public boolean managedByIntManager = false;
	private boolean hasIntersectionInFront = false; //True when finds an intersection and then a vehicle
	protected boolean onIntersection = false; //True when vehicle is on intersection
	protected boolean done = false; //True when vehicle arrives at end of route
	protected boolean terminateVehicle = false; //True when done is true or when vehicle is killed (road rebuild)
	protected boolean intersectionToken = false;//True when vehicle has permission to enter intersection
	protected boolean readyToCross = false; //True when vehicle is standing still @ 0.2 from intersection

	//Assigned route
	private int assignedRouteIndex;
	private int currentBlockId;//Current block id
	private int currentRouteBlockIndex = 0;//Current position in the route array
	private int[] routeRoadBlocks;
	private String currentPathIndex;
	private float assignedBlockPathLenght;
	private float[] pathPoints;//Rotated path
	private float[] currentPathPoints;//Rotated and moved path
	public String[] currentConfig;
	private RoadBlock currentRoadBlock;
	private String currentDirectionalValue;
	public float tempPathTraveled = 0f;
	private float distFromNextInt = 0f;
	public volatile ResettableCountDownLatch personalTimer;

	private Iterator<Vehicle> itr;

	//Stores: srcBlockId-srcCurrentRouteBlockIndex-destBlockId-destCurrentRouteBlockIndex
	private ArrayList<Object> vehicleReferenceData = new ArrayList<Object>();

	//Temp attributes
	private Vector3f tempVec = new Vector3f();

	public Vehicle(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, float alpha, Vector3f colorId)
	{
		super(model, position, rotX, rotY, rotZ, scale, null, true, alpha, colorId);
	}

	public void initialize(int choosenRoute)
	{//Initializes vehicle

		this.personalTimer = new ResettableCountDownLatch(1);

		this.assignedRouteIndex = choosenRoute;
		this.routeRoadBlocks = RoadData.routeToBlocksId.get(choosenRoute);

		this.vehicleReferenceData.add(null);
		this.vehicleReferenceData.add(null);
		this.vehicleReferenceData.add(null);
		this.vehicleReferenceData.add(null);

		this.goToNextBlock();
		this.updateBlockIdToVehicleMap();

		//Introduce vehicle in vehicles hierarchy
		this.loadNextVehicleData();

		RoadData.activeVehicles.add(this);

		this.justBorn = false;

		//-------------------

		this.computeNextEdgeLenght();

		this.setPosition(this.nextPosition);
	}

	public void giveToken()
	{
		this.intersectionToken = true;
	}

	protected void removeVehicleFromRoad()
	{
		RoadData.blockIdToVehicles.get(this.currentBlockId).get(this.currentDirectionalValue).remove(this);

		RoadData.solvedVehicles++;

		RoadData.deadVehicles.add(this);
		RoadData.activeVehicles.remove(this);
	}

	protected void setOnIntersectionFlag()
	{//True if vehicle is inside intersection

		this.onIntersection = false;//TODO SEMPLIFICARE

		int gridPos[] = RoadData.getBlockCoordsFromId(currentBlockId);

		if (RoadData.IntersectionManagerGrid.get(gridPos[1]).get(gridPos[0]) != null)
		{
			this.onIntersection = true;
		}
	}

	protected void computeNextStatus()
	{
		if (!this.blockedByUser && !this.done)
		{
			this.computeVelocity();
			this.computeNextPosition();

			//this.goToNextPoint();			
		}
		else
		{
			this.velocity = 0f;
		}
	}

	protected void updateBlockIdToVehicleMap()
	{//Uses data in vehicleReferenceData to update vehicle's references in data structures

		if (this.vehicleReferenceData.get(0) != null)
		{
			//Remove current vehicle reference
			RoadData.blockIdToVehicles.get(this.vehicleReferenceData.get(0)).get(RoadData.routeIndexToDirectionValues.get(this.assignedRouteIndex)[(int) this.vehicleReferenceData.get(1)])
					.remove(this);
		}

		if (this.vehicleReferenceData.get(2) != null)
		{
			//Add current vehicle reference
			RoadData.blockIdToVehicles.get(this.vehicleReferenceData.get(2)).get(RoadData.routeIndexToDirectionValues.get(this.assignedRouteIndex)[(int) this.vehicleReferenceData.get(3)])
					.addFirst(this);
		}

		this.vehicleReferenceData.set(0, null);
		this.vehicleReferenceData.set(1, null);
		this.vehicleReferenceData.set(2, null);
		this.vehicleReferenceData.set(3, null);
	}

	protected void loadNextVehicleData()
	{//Finds next vehicle and computes distance from it, -1 if intersection is encountered (no vehicles in intersection)
		//Loads reference to next int manager

		int tempBlockId = this.currentBlockId;
		int[] gridPos = RoadData.getBlockCoordsFromId(tempBlockId);
		boolean foundMe = false;
		String tempDirValue;
		Vehicle tempV, tempV1;

		//Reset
		this.nextVehicle = null;
		this.distFromNextVehicle = 0f;
		this.hasIntersectionInFront = false;
		this.distFromNextInt = 0f;

		tempV = RoadData.blockIdToVehicles.get(tempBlockId).get(this.currentDirectionalValue).peekLast();

		//Check First Block
		if (tempV != this)
		{//If this not last on block, must iterate from the first to current vehicle, then get the one after current

			//Using a cocurrentLinkedDeque doesnt allow to use .get(index) so I must do it this way to get the vehicle immediately after this
			itr = RoadData.blockIdToVehicles.get(tempBlockId).get(this.currentDirectionalValue).iterator();

			while (itr.hasNext())
			{
				tempV = itr.next();

				if (foundMe)
				{
					this.nextVehicle = tempV;
					this.distFromNextVehicle = this.nextVehicle.lengthOfBlockPathTraveled - this.lengthOfBlockPathTraveled;

					return;
				}
				else
				{
					if (tempV == this) foundMe = true;
				}
			}
		}

		//Check all the following blocks
		this.distFromNextVehicle = this.assignedBlockPathLenght - this.lengthOfBlockPathTraveled;
		this.distFromNextInt += this.assignedBlockPathLenght - this.lengthOfBlockPathTraveled;

		//For each block on route
		for (int i = this.currentRouteBlockIndex + 1; i < RoadData.routeToBlocksId.get(this.assignedRouteIndex).length; i++)
		{
			tempBlockId = RoadData.routeToBlocksId.get(this.assignedRouteIndex)[i];
			gridPos = RoadData.getBlockCoordsFromId(tempBlockId);
			tempDirValue = RoadData.routeIndexToDirectionValues.get(this.assignedRouteIndex)[i];

			//Check for Intersection Manager
			if (RoadData.IntersectionManagerGrid.get(gridPos[1]).get(gridPos[0]) != null)
			{ //If there is an intersection directly in front of vehicle, no other vehicles in between
				this.hasIntersectionInFront = true;
				this.nextIntManager = RoadData.IntersectionManagerGrid.get(gridPos[1]).get(gridPos[0]);//Just for vehicle gui nextIntExitDirection
				//break;
			}
			else
			{//If yet to found an intersection, keep counting

				this.distFromNextInt += Float.parseFloat(RoadData.pathIndexToPathInfo.get(RoadData.directionsToPathsIndices.get(tempDirValue))[1]);
			}

			tempV1 = RoadData.blockIdToVehicles.get(tempBlockId).get(tempDirValue).peekFirst();

			//Check for vehicle
			if (tempV1 != null && !tempV1.isDone())
			{
				this.nextVehicle = tempV1;
				this.distFromNextVehicle += this.nextVehicle.getLengthOfPathTravelled();

				break;
			}
			else
			{//else this block is empty, go check next one
				this.distFromNextVehicle += Float
						.parseFloat(RoadData.pathIndexToPathInfo.get(RoadData.directionsToPathsIndices.get(RoadData.routeIndexToDirectionValues.get(this.assignedRouteIndex)[i]))[1]);
			}

			if (this.hasIntersectionInFront) break;
		}

		if (this.nextVehicle == null && !this.hasIntersectionInFront)
		{
			this.distFromNextInt = 0f;
			this.distFromNextVehicle = 0f;
		}

		return;
	}

	private void computeNextPosition()
	{//CGI experience pays off

		//Velocity has already been computed in computeNextStep method
		this.incrementStep = this.velocity;

		if (this.velocity > 0f)
		{
			double edgeLeftOverStep = this.currentEdgeLength - this.lengthOfEdgeTravelled;

			if (edgeLeftOverStep - this.incrementStep > 0.0001)
			{//incrementStep < stepLeftOver
				//Muovo Obj
				//@P += _incrementStep * _currentAim;

				this.moveForward(this.incrementStep);

				this.distanceTravelled += this.incrementStep;

				this.lengthOfEdgeTravelled += this.incrementStep;

				this.tempPathTraveled += this.incrementStep;
			}
			else if ((this.incrementStep - edgeLeftOverStep) > 0.0001)
			{//incrementStep > stepLeftOver

				double leftToTravel = this.incrementStep;

				while (leftToTravel > 0.0001) //Finchè non ho percorso tutto lo step
				{//leftToTravel > 0.001

					if (leftToTravel - edgeLeftOverStep > 0.0001)
					{//Step left over is greater than current edge length, so jump on the next edge

						this.distanceTravelled += edgeLeftOverStep;

						this.tempPathTraveled += edgeLeftOverStep;

						this.lengthOfEdgeTravelled = 0f;

						leftToTravel -= edgeLeftOverStep;

						this.currentPointOnPath++;

						//If on last point of path, go to next block
						if (this.currentPointOnPath == (this.pathPoints.length / 3) - 1)
						{
							this.goToNextBlock();

							if (this.done) break;
						}
						else
						{
							this.moveToNextPoint();

							//this.lenghtOfPathTravelled += this.currentEdgeLength;
						}

						this.computeNextEdgeLenght();

						edgeLeftOverStep = this.currentEdgeLength;
					}
					else
					{
						//Move
						this.moveForward(leftToTravel);

						this.distanceTravelled += leftToTravel;
						this.lengthOfEdgeTravelled += leftToTravel;
						this.tempPathTraveled += leftToTravel;

						leftToTravel = 0;
					}
				}
			}
			else
			{//incrementStep ≈ what remains of the current edge

				this.distanceTravelled += incrementStep;

				this.currentPointOnPath++;

				if (this.currentPointOnPath == (this.pathPoints.length / 3) - 1)
				{
					this.goToNextBlock();
				}
				else
				{
					this.moveToNextPoint();
				}

				if (!this.done)
				{
					this.computeNextEdgeLenght();
					this.lengthOfEdgeTravelled = 0f;
				}
			}
		}
	}

	private void computeVelocity()
	{//Modifies vehicle's velocity based on distance from next vehicle or intersection

		incrementedAlready = false;//To avoid incrementing twice

		//Check for intersection
		if (this.hasIntersectionInFront)
		{
			if (this.intersectionToken)
			{//Gradually increase velocity until @ cruise speed
				this.velocity += RoadData.cruiseVelocity / 5f;
				incrementedAlready = true;
				//Vehicle was waiting still for token to enterm now enter intersection
				if (readyToCross) readyToCross = false;
			}
			else
			{//@ this point vehicle has been signaled from int manager to stop, instead of stopping at random distance from intersection, stop right when
				//the vehicle's model is about to enter intersection
				if (!this.readyToCross)
				{
					if (this.distFromNextInt <= 0.2f)
					{
						this.velocity = 0f;
						this.readyToCross = true;
					}
					else if ((this.distFromNextInt - this.velocity) <= 0.2f)
					{
						this.velocity -= 0.2f - (this.distFromNextInt - this.velocity);
					}
					else
					{
						this.velocity += RoadData.cruiseVelocity / 5;
						incrementedAlready = true;
						this.clampVelocityValue(RoadData.cruiseVelocity);
					}
				}
			}

			this.clampVelocityValue(RoadData.cruiseVelocity);
		}

		//Check for vehicle
		if ((hasIntersectionInFront && intersectionToken) || !hasIntersectionInFront)
		{
			if (this.nextVehicle != null && !this.nextVehicle.isDone())
			{//If have a vehicle in front of me
				distDifference = this.distFromNextVehicle - RoadData.targetChasingDistance;
				if (Math.abs(distDifference) >= 0.005f)//Be careful with this setting, if too low cars will be jerky (or at least they used to be like this)
				{
					this.velocity += distDifference;
					this.clampVelocityValue(RoadData.chasingVelocity);
				}
				else this.velocity = this.nextVehicle.getVelocity();

				//This will prevent a vehicle to go under the minimum distance from next vehicle when chasing it
				if (this.velocity > distDifference + this.nextVehicle.getVelocity())
				{//This piece of code is kept outside the above if stmnt because I need to call clampVelocityValue() first,
					//because of the 'this.velocity += distDifference' statement
					this.velocity -= this.velocity - (distDifference + this.nextVehicle.getVelocity());
				}
			}
			else if (!incrementedAlready)
			{
				this.velocity += RoadData.cruiseVelocity / 5f;
				this.clampVelocityValue(RoadData.cruiseVelocity);
			}
		}
	}

	private void clampVelocityValue(float maxV)
	{
		//Clamp value
		if (this.velocity < RoadData.cruiseVelocity / 5f)//Thx Gian
		{
			this.velocity = 0f;
		}
		else if (this.velocity > maxV)
		{
			if (this.onIntersection) this.velocity = RoadData.cruiseVelocity; //Dont speed on intersection!!
			else this.velocity = maxV;
		}
	}

	private void moveForward(double incrementStep)
	{//Moves car forward by given amount

		Vector3f tempVec = new Vector3f(this.aim);
		tempVec.x *= incrementStep;
		tempVec.y *= incrementStep;
		tempVec.z *= incrementStep;

		this.increaseNextPosition(tempVec.getX(), tempVec.getY(), tempVec.getZ());
	}

	private void moveToNextPoint()
	{
		this.nextPosition.set(this.currentPathPoints[this.currentPointOnPath * 3], RoadData.vehicleHeight, this.currentPathPoints[this.currentPointOnPath * 3 + 2]);

		this.orientVehicle(this.currentPointOnPath);
	}

	private void goToNextPoint()
	{//UNUSED METHOD//

		//If current point on path is not last two of the path
		if (this.currentPointOnPath < ((this.pathPoints.length / 3) - 2))
		{
			this.currentPointOnPath++;

			this.setPosition(this.currentPathPoints[this.currentPointOnPath * 3], RoadData.vehicleHeight, this.currentPathPoints[this.currentPointOnPath * 3 + 2]);

			this.orientVehicle(this.currentPointOnPath);

		}
		//Else, if current block is not the last on route
		else if (this.currentRouteBlockIndex < this.routeRoadBlocks.length - 1)
		{
			this.goToNextBlock();
		}
		//Else vehicle is @ last block and reached end of path
		else
		{
			//Remove vehicle from direction queue
			//RoadData.blockIdToVehicles.get(this.currentBlockId).get(RoadData.routeIndexToDirectionValues.get(this.assignedRouteIndex)[this.currentRouteBlockIndex]).remove(this);

			//this.moveVehicleReference.set(0, this.assignedRouteIndex);
			//this.moveVehicleReference.set(1, this.currentBlockId);
			//this.moveVehicleReference.set(2, this.currentRouteBlockIndex);
			this.modifyVehicleRefs(this.currentBlockId, this.currentRouteBlockIndex, true);

			this.currentPointOnPath++;

			this.setPosition(this.currentPathPoints[this.currentPointOnPath * 3], RoadData.vehicleHeight, this.currentPathPoints[this.currentPointOnPath * 3 + 2]);

			this.done = true;
		}
	}

	private void goToNextBlock()
	{
		int[] blockCoords;
		this.crossedBlocks++;

		if (this.crossedBlocks == RoadData.routeToBlocksId.get(this.assignedRouteIndex).length)
		{//If currently on last block

			this.modifyVehicleRefs(this.currentBlockId, this.currentRouteBlockIndex, true);
			this.done = true;
		}
		else
		{
			if (!this.justBorn)
			{
				//Remove vehicle from previous block
				this.modifyVehicleRefs(this.currentBlockId, this.currentRouteBlockIndex, true);

				this.currentRouteBlockIndex++;
				this.currentPointOnPath = 0;
				this.tempPathTraveled = 0f;
			}

			//Change block
			this.currentBlockId = RoadData.routeToBlocksId.get(this.assignedRouteIndex)[this.currentRouteBlockIndex];

			//Loading current directional value - e.g. "01"/"34"/....
			this.currentDirectionalValue = RoadData.routeIndexToDirectionValues.get(this.assignedRouteIndex)[this.currentRouteBlockIndex];

			//Add vehicle into next block
			this.modifyVehicleRefs(this.currentBlockId, this.currentRouteBlockIndex, false);

			//Load path configuration
			this.currentConfig = RoadData.pathsConfigs.get(RoadData.routeIndexToConfigId.get(this.assignedRouteIndex)[this.currentRouteBlockIndex]);

			//Get first path
			this.currentPathIndex = currentConfig[0];

			//Load path length
			this.assignedBlockPathLenght = Float.parseFloat(RoadData.pathIndexToPathInfo.get(this.currentPathIndex)[1]);

			blockCoords = RoadData.getBlockCoordsFromId(this.currentBlockId);
			//Load current RoadBlock reference for later use
			this.currentRoadBlock = RoadData.road.get(blockCoords[1]).get(blockCoords[0]);

			//Load local copy of path points
			this.pathPoints = RoadData.pathsIndicesPathObj.get(this.currentPathIndex).getPoints();
			this.currentPathPoints = this.pathPoints.clone();

			//ADJUST PATH

			//1-Rotate path to match block's rotation		
			this.rotatePath(Integer.parseInt(currentConfig[1]));

			//2-Move path to match block's position
			this.movePath();

			//APPARENTLY I DONT USE THIS ANYMORE
			/*
			//3-Reverse path if needed (both ones)
			if (currentConfig[2].equals("1"))
			{
				System.out.println("SIIIIIIIIIIIIIIIII " + this.currentBlockId);
				this.reversePaths();
			}
			*/

			//Modify vehicle rotation to match path's
			this.orientVehicle(this.currentPointOnPath);

			//Set initial vehicle position to first point on first path
			this.nextPosition.set(this.currentPathPoints[0], RoadData.vehicleHeight, this.currentPathPoints[2]);
		}
	}

	private void modifyVehicleRefs(int currentBlockId, int currentRouteBlockIndex, boolean comingGoingTo)
	{//comingGoingTo, true when setting comingFrom value, false when setting goingTo value

		if (comingGoingTo)
		{
			//To check if comingFrom value have already been set, just check the first and see if its null, otherwise it was already been set
			if (this.vehicleReferenceData.get(0) == null)
			{
				//Need to check if comingFrom data has already been set because goToNextBlock method can be executed more than once per simulation cycle:
				//If vehicle v moves from roadBlock 1 to roadblock 3, there will be two calls to modify comingFrom data,
				//the first one will be "Delete vehicle from block 1", the second one "Delete vehicle from block 2". But vehicle has not yet moved from vehicle 1
				//so it needs to be deleted from block 1 (the first value was used to set comingFrom data)

				this.vehicleReferenceData.set(0, currentBlockId);
				this.vehicleReferenceData.set(1, currentRouteBlockIndex);
			}
		}
		else
		{
			this.vehicleReferenceData.set(2, currentBlockId);
			this.vehicleReferenceData.set(3, currentRouteBlockIndex);
		}
	}

	private void orientVehicle(int point)
	{//Given point on path, modifies vehicle y rotation to match path's

		Vector3f.sub(new Vector3f(this.pathPoints[(point + 1) * 3 + 0], this.pathPoints[(point + 1) * 3 + 1], this.pathPoints[(point + 1) * 3 + 2]),
				new Vector3f(this.pathPoints[point * 3 + 0], this.pathPoints[point * 3 + 1], this.pathPoints[point * 3 + 2]), this.tempVec);

		this.tempVec.normalise();

		//angle = arccos(dot(A,B) / (|A|* |B|))
		float angle = (float) Math.toDegrees(Math.acos(Vector3f.dot(this.tempVec, this.aim)));

		this.perpendicularAim.setX(this.aim.getZ());
		this.perpendicularAim.setZ(this.aim.getX() * -1);

		this.aim = new Vector3f(this.tempVec);

		if (Vector3f.dot(this.aim, this.perpendicularAim) < 0)
		{
			angle *= -1;
		}

		this.setRotY(this.getRotY() + angle);
	}

	private void rotatePath(int rotation)
	{//Rotates path's pts to match roadblock's

		Vector2f currentPt = new Vector2f();

		//Rotate each point
		for (int i = 0; i < this.pathPoints.length; i += 3)
		{
			currentPt.setX(this.currentPathPoints[i]);
			//Y is always the same
			currentPt.setY(this.currentPathPoints[i + 2]);

			this.rotatePoint(currentPt, (float) Math.toRadians(rotation));

			this.currentPathPoints[i] = currentPt.x;
			//Y is always the same
			this.currentPathPoints[i + 2] = currentPt.y;
		}

		this.pathPoints = this.currentPathPoints.clone();
	}

	private void rotatePoint(Vector2f ptToRotate, float angle)
	{//Rotates (in a counter clock-wise direction, see link for info) given point around origin
		//https://stackoverflow.com/a/25196651/6118785

		float s = (float) Math.sin(angle);
		float c = (float) Math.cos(angle);

		//Calculate new coords
		float xnew = ptToRotate.x * c + ptToRotate.y * s;
		float znew = -ptToRotate.x * s + ptToRotate.y * c;

		//Apply new position
		ptToRotate.x = xnew;
		ptToRotate.y = znew;
	}

	public void movePath()
	{//Moves path's pts into current block position

		Vector3f blockPos = new Vector3f(this.currentRoadBlock.getPosition());

		for (int i = 0; i < this.pathPoints.length; i += 3)
		{
			this.currentPathPoints[i] = this.pathPoints[i] + blockPos.getX();
			//Y is always the same
			this.currentPathPoints[i + 2] = this.pathPoints[i + 2] + blockPos.getZ();
		}
	}

	private void reversePaths()
	{//Reverses path's points order (Note: remember that a point consist in 3 positions in the array, not 1)
		//Reverses both this.currentPathPoints and this.pathPoints

		float x;
		float y;
		float z;
		int pathLen = this.currentPathPoints.length;

		//For each point
		for (int i = 0; i < (pathLen / 3) / 2; i++)
		{
			i *= 3;

			//Reverse
			x = this.currentPathPoints[i];
			y = this.currentPathPoints[i + 1];
			z = this.currentPathPoints[i + 2];

			this.currentPathPoints[i] = this.currentPathPoints[(pathLen - 1) - i - 2];
			this.currentPathPoints[i + 1] = this.currentPathPoints[(pathLen - 1) - i - 1];
			this.currentPathPoints[i + 2] = this.currentPathPoints[(pathLen - 1) - i];

			this.currentPathPoints[(pathLen - 1) - i - 2] = x;
			this.currentPathPoints[(pathLen - 1) - i - 1] = y;
			this.currentPathPoints[(pathLen - 1) - i] = z;

			//Reverse
			x = this.pathPoints[i];
			y = this.pathPoints[i + 1];
			z = this.pathPoints[i + 2];

			this.pathPoints[i] = this.pathPoints[(pathLen - 1) - i - 2];
			this.pathPoints[i + 1] = this.pathPoints[(pathLen - 1) - i - 1];
			this.pathPoints[i + 2] = this.pathPoints[(pathLen - 1) - i];

			this.pathPoints[(pathLen - 1) - i - 2] = x;
			this.pathPoints[(pathLen - 1) - i - 1] = y;
			this.pathPoints[(pathLen - 1) - i] = z;

			i /= 3;
		}
	}

	private void computeNextEdgeLenght()
	{
		//Replace this with data from xml file, computing this thing hundreds of times per seconds is unnecessary TODO
		this.currentEdgeLength = MathUtils.distance(this.pathPoints[this.currentPointOnPath * 3], this.pathPoints[(this.currentPointOnPath * 3) + 2],
				this.pathPoints[(this.currentPointOnPath + 1) * 3], this.pathPoints[((this.currentPointOnPath + 1) * 3) + 2]);
	}

	private void increaseNextPosition(float x, float y, float z)
	{
		this.nextPosition.x += x;
		this.nextPosition.y += y;
		this.nextPosition.z += z;
	}

	protected void waitTurn()
	{//Waits for nextVehicle to signal its child, this v included, that it has computed and they can execute

		if (this.nextVehicle != null && !this.nextVehicle.isDone())
		{
			try
			{
				this.nextVehicle.personalTimer.await();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void invertVehicleStatus()
	{
		this.blockedByUser = !this.blockedByUser;

		if (this.blockedByUser)
		{
			this.setOverlayColor(new Vector3f(255, 81, 0));
		}
		else
		{
			this.setOverlayColor(new Vector3f(255, 255, 255));
		}
	}

	public void updateValuesInGUI()
	{//https://stackoverflow.com/questions/32542355/why-using-split-returns-an-empty-array

		if (EngineData.selectedEntity == this || EngineData.lastSelectedVehicle == this)
		{
			//this.loadNextVehicleData();
			Gui_VehicleStatus.vName.setText(this.toString().split("\\.")[1]);
			Gui_VehicleStatus.velocity.setText(Float.toString(this.velocity));
			Gui_VehicleStatus.justBorn.setText(Boolean.toString(justBorn));
			Gui_VehicleStatus.isDone.setText(Boolean.toString(this.isDone()));
			Gui_VehicleStatus.isBlockedByUser.setText(Boolean.toString(this.blockedByUser));
			Gui_VehicleStatus.isManagedByIntManager.setText(Boolean.toString(this.managedByIntManager));
			Gui_VehicleStatus.hasIntersectionToken.setText(Boolean.toString(this.intersectionToken));
			Gui_VehicleStatus.currRoadBlock.setText(this.currentRoadBlock.toString().split("\\.")[1] + " with ID: " + Integer.toString(this.currentBlockId) + " @ "
					+ Arrays.toString(RoadData.getBlockCoordsFromId(this.currentBlockId)));
			Gui_VehicleStatus.currDirValue.setText(this.currentDirectionalValue);
			Gui_VehicleStatus.currPathConfig.setText(Arrays.toString(this.currentConfig));
			Gui_VehicleStatus.nextIntExitDir.setText(this.getNextIntDirValue(-1));
			Gui_VehicleStatus.distFromIntersection.setText(Float.toString(this.distFromNextInt));
			Gui_VehicleStatus.assignedPathLength.setText(Float.toString(this.assignedBlockPathLenght));
			Gui_VehicleStatus.lengthOfPathTravelled.setText(Float.toString(this.lengthOfBlockPathTraveled));
			Gui_VehicleStatus.lengthOfEdgeTravelled.setText(Float.toString(this.lengthOfEdgeTravelled));
			Gui_VehicleStatus.distTravelled.setText(Float.toString(this.distanceTravelled));
			Gui_VehicleStatus.distFromNextVehicle.setText(Float.toString(this.distFromNextVehicle));
			Gui_VehicleStatus.intersectionInTheWay.setText(Boolean.toString(this.hasIntersectionInFront));

			Gui_VehicleStatus.isReadyToCross.setText(Boolean.toString(this.getIsReadyToCross()));
			Gui_VehicleStatus.currBitMask.setText(Boolean.toString(this.terminateVehicle));

			if (this.nextVehicle != null)
			{
				Gui_VehicleStatus.nextVehicle.setText(this.nextVehicle.toString());
			}
			else
			{
				Gui_VehicleStatus.nextVehicle.setText("None");
			}
		}
	}

	public float getVelocity()
	{
		return velocity;
	}

	public boolean isBlockedByUser()
	{
		return this.blockedByUser;
	}

	public float getLengthOfPathTravelled()
	{
		return lengthOfBlockPathTraveled;
	}

	public boolean isDone()
	{
		return this.done;
	}

	public void setManagedByIntManager(boolean value)
	{
		this.managedByIntManager = value;
	}

	public void terminateVehicle()
	{
		this.terminateVehicle = true;
	}

	public String getCurrentDirectionValues()
	{
		return this.currentDirectionalValue;
	}

	public String getNextIntDirValue(int intManagerBlockId)
	{
		ArrayList<Integer> casdaf = RoadManager.arrayToArrayList(this.routeRoadBlocks);

		if (intManagerBlockId != -1)
		{
			return RoadData.routeIndexToDirectionValues.get(this.assignedRouteIndex)[casdaf.indexOf(intManagerBlockId)];
		}
		else
		{//If it exists, returns next int exit direction, "/" otherwise

			if (hasIntersectionInFront)
			{
				return RoadData.routeIndexToDirectionValues.get(this.assignedRouteIndex)[casdaf.indexOf(this.nextIntManager.getBlockId())];
			}
			else
			{
				return "/";
			}
		}
	}

	public float getRemainingToTravelOfBlock()
	{
		return this.assignedBlockPathLenght - this.lengthOfBlockPathTraveled;
	}

	public boolean getIsReadyToCross()
	{
		return readyToCross;
	}

	public boolean isNextVehicleBlocked()
	{
		if (this.nextVehicle != null)
		{
			if (this.nextVehicle.isBlockedByUser()) return true;
			else return false;
		}

		return false;

	}
}