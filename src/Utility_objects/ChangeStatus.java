package Utility_objects;

public class ChangeStatus
{
	private volatile static boolean changeContex = false;
	private volatile static boolean freeToGo = false;

	public static synchronized boolean isContextRequested()
	{
		return changeContex;
	}

	public static synchronized boolean canProceed()
	{
		return freeToGo;
	}

	public static synchronized void changeValue(boolean value)
	{
		changeContex = value;
	}

	public static synchronized void setGoFlag(boolean flag)
	{
		freeToGo = flag;
	}
}