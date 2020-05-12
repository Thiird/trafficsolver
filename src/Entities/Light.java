package Entities;

import org.lwjgl.util.vector.Vector3f;

public class Light
{
	private Vector3f position;
	private Vector3f colour;

	public Light(Vector3f position, Vector3f colour)
	{
		this.position = position;
		this.colour = colour;
	}

	public Vector3f getPosition()
	{
		return position;
	}

	public void setPosition(float x, float y, float z)
	{
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}

	public Vector3f getColour()
	{
		return colour;
	}

	public void setColour(Vector3f colour)
	{
		this.colour = colour;
	}
}