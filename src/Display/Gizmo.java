package Display;

import org.lwjgl.util.vector.Vector3f;

public class Gizmo
{
	private Vector3f position = new Vector3f(0, 0, 0);

	public Gizmo()
	{

	}

	public void setPosition(Vector3f pos)
	{
		this.position = new Vector3f(pos);
	}

	public Vector3f getPosition()
	{
		return this.position;
	}
}