package Textures;

public class ModelTexture
{
	private int textureId;
	private int shineDamper = 1;
	private float reflectivity = 0;

	private boolean hasTransparency = false;
	private boolean useFakeLighting = false;

	public ModelTexture(int id)
	{
		this.textureId = id;
	}

	public boolean getUseFakeLighting()
	{
		return useFakeLighting;
	}

	public boolean getHasTransparency()
	{
		return hasTransparency;
	}

	public int getId()
	{
		return textureId;
	}

	public int getShineDamper()
	{
		return shineDamper;
	}

	public void setShineDamper(int shineDamper)
	{
		this.shineDamper = shineDamper;
	}

	public float getReflectivity()
	{
		return reflectivity;
	}

	public void setReflectivity(float reflectivity)
	{
		this.reflectivity = reflectivity;
	}
}