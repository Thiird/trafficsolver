package PathFinder;

import java.util.ArrayList;

public class Node
{
	//Node data
	private float f;
	private int g;
	private float h;
	private int index;
	private ArrayList<Node> neighbors;
	private Node previous = null;
	private boolean isObtsacle;

	//Node pos in grid
	private int x;
	private int y;

	public Node(int index, int x, int y, boolean isObstacle)
	{
		this.index = index;

		this.x = x;
		this.y = y;

		this.isObtsacle = isObstacle;

		this.neighbors = new ArrayList<Node>();
	}

	public void addNeighbors(ArrayList<ArrayList<Node>> map, int mapWidth, int mapHeight)
	{
		if (this.x < mapWidth - 1)
		{
			this.neighbors.add(map.get(y).get(x + 1));
		}

		if (this.x > 0)
		{
			this.neighbors.add(map.get(y).get(x - 1));
		}

		if (this.y < mapHeight - 1)
		{
			this.neighbors.add(map.get(y + 1).get(x));
		}

		if (this.y > 0)
		{
			this.neighbors.add(map.get(y - 1).get(x));
		}
	}

	public ArrayList<Node> getNeighbors()
	{
		return this.neighbors;
	}

	public void setPrevious(Node n)
	{
		this.previous = n;
	}

	public Node getPrevious()
	{
		return this.previous;
	}

	public int getIndex()
	{
		return this.index;
	}

	public float getF()
	{
		return f;
	}

	public void setF(float f)
	{
		this.f = f;
	}

	public int getG()
	{
		return g;
	}

	public void setG(int g)
	{
		this.g = g;
	}

	public float getH()
	{
		return h;
	}

	public void setH(float h)
	{
		this.h = h;
	}

	public int getX()
	{
		return this.x;
	}

	public int getY()
	{
		return y;
	}

	public boolean isObtsacle()
	{
		return isObtsacle;
	}

	public void setObtsacle(boolean isObtsacle)
	{
		this.isObtsacle = isObtsacle;
	}
}