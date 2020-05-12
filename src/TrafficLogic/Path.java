package TrafficLogic;

import org.lwjgl.util.vector.Vector3f;

import Entities.Entity;
import Models.TexturedModel;

public class Path extends Entity
{
	private float[] pointsCoordinates = null;

	public Path(TexturedModel model, int id, Vector3f position, float rotX, float rotY, float rotZ, float scale, float alpha, float[] points, Vector3f colorId)
	{
		super(model, position, rotX, rotY, rotZ, scale, null, true, alpha, colorId);
		this.pointsCoordinates = points;
	}

	public int getPathLenght()
	{
		return this.pointsCoordinates.length / 3;
	}

	public float[] getPoints()
	{
		return this.pointsCoordinates.clone();
	}
}