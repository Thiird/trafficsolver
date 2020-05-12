package Shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class PointShader extends ShaderProgram
{
	private static final String VERTEX_FILE = "Shaders/point_vertexShader.txt";
	private static final String FRAGMENT_FILE = "Shaders/point_fragmentShader.txt";

	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_camPos;
	private int location_bgColor;
	private int location_ptSize;

	public PointShader()
	{
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes()
	{
		super.bindAttributes(0, "position");
		super.bindAttributes(1, "textureCoords");//DA togliere?
		super.bindAttributes(2, "normal");
	}

	@Override
	public void start()
	{
		super.start();
		GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
	}

	@Override
	public void stop()
	{
		super.stop();
		GL11.glDisable(GL32.GL_PROGRAM_POINT_SIZE);
	}

	@Override
	protected void getAllUniformLocations()
	{
		this.location_transformationMatrix = super.getUniFormLocation("transformationMatrix");
		this.location_projectionMatrix = super.getUniFormLocation("projectionMatrix");
		this.location_viewMatrix = super.getUniFormLocation("viewMatrix");
		this.location_bgColor = super.getUniFormLocation("bgColor");
		this.location_camPos = super.getUniFormLocation("camPos");
		this.location_ptSize = super.getUniFormLocation("ptSize");
	}

	public void loadSkyColour(Vector3f bgColor)
	{
		super.loadVector(location_bgColor, new Vector3f(bgColor));
	}

	public void loadCamPos(Vector3f camPos)
	{
		super.loadVector(location_camPos, camPos);
	}

	public void loadPtSize(float ptSize)
	{
		super.loadFloat(this.location_ptSize, ptSize);
	}

	public void loadTransformationMatrix(Matrix4f matrix)
	{
		super.loadMatrix(location_transformationMatrix, matrix);
	}

	public void loadViewMatrix(Matrix4f viewMat)
	{
		super.loadMatrix(location_viewMatrix, viewMat);
	}

	public void loadProjectionMatrix(Matrix4f projection)
	{
		super.loadMatrix(location_projectionMatrix, projection);
	}
}