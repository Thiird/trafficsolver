package Entities;

import org.lwjgl.util.vector.Vector3f;

import Models.TexturedModel;

public class Entity
{
	private TexturedModel model;

	private Vector3f position;
	private float rotX, rotY, rotZ;
	private float scale;
	private Vector3f overlayColor;
	private float alpha;
	private boolean toRender = true;
	private Vector3f colorId;

	public Entity(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, Vector3f oColor, boolean toRender, float alpha, Vector3f colorId)
	{
		this.model = model;

		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;

		if (oColor != null)
		{
			this.overlayColor = oColor;
		}
		else
		{
			this.overlayColor = new Vector3f(255, 255, 255);
		}

		this.toRender = toRender;

		this.alpha = alpha;

		this.colorId = colorId;
	}

	public void increasePosition(float dx, float dy, float dz)
	{
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}

	public void increaseRotation(float dx, float dy, float dz)
	{
		this.rotX += dx;
		this.rotY += dy;
		this.rotZ += dz;
	}

	public TexturedModel getModel()
	{
		return model;
	}

	public void setModel(TexturedModel model)
	{
		this.model = model;
	}

	public Vector3f getPosition()
	{
		return position;
	}

	public void setPosition(Vector3f position)
	{
		this.position = position;
	}

	public void setPosition(float x, float y, float z)
	{
		this.position.set(x, y, z);
	}

	public float getRotX()
	{
		return rotX;
	}

	public void setRotX(float rotX)
	{
		this.rotX = rotX;
	}

	public float getRotY()
	{
		return rotY;
	}

	public void setRotY(float rotY)
	{
		this.rotY = rotY;
	}

	public float getRotZ()
	{
		return rotZ;
	}

	public void setRotZ(float rotZ)
	{
		this.rotZ = rotZ;
	}

	public float getScale()
	{
		return scale;
	}

	public void setScale(float scale)
	{
		this.scale = scale;
	}

	public Vector3f getOverlayColor()
	{
		return this.overlayColor;
	}

	public void setOverlayColor(Vector3f oColor)
	{
		this.overlayColor.x = oColor.x;
		this.overlayColor.y = oColor.y;
		this.overlayColor.z = oColor.z;
	}

	public float getAlpha()
	{
		return alpha;
	}

	public void setAlpha(float alpha)
	{
		this.alpha = alpha;
	}

	public boolean isToRender()
	{
		return this.toRender;
	}

	public void setToRender(boolean toRender)
	{
		this.toRender = toRender;
	}

	public Vector3f getColorId()
	{
		return this.colorId;
	}
}