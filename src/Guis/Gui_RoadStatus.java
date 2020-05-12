package Guis;

import java.awt.Font;
import java.awt.Window.Type;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Gui_RoadStatus
{
	public static JFrame mainFrame;
	public JLabel lblNOfRoutes;
	public static JLabel nOfRoutes, nOfActiveVehicles, nOfSolvedVehicles, nOfIntersections, edgeOfPathTravelled, nOfObstacleBlocks, nOfRoadBlocks, roadGridDimensions;

	public Gui_RoadStatus()
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
		mainFrame.setTitle("Road statistics");
		mainFrame.setType(Type.UTILITY);
		mainFrame.setBounds(100, 100, 250, 210);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(null);
		mainFrame.setLocationRelativeTo(null);

		lblNOfRoutes = new JLabel("N\u00B0 of routes:");
		lblNOfRoutes.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNOfRoutes.setBounds(10, 34, 80, 14);
		mainFrame.getContentPane().add(lblNOfRoutes);

		JLabel lblNOfActiveVehicles = new JLabel("N\u00B0 of active vehicles:");
		lblNOfActiveVehicles.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNOfActiveVehicles.setBounds(10, 130, 125, 14);
		mainFrame.getContentPane().add(lblNOfActiveVehicles);

		JLabel lblNOfIntersections = new JLabel("N\u00B0 of intersections:");
		lblNOfIntersections.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNOfIntersections.setBounds(10, 106, 117, 14);
		mainFrame.getContentPane().add(lblNOfIntersections);

		JLabel lblNOfSolvedVehicles = new JLabel("N\u00B0 of solved vehicles:");
		lblNOfSolvedVehicles.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNOfSolvedVehicles.setBounds(10, 154, 125, 14);
		mainFrame.getContentPane().add(lblNOfSolvedVehicles);

		JLabel lblNOfObstacleBlock = new JLabel("N\u00B0 of obstacle blocks:");
		lblNOfObstacleBlock.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNOfObstacleBlock.setBounds(10, 82, 132, 14);
		mainFrame.getContentPane().add(lblNOfObstacleBlock);

		JLabel lblNOfRoadBlocks = new JLabel("N\u00B0 of Road Blocks:");
		lblNOfRoadBlocks.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNOfRoadBlocks.setBounds(10, 58, 111, 14);
		mainFrame.getContentPane().add(lblNOfRoadBlocks);

		JLabel lblRoadGridSizes = new JLabel("Road Grid sizes (X * Y):");
		lblRoadGridSizes.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRoadGridSizes.setBounds(10, 10, 141, 14);
		mainFrame.getContentPane().add(lblRoadGridSizes);

		//Values label
		nOfRoutes = new JLabel("/");
		nOfRoutes.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nOfRoutes.setBounds(93, 34, 59, 14);
		mainFrame.getContentPane().add(nOfRoutes);

		nOfActiveVehicles = new JLabel("/");
		nOfActiveVehicles.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nOfActiveVehicles.setBounds(137, 130, 70, 14);
		mainFrame.getContentPane().add(nOfActiveVehicles);

		nOfIntersections = new JLabel("/");
		nOfIntersections.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nOfIntersections.setBounds(128, 106, 70, 14);
		mainFrame.getContentPane().add(nOfIntersections);

		nOfSolvedVehicles = new JLabel("/");
		nOfSolvedVehicles.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nOfSolvedVehicles.setBounds(138, 154, 70, 14);
		mainFrame.getContentPane().add(nOfSolvedVehicles);

		nOfObstacleBlocks = new JLabel("/");
		nOfObstacleBlocks.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nOfObstacleBlocks.setBounds(144, 82, 64, 14);
		mainFrame.getContentPane().add(nOfObstacleBlocks);

		nOfRoadBlocks = new JLabel("/");
		nOfRoadBlocks.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nOfRoadBlocks.setBounds(123, 59, 59, 14);
		mainFrame.getContentPane().add(nOfRoadBlocks);

		roadGridDimensions = new JLabel("/");
		roadGridDimensions.setFont(new Font("Tahoma", Font.PLAIN, 13));
		roadGridDimensions.setBounds(155, 10, 70, 14);
		mainFrame.getContentPane().add(roadGridDimensions);
	}

	public void setVisible(boolean isVisible)
	{
		Gui_RoadStatus.mainFrame.setVisible(isVisible);
	}
}