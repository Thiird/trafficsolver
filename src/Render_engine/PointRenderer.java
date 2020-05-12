package Render_engine;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Entities.Entity;
import Models.RawModel;
import Models.TexturedModel;
import Shaders.PointShader;
import Utility.MathUtils;

public class PointRenderer
{
	private Vector3f skyColor = new Vector3f();
	private PointShader shader = new PointShader();

	public PointRenderer(Matrix4f projectionMatrix, Vector3f skyColor)
	{
		this.skyColor = skyColor;

		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void render(ArrayList<ArrayList<Object>> batch, Matrix4f viewMat, Vector3f campPos)
	{
		this.shader.start();

		this.shader.loadSkyColour(this.skyColor);
		this.shader.loadViewMatrix(viewMat);
		this.shader.loadCamPos(campPos);
		//this.shader.loadPtSize(5f); this is calculated in the shader

		for (ArrayList<Object> tempArray : batch)
		{
			this.prepareModel((TexturedModel) tempArray.get(0));

			for (Entity e : (ArrayList<Entity>) tempArray.get(1))
			{
				if (e.isToRender())
				{
					this.prepareInstance(e);

					// Render
					GL11.glDrawElements(GL11.GL_POINTS, (e.getModel().getRawModel()).getIndicesCount(), GL11.GL_UNSIGNED_INT, 0);
				}
			}
		}

		this.shader.stop();

		this.unbindModel();
	}

	private void prepareModel(TexturedModel model)
	{
		RawModel rawModel = model.getRawModel();

		GL30.glBindVertexArray(rawModel.getVaoID());

		// Enabling attributes in the VAO
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
	}

	private void unbindModel()
	{
		this.enableCulling();

		// Disabling attributes in the VAO
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
	}

	private void enableCulling()
	{
		// Not render polys facing away from the camera
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	private void prepareInstance(Entity entity)
	{
		Matrix4f transformationMatrix = MathUtils.createTransfMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());

		this.shader.loadTransformationMatrix(transformationMatrix);
	}

	public void cleanUpShader()
	{
		this.shader.cleanUp();
	}

	public void setSkyColor(Vector3f skyColor)
	{
		this.skyColor = skyColor;
	}
}