package PathFinder;

public class ProximitySensor
{
	private boolean facingNorth = false;
	private boolean facingSouth = false;
	private boolean facingEast = false;
	private boolean facingWest = false;

	public ProximitySensor(boolean N, boolean S, boolean E, boolean W)
	{
		this.facingNorth = N;
		this.facingSouth = S;
		this.facingEast = E;
		this.facingWest = W;
	}

	public void getProximityValues(int[] proximityArray)
	{
		//N
		if (this.facingNorth)
		{
			proximityArray[0] = 1;
		}
		else
		{
			proximityArray[0] = 0;
		}

		//S
		if (this.facingSouth)
		{
			proximityArray[1] = 1;
		}
		else
		{
			proximityArray[1] = 0;
		}

		//E
		if (this.facingEast)
		{
			proximityArray[2] = 1;
		}
		else
		{
			proximityArray[2] = 0;
		}

		//W
		if (this.facingWest)
		{
			proximityArray[3] = 1;
		}
		else
		{
			proximityArray[3] = 0;
		}
	}

	public void setProximities(int[] newProxValues)
	{
		//Setting N value
		if (newProxValues[0] == 1)
		{
			this.facingNorth = true;
		}
		else//TODO togliere tutti gli else?
		{
			this.facingNorth = false;
		}

		//Setting S value
		if (newProxValues[1] == 1)
		{
			this.facingSouth = true;
		}
		else
		{
			this.facingSouth = false;
		}

		//Setting E value
		if (newProxValues[2] == 1)
		{
			this.facingEast = true;
		}
		else
		{
			this.facingEast = false;
		}

		//Setting W value
		if (newProxValues[3] == 1)
		{
			this.facingWest = true;
		}
		else
		{
			this.facingWest = false;
		}
	}

	public boolean facesNorth()
	{
		return facingNorth;
	}

	public boolean facesSouth()
	{
		return facingSouth;
	}

	public boolean facesEast()
	{
		return facingEast;
	}

	public boolean facesWest()
	{
		return facingWest;
	}

	public String toString()
	{
		String s = "";

		//N
		if (this.facingNorth) s += "1";
		else s += "0";

		//S
		if (this.facingSouth) s += "1";
		else s += "0";

		//E
		if (this.facingEast) s += "1";
		else s += "0";

		//W
		if (this.facingWest) s += "1";
		else s += "0";

		return s;
	}
}