package Render_engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import Entities.Entity;
import Guis.Gui_MainWindow;
import Guis.Gui_VehicleStatus;
import Models.RawModel;
import Models.TexturedModel;
import Textures.ModelTexture;
import Threads.Executor;
import Threads.ThreadVehicle;
import TrafficLogic.IntersectionManager;
import TrafficLogic.Path;
import TrafficLogic.RoadBlock;
import TrafficLogic.RoadData;
import TrafficLogic.Vehicle;
import TrafficLogic.VehiclesManager;

public class EngineData
{
	private static Entity[] showreelEntities = new Entity[17];
	public static Entity directionsGizmo;

	public static final HashMap<String, Object[]> defaultSettings = new HashMap<String, Object[]>();
	public static HashMap<String, Object[]> currentSettings;

	public static HashMap<Integer, ArrayList<Entity>> tModelsEntitiesMap = new HashMap<Integer, ArrayList<Entity>>();
	private static HashMap<String, TexturedModel> tModelsNameMap = new HashMap<String, TexturedModel>();
	private static HashMap<TexturedModel, Integer> tModelsDataMap = new HashMap<TexturedModel, Integer>();

	private static HashMap<Integer, Integer> vaosMap = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> vbosMap = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> vaoToVbo = new HashMap<Integer, Integer>();
	private static HashMap<String, ModelTexture> texturesMap = new HashMap<String, ModelTexture>();

	public static AtomicInteger nOfVehiclesToLoad = new AtomicInteger();
	public static HashMap<Integer, Integer> toLoadData = new HashMap<Integer, Integer>();

	public static HashMap<Integer, Entity> entitiesIdMap = new HashMap<Integer, Entity>();

	public static Entity hoveringEntity = null;
	public static Entity selectedEntity = null;
	public static int hoveringEntityType = -1;
	public static int selectedEntityType = -1;

	//These are true when a vehicle/intM/roadBlock had been selected and its gui window is still open, false when closing its window
	public static Vehicle lastSelectedVehicle = null;
	public static IntersectionManager lastSelectedIntManager = null;
	public static RoadBlock lastSelectedRoadBlock = null;

	private static int vaoCounter = 0;
	private static int vboCounter = 0;
	private static int tModelsIndex = 0;
	public static int entityCounter = 1;//TODO why 1?

	private static final int MAX_INSTANCES = 100000; //If set too high this will cause problems, vehicles wont spawn regularly
	private static final int INSTANCE_DATA_LENGTH = 23;
	private static Vector3f tempClr;

	public static volatile boolean isRendering = true;

