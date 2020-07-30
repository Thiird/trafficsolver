package Utility_objects;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import Render_engine.EngineData;

public class XmlLoader
{
	private static Document loadXml(String filename)
	{
		try
		{
			java.nio.file.Path temp = Files.createTempFile(filename.split("/")[filename.split("/").length - 1], ".png");
			Files.copy(EngineData.class.getClassLoader().getResourceAsStream("xml/" + filename + ".xml"), temp, StandardCopyOption.REPLACE_EXISTING);

			File file = temp.toFile();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();

			return doc;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return null;
		}
	}

	public static HashMap<String, String[]> loadBitMasks()
	{
		HashMap<String, String[]> bitMasks = new HashMap<String, String[]>();

		Document doc = loadXml("bitMasks");
		NodeList nList = doc.getElementsByTagName("bitMasks");

		Node node;
		String[] value = new String[2];

		for (int nodeIndex = 0; nodeIndex < nList.getLength(); nodeIndex++)
		{
			node = nList.item(nodeIndex);

			//System.out.println("Current Element : " + node.getNodeName());

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element root = (Element) node;

				for (int i = 0; i < root.getElementsByTagName("block").getLength(); i++)
				{
					value[0] = root.getElementsByTagName("block").item(i).getAttributes().getNamedItem("block").getNodeValue();
					value[1] = root.getElementsByTagName("block").item(i).getAttributes().getNamedItem("rotation").getNodeValue();
					bitMasks.put(root.getElementsByTagName("block").item(i).getAttributes().getNamedItem("index").getNodeValue(), new String[] { value[0], value[1] });
				}
			}
		}

