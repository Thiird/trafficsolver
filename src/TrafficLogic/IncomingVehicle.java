package TrafficLogic;

public class IncomingVehicle
{
	private Vehicle vehicle;
	private float distFromIntersection;
	private String dir; //Direction once inside intersection

	public IncomingVehicle(Vehicle v, float d, String dir)
	{
		this.vehicle = v;
		this.distFromIntersection = d;
		this.dir = dir;
	}

	public IncomingVehicle()
	{
	}

	public Vehicle getVehicle()
	{
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle)
	{
		//System.out.println("Setted to : " + vehicle);
		this.vehicle = vehicle;
	}

	public float getDistFromIntersection()
	{
		return distFromIntersection;
	}

	public void setDistFromIntersection(float distFromIntersection)
	{
		this.distFromIntersection = distFromIntersection;
	}

	public void setDirection(String dir)
	{
		this.dir = dir;
	}

	public String getDirectionInIntersection()
	{//Returns the direction that this vehicle wil go through in the intersection
		return dir;
	}

	public int getIntEnterDir()
	{
		return Character.getNumericValue(dir.charAt(0));
	}
}