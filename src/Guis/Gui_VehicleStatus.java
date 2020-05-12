package Guis;

import java.awt.Font;
import java.awt.Window.Type;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import Render_engine.EngineData;
import Threads.KeyboardListener;

public class Gui_VehicleStatus
{
	public static JFrame mainFrame;
	public static JLabel vName, velocity, isBlockedByUser, hasIntersectionToken, isReadyToCross, isManagedByIntManager, currBitMask, currDirValue, currRoadBlock, assignedPathLength,
			lengthOfPathTravelled, lengthOfEdgeTravelled, edgeOfPathTravelled, distTravelled, distFromNextVehicle, isDone, currPathConfig, justBorn, nextIntExitDir, nextVehicle, distFromIntersection,
			intersectionInTheWay;
	private JLabel lblIsManagedByIntManager;

	public Gui_VehicleStatus()
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
		mainFrame.setTitle("Vehicle status");
		mainFrame.setType(Type.UTILITY);
		mainFrame.setBounds(100, 100, 370, 490);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(null);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setAutoRequestFocus(false);
		mainFrame.addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent)
			{
				EngineData.lastSelectedVehicle = null;
				KeyboardListener.deselectEntity();
			}
		});

		JLabel lblVehicleName = new JLabel("Vehicle:");
		lblVehicleName.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblVehicleName.setBounds(12, 6, 59, 15);
		mainFrame.getContentPane().add(lblVehicleName);

		JLabel lblVelocity = new JLabel("Velocity:");
		lblVelocity.setToolTipText("meters/simStep");
		lblVelocity.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblVelocity.setBounds(12, 27, 59, 15);
		mainFrame.getContentPane().add(lblVelocity);

		JLabel lblJustBorn = new JLabel("JustBorn:");
		lblJustBorn.setToolTipText("true when thread has just been started, not inited yet");
		lblJustBorn.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblJustBorn.setBounds(12, 48, 53, 15);
		mainFrame.getContentPane().add(lblJustBorn);

		JLabel lblIsDone = new JLabel("IsDone:");
		lblIsDone.setToolTipText("true when vehicle has reached the end of the route");
		lblIsDone.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblIsDone.setBounds(12, 69, 44, 15);
		mainFrame.getContentPane().add(lblIsDone);

		JLabel lblBlockedByUser = new JLabel("Blocked by user:");
		lblBlockedByUser.setToolTipText("true when user has blocked vehicle");
		lblBlockedByUser.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblBlockedByUser.setBounds(12, 90, 97, 15);
		mainFrame.getContentPane().add(lblBlockedByUser);

		JLabel lblHasIntToken = new JLabel("hasIntToken:");
		lblHasIntToken.setToolTipText("true when can enter intersection safely");
		lblHasIntToken.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblHasIntToken.setBounds(12, 111, 80, 15);
		mainFrame.getContentPane().add(lblHasIntToken);

		JLabel lblIsManagedByIntManager = new JLabel("IsManagedByIntManager:");
		lblIsManagedByIntManager.setToolTipText("true when netIntersection has reference to this vehicle");
		lblIsManagedByIntManager.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblIsManagedByIntManager.setBounds(12, 132, 148, 15);
		mainFrame.getContentPane().add(lblIsManagedByIntManager);

		JLabel lblRoadblock = new JLabel("RoadBlock:");
		lblRoadblock.setToolTipText("reference of current RoadBlock obj");
		lblRoadblock.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRoadblock.setBounds(12, 173, 66, 15);
		mainFrame.getContentPane().add(lblRoadblock);

		JLabel lblCurrentDirectionalValue = new JLabel("Current Directional Value:");
		lblCurrentDirectionalValue.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblCurrentDirectionalValue.setBounds(12, 194, 148, 15);
		mainFrame.getContentPane().add(lblCurrentDirectionalValue);

		JLabel lblIntersectionInTheWay = new JLabel("Intersection in the way:");
		lblIntersectionInTheWay.setToolTipText("true when there's an intersection in front ofcurr vehicle");
		lblIntersectionInTheWay.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblIntersectionInTheWay.setBounds(12, 215, 141, 15);
		mainFrame.getContentPane().add(lblIntersectionInTheWay);

		JLabel lblDistFromIntersection = new JLabel("Distance from intersection:");
		lblDistFromIntersection.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblDistFromIntersection.setBounds(12, 236, 156, 15);
		mainFrame.getContentPane().add(lblDistFromIntersection);

		JLabel lblLenghtOfPath = new JLabel("Lenght Of Path Travelled:");
		lblLenghtOfPath.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblLenghtOfPath.setBounds(12, 320, 148, 15);
		mainFrame.getContentPane().add(lblLenghtOfPath);

		JLabel lblLenghtOfEdge = new JLabel("Lenght Of Edge Travelled:");
		lblLenghtOfEdge.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblLenghtOfEdge.setBounds(12, 341, 156, 15);
		mainFrame.getContentPane().add(lblLenghtOfEdge);

		JLabel lblDistanceTravelled = new JLabel("Distance travelled:");
		lblDistanceTravelled.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblDistanceTravelled.setBounds(12, 362, 117, 15);
		mainFrame.getContentPane().add(lblDistanceTravelled);

		JLabel lblDistFromNext = new JLabel("Dist From Next Vehicle:");
		lblDistFromNext.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblDistFromNext.setBounds(12, 383, 141, 15);
		mainFrame.getContentPane().add(lblDistFromNext);

		JLabel lblCurrentPathConfig = new JLabel("Current Path Config:");
		lblCurrentPathConfig.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblCurrentPathConfig.setBounds(12, 257, 124, 15);
		mainFrame.getContentPane().add(lblCurrentPathConfig);

		JLabel lblDirInsideNextInt = new JLabel("Path config index in next int:");
		lblDirInsideNextInt.setToolTipText("Path configuration index inside next intersection");
		lblDirInsideNextInt.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblDirInsideNextInt.setBounds(12, 278, 165, 15);
		mainFrame.getContentPane().add(lblDirInsideNextInt);

		JLabel lblAssignedPathLenght = new JLabel("Assigned Path Lenght:");
		lblAssignedPathLenght.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblAssignedPathLenght.setBounds(12, 299, 132, 15);
		mainFrame.getContentPane().add(lblAssignedPathLenght);

		JLabel lblNextVehicle = new JLabel("Next Vehicle:");
		lblNextVehicle.setToolTipText("reference to the vehicle in front of me (null if I find and intersection before a vehicle)");
		lblNextVehicle.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNextVehicle.setBounds(12, 404, 80, 15);
		mainFrame.getContentPane().add(lblNextVehicle);

		//Values label
		vName = new JLabel("/");
		vName.setFont(new Font("Tahoma", Font.PLAIN, 13));
		vName.setBounds(64, 6, 290, 15);
		mainFrame.getContentPane().add(vName);

		velocity = new JLabel("/");
		velocity.setFont(new Font("Tahoma", Font.PLAIN, 13));
		velocity.setBounds(69, 27, 102, 15);
		mainFrame.getContentPane().add(velocity);

		isBlockedByUser = new JLabel("/");
		isBlockedByUser.setFont(new Font("Tahoma", Font.PLAIN, 13));
		isBlockedByUser.setBounds(111, 90, 53, 15);
		mainFrame.getContentPane().add(isBlockedByUser);

		hasIntersectionToken = new JLabel("/");
		hasIntersectionToken.setFont(new Font("Tahoma", Font.PLAIN, 13));
		hasIntersectionToken.setBounds(94, 111, 53, 15);
		mainFrame.getContentPane().add(hasIntersectionToken);

		currRoadBlock = new JLabel("/");
		currRoadBlock.setFont(new Font("Tahoma", Font.PLAIN, 13));
		currRoadBlock.setBounds(81, 173, 273, 15);
		mainFrame.getContentPane().add(currRoadBlock);

		currDirValue = new JLabel("/");
		currDirValue.setFont(new Font("Tahoma", Font.PLAIN, 13));
		currDirValue.setBounds(167, 194, 66, 15);
		mainFrame.getContentPane().add(currDirValue);

		assignedPathLength = new JLabel("/");
		assignedPathLength.setFont(new Font("Tahoma", Font.PLAIN, 13));
		assignedPathLength.setBounds(149, 299, 63, 15);
		mainFrame.getContentPane().add(assignedPathLength);

		lengthOfPathTravelled = new JLabel("/");
		lengthOfPathTravelled.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lengthOfPathTravelled.setBounds(164, 320, 86, 15);
		mainFrame.getContentPane().add(lengthOfPathTravelled);

		lengthOfEdgeTravelled = new JLabel("/");
		lengthOfEdgeTravelled.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lengthOfEdgeTravelled.setBounds(169, 341, 86, 15);
		mainFrame.getContentPane().add(lengthOfEdgeTravelled);

		distTravelled = new JLabel("/");
		distTravelled.setFont(new Font("Tahoma", Font.PLAIN, 13));
		distTravelled.setBounds(126, 362, 102, 15);
		mainFrame.getContentPane().add(distTravelled);

		distFromNextVehicle = new JLabel("/");
		distFromNextVehicle.setFont(new Font("Tahoma", Font.PLAIN, 13));
		distFromNextVehicle.setBounds(153, 383, 107, 15);
		mainFrame.getContentPane().add(distFromNextVehicle);

		nextVehicle = new JLabel("/");
		nextVehicle.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nextVehicle.setBounds(100, 404, 254, 15);
		mainFrame.getContentPane().add(nextVehicle);

		currPathConfig = new JLabel("/");
		currPathConfig.setFont(new Font("Tahoma", Font.PLAIN, 13));
		currPathConfig.setBounds(138, 257, 141, 15);
		mainFrame.getContentPane().add(currPathConfig);

		distFromIntersection = new JLabel("/");
		distFromIntersection.setFont(new Font("Tahoma", Font.PLAIN, 13));
		distFromIntersection.setBounds(174, 236, 85, 15);
		mainFrame.getContentPane().add(distFromIntersection);

		intersectionInTheWay = new JLabel("/");
		intersectionInTheWay.setFont(new Font("Tahoma", Font.PLAIN, 13));
		intersectionInTheWay.setBounds(155, 215, 141, 15);
		mainFrame.getContentPane().add(intersectionInTheWay);

		justBorn = new JLabel("/");
		justBorn.setBounds(71, 48, 59, 15);
		mainFrame.getContentPane().add(justBorn);

		isDone = new JLabel("/");
		isDone.setBounds(62, 69, 58, 15);
		mainFrame.getContentPane().add(isDone);

		nextIntExitDir = new JLabel("/");
		nextIntExitDir.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nextIntExitDir.setBounds(181, 278, 93, 15);
		mainFrame.getContentPane().add(nextIntExitDir);

		isManagedByIntManager = new JLabel("/");
		isManagedByIntManager.setFont(new Font("Tahoma", Font.PLAIN, 13));
		isManagedByIntManager.setBounds(163, 132, 53, 15);
		mainFrame.getContentPane().add(isManagedByIntManager);

		isReadyToCross = new JLabel("/");
		isReadyToCross.setBounds(116, 153, 46, 14);
		mainFrame.getContentPane().add(isReadyToCross);

		JLabel lblIsReadyToCross = new JLabel("IsReadyToCross:");
		lblIsReadyToCross.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblIsReadyToCross.setBounds(12, 153, 107, 14);
		mainFrame.getContentPane().add(lblIsReadyToCross);

		JLabel lblNewLabel_1 = new JLabel("Current bit mask:");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel_1.setBounds(12, 431, 102, 14);
		mainFrame.getContentPane().add(lblNewLabel_1);

		currBitMask = new JLabel("/");
		currBitMask.setFont(new Font("Tahoma", Font.PLAIN, 13));
		currBitMask.setBounds(122, 431, 90, 14);
		mainFrame.getContentPane().add(currBitMask);
	}

	public static void resetValues()
	{
		vName.setText("/");
		justBorn.setText("/");
		isDone.setText("/");
		velocity.setText("/");
		isBlockedByUser.setText("/");
		hasIntersectionToken.setText("/");
		currRoadBlock.setText("/");
		currDirValue.setText("/");
		currPathConfig.setText("/");
		distFromIntersection.setText("/");
		assignedPathLength.setText("/");
		lengthOfPathTravelled.setText("/");
		lengthOfEdgeTravelled.setText("/");
		distTravelled.setText("/");
		distFromNextVehicle.setText("/");
		intersectionInTheWay.setText("/");
	}

	public void setVisible(boolean isVisible)
	{
		mainFrame.setVisible(isVisible);
	}
}