		return bitMasks;
	}

	public static HashMap<String, String> loadBitMaskinToIndex()
	{
		HashMap<String, String> bitMasksToIndex = new HashMap<String, String>();

		Document doc = loadXml("bitMasks");
		NodeList nList = doc.getElementsByTagName("bitMasks");

		Node node;
		String value;

		for (int nodeIndex = 0; nodeIndex < nList.getLength(); nodeIndex++)
		{
			node = nList.item(nodeIndex);

			//System.out.println("Current Element : " + node.getNodeName());

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element root = (Element) node;

				for (int i = 0; i < root.getElementsByTagName("block").getLength(); i++)
				{
					value = root.getElementsByTagName("block").item(i).getAttributes().getNamedItem("index").getNodeValue();
					bitMasksToIndex.put(root.getElementsByTagName("block").item(i).getAttributes().getNamedItem("proximity").getNodeValue(), value);
				}
			}
		}

		return bitMasksToIndex;
	}

	public static HashMap<String, ArrayList<String>> loadBlocksData()
	{
		HashMap<String, ArrayList<String>> blocksData = new HashMap<String, ArrayList<String>>();

		Document doc = loadXml("roadBlocksMapping");
		NodeList nList = doc.getElementsByTagName("roadBlocksMapping");

		Node node;
		NodeList blocksList;
		NodeList tempRoutesList;
		Node roadBlock;
		ArrayList<String> value = new ArrayList<String>();

		for (int nodeIndex = 0; nodeIndex < nList.getLength(); nodeIndex++)
		{
			node = nList.item(nodeIndex);

			//System.out.println("Current Element : " + node.getNodeName());

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element root = (Element) node;
				blocksList = root.getElementsByTagName("roadBlock");

				for (int i = 0; i < blocksList.getLength(); i++)
				{
					roadBlock = blocksList.item(i);

					value.add(roadBlock.getAttributes().getNamedItem("name").getTextContent());
					value.add(roadBlock.getAttributes().getNamedItem("description").getTextContent());

					tempRoutesList = roadBlock.getChildNodes();

					for (int j = 0; j < tempRoutesList.getLength(); j++)
					{
						if (!tempRoutesList.item(j).getTextContent().replaceAll("[\t\n]", "").equals("")) //Don't know why but some are empty
						{
							value.add(tempRoutesList.item(j).getTextContent().replaceAll("[\t\n]", ""));
						}
					}
					blocksData.put(root.getElementsByTagName("roadBlock").item(i).getAttributes().getNamedItem("index").getNodeValue(), (ArrayList<String>) value.clone());

					value.clear();
				}
			}
		}

		return blocksData;
	}

	public static HashMap<String, String> LoadDirectionsToPathsIndices()
	{
		HashMap<String, String> directionsToPathsIndices = new HashMap<String, String>();

		Document doc = loadXml("directionsToPathsIndices");
		NodeList nList = doc.getElementsByTagName("directionsToPathsIndices");

		Node node;
		NodeList blocksList;
		Node roadBlock;
		String[] value = new String[2];

		for (int nodeIndex = 0; nodeIndex < nList.getLength(); nodeIndex++)
		{
			node = nList.item(nodeIndex);

			//System.out.println("Current Element : " + node.getNodeName());

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element root = (Element) node;
				blocksList = root.getElementsByTagName("path");

				//For each path node in the file
				for (int i = 0; i < blocksList.getLength(); i++)
				{
					roadBlock = blocksList.item(i);

					value[0] = roadBlock.getAttributes().getNamedItem("directionValues").getTextContent();
					value[1] = roadBlock.getAttributes().getNamedItem("pathId").getTextContent();

					directionsToPathsIndices.put(value[0], value[1]);

					//Cleaning array
					value[0] = "";
					value[1] = "";
				}
			}
		}

		return directionsToPathsIndices;
	}

	public static HashMap<String, String[]> LoadPathIndexToPathInfo()
	{
		HashMap<String, String[]> pathIndexToPathInfo = new HashMap<String, String[]>();

		Document doc = loadXml("pathIndexToPathInfo");
		NodeList nList = doc.getElementsByTagName("pathIndexToPathInfo");

		Node node;
		NodeList blocksList;
		Node roadBlock;
		String[] value = new String[3];

		for (int nodeIndex = 0; nodeIndex < nList.getLength(); nodeIndex++)
		{
			node = nList.item(nodeIndex);

			//System.out.println("Current Element : " + node.getNodeName());

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element root = (Element) node;
				blocksList = root.getElementsByTagName("pathToInfo");

				//For each path node in the file
				for (int i = 0; i < blocksList.getLength(); i++)
				{
					roadBlock = blocksList.item(i);

					value[0] = roadBlock.getAttributes().getNamedItem("id").getTextContent();
					value[1] = roadBlock.getAttributes().getNamedItem("filePath").getTextContent();
					value[2] = roadBlock.getAttributes().getNamedItem("totLenght").getTextContent();

					pathIndexToPathInfo.put(value[0], new String[] { value[1], value[2] });
				}
			}
		}

		return pathIndexToPathInfo;
	}

	public static HashMap<String, String[]> LoadPathsConfigurations()
	{
		HashMap<String, String[]> pathsConfigs = new HashMap<String, String[]>();

		Document doc = loadXml("pathsConfigs");
		NodeList nList = doc.getElementsByTagName("pathsConfigs");

		Node node;
		NodeList blocksList;
		Node roadBlock;
		String key = "";
		String[] value = new String[2];

		for (int nodeIndex = 0; nodeIndex < nList.getLength(); nodeIndex++)
		{
			node = nList.item(nodeIndex);

			//System.out.println("Current Element : " + node.getNodeName());

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element root = (Element) node;
				blocksList = root.getElementsByTagName("configuration");

				//For each path node in the file
				for (int i = 0; i < blocksList.getLength(); i++)
				{
					roadBlock = blocksList.item(i);

					key = roadBlock.getAttributes().getNamedItem("idConfig").getTextContent();

					value[0] = roadBlock.getAttributes().getNamedItem("pathIndex").getTextContent();
					value[1] = roadBlock.getAttributes().getNamedItem("rotation").getTextContent();

					pathsConfigs.put(key, value.clone());
				}
			}
		}

		return pathsConfigs;
	}

	public static HashMap<String, float[]> LoadCollisionData()
	{
		HashMap<String, float[]> collisionData = new HashMap<String, float[]>();

		Document doc = loadXml("collisionData");
		NodeList nList = doc.getElementsByTagName("collisionData");

		Node node;
		NodeList collisionDataList;
		Node collisionInstance;
		String key = "";
		float[] value = new float[2];

		for (int nodeIndex = 0; nodeIndex < nList.getLength(); nodeIndex++)
		{
			node = nList.item(nodeIndex);

			//System.out.println("Current Element : " + node.getNodeName());

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element root = (Element) node;
				collisionDataList = root.getElementsByTagName("collision");

				//For each collision data node in the file
				for (int i = 0; i < collisionDataList.getLength(); i++)
				{
					collisionInstance = collisionDataList.item(i);

					key = collisionInstance.getAttributes().getNamedItem("idCollision").getTextContent();

					value[0] = Float.parseFloat(collisionInstance.getAttributes().getNamedItem("fPathLen").getTextContent());
					value[1] = Float.parseFloat(collisionInstance.getAttributes().getNamedItem("sPathLen").getTextContent());

					collisionData.put(key, value.clone());
				}
			}
		}

		return collisionData;
	}
}