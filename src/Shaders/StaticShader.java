package Shaders;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Entities.Light;

public class StaticShader extends ShaderProgram
{
	private static final String VERTEX_FILE = "Shaders/static_vertexShader.txt";
	private static final String FRAGMENT_FILE = "Shaders/static_fragmentShader.txt";

	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_lightPosition;
	private int location_lightColour;
	private int location_shineDamper;
	private int location_reflectivity;
	private int location_useFakeLighting;
	private int location_bgColor;

	public StaticShader()
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
		location_projectionMatrix = super.getUniFormLocation("projectionMatrix");
		location_viewMatrix = super.getUniFormLocation("viewMatrix");
		location_lightPosition = super.getUniFormLocation("lightPosition");
		location_lightColour = super.getUniFormLocation("lightColour");
		location_shineDamper = super.getUniFormLocation("shineDamper");
		location_reflectivity = super.getUniFormLocation("reflectivity");
		location_useFakeLighting = super.getUniFormLocation("useFakeLighting");
		location_bgColor = super.getUniFormLocation("bgColor");
	}

	public void loadBgColor(Vector3f bgColor)
	{
		super.loadVector(location_bgColor, new Vector3f(bgColor));
	}

	public void loadFakeLighting(boolean useFake)
	{
		super.loadBoolean(location_useFakeLighting, useFake);
	}

	public void loadShineVariable(float damper, float reflectivity)
	{
		super.loadFloat(location_shineDamper, damper);
		super.loadFloat(location_reflectivity, reflectivity);
	}

	public void loadLight(Light light)
	{
		super.loadVector(location_lightPosition, light.getPosition());
		super.loadVector(location_lightColour, light.getColour());
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