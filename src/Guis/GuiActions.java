package Guis;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import Render_engine.EngineData;

public class GuiActions
{
	public static Color redColor = new Color(255, 0, 0);
	public static Color blackColor = new Color(0, 0, 0);
	public static Color greyColor = new Color(100, 100, 100);

	public static void updateValue(JTextField tField, JSlider slider, float min, float max)
	{
		if (tField.getText().replaceAll(",", ".").matches("[-+]?[0-9]*\\.?[0-9]+"))//Checks if number
		{
			float value = Float.parseFloat(tField.getText().replaceAll(",", "."));

			updateSliderValue(tField, slider, value, min, max);

			if (slider.getName().contains("F_"))
			{
				tField.setText(String.valueOf(round((max * slider.getValue()) / slider.getMaximum(), 2)));
			}
			else
			{
				tField.setText(String.valueOf((int) (max * slider.getValue()) / slider.getMaximum()));
			}
		}
		else
		{
			//If input is non well-formed set to previous value
			if (slider.getName().contains("F_"))
			{
				tField.setText(Double.toString(round((slider.getValue() * max) / slider.getMaximum(), 2)));
			}
			else
			{
				tField.setText(Integer.toString((int) (slider.getValue() * max) / slider.getMaximum()));
			}
		}
	}

	private static void updateSliderValue(JTextField tField, JSlider slider, float newValue, float min, float max)
	{
		if (slider.getName().contains("F_"))
		{//slider contains float value

			if (newValue < min) slider.setValue(slider.getMinimum());
			else if (newValue > max) slider.setValue(slider.getMaximum());
			else slider.setValue((int) (((slider.getMaximum() * newValue) / max) + min));

		}
		else
		{//slider contains int value
			int valueInt = (int) newValue;

			if (valueInt < slider.getMinimum()) slider.setValue(slider.getMinimum());
			else if (valueInt > slider.getMaximum()) slider.setValue(slider.getMaximum());
			else
			{
				slider.setValue((int) ((slider.getMaximum() * valueInt) / max));
			}
		}

		return;
	}

	public static void updateLabelColor(JComponent component, JLabel label)
	{
		if (label == null)
		{//component is a checkBox or comboBox//TODO?? comboBox is used in the next if

			if (component instanceof JComboBox)
			{
				if (((JComboBox<?>) component).getSelectedIndex() != ((int) (EngineData.currentSettings.get(((JComboBox<?>) component).getName())[0])))
				{
					((JComboBox<?>) component).setForeground(redColor);
				}
				else
				{
					((JComboBox<?>) component).setForeground(blackColor);
				}
			}
			else
			{
				if (((JCheckBox) component).isSelected() != ((boolean) (EngineData.currentSettings.get(((JCheckBox) component).getName())[0])))
				{
					component.setForeground(redColor);
				}
				else
				{
					component.setForeground(blackColor);
				}
			}
		}
		else
		{
			if (component instanceof JComboBox)
			{//component is JComboBox

				if (((JComboBox) component).getSelectedIndex() != ((int) (EngineData.currentSettings.get(((JComboBox) component).getName())[0])))
				{
					label.setForeground(redColor);
				}
				else
				{
					label.setForeground(blackColor);
				}
			}
			else
			{//component is JSlider

				JSlider slider = ((JSlider) component);

				if (slider.getName().substring(0, 1).equals("F"))
				{
					if ((float) slider.getValue() != (int) ((slider.getMaximum() * (float) (EngineData.currentSettings.get(slider.getName().substring(2))[0]))
							/ (float) (EngineData.currentSettings.get(slider.getName().substring(2))[2])))
					{
						label.setForeground(redColor);
					}
					else
					{
						label.setForeground(blackColor);
					}
				}
				else
				{
					if (slider.getValue() != (int) (EngineData.currentSettings.get(slider.getName().substring(2))[0]))
					{
						label.setForeground(redColor);
					}
					else
					{
						label.setForeground(blackColor);
					}
				}
			}
		}
	}

	public static double round(double value, int precision)
	{//https://stackoverflow.com/questions/22186778/using-math-round-to-round-to-one-decimal-place
		int scale = (int) Math.pow(10, precision);
		return (double) Math.round(value * scale) / scale;
	}
}