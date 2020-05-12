package Display;

public class Clock
{
	private double lastFrameTime;
	private double delta;

	public Clock()
	{
		this.lastFrameTime = this.getCurrentTime();
	}

	public void update()
	{
		double currentFrameTime = this.getCurrentTime();
		this.delta = (currentFrameTime - this.lastFrameTime) / 1000f;
		this.lastFrameTime = currentFrameTime;
	}

	public double getFrameTimeSeconds()
	{
		return this.delta;
	}

	public long getCurrentTime()
	{
		return System.currentTimeMillis();
	}
}