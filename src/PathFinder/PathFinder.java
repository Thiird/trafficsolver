package PathFinder;

import java.util.ArrayList;
import java.util.Collections;

import Exceptions.NoSolutionToPathFindingProcess;
import TrafficLogic.RoadData;
import Utility.MathUtils;

public class PathFinder
{
	public static ArrayList<Integer> findPath(int xStart, int yStart, int xEnd, int yEnd) throws NoSolutionToPathFindingProcess
	{//A STARRRR
		//Leave an outer ring, so that I dont create intManagers on the road borders, not nice to spawn vehicles there
		int roadWidth = RoadData.gridWidth - 2;
		int roadHeight = RoadData.gridHeight - 2;

		int index = 0;
		int winner = 0;

		Node tempNode1 = null;
		Node tempNode2;

		ArrayList<Node> openSet = new ArrayList<Node>();
		ArrayList<Node> closedSet = new ArrayList<Node>();

		ArrayList<ArrayList<Node>> map = new ArrayList<ArrayList<Node>>();
		ArrayList<Integer> finalPath = new ArrayList<Integer>();

		//Init nodeMap, set obstacle nodes
		for (int y = 0; y < roadHeight; y++) //For each row
		{
			map.add(new ArrayList<Node>());

			for (int x = 0; x < roadWidth; x++) //For each column
			{
				map.get(y).add(new Node(index, x, y, RoadData.bitMap[y + 1][x + 1] == -1));
				index++;
			}
		}

		//Init nodes neighbors
		for (int y = 0; y < roadHeight; y++) //For each row
		{
			for (int x = 0; x < roadWidth; x++) //For each column
			{
				map.get(y).get(x).addNeighbors(map, roadWidth, roadHeight);
			}
		}

		//Calculate route--------------------------------

		//Add start element to openSet
		openSet.add(map.get(yStart).get(xStart));

		while (true)
		{
			if (openSet.size() > 0)
			{
				winner = 0;

				for (int i = 0; i < openSet.size(); i++)
				{
					if (openSet.get(i).getF() < openSet.get(winner).getF()) winner = i;
				}

				tempNode1 = openSet.get(winner);

				if (tempNode1.getX() == xEnd && tempNode1.getY() == yEnd)
				{
					while (tempNode1 != null)
					{
						finalPath.add(tempNode1.getIndex());

						//Load next block
						tempNode1 = tempNode1.getPrevious();
					}

					Collections.reverse(finalPath);

					return finalPath;
				}

				closedSet.add(tempNode1);
				openSet.remove(tempNode1);

				ArrayList<Node> tempNeighbors = tempNode1.getNeighbors();

				for (int i = 0; i < tempNeighbors.size(); i++)
				{
					tempNode2 = tempNeighbors.get(i);
					if (!closedSet.contains(tempNode2) && !tempNode2.isObtsacle())
					{
						int g = tempNode1.getG() + 1;

						if (openSet.contains(tempNode2))
						{
							if (g < tempNode2.getG()) tempNode2.setG(g);
						}
						else
						{
							tempNode2.setG(g);
							openSet.add(tempNode2);
						}

						//Compute heuristic -- H value
						tempNode2.setH(MathUtils.distance(tempNode2.getX(), tempNode2.getY(), xEnd, yEnd));
						tempNode2.setF(tempNode2.getG() + tempNode2.getH());
						tempNode2.setPrevious(tempNode1);
					}
				}
			}
			else
			{
				throw new NoSolutionToPathFindingProcess();
			}
		}
	}
}