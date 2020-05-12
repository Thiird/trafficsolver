package Shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class LineShader extends ShaderProgram
{
	private static final String VERTEX_FILE = "Shaders/line_vertexShader.txt";
	private static final String FRAGMENT_FILE = "Shaders/line_fragmentShader.txt";

	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_camPos;
	private int location_lineWidth;

	public LineShader()
	{
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes()
	{
		super.bindAttributes(0, "position");
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
		this.location_camPos = super.getUniFormLocation("camPos");
		this.location_lineWidth = super.getUniFormLocation("lineWidth");
	}

	public void loadLineWidth(float ptSize)
	{
		super.loadFloat(this.location_lineWidth, ptSize);
	}

	public void loadCamPos(Vector3f camPos)
	{
		super.loadVector(location_camPos, camPos);
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