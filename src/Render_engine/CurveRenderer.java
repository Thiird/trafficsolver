package Render_engine;

import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class CurveRenderer
{
	private Vector3f skyColor = new Vector3f();
	private PointRenderer pointRenderer;
	private LineRenderer lineRenderer;

	public CurveRenderer(Matrix4f projectionMatrix, Vector3f skyColor)
	{
		this.skyColor = skyColor;
		pointRenderer = new PointRenderer(projectionMatrix, skyColor);
		lineRenderer = new LineRenderer(projectionMatrix, skyColor);
	}

	public void render(ArrayList<ArrayList<Object>> batch, Matrix4f viewMat, Vector3f camPos)
	{
		this.lineRenderer.render(batch, viewMat, camPos);
		this.pointRenderer.render(batch, viewMat, camPos);
	}

	public void cleanUpShaders()
	{
		this.pointRenderer.cleanUpShader();
		this.lineRenderer.cleanUpShader();
	}

	public void setSkyColor(Vector3f skyColor)
	{
		this.pointRenderer.setSkyColor(skyColor);
		this.lineRenderer.setSkyColor(skyColor);
	}
}