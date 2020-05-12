package Guis;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Render_engine.EngineData;
import TrafficLogic.RoadManager;

public class CustomChangeListener implements ChangeListener
{
	JTextField tField;
	JComponent component;
	JLabel label;

	public CustomChangeListener(JTextField tField, JComponent component, JLabel label)
	{
		this.tField = tField;
		this.component = component;
		this.label = label;
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (component instanceof JSlider)
		{
			JSlider slider = (JSlider) component;
			Object[] values = EngineData.currentSettings.get(slider.getName().substring(2));

			if (tField.getName().startsWith("F")) tField.setText(String.valueOf(GuiActions.round(((float) values[2] * slider.getValue()) / slider.getMaximum(), 2)));
			else tField.setText(String.valueOf(((int) values[2] * slider.getValue()) / slider.getMaximum()));

			//Specific actions
			if (slider.getName().equals("F_roadBlocksOffset"))
			{
				EngineData.currentSettings.get("roadBlocksOffset")[0] = slider.getValue() / 100f;
				EngineData.updateGeneralVariables();

				RoadManager.setRoadBlocksPositions();
			}
		}

		GuiActions.updateLabelColor(component, label);
	}
}