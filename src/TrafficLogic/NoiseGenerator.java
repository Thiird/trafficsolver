package TrafficLogic;

import java.util.ArrayList;
import java.util.Random;

public class NoiseGenerator
{
	public static void generateNoise(float noiseAmount, boolean spacing)
	{
		//System.out.println("START GEN NOISE");

		int[][] bitMap = RoadData.bitMap;

		int type = 1;

		Random r = new Random();
		ArrayList<String> checked = new ArrayList<String>();

		int innerHeight = RoadData.gridHeight - 4 - 1;
		int innerWidth = RoadData.gridWidth - 4 - 1;

		//At most 1/4 of total (inner)road grid can be obstacles
		int obstaclesToCreate = (int) (((innerWidth * innerHeight) / 4) * noiseAmount);

		int obstaclesCreated = 0;
		int nOfSpacingChecks = 0;

		int x = 0;
		int y = 0;

		int[] values = new int[] { 1, -1 };

		//System.out.println("Range coordinate: " + innerWidth + " : " + innerHeight);

		//Until I didnt created enough obstacles
		while (obstaclesCreated < obstaclesToCreate)
		{
			checked.clear();

			//Until I didnt try enough time to find a perfect fit for the obstacle
			while (true)
			{
				if (nOfSpacingChecks == ((innerWidth * innerHeight) - obstaclesCreated))
				{
					if (type == 1)
					{
						type = 0;
					}
					else if (type == 0)
					{
						spacing = false;
					}
				}

				//Generate random coords (inner grid space)
				//r.nextInt((max - min) + 1) + min;
				x = r.nextInt(((innerWidth) - 0) + 1) + 0;
				y = r.nextInt(((innerHeight) - 0) + 1) + 0;

				//Selected block must not be already "used"
				if (bitMap[y][x] != -1)
				{
					//Noise type 0
					if (type == 0)
					{
						if (spacing)
						{
							//Check if position has not already been checked
							if (!checked.contains(Integer.toString(x) + Integer.toString(y)))
							{
								//Check if theres enough space
								if (canPlace(bitMap, innerWidth, innerHeight, x, y, 1, 0))
								{
									bitMap[y + 2][x + 2] = -1;

									checked.add(Integer.toString(x) + Integer.toString(y));
									obstaclesCreated++;
									break;
								}
								else
								{
									//System.out.println("Checked with " + 1 + " padding and not placed");

									//Position has been checked
									checked.add(Integer.toString(x) + Integer.toString(y));

									nOfSpacingChecks++;
								}
							}
							else
							{
								nOfSpacingChecks++;
							}
						}
						else
						{
							//System.out.println("Not checked with " + 1 + " padding and placed " + x + ", " + y);
							bitMap[y + 2][x + 2] = -1;

							obstaclesCreated++;
							break;
						}
					}
					else if (type == 1)
					{//TODO to fix empty spaces
						//If I can still lay a 2x2 obstacle
						if (obstaclesCreated + 4 <= obstaclesToCreate)
						{
							if (spacing)
							{
								//Check if position has not already been checked
								if (!checked.contains(Integer.toString(x) + Integer.toString(y)))
								{
									if (canPlace(bitMap, innerWidth, innerHeight, x, y, 2, 1))
									{
										//System.out.println("Checked with " + 2 + " padding and placed");

										layObstacle(bitMap, innerWidth, innerHeight, x, y, 2, getUpDownValues(innerWidth, innerHeight, x, y, 2, values), checked);
										obstaclesCreated += 4;
										break;
									}
									else
									{
										//System.out.println("Checked with " + 2 + " padding and not placed");

										nOfSpacingChecks++;
									}
								}
								else nOfSpacingChecks++;

							}
							else
							{
								layObstacle(bitMap, innerWidth, innerHeight, x, y, 2, genRandomUpDownValues(r, values), checked);

								break;
							}
						}
						//Change type to 0 cant place 2*2 blocks anymore
						else
						{
							type = 0;
							break;
						}
					}
				}
				else
				{
					checked.add(Integer.toString(x) + Integer.toString(y));
				}
			}

			nOfSpacingChecks = 0;
		}
	}

	private static int[] genRandomUpDownValues(Random r, int[] values)
	{
		int[] upDownValues = new int[2];
		upDownValues[0] = values[r.nextInt((1 - 0) + 1) + 0];
		upDownValues[1] = values[r.nextInt((1 - 0) + 1) + 0];

		return upDownValues;
	}

