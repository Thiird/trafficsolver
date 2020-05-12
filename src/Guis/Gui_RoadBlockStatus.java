package Guis;

import java.awt.Font;
import java.awt.Window.Type;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import Render_engine.EngineData;
import Threads.KeyboardListener;

public class Gui_RoadBlockStatus
{
	public JFrame mainFrame;
	public static JLabel roadBlockName, roadBlockPosition, edgeOfPathTravelled, totNOfVehicles, trafficSummary;
	public JLabel lblRoadBlockName, lblRoadBlockPosition, lblTotNOfVehicles, lblTrafficSummary;

	public Gui_RoadBlockStatus()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}

		initialize();
	}

	private void initialize()
	{
		mainFrame = new JFrame();
		mainFrame.setResizable(false);
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setTitle("RoadBlock status");
		mainFrame.setType(Type.UTILITY);
		mainFrame.setBounds(100, 100, 300, 260);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(null);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent)
			{
				EngineData.lastSelectedRoadBlock = null;
				KeyboardListener.deselectEntity();
			}
		});

		lblRoadBlockName = new JLabel("RoadBlock:");
		lblRoadBlockName.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRoadBlockName.setBounds(10, 13, 63, 14);
		mainFrame.getContentPane().add(lblRoadBlockName);

		lblRoadBlockPosition = new JLabel("Position:");
		lblRoadBlockPosition.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRoadBlockPosition.setBounds(10, 40, 63, 14);
		mainFrame.getContentPane().add(lblRoadBlockPosition);

		lblTotNOfVehicles = new JLabel("Total n\u00B0 of Vehicles:");
		lblTotNOfVehicles.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTotNOfVehicles.setBounds(10, 67, 118, 14);
		mainFrame.getContentPane().add(lblTotNOfVehicles);

		lblTrafficSummary = new JLabel("Traffic summary:");
		lblTrafficSummary.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTrafficSummary.setBounds(10, 94, 104, 14);
		mainFrame.getContentPane().add(lblTrafficSummary);

		//Values label
		roadBlockName = new JLabel("/");
		roadBlockName.setFont(new Font("Tahoma", Font.PLAIN, 13));
		roadBlockName.setBounds(79, 13, 205, 14);
		mainFrame.getContentPane().add(roadBlockName);

		roadBlockPosition = new JLabel("/");
		roadBlockPosition.setFont(new Font("Tahoma", Font.PLAIN, 13));
		roadBlockPosition.setBounds(79, 40, 150, 14);
		mainFrame.getContentPane().add(roadBlockPosition);

		totNOfVehicles = new JLabel("/");
		totNOfVehicles.setFont(new Font("Tahoma", Font.PLAIN, 13));
		totNOfVehicles.setBounds(133, 67, 38, 14);
		mainFrame.getContentPane().add(totNOfVehicles);

		trafficSummary = new JLabel("/");
		trafficSummary.setVerticalAlignment(SwingConstants.TOP);
		trafficSummary.setHorizontalAlignment(SwingConstants.LEFT);
		trafficSummary.setFont(new Font("Tahoma", Font.PLAIN, 13));
		trafficSummary.setBackground(UIManager.getColor("Button.background"));
		trafficSummary.setBounds(10, 121, 274, 100);
		mainFrame.getContentPane().add(trafficSummary);
	}

	public void resetValues()
	{
		roadBlockName.setText("/");
		roadBlockPosition.setText("/");
		totNOfVehicles.setText("/");
		trafficSummary.setText("/");
	}

	public void setVisible(boolean isVisible)
	{
		this.mainFrame.setVisible(isVisible);
	}
}