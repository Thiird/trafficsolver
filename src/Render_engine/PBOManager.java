package Render_engine;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

public class PBOManager
{
	private static final int BYTES_PER_PIXEL = 4;
	private int pbos[];
	private int index = 0;
	private int nextIndex = 1;

	public static ByteBuffer buffer;

	public static boolean colorIsAvaible = false;

	public PBOManager()
	{
		buffer = BufferUtils.createByteBuffer(BYTES_PER_PIXEL);

		initPbos(2);
	}

	private void initPbos(int count)
	{
		this.pbos = new int[count];

		for (int i = 0; i < pbos.length; i++)
		{
			this.pbos[i] = createPbo();
		}
	}

	private int createPbo()
	{
		int pbo = GL15.glGenBuffers();

		GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, pbo);

		GL15.glBufferData(GL21.GL_PIXEL_PACK_BUFFER, BYTES_PER_PIXEL, GL15.GL_STREAM_READ);

		GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);

		return pbo;
	}

	public void readPixelData()
	{
		// "index" is used to read pixels from framebuffer to a PBO
		// "nextIndex" is used to update pixels in the other PBO

		// set the target framebuffer to read
		GL11.glReadBuffer(GL11.GL_BACK);

		// read pixels from framebuffer to PBO
		// glReadPixels() should return immediately.
		GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, this.pbos[this.index]);

		GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0);

		// map the PBO to process its data by CPU
		GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, this.pbos[this.nextIndex]);
		buffer = GL15.glMapBuffer(GL21.GL_PIXEL_PACK_BUFFER, GL15.GL_READ_ONLY, buffer);

		//If buffer not empty (will be empty only on first frame), process pixel data, in this case just print it out
		if (buffer != null && buffer.hasRemaining())
		{
			//this.printCurrentPixelColor();
			this.loadHoveringEntity();

			GL15.glUnmapBuffer(GL21.GL_PIXEL_PACK_BUFFER);
		}
		else
		{
			//TODO why removed this???
			//EngineData.selectedEntity = null;
		}

		// back to conventional pixel operation
		GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);

		this.index = (this.index + 1) % 2;
		this.nextIndex = (this.index + 1) % 2;
	}

	private void loadHoveringEntity()
	{
		//ByteBuffer to RGBA int array
		int[] rgbArray = new int[buffer.capacity()];

		for (int i = 0; i < rgbArray.length; i++)
		{
			rgbArray[i] = buffer.get(i) & 0xFF;
		}

		EngineData.hoveringEntity = EngineData.entitiesIdMap.get(rgbArray[1] + rgbArray[2] * 256);

		//Load hoveringEntityType
		if (rgbArray[0] == 0)
		{//Vehicle
			//System.out.print("Vehicle, id: ");
			EngineData.hoveringEntityType = 0;
		}
		else if (rgbArray[0] == 50)
		{//Roadblock
			//System.out.print("Roadblock, id: ");
			EngineData.hoveringEntityType = 1;
		}
		else if (rgbArray[0] == 85)
		{//Obstacle/Null block
			//System.out.print("Obstacle/Null block, id ");
			EngineData.hoveringEntityType = 2;
		}
		else if (rgbArray[0] == 170)
		{//Intersection block
			//System.out.print("Intersection block, id ");
			EngineData.hoveringEntityType = 3;
		}
		else if (rgbArray[0] == 255)
		{//General Entity
			//System.out.print("OGeneral Entity, id ");
			EngineData.hoveringEntityType = 4;
		}
		else
		{//Background
			//System.out.println("Background");
			EngineData.hoveringEntityType = -1;
		}
	}

	private void printCurrentPixelColor()
	{
		System.out.println("Mouse @:" + Mouse.getX() + ":" + Mouse.getY());
		while (buffer.hasRemaining())
		{
			System.out.print(((int) buffer.get() & 0xFF) + ":");
		}

		System.out.println();
	}

	public void cleanUp()
	{
		GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);

		for (int pbo : this.pbos)
		{
			GL15.glDeleteBuffers(pbo);
		}
	}
}