	private static void layObstacle(int[][] bitMap, int gridWidth, int gridHeight, int x, int y, int padding, int[] upDownValues, ArrayList<String> checked)
	{//In x/y values are of the inner grid

		int upDownX = upDownValues[0];
		int upDownY = upDownValues[1];

		x += 2;
		y += 2;

		//Block is on the first column
		if (x % gridWidth == 0)
		{
			//System.out.println("First column");
			//First column block
			if (y == padding)
			{
				//System.out.println("First block");
				bitMap[y][x] = -1;
				bitMap[y + upDownY][x] = -1;
				bitMap[y + upDownY][x + upDownX] = -1;
				bitMap[y][x + upDownX] = -1;

				checked.add(Integer.toString(x) + Integer.toString(y));
				checked.add(Integer.toString(x) + Integer.toString(y + upDownY));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y + upDownY));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
			}
			//Last column block
			else if (y == gridHeight - padding)
			{
				//System.out.println("Last block");
				bitMap[y][x] = -1;
				bitMap[y][x + upDownX] = -1;
				bitMap[y + upDownY][x + upDownX] = -1;
				bitMap[y + upDownY][x] = -1;

				checked.add(Integer.toString(x) + Integer.toString(y));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y - upDownY));
				checked.add(Integer.toString(x) + Integer.toString(y - upDownY));
			}
			//Middle column block
			else
			{
				//System.out.println("Middle column");
				bitMap[y][x] = -1;
				bitMap[y][x + upDownX] = -1;
				bitMap[y + upDownY][x] = -1;
				bitMap[y + upDownY][x + upDownX] = -1;

				checked.add(Integer.toString(x) + Integer.toString(y));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
				checked.add(Integer.toString(x) + Integer.toString(y + upDownY));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y + upDownY));
			}
		}
		//Block is on the last column
		else if ((x + padding) % (gridWidth) == 0)
		{
			//System.out.println("Last column");
			//First column block
			if (y == padding)
			{
				//System.out.println("First block");
				bitMap[y][x] = -1;
				bitMap[y + upDownY][x] = -1;
				bitMap[y + upDownY][x + upDownX] = -1;
				bitMap[y][x + upDownX] = -1;

				checked.add(Integer.toString(x) + Integer.toString(y));
				checked.add(Integer.toString(x) + Integer.toString(y + upDownY));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y + upDownY));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
			}
			//Last column block
			else if (y == gridHeight - padding)
			{
				//System.out.println("Last block");
				bitMap[y][x] = -1;
				bitMap[y][x + upDownX] = -1;
				bitMap[y + upDownY][x + upDownX] = -1;
				bitMap[y + upDownY][x] = -1;

				checked.add(Integer.toString(x) + Integer.toString(y));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y + upDownY));
				checked.add(Integer.toString(x) + Integer.toString(y + upDownY));
			}
			//Middle column block
			else
			{
				//System.out.println("Middle column");
				bitMap[y][x] = -1;
				bitMap[y + upDownY][x] = -1;
				bitMap[y + upDownY][x + upDownX] = -1;
				bitMap[y][x + upDownX] = -1;

				checked.add(Integer.toString(x) + Integer.toString(y));
				checked.add(Integer.toString(x) + Integer.toString(y + upDownY));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y + upDownY));
				checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
			}
		}
		//Block is on the first row (corners excluded)
		else if (y == padding && x != padding && x != gridWidth - padding)
		{
			//System.out.println("First row");
			bitMap[y][x] = -1;
			bitMap[y + upDownY][x] = -1;
			bitMap[y + upDownY][x + upDownX] = -1;
			bitMap[y][x + upDownX] = -1;

			checked.add(Integer.toString(x) + Integer.toString(y));
			checked.add(Integer.toString(x) + Integer.toString(y + upDownY));
			checked.add(Integer.toString(x + upDownX) + Integer.toString(y + upDownY));
			checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
		}
		//Block is on the last row (corners excluded)
		else if (y == (gridHeight - padding))
		{
			//System.out.println("Last row");
			bitMap[y][x] = -1;
			bitMap[y][x + upDownX] = -1;
			bitMap[y + upDownY][x + upDownX] = -1;
			bitMap[y + upDownY][x] = -1;

			checked.add(Integer.toString(x) + Integer.toString(y));
			checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
			checked.add(Integer.toString(x + upDownX) + Integer.toString(y + upDownY));
			checked.add(Integer.toString(x) + Integer.toString(y + upDownY));
		}
		//Block is not the edges of the grid
		else
		{
			//System.out.println("Center grid");
			bitMap[y][x] = -1;
			bitMap[y][x + upDownX] = -1;
			bitMap[y + upDownY][x] = -1;
			bitMap[y + upDownY][x + upDownX] = -1;

			checked.add(Integer.toString(x) + Integer.toString(y));
			checked.add(Integer.toString(x + upDownX) + Integer.toString(y));
			checked.add(Integer.toString(x) + Integer.toString(y + upDownY));
			checked.add(Integer.toString(x + upDownX) + Integer.toString(y + upDownY));
		}
	}

	private static int[] getUpDownValues(int gridWidth, int gridHeight, int x, int y, int padding, int[] values)
	{
		//Store x-y upDown values
		int[] upDownValues = new int[2];

		Random r = new Random();

		if (x - padding < 0)
		{
			//System.out.println("x - padding < 0");
			upDownValues[0] = +1;
		}

		if (x + padding > gridWidth - 1)
		{
			//System.out.println("x + padding > gridWidth - 1");
			upDownValues[0] = -1;
		}

		if (y - padding < 0)
		{
			//System.out.println("y - padding < 0");
			upDownValues[1] = +1;
		}

		if (y + padding > gridHeight - 1)
		{
			//System.out.println("y + padding > gridHeight - 1");
			upDownValues[1] = -1;
		}

		//Set remaining values
		if (upDownValues[0] == 0)
		{
			upDownValues[0] = values[r.nextInt((1 - 0) + 1) + 0];
		}
		if (upDownValues[1] == 0)
		{
			upDownValues[1] = values[r.nextInt((1 - 0) + 1) + 0];
		}

		return upDownValues;
	}

	private static boolean canPlace(int[][] bitMap, int gridWidth, int gridHeight, int x, int y, int padding, int noiseType)
	{ //Checks if blocks around the current position are free

		x += 2;
		y += 2;

		//System.out.println("PADDING: " + padding);

		boolean canBePlaced = true;
		if ((x - padding >= 0) && (y - padding >= 0) && (x + padding < gridWidth) && (y + padding < gridHeight))
		{
			if (noiseType == 0)
			{

				if (bitMap[y][x - padding] != 0 || bitMap[y + padding][x - padding] != 0 || bitMap[y + padding][x] != 0 || bitMap[y + padding][x + padding] != 0 || bitMap[y][x + padding] != 0
						|| bitMap[y - padding][x + padding] != 0 || bitMap[y - padding][x] != 0 || bitMap[y - padding][x - padding] != 0)
				{
					canBePlaced = false;
				}
			}

			else if (noiseType == 1)
			{
				//Check if coords dont go outOfIndex
				//First column
				if ((x - padding < 0))
				{
					//System.out.println("First column");
					//First block
					if (y == 1)
					{
						//System.out.println("First block");
						if (bitMap[y + padding][x] != 0 || bitMap[y + padding][x + padding] != 0 || bitMap[y][x + padding] != 0)
						{
							//System.out.println("QUA");
							canBePlaced = false;
						}
					}
					//Last block
					else if (y == gridHeight - padding)
					{
						//System.out.println("Last block");
						if (bitMap[y - padding][x] != 0 || bitMap[y][x + padding] != 0 || bitMap[y - padding][x + padding] != 0)
						{
							//System.out.println("QUA");
							canBePlaced = false;
						}
					}
					//Middle column
					else
					{
						//System.out.println("Middle column");
						if (bitMap[y + padding][x] != 0 || bitMap[y + padding][x + padding] != 0 || bitMap[y][x + padding] != 0 || bitMap[y - padding][x + padding] != 0 || bitMap[y - padding][x] != 0)
						{
							//System.out.println("QUA");
							canBePlaced = false;
						}
					}
				}
				//Last column
				else if (x + padding > gridWidth - padding)
				{
					//System.out.println("Last column");
					//First block
					if (y == 1)
					{
						//System.out.println("First block");
						if (bitMap[y][x - padding] != 0 || bitMap[y + padding][x - padding] != 0 || bitMap[y + padding][x] != 0)
						{
							//System.out.println("QUA");
							canBePlaced = false;
						}
					}
					//Last block
					else if (y == gridHeight - padding)
					{
						//System.out.println("Last block");
						if (bitMap[y - padding][x] != 0 || bitMap[y - padding][x - padding] != 0 || bitMap[y][x - padding] != 0)
						{
							//System.out.println("QUA");
							canBePlaced = false;
						}
					}
					//Middle block
					else
					{
						if ((y + padding > gridHeight))
						{
							//System.out.println("Middle block");
							if (bitMap[y + padding][x] != 0 || bitMap[y - padding][x] != 0 || bitMap[y - padding][x - padding] != 0 || bitMap[y][x - padding] != 0
									|| bitMap[y + padding][x - padding] != 0)
							{
								//System.out.println("QUA");
								canBePlaced = false;
							}
						}
					}
				}
				//First Row
				else if (y - padding < 0)
				{
					//System.out.println("First row");
					if (bitMap[y][x - padding] != 0 || bitMap[y + padding][x - padding] != 0 || bitMap[y + padding][x] != 0 || bitMap[y + padding][x + padding] != 0 || bitMap[y][x + padding] != 0)
					{
						//System.out.println("QUA");
						canBePlaced = false;
					}
				}
				//Last row
				else if (y + padding > gridHeight - padding)
				{
					//System.out.println("Last row");
					if (bitMap[y][x - padding] != 0 || bitMap[y][x + padding] != 0 || bitMap[y - padding][x + padding] != 0 || bitMap[y - padding][x] != 0 || bitMap[y - padding][x - padding] != 0)
					{
						//System.out.println("QUA");
						canBePlaced = false;
					}
				}

				//Middle grid
				else
				{
					//System.out.println("Middle grid");
					//Check all around
					if (bitMap[y][x - padding] != 0 || bitMap[y + padding][x - padding] != 0 || bitMap[y + padding][x] != 0 || bitMap[y + padding][x + padding] != 0 || bitMap[y][x + padding] != 0
							|| bitMap[y - padding][x + padding] != 0 || bitMap[y - padding][x] != 0 || bitMap[y - padding][x - padding] != 0)
					{
						//System.out.println("QUA");
						canBePlaced = false;
					}
				}
			}
		}

		return canBePlaced;
	}
}