package Render_engine;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Entities.Light;
import Models.TexturedModel;
import Utility.MathUtils;

public class MasterRenderer
{
	private static final int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
	public static ByteBuffer buffer = BufferUtils.createByteBuffer(1 * 1 * bpp);

	public static boolean renderIdView = true;

	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 300;

	private static TexturedModel tmTemp;

	private static ArrayList<ArrayList<Object>> tModelsToRender = new ArrayList<ArrayList<Object>>();
	private static ArrayList<ArrayList<Object>> curvesToRender = new ArrayList<ArrayList<Object>>();
	private static ArrayList<ArrayList<Object>> entitiesToRender;

	private Vector3f bgColor = new Vector3f();
	private Matrix4f projectionMatrix;

	private EntityRenderer entityRenderer;
	private CurveRenderer curveRenderer;

	public MasterRenderer(Vector3f skyColor)
	{
		this.bgColor = skyColor;
		this.bgColor = MathUtils.normalize(this.bgColor);//Just in case

		this.enableCulling();

		this.createProjectionMatrix();

		this.entityRenderer = new EntityRenderer(projectionMatrix, skyColor);
		this.curveRenderer = new CurveRenderer(projectionMatrix, skyColor);

	}

	public void renderScene(Light sun, Matrix4f viewMat, Vector3f camPos)
	{
		// Prepare scene for rendering
		this.prepare();

		entitiesToRender = EngineData.getEntitiesToRenderList();

		for (ArrayList<Object> tempArray : entitiesToRender)
		{
			tmTemp = (TexturedModel) tempArray.get(0);

			if (tmTemp.getTexture() == null) //Curve rendering, render model as wireframe
			{
				curvesToRender.add(new ArrayList<Object>(Arrays.asList(tmTemp, tempArray.get(1))));
			}
			else //Entities rendering, standard rendering
			{
				tModelsToRender.add(new ArrayList<Object>(Arrays.asList(tmTemp, tempArray.get(1))));
			}
		}

		//Render -- called here and not into the for loop so I can avoid starting/stopping the shaders more than once per kind of entity/frame
		if (tModelsToRender.size() != 0)
		{
			this.entityRenderer.render(tModelsToRender, sun, viewMat);
		}
		if (curvesToRender.size() != 0)
		{
			this.curveRenderer.render(curvesToRender, viewMat, camPos);
		}

		tModelsToRender.clear();
		curvesToRender.clear();
	}

	private void enableCulling()
	{
		// Not render polys facing away from the camera
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	public void prepare()
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Background color
		GL11.glClearColor(this.bgColor.x, this.bgColor.y, this.bgColor.z, 1);
	}

	public void cleanUp()
	{
		this.entityRenderer.cleanUpShader();
		this.curveRenderer.cleanUpShaders();
	}

	private void createProjectionMatrix()
	{
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		this.projectionMatrix = new Matrix4f();
		this.projectionMatrix.m00 = x_scale;
		this.projectionMatrix.m11 = y_scale;
		this.projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		this.projectionMatrix.m23 = -1;
		this.projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		this.projectionMatrix.m33 = 0;
	}

	public void setProjectionMatrix(Matrix4f projMat)
	{
		this.projectionMatrix = projMat;
	}

	public Matrix4f getProjectionMatrix()
	{
		return projectionMatrix;
	}

}