package Exceptions;

public class NonValidRoadBlockConfigException extends Exception
{
	public NonValidRoadBlockConfigException(String config)
	{
		super(config);
	}
}