package Guis;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSlider;
import javax.swing.JTextField;

public class CustomActionLister implements ActionListener
{
	JTextField tField;
	JSlider slider;
	float min, max; //min and max of value displayed in text field

	public CustomActionLister(JTextField tField, JSlider slider, float min, float max)
	{
		this.tField = tField;
		this.slider = slider;
		this.min = min;
		this.max = max;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{//When pressing enter

		GuiActions.updateValue(tField, slider, min, max);
	}
}