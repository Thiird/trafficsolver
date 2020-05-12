package Shaders;

import org.lwjgl.util.vector.Matrix4f;

public class IdShader extends ShaderProgram
{
	private static final String VERTEX_FILE = "Shaders/id_vertexShader.txt";
	private static final String FRAGMENT_FILE = "Shaders/id_fragmentShader.txt";

	private int location_projectionMatrix;
	private int location_viewMatrix;

	public IdShader()
	{
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes()
	{
		super.bindAttributes(0, "position");
		super.bindAttributes(1, "textureCoords");
		super.bindAttributes(2, "normal");
		super.bindAttributes(3, "transformationMatrix");
		super.bindAttributes(7, "overlayColor");
		super.bindAttributes(8, "alpha");
		super.bindAttributes(9, "id");
	}

	@Override
	protected void getAllUniformLocations()
	{
		this.location_projectionMatrix = super.getUniFormLocation("projectionMatrix");
		this.location_viewMatrix = super.getUniFormLocation("viewMatrix");
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