	public static RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices)
	{
		boolean hasUvs = false;
		boolean hasNormals = false;

		int vaoID = createVAO();

		// Bind newly created Vao
		GL30.glBindVertexArray(vaoID);

		bindIndicesVBO(indices);

		storeDataInVbo(0, 3, positions);

		if (textureCoords != null)
		{
			storeDataInVbo(1, 2, textureCoords);
			hasUvs = true;
		}

		if (normals != null)
		{
			storeDataInVbo(2, 3, normals);
			hasNormals = true;
		}

		//INSTANCE CODE
		int vboID = createEmpytyVbo(INSTANCE_DATA_LENGTH * MAX_INSTANCES);

		//Store transformation matrix
		storeInstancedDataInVbo(3, 4, INSTANCE_DATA_LENGTH, 0);
		storeInstancedDataInVbo(4, 4, INSTANCE_DATA_LENGTH, 4);
		storeInstancedDataInVbo(5, 4, INSTANCE_DATA_LENGTH, 8);
		storeInstancedDataInVbo(6, 4, INSTANCE_DATA_LENGTH, 12);

		//Store overlay color values
		storeInstancedDataInVbo(7, 3, INSTANCE_DATA_LENGTH, 16);

		//Store masking alpha value
		storeInstancedDataInVbo(8, 1, INSTANCE_DATA_LENGTH, 19);

		//Store color id value
		storeInstancedDataInVbo(9, 3, INSTANCE_DATA_LENGTH, 20);

		//Unbind Vbo
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		//Unbind VAO
		GL30.glBindVertexArray(0);

		vaoToVbo.put(vaoID, vboID);

		return new RawModel(vaoID, indices.length, hasUvs, hasNormals);
	}

	public static void updateVbo(int vao, float[] data, FloatBuffer buffer)
	{
		buffer.clear();
		buffer.put(data);
		buffer.flip();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vaoToVbo.get(vao));

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity() * 4, GL15.GL_STREAM_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);//TODO perchè un-bind il vbo corrente?
	}

	public static int createEmpytyVbo(int floatCount)
	{
		int vbo = GL15.glGenBuffers();

		vbosMap.put(vboCounter, vbo);
		vboCounter++;

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatCount * 4, GL15.GL_STREAM_DRAW);

		//Unbinding this vbo later in the loadToVao method

		return vbo;
	}

	private static void storeDataInVbo(int attributeNumber, int dataSize, float[] data)
	{
		// Create empty VBO and storing its ID
		int vboID = GL15.glGenBuffers();

		// Store it in the VBOs list
		vbosMap.put(vboCounter, vboID);
		vboCounter++;

		// Bind the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);

		// Create buffer obj
		FloatBuffer buffer = createFloatBuffer(data);

		// Put it in the VBO
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		// Put the VBO in the currentlu bound VAO
		GL20.glVertexAttribPointer(attributeNumber, dataSize, GL11.GL_FLOAT, false, 0, 0);

		// Un-Bind the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public static void storeInstancedDataInVbo(int attribute, int dataSize, int instancedDataLength, int offset)
	{
		GL20.glVertexAttribPointer(attribute, dataSize, GL11.GL_FLOAT, false, instancedDataLength * 4, offset * 4);
		GL33.glVertexAttribDivisor(attribute, 1);
	}

	public static ModelTexture loadTexture(String fileName)
	{
		Texture texture = null;

		if (!texturesMap.containsKey(fileName))
		{
			try
			{
				java.nio.file.Path temp = Files.createTempFile(fileName.split("/")[fileName.split("/").length - 1], ".png");
				Files.copy(EngineData.class.getClassLoader().getResourceAsStream("img/textures/" + fileName + ".png"), temp, StandardCopyOption.REPLACE_EXISTING);
				FileInputStream input = new FileInputStream(temp.toFile());

				texture = TextureLoader.getTexture("PNG", input);
			}
			catch (FileNotFoundException e)
			{
				System.err.println("Couldn't load image file!: " + fileName);
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			//Add texture to texturesMap
			ModelTexture tempText = new ModelTexture(texture.getTextureID());

			texturesMap.put(fileName, tempText);

			return tempText;
		}
		else
		{
			return (ModelTexture) texturesMap.get(fileName);
		}
	}

	private static int createVAO()
	{
		// Create empty VAO and storing its ID
		int vaoID = GL30.glGenVertexArrays();

		// Store it in the VAOs list
		vaosMap.put(vaoCounter, vaoID);
		vaoCounter++;

		return vaoID;
	}

	private static void bindIndicesVBO(int[] indices)
	{
		// Create empty VAO and storing its ID
		int vboID = GL15.glGenBuffers();

		// Store it in the VBOs list
		vbosMap.put(vboCounter, vboID);
		vboCounter++;

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);

		IntBuffer buffer = createIntBuffer(indices);

		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		//GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public static RawModel loadRawModel(String filename)
	{
		ArrayList<Object> objData = ObjLoader.loadObjFile(filename);

		return loadToVAO((float[]) objData.get(0), (float[]) objData.get(1), (float[]) objData.get(2), (int[]) objData.get(3));
	}

	public static boolean loadTexturedModel(String objName, String textureName)
	{
		TexturedModel tempTModel;
		ModelTexture tempTexture;

		boolean alreadyInMemory = false;
		//Load texture
		if (textureName != null)//If model has a texture to load
		{
			//If image not already in memory
			if (!texturesMap.containsKey(textureName)) //Load a new one and add it to the textures Map
			{
				tempTexture = EngineData.loadTexture(textureName);
				texturesMap.put(textureName, tempTexture);
			}
			else tempTexture = texturesMap.get(textureName);
		}
		else tempTexture = null;

		//Load TexturedModel		
		if (!tModelsNameMap.containsKey(objName + "_" + textureName))//If model not already in memory
		{
			tempTModel = new TexturedModel(loadRawModel(objName), tempTexture);

			//Update tModelsNameMap
			tModelsNameMap.put(objName + "_" + textureName, tempTModel);

			//Update tModelsDataMap
			tModelsDataMap.put(tempTModel, tModelsIndex);

			tModelsIndex++;
		}
		else alreadyInMemory = true;

		return alreadyInMemory;
	}

	public static ThreadVehicle loadVehicle(String filename, String textureName, Vector3f position, float rotX, float rotY, float rotZ, float scale, Vector3f cOverlay, float alpha, int typeOfEntity)
	{
		ThreadVehicle tempVehicle;
		TexturedModel tempTModel;

		boolean alreadyInMemory = loadTexturedModel(filename, textureName);

		tempTModel = tModelsNameMap.get(filename + "_" + textureName);
		tempVehicle = new ThreadVehicle(tempTModel, position, rotX, rotY, rotZ, scale, alpha, getColorIdFromIntegerId(typeOfEntity));

		addEntity(tempVehicle, alreadyInMemory);

		entitiesIdMap.put(entityCounter, tempVehicle);

		entityCounter++;

		return tempVehicle;
	}

	public static RoadBlock loadRoadBlock(String filename, int x, int y, String textureName, Vector3f position, float rotX, float rotY, float rotZ, float scale, Vector3f colorId, float alpha,
			String description, int typeOfEntity, int blockId)
	{
		RoadBlock tempBlock;
		TexturedModel tempTModel;
		boolean alreadyInMemory = loadTexturedModel(filename, textureName);

		tempTModel = tModelsNameMap.get(filename + "_" + textureName);
		tempBlock = new RoadBlock(tempTModel, position, rotX, rotY, rotZ, scale, alpha, getColorIdFromIntegerId(typeOfEntity), description, blockId);

		addEntity(tempBlock, alreadyInMemory);

		entitiesIdMap.put(entityCounter, tempBlock);

		entityCounter++;

		return tempBlock;
	}

	public static Entity loadEntity(String filename, String textureName, Vector3f position, float rotX, float rotY, float rotZ, float scale, Vector3f cOverlay, boolean toRender, float alpha,
			int typeOfEntity)
	{
		Entity tempEntity;
		TexturedModel tempTModel;
		boolean alreadyInMemory = loadTexturedModel(filename, textureName);

		tempTModel = tModelsNameMap.get(filename + "_" + textureName);
		tempEntity = new Entity(tempTModel, position, rotX, rotY, rotZ, scale, cOverlay, toRender, alpha, getColorIdFromIntegerId(typeOfEntity));

		addEntity(tempEntity, alreadyInMemory);

		entitiesIdMap.put(entityCounter, tempEntity);

		entityCounter++;

		return tempEntity;
	}

	public static Path loadPath(String filename, String textureName, int id, Vector3f position, float rotX, float rotY, float rotZ, float scale, Vector3f cOverlay, float alpha, int typeOfEntity)
	{
		ArrayList<Object> objData = ObjLoader.loadObjFile(filename);

		Path tempPath;
		TexturedModel tempTModel;
		boolean alreadyInMemory = loadTexturedModel(filename, textureName);

		tempTModel = tModelsNameMap.get(filename + "_" + textureName);

		tempPath = new Path(tempTModel, id, position, rotX, rotY, rotZ, scale, alpha, (float[]) objData.get(0), getColorIdFromIntegerId(typeOfEntity));

		//By default all paths are hidden
		tempPath.setToRender(false);

		addEntity(tempPath, alreadyInMemory);

		entityCounter++;

		return tempPath;
	}

	private static void addEntity(Entity e, Boolean alreadyInMemory)
	{//Adds entity to the active entities in the scene

		if (!alreadyInMemory)
		{
			//Update tModelsEntitiesMap
			tModelsEntitiesMap.put(tModelsIndex - 1, new ArrayList<Entity>(Arrays.asList(e)));
		}
		else
		{
			//GetIndex
			int index = tModelsDataMap.get(e.getModel());

			//Update Entities list for the specific tm
			tModelsEntitiesMap.get(index).add(e);
		}
	}

	private static FloatBuffer createFloatBuffer(float[] data)
	{
		// Create empty buffer
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);

		// Fill it
		buffer.put(data);

		// Making it readable
		buffer.flip();

		return buffer;
	}

	private static IntBuffer createIntBuffer(int[] data)
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);

		buffer.put(data);
		buffer.flip();

		return buffer;
	}

	public static ArrayList<ArrayList<Object>> getEntitiesToRenderList()
	{//Returns the array containing all ArrayLists of entities to render, one for each texturedModel in memory

		ArrayList<ArrayList<Object>> returnArray = new ArrayList<ArrayList<Object>>();
		ArrayList<Object> innerArray = new ArrayList<Object>();

		for (TexturedModel tm : tModelsDataMap.keySet())
		{
			innerArray.add(tm);
			innerArray.add(tModelsEntitiesMap.get(tModelsDataMap.get(tm)));

			returnArray.add(innerArray);

			//Clear the array
			innerArray = new ArrayList<Object>();
		}

		return returnArray;
	}

	public static void cleanUp()
	{
		// Delete all vaos
		for (int vaoID : vaosMap.values())
		{
			GL30.glDeleteVertexArrays(vaoID);
		}
		// Delete all vbos
		for (int vboID : vbosMap.values())
		{
			GL15.glDeleteBuffers(vboID);
		}
		// Delete all textures
		for (ModelTexture texture : texturesMap.values())
		{
			GL11.glDeleteTextures(texture.getId());
		}
	}

	public static void initShowreelScene()
	{//Loads showreel scenes but wont render them

		Gui_MainWindow.showProgressBar(true);
		Gui_MainWindow.setProgressBar("Loading 3D models...", 1);

		//RoadBlocks models
		showreelEntities[0] = EngineData.loadEntity("roadModels/roadBlocks/straight", "roadBlocks/straight", new Vector3f(-3.75f, 10f, 0f), 0f, 0f, 0f, 1f, new Vector3f(255, 0, 0), false, 0f, 2);
		showreelEntities[1] = EngineData.loadEntity("roadModels/roadBlocks/turn", "roadBlocks/turn", new Vector3f(-2.5f, 10f, 0f), 0f, 0f, 0f, 1f, new Vector3f(255, 0, 0), false, 0f, 2);
		showreelEntities[2] = EngineData.loadEntity("roadModels/roadBlocks/t_intersection", "roadBlocks/t_intersection", new Vector3f(-1.25f, 10f, 0f), 0f, 0f, 0f, 1f, new Vector3f(255, 0, 0), false,
				0f, 2);
		Gui_MainWindow.setProgressBar("Loading 3D models...", 25);
		showreelEntities[3] = EngineData.loadEntity("roadModels/roadBlocks/4_way_intersection", "roadBlocks/4_way_intersection", new Vector3f(0f, 10f, 0f), 0f, 0f, 0f, 1f, new Vector3f(255, 0, 0),
				false, 0f, 2);
		showreelEntities[4] = EngineData.loadEntity("roadModels/roadBlocks/null", "roadBlocks/null", new Vector3f(1.25f, 10f, 0f), 0f, 0f, 0f, 1f, new Vector3f(255, 0, 0), false, 0f, 2);

		//RoadObstacles models
		showreelEntities[5] = EngineData.loadEntity("roadModels/roadBlocks/building_1", "roadBlocks/building_1", new Vector3f(3.75f, 10f, -2.5f), 0f, 0f, 0f, 1f, new Vector3f(255, 0, 0), false, 0f,
				2);
		showreelEntities[6] = EngineData.loadEntity("roadModels/roadBlocks/building_2", "roadBlocks/building_2", new Vector3f(3.75f, 10f, -1.25f), 0f, 0f, 0f, 1f, new Vector3f(255, 0, 0), false, 0f,
				2);
		showreelEntities[7] = EngineData.loadEntity("roadModels/roadBlocks/building_3", "roadBlocks/building_3", new Vector3f(3.75f, 10f, 0f), 0f, 0f, 0f, 1f, new Vector3f(255, 0, 0), false, 0f, 2);
		Gui_MainWindow.setProgressBar("Loading 3D models...", 50);
		//Paths models
		showreelEntities[8] = EngineData.loadPath("roadModels/routes/straight", null, 0, new Vector3f(-3.75f, 10f, -1.1f), 0f, 0f, 0f, 1f, null, 0f, 4);
		showreelEntities[9] = EngineData.loadPath("roadModels/routes/turn_small", null, 0, new Vector3f(-2.5f, 10f, -1.1f), 0f, 0f, 0f, 1f, null, 0f, 4);
		showreelEntities[10] = EngineData.loadPath("roadModels/routes/turn_large", null, 0, new Vector3f(-1.25f, 10f, -1.1f), 0f, 0f, 0f, 1f, null, 0f, 4);

		//Vehicles models
		showreelEntities[11] = EngineData.loadVehicle("others/car", "car_1", new Vector3f(0f, 10f, -1.1f), 0, 0, 0, 1f, new Vector3f(0, 0, 0), 0, 0);
		showreelEntities[12] = EngineData.loadVehicle("others/car", "car_2", new Vector3f(0f, 10f, -1.7f), 0, 0, 0, 1f, new Vector3f(0, 0, 0), 0, 0);
		showreelEntities[13] = EngineData.loadVehicle("others/car", "car_3", new Vector3f(0f, 10f, -2.3f), 0, 0, 0, 1f, new Vector3f(0, 0, 0), 0, 0);
		Gui_MainWindow.setProgressBar("Loading 3D models...", 75);
		showreelEntities[14] = EngineData.loadVehicle("others/car", "car_4", new Vector3f(0f, 10f, -2.9f), 0, 0, 0, 1f, new Vector3f(0, 0, 0), 0, 0);
		showreelEntities[15] = EngineData.loadVehicle("others/car", "car_5", new Vector3f(0f, 10f, -3.5f), 0, 0, 0, 1f, new Vector3f(0, 0, 0), 0, 0);
		showreelEntities[16] = EngineData.loadVehicle("others/car", "cool_car", new Vector3f(1.25f, 10f, -1.1f), 0, 0, 0, 1f, new Vector3f(0, 0, 0), 0, 0);
		Gui_MainWindow.setProgressBar("Loading 3D models...", 100);
		showreelEntities[11].setToRender(false);
		showreelEntities[12].setToRender(false);
		showreelEntities[13].setToRender(false);
		showreelEntities[14].setToRender(false);
		showreelEntities[15].setToRender(false);
		showreelEntities[16].setToRender(false);

	}

	public static void renderShowreelScene(boolean toRender)
	{
		for (int i = 0; i < showreelEntities.length; i++)
		{
			showreelEntities[i].setToRender(toRender);
		}
	}

	public static void initializeSettings()
	{//Settings are {default, min, max}

		//Control panel tab 1
		defaultSettings.put("gridWidth", new Object[] { 10, 10, 100 });
		defaultSettings.put("gridHeight", new Object[] { 10, 10, 100 });
		defaultSettings.put("roadsAmount", new Object[] { 0f, 0f, 1f });
		defaultSettings.put("obstaclesAmount", new Object[] { 1f, 0f, 1f });
		defaultSettings.put("roadBlocksOffset", new Object[] { 0f, 0f, 1f });

		//Control panel tab 2
		defaultSettings.put("vehiclesPerMinute", new Object[] { 0, 0, 1000 });
		defaultSettings.put("solverDelay", new Object[] { 1, 0, 500 }); //aka Executor.threadExecutionOffset
		defaultSettings.put("cruiseVelocity", new Object[] { 0.05f, 0f, 0.3f });
		defaultSettings.put("solveMode", new Object[] { 1, 0, 2 });
		defaultSettings.put("solveIntersections", new Object[] { true, false, false });
		defaultSettings.put("transitLimit", new Object[] { 2, 1, 5 });
		defaultSettings.put("showColors", new Object[] { false, null, null });

		//Initial current settings are the default
		currentSettings = new HashMap<String, Object[]>();

		//Dont use 	currentSettings = new HashMap<String, Object[]>(defaultSettings), the Object[] array will be shared, setting reset wont work 

		//Control panel tab 1
		currentSettings.put("gridWidth", Arrays.copyOf(defaultSettings.get("gridWidth"), 3));
		currentSettings.put("gridHeight", Arrays.copyOf(defaultSettings.get("gridHeight"), 3));
		currentSettings.put("roadsAmount", Arrays.copyOf(defaultSettings.get("roadsAmount"), 3));
		currentSettings.put("obstaclesAmount", Arrays.copyOf(defaultSettings.get("obstaclesAmount"), 3));
		currentSettings.put("roadBlocksOffset", Arrays.copyOf(defaultSettings.get("roadBlocksOffset"), 3));

		//Control panel tab 2
		currentSettings.put("vehiclesPerMinute", Arrays.copyOf(defaultSettings.get("vehiclesPerMinute"), 3));
		currentSettings.put("solverDelay", Arrays.copyOf(defaultSettings.get("solverDelay"), 3)); //aka Executor.threadExecutionOffset
		currentSettings.put("cruiseVelocity", Arrays.copyOf(defaultSettings.get("cruiseVelocity"), 3));
		currentSettings.put("solveMode", Arrays.copyOf(defaultSettings.get("solveMode"), 3));
		currentSettings.put("solveIntersections", Arrays.copyOf(defaultSettings.get("solveIntersections"), 3));
		currentSettings.put("transitLimit", Arrays.copyOf(defaultSettings.get("transitLimit"), 3));
		currentSettings.put("showColors", Arrays.copyOf(defaultSettings.get("showColors"), 3));
	}

	public static void updateGeneralVariables()
	{
		//Control panel tab 1
		RoadData.gridWidth = (int) currentSettings.get("gridWidth")[0];
		RoadData.gridHeight = (int) currentSettings.get("gridHeight")[0];
		RoadData.roadsAmount = (float) currentSettings.get("roadsAmount")[0];
		RoadData.obstaclesAmount = (float) currentSettings.get("obstaclesAmount")[0];
		RoadData.roadBlocksOffset = (float) currentSettings.get("roadBlocksOffset")[0];

		//Control panel tab 2
		RoadData.vehiclesPerMinute = (int) currentSettings.get("vehiclesPerMinute")[0];
		VehiclesManager.updateSpawnIntervalValue();
		Executor.resetVehicleTimer();
		RoadData.cruiseVelocity = (float) currentSettings.get("cruiseVelocity")[0];
		RoadData.chasingVelocity = RoadData.cruiseVelocity * RoadData.chasingFactor;
		Executor.solverDelay = (int) currentSettings.get("solverDelay")[0]; //aka Executor.threadExecutionOffset
		Executor.showColors = (boolean) currentSettings.get("showColors")[0];

		//Control panel tab 3
	}

	public static void updateTrafficVariables()
	{
		RoadData.solveMode = (int) currentSettings.get("solveMode")[0];
		RoadData.transitsPerDirLimit = (int) currentSettings.get("transitLimit")[0];
	}

	public static void restoreDefaultSettings()
	{
		//RESET DATA STRUCTURES
		//Control panel tab 1
		currentSettings.put("gridWidth", defaultSettings.get("gridWidth"));
		currentSettings.put("gridHeight", defaultSettings.get("gridHeight"));
		currentSettings.put("obstaclesAmount", defaultSettings.get("obstaclesAmount"));
		currentSettings.put("roadsAmount", defaultSettings.get("roadsAmount"));
		currentSettings.put("roadBlocksOffset", defaultSettings.get("roadBlocksOffset"));

		//Control panel tab 2
		currentSettings.put("vehiclesPerMinute", defaultSettings.get("vehiclesPerMinute"));
		currentSettings.put("solverDelay", defaultSettings.get("solverDelay"));
		currentSettings.put("cruiseVelocity", defaultSettings.get("cruiseVelocity"));
		currentSettings.put("solveMode", defaultSettings.get("solveMode"));
		currentSettings.put("transitLimit", defaultSettings.get("transitLimit"));
		currentSettings.put("solveIntersections", defaultSettings.get("solveIntersections"));
		currentSettings.put("showColors", defaultSettings.get("showColors"));

		//Control panel tab 3
		currentSettings.put("windowResolution", defaultSettings.get("windowResolution"));
		currentSettings.put("antiAliasing", defaultSettings.get("antiAliasing"));
		currentSettings.put("aASamplesIndex", defaultSettings.get("aASamplesIndex"));
		currentSettings.put("dirGizmo", defaultSettings.get("dirGizmo"));

		//RESET VARIABLES
		updateGeneralVariables();
		updateTrafficVariables();
	}

	public static int getVbo(int vaoID)
	{
		return vaoToVbo.get(vaoID);
	}

	public static int getMaxInstances()
	{
		return MAX_INSTANCES;
	}

	public static int getInstanceDataLength()
	{
		return INSTANCE_DATA_LENGTH;
	}

	public static void removeDeadVehiclesFromRoad()
	{//Deletes dead vehicles

		//Delete all dead vehicle
		for (Vehicle v : RoadData.deadVehicles)
		{
			EngineData.removeEntities(v.getModel(), v);

			if (Gui_VehicleStatus.mainFrame.isVisible() && EngineData.selectedEntity == v) Gui_VehicleStatus.resetValues();

			tempClr = v.getColorId();
			EngineData.entitiesIdMap.remove((int) (tempClr.x + tempClr.y * 256 + tempClr.z * 256 * 256));
		}

		RoadData.deadVehicles.clear();
	}

	public static void removeEntities(TexturedModel tm, Entity e)
	{//Remove a single entity
		tModelsEntitiesMap.get(tModelsDataMap.get(tm)).remove(e);
	}

	public static void removeEntities(TexturedModel tm, ArrayList<Entity> entitiesList)
	{//Remove all the given entities which belong to the given texturedModel
		tModelsEntitiesMap.get(tModelsDataMap.get(tm)).removeAll(entitiesList);
	}

	public static void resetSelections()
	{//Deselects everything

		EngineData.lastSelectedVehicle = null;
		Gui_MainWindow.vehicleStatus.setVisible(false);
		Gui_VehicleStatus.resetValues();

		EngineData.lastSelectedRoadBlock = null;
		Gui_MainWindow.roadBlockStatus.setVisible(false);
		Gui_MainWindow.roadBlockStatus.resetValues();

		EngineData.lastSelectedIntManager = null;
		Gui_MainWindow.intersectionStatus.setVisible(false);
		Gui_MainWindow.intersectionStatus.resetValues();

		if (EngineData.selectedEntity != null)
		{

			EngineData.selectedEntityType = -1;
			EngineData.selectedEntity.setAlpha(0f);
			EngineData.selectedEntity = null;
		}
	}

	public static void getInfo()
	{
		System.out.println(tModelsEntitiesMap);
		System.out.println(tModelsDataMap);
		System.out.println(texturesMap);
		System.out.println("...........");

		for (ArrayList<Entity> list : tModelsEntitiesMap.values())
		{
			System.out.println(list.size());
		}

		System.out.println("========================");
	}

	private static Vector3f getColorIdFromIntegerId(int typeOfEntity)
	{
		if (typeOfEntity == 0)
		{//Vehicle
			return new Vector3f(0, (entityCounter & 0x000000FF) >> 0, (entityCounter & 0x0000FF00) >> 8);
		}
		else if (typeOfEntity == 1)
		{//Roadblock
			return new Vector3f(50, (entityCounter & 0x000000FF) >> 0, (entityCounter & 0x0000FF00) >> 8);
		}
		else if (typeOfEntity == 2)
		{//Obstacle/Null roadblock
			return new Vector3f(85, (entityCounter & 0x000000FF) >> 0, (entityCounter & 0x0000FF00) >> 8);
		}
		else if (typeOfEntity == 3)
		{//Intersection manager
			return new Vector3f(170, (entityCounter & 0x000000FF) >> 0, (entityCounter & 0x0000FF00) >> 8);
		}
		else//typeOfEntity == 4
		{//General entity
			return new Vector3f(255, (entityCounter & 0x000000FF) >> 0, (entityCounter & 0x0000FF00) >> 8);
		}
	}
}