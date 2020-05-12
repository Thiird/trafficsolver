package Guis;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Window.Type;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import Render_engine.EngineData;
import Threads.KeyboardListener;

public class Gui_IntersectionStatus
{
	public static JFrame mainFrame;
	public static JLabel intersectionManagerName, IntersectionManagerPosition, roadBlock, edgeOfPathTravelled, totNOfVehicles, arrivalOrderDistance, isIntersectionFree, transits, incomingTraffic,
			exitingTraffic;

	public Gui_IntersectionStatus()
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
		//Tooltip popup timing settings
		javax.swing.ToolTipManager.sharedInstance().setInitialDelay(1000);
		javax.swing.ToolTipManager.sharedInstance().setDismissDelay(7000);

		mainFrame = new JFrame();
		mainFrame.setResizable(false);
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setTitle("Intersection status");
		mainFrame.setType(Type.UTILITY);
		mainFrame.setBounds(100, 100, 490, 290);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(null);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setAutoRequestFocus(false);
		mainFrame.addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent)
			{
				EngineData.lastSelectedIntManager = null;
				KeyboardListener.deselectEntity();
			}
		});

		JLabel lblIntersectionManagerName = new JLabel("Intersection Manager:");
		lblIntersectionManagerName.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblIntersectionManagerName.setBounds(10, 7, 126, 15);
		mainFrame.getContentPane().add(lblIntersectionManagerName);

		JLabel lblIntersectionManagerPosition = new JLabel("Position:");
		lblIntersectionManagerPosition.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblIntersectionManagerPosition.setBounds(10, 29, 53, 14);
		mainFrame.getContentPane().add(lblIntersectionManagerPosition);

		JLabel lblRoadblock = new JLabel("RoadBlock:");
		lblRoadblock.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRoadblock.setBounds(10, 50, 63, 14);
		mainFrame.getContentPane().add(lblRoadblock);

		JLabel lblTotNOfVehicles = new JLabel("Total n\u00B0 of Vehicles:");
		lblTotNOfVehicles.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTotNOfVehicles.setBounds(10, 71, 118, 14);
		mainFrame.getContentPane().add(lblTotNOfVehicles);

		JLabel lblArrivalOrderDistance = new JLabel("Arrival order (distance):");
		lblArrivalOrderDistance.setToolTipText("By distance: closest to furthest");
		lblArrivalOrderDistance.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblArrivalOrderDistance.setBounds(10, 92, 144, 14);
		mainFrame.getContentPane().add(lblArrivalOrderDistance);

		JLabel lblIsIntersectionFree = new JLabel("isIntersectionFree:");
		lblIsIntersectionFree.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblIsIntersectionFree.setBounds(10, 113, 107, 14);
		mainFrame.getContentPane().add(lblIsIntersectionFree);

		JLabel lblIncomingTraffic = new JLabel("Incoming traffic:");
		lblIncomingTraffic.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblIncomingTraffic.setBounds(10, 155, 97, 15);
		mainFrame.getContentPane().add(lblIncomingTraffic);

		JLabel lblExitingTraffic = new JLabel("Exiting traffic:");
		lblExitingTraffic.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblExitingTraffic.setBounds(281, 155, 84, 15);
		mainFrame.getContentPane().add(lblExitingTraffic);

		JLabel lblTransits = new JLabel("Transits:");
		lblTransits.setToolTipText("Main direction | transits occured on main direction");
		lblTransits.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTransits.setBounds(10, 134, 53, 14);
		mainFrame.getContentPane().add(lblTransits);

		//Values label
		intersectionManagerName = new JLabel("/");
		intersectionManagerName.setFont(new Font("Tahoma", Font.PLAIN, 13));
		intersectionManagerName.setBounds(145, 7, 319, 14);
		mainFrame.getContentPane().add(intersectionManagerName);

		IntersectionManagerPosition = new JLabel("/");
		IntersectionManagerPosition.setFont(new Font("Tahoma", Font.PLAIN, 13));
		IntersectionManagerPosition.setBounds(67, 29, 84, 14);
		mainFrame.getContentPane().add(IntersectionManagerPosition);

		totNOfVehicles = new JLabel("/");
		totNOfVehicles.setFont(new Font("Tahoma", Font.PLAIN, 13));
		totNOfVehicles.setBounds(133, 71, 53, 14);
		mainFrame.getContentPane().add(totNOfVehicles);

		incomingTraffic = new JLabel("None");
		incomingTraffic.setVerticalAlignment(SwingConstants.TOP);
		incomingTraffic.setHorizontalAlignment(SwingConstants.LEFT);
		incomingTraffic.setFont(new Font("Tahoma", Font.PLAIN, 13));
		incomingTraffic.setBackground(UIManager.getColor("Button.background"));
		incomingTraffic.setBounds(10, 177, 259, 70);
		mainFrame.getContentPane().add(incomingTraffic);

		exitingTraffic = new JLabel("None");
		exitingTraffic.setVerticalAlignment(SwingConstants.TOP);
		exitingTraffic.setHorizontalAlignment(SwingConstants.LEFT);
		exitingTraffic.setFont(new Font("Tahoma", Font.PLAIN, 13));
		exitingTraffic.setBackground(SystemColor.menu);
		exitingTraffic.setBounds(281, 176, 193, 70);
		mainFrame.getContentPane().add(exitingTraffic);

		arrivalOrderDistance = new JLabel("/");
		arrivalOrderDistance.setFont(new Font("Tahoma", Font.PLAIN, 13));
		arrivalOrderDistance.setBounds(155, 92, 73, 14);
		mainFrame.getContentPane().add(arrivalOrderDistance);

		roadBlock = new JLabel("/");
		roadBlock.setFont(new Font("Tahoma", Font.PLAIN, 13));
		roadBlock.setBounds(81, 50, 237, 14);
		mainFrame.getContentPane().add(roadBlock);

		isIntersectionFree = new JLabel("/");
		isIntersectionFree.setFont(new Font("Tahoma", Font.PLAIN, 13));
		isIntersectionFree.setBounds(125, 113, 61, 14);
		mainFrame.getContentPane().add(isIntersectionFree);

		transits = new JLabel("/");
		transits.setFont(new Font("Tahoma", Font.PLAIN, 13));
		transits.setBounds(66, 134, 61, 14);
		mainFrame.getContentPane().add(transits);
	}

	public void resetValues()
	{
		intersectionManagerName.setText("/");
		IntersectionManagerPosition.setText("/");
		totNOfVehicles.setText("/");
		incomingTraffic.setText("/");
		exitingTraffic.setText("/");
		arrivalOrderDistance.setText("/");
		roadBlock.setText("/");
	}

	public void setVisible(boolean isVisible)
	{
		this.mainFrame.setVisible(isVisible);
	}
}