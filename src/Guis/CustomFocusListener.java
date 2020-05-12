package Guis;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JSlider;
import javax.swing.JTextField;

public class CustomFocusListener implements FocusListener
{
	JTextField tField;
	JSlider slider;
	float min, max; //min and max of value displayed in text field

	public CustomFocusListener(JTextField tField, JSlider slider, float min, float max)
	{
		this.tField = tField;
		this.slider = slider;
		this.min = min;
		this.max = max;
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		GuiActions.updateValue(tField, slider, min, max);
	}

	@Override
	public void focusGained(FocusEvent e)
	{
	}
}