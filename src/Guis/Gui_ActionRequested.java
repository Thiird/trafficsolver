package Guis;

import java.awt.Font;
import java.awt.Window.Type;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Gui_ActionRequested
{
	public JFrame mainFrame;

	public static JLabel actionRequested, actionMessage;

	public Gui_ActionRequested()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}

		this.initialize();
	}

	private void initialize()
	{
		mainFrame = new JFrame();
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setResizable(false);
		mainFrame.setType(Type.UTILITY);
		mainFrame.setBounds(100, 100, 400, 150);
		mainFrame.setTitle("Warning");
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(null);

		JLabel lblActionRequested = new JLabel("Action Requested:");
		lblActionRequested.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblActionRequested.setBounds(10, 7, 109, 15);
		mainFrame.getContentPane().add(lblActionRequested);

		actionRequested = new JLabel("/");
		actionRequested.setBounds(123, 7, 239, 14);
		mainFrame.getContentPane().add(actionRequested);

		JLabel lblActionMessage = new JLabel("Message:");
		lblActionMessage.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblActionMessage.setBounds(10, 29, 57, 15);
		mainFrame.getContentPane().add(lblActionMessage);

		actionMessage = new JLabel("/");
		actionMessage.setVerticalAlignment(SwingConstants.TOP);
		actionMessage.setBounds(10, 51, 374, 61);
		mainFrame.getContentPane().add(actionMessage);
		mainFrame.setLocationRelativeTo(null);

	}

	public void setVisible(boolean isVisible)
	{
		this.mainFrame.setVisible(isVisible);
	}
}