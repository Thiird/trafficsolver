package Utility;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class MathUtils
{
	public static Matrix4f createTransfMatrix(Vector2f translation, Vector2f scale)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
		return matrix;
	}

	public static Matrix4f createTransfMatrix(Vector3f translation, float rx, float ry, float rz, float scale)
	{
		Matrix4f matrix = new Matrix4f();

		matrix.setIdentity();

		//Translate		
		Matrix4f.translate(translation, matrix, matrix);

		//Rotate
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);

		//Scale
		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);

		return matrix;
	}

	public static float distance(Vector3f one, Vector3f two)
	{
		return (float) Math.sqrt(Math.pow(one.x - two.x, 2) + Math.pow(one.y - two.y, 2) + Math.pow(one.z - two.z, 2));
	}

	public static float distance(float xStart, float yStart, float xEnd, float yEnd)
	{
		return (float) Math.sqrt(Math.pow(xStart - xEnd, 2) + Math.pow(yStart - yEnd, 2));
	}

	public static Vector3f normalize(Vector3f vec)
	{
		if (vec.length() == 0)
		{
			vec.set(0f, 0f, 0f);
			return vec;
		}
		else
		{
			return new Vector3f(vec.x / vec.length(), vec.y / vec.length(), vec.z / vec.length());
		}
	}
}