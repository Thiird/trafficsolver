package Models;

public class RawModel
{
	private int vaoID;
	private int indicesCount;
	private boolean hasUvs;
	private boolean hasNormals;

	public RawModel(int vaoID, int indicesCount, boolean hasUvs, boolean hasNormals)
	{
		this.vaoID = vaoID;
		this.indicesCount = indicesCount;
		this.hasUvs = hasUvs;
		this.hasNormals = hasNormals;
	}

	public int getVaoID()
	{
		return vaoID;
	}

	public void setVaoID(int vaoID)
	{
		this.vaoID = vaoID;
	}

	public int getIndicesCount()
	{
		return indicesCount;
	}

	public void setVertexCount(int indicesCount)
	{
		this.indicesCount = indicesCount;
	}

	public boolean hasUvs()
	{
		return hasUvs;
	}

	public boolean hasNormals()
	{
		return hasNormals;
	}
}