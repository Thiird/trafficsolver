package Render_engine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class ObjLoader
{
	//Load obj model methods
	public static ArrayList<Object> loadObjFile(String filename)
	{
		ArrayList<Object> objData = new ArrayList<Object>();

		InputStream inputStream = EngineData.class.getClassLoader().getResourceAsStream("3dModels/tris/" + filename + ".obj");
		InputStreamReader streamReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(streamReader);

		String record;

		// Data to store in VAO
		float[] verticesArray = null;
		float[] uvsArray = null;
		float[] normalsArray = null;
		int[] indicesArray = null;

		// Data from file
		List<Vector3f> vertices = new ArrayList<Vector3f>();
		List<Vector2f> textures = new ArrayList<Vector2f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		List<Integer> indices = new ArrayList<Integer>();

		try
		{
			record = reader.readLine();

			while (record != null)
			{
				String[] currentLine = record.split(" ");
				Float[] solvedLine = new Float[3];

				if (record.startsWith("v "))
				{
					solvedLine = processLine(currentLine);
					Vector3f vertex = new Vector3f(solvedLine[0], solvedLine[1], solvedLine[2]);

					vertices.add(vertex);
				}
				else if (record.startsWith("vt "))
				{
					solvedLine = processLine(currentLine);
					Vector2f texture = new Vector2f(solvedLine[0], solvedLine[1]);

					textures.add(texture);
				}
				else if (record.startsWith("vn "))
				{
					solvedLine = processLine(currentLine);
					Vector3f normal = new Vector3f(solvedLine[0], solvedLine[1], solvedLine[2]);

					normals.add(normal);
				}
				else if (record.startsWith("f ") | record.startsWith("l "))// If true reached "Polygonal face element" records
				{
					//Dont init uvsArray and normalArray here, might be null (file might not contain them)

					break;
				}

				Arrays.fill(solvedLine, null);

				record = reader.readLine();
			}

			// Manage "Polygonal face element" or "Line elements" records
			while (record != null)
			{
				// Always check, there might be a group name line
				// Example:
				// f 109/940 108/941 206/942
				// g Blonde_Elexis_nude_Blonde_Elexis_nude_face
				// f 210/943 211/944 225/945
				if (record.startsWith("f ") | record.startsWith("l "))
				{
					String[] splitRecord = record.split(" ");

					if (splitRecord[1].contains("//")) // f v1//vn1 v2//vn2
					{
						//System.out.println("---CASO 1");

						//Init on first detection of normals
						if (normalsArray == null)
						{
							normalsArray = new float[vertices.size() * 3];
						}

						for (int i = 1; i < splitRecord.length; i++)
						{
							processVertex(splitRecord[i].split("//"), indices, textures, normals, uvsArray, normalsArray, false, true, false, 1);
						}
					}
					else if (splitRecord[1].contains("/"))// Two cases
					{

						if ((splitRecord[1].split("/")).length == 2) // Case one: f v1/vt1 v2/vt2 v3/vt3
						{
							//System.out.println("---CASO 2");

							//Init on first detection of uvs coords
							if (uvsArray == null)
							{
								uvsArray = new float[vertices.size() * 2];
							}

							for (int i = 1; i < splitRecord.length; i++)
							{
								processVertex(splitRecord[i].split("/"), indices, textures, normals, uvsArray, normalsArray, true, false, false, 0);
							}
						}
						else // Case two: f v1/vt1/vn1 v2/vt2/vn2
						{
							//System.out.println("---CASO 3");

							//Init on first detection of normals
							if (normalsArray == null)
							{
								normalsArray = new float[vertices.size() * 3];
							}

							//Init on first detection of uvs coords
							if (uvsArray == null)
							{
								uvsArray = new float[vertices.size() * 2];
							}

							for (int i = 1; i < splitRecord.length; i++)
							{
								processVertex(splitRecord[i].split("/"), indices, textures, normals, uvsArray, normalsArray, true, true, false, 3);
							}
						}
					}
					else
					{//Records like "f v1 v2 v3" and "l v1 v2 v3" go here

						ArrayList<String> tmpArray = new ArrayList<String>();

						for (int i = 1; i < splitRecord.length; i++)
						{
							tmpArray.add(splitRecord[i]);
						}

						String[] stockArr = new String[tmpArray.size()];
						stockArr = (String[]) tmpArray.toArray(stockArr);

						processVertex(stockArr, indices, textures, normals, uvsArray, normalsArray, false, false, true, 0);
					}
				}

				record = reader.readLine();
			}

			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		verticesArray = new float[vertices.size() * 3];

		indicesArray = new int[indices.size()];

		int vertexPointer = 0;

		for (Vector3f vertex : vertices)
		{
			verticesArray[vertexPointer++] = vertex.x;
			verticesArray[vertexPointer++] = vertex.y;
			verticesArray[vertexPointer++] = vertex.z;
		}

		for (int i = 0; i < indices.size(); i++)
		{
			indicesArray[i] = indices.get(i);
		}

		/*
		System.out.println("Lunghezza array dei vertici: " + verticesArray.length);
		System.out.println("Lunghezza array dei UV: " + uvsArray.length);
		System.out.println("Lunghezza array delle normali: " + normalsArray.length);
		System.out.println("Lunghezza array degli indici: " + indicesArray.length);
		System.out.println("Array dei vertici: " + Arrays.toString(verticesArray));
		System.out.println("Array dei UV: " + Arrays.toString(uvsArray));
		System.out.println("Array delle normali: " + Arrays.toString(normalsArray));
		System.out.println("Array degli indici: " + Arrays.toString(indicesArray));
		System.out.println("****************");
		 */

		objData.add(verticesArray);
		objData.add(uvsArray);
		objData.add(normalsArray);
		objData.add(indicesArray);

		return objData;
	}

	private static void processVertex(String[] vertexData, List<Integer> indices, List<Vector2f> textures, List<Vector3f> normals, float[] uvsArray, float[] normalsArray, boolean hasUVs,
			boolean hasNormals, boolean justIndices, int _case)
	{
		int currentVertexPointer;

		if (justIndices)
		{
			//System.out.println("JUST INDICES");

			//uvsArray[0] = 1;
			//uvsArray[1] = 1;

			//normalsArray[0] = 0f;
			//normalsArray[1] = 0f;
			//normalsArray[2] = 0f;

			for (int i = 0; i < vertexData.length; i++)
			{
				currentVertexPointer = Integer.parseInt(vertexData[i]) - 1;

				indices.add(currentVertexPointer);
			}
		}
		else
		{
			currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
			indices.add(currentVertexPointer);

			if (hasUVs)
			{
				Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1]) - 1);
				uvsArray[currentVertexPointer * 2] = currentTex.x;
				uvsArray[currentVertexPointer * 2 + 1] = 1 - currentTex.y;
			}
			else// Create default normal of (1,1) for each vertex
			{
				//uvsArray[currentVertexPointer * 2] = 1;
				//uvsArray[currentVertexPointer * 2 + 1] = 1;
			}

			if (hasNormals)
			{
				if (_case == 1)
				{
					Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[1]) - 1);
					normalsArray[currentVertexPointer * 3] = currentNorm.x;
					normalsArray[currentVertexPointer * 3 + 1] = currentNorm.y;
					normalsArray[currentVertexPointer * 3 + 2] = currentNorm.z;
				}
				else if (_case == 3)
				{
					Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
					normalsArray[currentVertexPointer * 3] = currentNorm.x;
					normalsArray[currentVertexPointer * 3 + 1] = currentNorm.y;
					normalsArray[currentVertexPointer * 3 + 2] = currentNorm.z;
				}
			}
			else// Create default normal of (0,0,0) for each vertex
			{
				//normalsArray[currentVertexPointer * 3] = 0f;
				//normalsArray[currentVertexPointer * 3 + 1] = 0f;
				//normalsArray[currentVertexPointer * 3 + 2] = 0f;
			}
		}
	}

	private static Float[] processLine(String[] currentLine)
	{
		Float[] solvedLine = new Float[3];
		float tempValue;

		for (int i = 0; i < currentLine.length; i++)
		{
			try
			{
				tempValue = Float.parseFloat(currentLine[i]);

				// Add it to last free position
				for (int j = 0; j < solvedLine.length; j++)
				{
					if (solvedLine[j] == null)
					{
						solvedLine[j] = tempValue;
						break;
					}
				}
			}
			catch (NumberFormatException ex)
			{
				// System.out.println("Not a number");
			}
		}
		return solvedLine;
	}
}