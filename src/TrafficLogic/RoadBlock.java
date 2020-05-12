package TrafficLogic;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.lwjgl.util.vector.Vector3f;

import Entities.Entity;
import Guis.Gui_RoadBlockStatus;
import Models.TexturedModel;
import Render_engine.EngineData;

public class RoadBlock extends Entity
{
	private String description;
	private int id;
	private int totNOfVehicles = 0;
	private String trafficSummary = "";

	public RoadBlock(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, float alpha, Vector3f colorId, String description, int id)
	{
		super(model, position, rotX, rotY, rotZ, scale, null, true, alpha, colorId);
		this.description = description;
		this.id = id;
	}

	public void updateValuesInGUI()
	{
		if (EngineData.selectedEntity == this || EngineData.lastSelectedRoadBlock == this)
		{
			this.loadBlockInformations();
			Gui_RoadBlockStatus.roadBlockName.setText(this.toString().split("\\.")[1] + " with ID: " + this.id);
			Gui_RoadBlockStatus.roadBlockPosition.setText(Arrays.toString(RoadData.getBlockCoordsFromId(this.id)));
			Gui_RoadBlockStatus.totNOfVehicles.setText(Integer.toString(totNOfVehicles));
			Gui_RoadBlockStatus.trafficSummary.setText(trafficSummary);
		}
	}

	public int getId()
	{//To del, used for debugging traffic
		return this.id;
	}

	public void loadBlockInformations()
	{//computes totNOfVehicles and loads traffic summary message
		ConcurrentHashMap<String, ConcurrentLinkedDeque<Vehicle>> temp = RoadData.blockIdToVehicles.get(this.id);
		totNOfVehicles = 0;
		trafficSummary = "";

		boolean isThereTraffic = false;

		if (temp != null)
		{
			trafficSummary += "<html>";

			for (String direction : temp.keySet())
			{
				if (temp.get(direction).size() != 0)
				{
					isThereTraffic = true;

					totNOfVehicles += temp.get(direction).size();

					trafficSummary += translateDirValues(direction) + " : [";

					for (Vehicle v : temp.get(direction))
					{
						trafficSummary += v.toString().substring(8) + "<br>";
					}

					//Remove last "<br>"
					trafficSummary = trafficSummary.substring(0, trafficSummary.length() - 4);

					trafficSummary += "]<br>";

				}
			}
			trafficSummary += "<html/>";
		}

		if (!isThereTraffic)
		{
			trafficSummary = "No traffic on this block";
		}
	}

	private String translateDirValues(String dir)
	{//Translates dir values from "0123" to "NSEW" 

		String newDir = "";

		for (char c : dir.toCharArray())
		{
			if (c == '0')
			{
				newDir += "N";
			}
			else if (c == '1')
			{
				newDir += "S";
			}
			else if (c == '2')
			{
				newDir += "E";
			}
			else
			{//c == 3
				newDir += "W";
			}
		}

		return newDir;
	}

	public String getDescription()
	{
		return this.description;
	}

	public int getTotNOfVehicles()
	{
		return this.totNOfVehicles;
	}
}