package Render_engine;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Entities.Entity;
import Entities.Light;
import Models.RawModel;
import Models.TexturedModel;
import Shaders.IdShader;
import Shaders.ShaderProgram;
import Shaders.StaticShader;
import Threads.Executor;
import Utility.MathUtils;

public class EntityRenderer
{
	private Vector3f bgColor = new Vector3f();
	private StaticShader staticShader = new StaticShader();
	private IdShader idShader = new IdShader();
	private ShaderProgram currentShader = staticShader;//Initially the static shader is used

	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(EngineData.getMaxInstances() * EngineData.getInstanceDataLength());
	private int pointer = 0;
	float[] vboData;
	TexturedModel tempTm;
	ArrayList<Entity> tempEntityList;

	public EntityRenderer(Matrix4f projectionMatrix, Vector3f bgColor)
	{
		this.bgColor = bgColor;

		staticShader.start();
		staticShader.loadProjectionMatrix(projectionMatrix);
		staticShader.stop();

		idShader.start();
		idShader.loadProjectionMatrix(projectionMatrix);
		idShader.stop();
	}

	public void render(ArrayList<ArrayList<Object>> batch, Light sun, Matrix4f viewMat)
	{
		if (MasterRenderer.renderIdView)
		{
			this.idShader.start();
			this.idShader.loadViewMatrix(viewMat);
		}
		else
		{
			this.staticShader.start();
			this.staticShader.loadBgColor(this.bgColor);
			this.staticShader.loadLight(sun);
			this.staticShader.loadViewMatrix(viewMat);
		}

		for (ArrayList<Object> tempArray : batch)
		{
			this.tempTm = (TexturedModel) tempArray.get(0);
			this.tempEntityList = (ArrayList<Entity>) tempArray.get(1);

			this.bindTexturedModel(this.tempTm);

			this.vboData = new float[this.tempEntityList.size() * EngineData.getInstanceDataLength()];
			this.pointer = 0;

			for (Entity e : this.tempEntityList)
			{
				if (e.isToRender())
				{
					this.storeTransformationMatrix(e);
					this.storeOverlayColor(e.getOverlayColor());

					//Selected entity blinking effect
					if (EngineData.selectedEntity == e) this.storeAlpha(Executor.transparencyValue);
					else this.storeAlpha(e.getAlpha());

					this.storeColorId(e.getColorId());
				}
			}

			EngineData.updateVbo(this.tempTm.getRawModel().getVaoID(), this.vboData, this.buffer);

			// Render
			GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, this.tempTm.getRawModel().getIndicesCount(), GL11.GL_UNSIGNED_INT, 0L, tempEntityList.size());

			this.unbindTexturedModel();
		}

		if (MasterRenderer.renderIdView) this.idShader.stop();
		else this.staticShader.start();
	}

	private void storeTransformationMatrix(Entity e)
	{
		Matrix4f tMatrix = MathUtils.createTransfMatrix(e.getPosition(), e.getRotX(), e.getRotY(), e.getRotZ(), e.getScale());

		this.vboData[this.pointer++] = tMatrix.m00;
		this.vboData[this.pointer++] = tMatrix.m01;
		this.vboData[this.pointer++] = tMatrix.m02;
		this.vboData[this.pointer++] = tMatrix.m03;
		this.vboData[this.pointer++] = tMatrix.m10;
		this.vboData[this.pointer++] = tMatrix.m11;
		this.vboData[this.pointer++] = tMatrix.m12;
		this.vboData[this.pointer++] = tMatrix.m13;
		this.vboData[this.pointer++] = tMatrix.m20;
		this.vboData[this.pointer++] = tMatrix.m21;
		this.vboData[this.pointer++] = tMatrix.m22;
		this.vboData[this.pointer++] = tMatrix.m23;
		this.vboData[this.pointer++] = tMatrix.m30;
		this.vboData[this.pointer++] = tMatrix.m31;
		this.vboData[this.pointer++] = tMatrix.m32;
		this.vboData[this.pointer++] = tMatrix.m33;
	}

	private void storeOverlayColor(Vector3f oColor)
	{
		this.vboData[this.pointer++] = oColor.x;
		this.vboData[this.pointer++] = oColor.y;
		this.vboData[this.pointer++] = oColor.z;
	}

	private void storeAlpha(float alpha)
	{
		this.vboData[this.pointer++] = alpha;
	}

	private void storeTextureMap(Vector3f colorId)
	{
		this.vboData[this.pointer++] = colorId.x / 255f;
		this.vboData[this.pointer++] = colorId.y / 255f;
		this.vboData[this.pointer++] = colorId.z / 255f;
	}

	private void storeColorId(Vector3f colorId)
	{//OpenGl expects colors in [0,1] range so have to divide by 255

		this.vboData[this.pointer++] = colorId.x / 255f;
		this.vboData[this.pointer++] = colorId.y / 255f;
		this.vboData[this.pointer++] = colorId.z / 255f;
	}

	private void bindTexturedModel(TexturedModel tm)
	{
		RawModel rawModel = tm.getRawModel();

		//Bind VAO
		GL30.glBindVertexArray(rawModel.getVaoID());

		// Enabling attributes in the VAO

		GL20.glEnableVertexAttribArray(0);//Position

		if (rawModel.hasUvs())
		{
			GL20.glEnableVertexAttribArray(1);
		}
		if (rawModel.hasNormals())
		{
			GL20.glEnableVertexAttribArray(2);
		}

		//Transformation matrix
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		GL20.glEnableVertexAttribArray(5);
		GL20.glEnableVertexAttribArray(6);

		//Color overlay values
		GL20.glEnableVertexAttribArray(7);

		//Alpha masking values
		GL20.glEnableVertexAttribArray(8);

		//Color id value
		GL20.glEnableVertexAttribArray(9);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tm.getTexture().getId());
	}

	private void unbindTexturedModel()
	{
		// Disabling attributes in the VAO
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);

		//Transformation matrix
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL20.glDisableVertexAttribArray(5);
		GL20.glDisableVertexAttribArray(6);

		//Color overlay
		GL20.glDisableVertexAttribArray(7);

		//Alpha masking
		GL20.glDisableVertexAttribArray(8);

		//Color id
		GL20.glDisableVertexAttribArray(9);

		//UnBind VAO
		GL30.glBindVertexArray(0);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
	}

	public void cleanUpShader()
	{
		this.currentShader.cleanUp();
	}

	public void setSkyColor(Vector3f bgColor)
	{
		this.bgColor = bgColor;
	}
}