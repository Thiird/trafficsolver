package Guis;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import Display.DisplayManager;
import Render_engine.EngineData;

public class Gui_MainWindow
{
	public static JFrame mainFrame;
	private static Gui_AppInfos appInfos = new Gui_AppInfos();
	private static Gui_KeyboardShortcuts keyboardShortcuts = new Gui_KeyboardShortcuts();
	public static Gui_ControlPanel controlPanel = new Gui_ControlPanel();
	private static Gui_ProgressBar progressBar = new Gui_ProgressBar();
	public static Gui_VehicleStatus vehicleStatus = new Gui_VehicleStatus();
	public static Gui_ActionRequested actionRequested = new Gui_ActionRequested();
	public static Gui_IntersectionStatus intersectionStatus = new Gui_IntersectionStatus();
	public static Gui_RoadBlockStatus roadBlockStatus = new Gui_RoadBlockStatus();
	public static Gui_ExecutionVisualization execVisualizationWndw = new Gui_ExecutionVisualization();
	public static Gui_RoadStatus roadStatistics = new Gui_RoadStatus();
	public static Canvas mainCanvas;

	private final String DISPLAY_NAME = "[Traffic Solver v" + DisplayManager.appVersion + " - ©Stefano Nicolis]";

	public Gui_MainWindow()
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
		mainFrame.setVisible(true);
	}

	private void initialize()
	{
		mainFrame = new JFrame();
		mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\Stefano\\eclipse-workspace\\Traffic_solver_Stefano_Nicolis\\res\\img\\icons\\app_logo_window.png"));
		mainFrame.setResizable(false);
		mainFrame.setTitle(this.DISPLAY_NAME);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		mainFrame.setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		mainFrame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JMenuBar menuBar = new JMenuBar();
		menuBar.setForeground(SystemColor.scrollbar);
		menuBar.setBorderPainted(false);
		menuBar.setBackground(SystemColor.scrollbar);
		panel.add(menuBar, BorderLayout.NORTH);

		JMenu menu = new JMenu("Application");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.setBackground(SystemColor.scrollbar);
		menuBar.add(menu);

		JMenuItem controlPanelItem = new JMenuItem("Open Control Panel");
		menu.add(controlPanelItem);
		controlPanelItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Gui_ControlPanel.mainFrame.setLocationRelativeTo(null);
				controlPanel.setVisible(true);
			}
		});

		JMenuItem roadStatisticsItem = new JMenuItem("Road Statistics");
		menu.add(roadStatisticsItem);
		roadStatisticsItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Gui_RoadStatus.mainFrame.setLocationRelativeTo(null);
				roadStatistics.setVisible(true);
			}
		});

		JMenuItem vehicleInfo = new JMenuItem("Vehicle Info Box");
		menu.add(vehicleInfo);
		vehicleInfo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Gui_RoadStatus.mainFrame.setLocationRelativeTo(null);
				vehicleStatus.setVisible(true);
			}
		});

		JMenuItem intManagerInfoBox = new JMenuItem("Itersection Manager Info Box");
		menu.add(intManagerInfoBox);
		intManagerInfoBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Gui_RoadStatus.mainFrame.setLocationRelativeTo(null);
				intersectionStatus.setVisible(true);
			}
		});

		JMenuItem roadBlockInfoBox = new JMenuItem("RoadBlock Info Box");
		menu.add(roadBlockInfoBox);
		roadBlockInfoBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Gui_RoadStatus.mainFrame.setLocationRelativeTo(null);
				roadBlockStatus.setVisible(true);
			}
		});

		JCheckBoxMenuItem chckbxShowreel = new JCheckBoxMenuItem("Render Showreel Scene");
		chckbxShowreel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EngineData.renderShowreelScene(chckbxShowreel.isSelected());
			}
		});
		menu.add(chckbxShowreel);

		JMenuItem execVisualization = new JMenuItem("Execution Visualization");
		menu.add(execVisualization);
		execVisualization.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Gui_RoadStatus.mainFrame.setLocationRelativeTo(null);
				Gui_ExecutionVisualization.setVisible(true);
			}
		});

		JMenu menu_1 = new JMenu("Help");
		menu_1.setBackground(SystemColor.scrollbar);
		menuBar.add(menu_1);

		JMenuItem menuItemkeyboardShortcuts = new JMenuItem("Keyboard Shortcuts");
		menu_1.add(menuItemkeyboardShortcuts);

		menuItemkeyboardShortcuts.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				keyboardShortcuts.mainFrame.setLocationRelativeTo(null);
				keyboardShortcuts.setVisible(true);
			}
		});

		JMenuItem menuItem_3 = new JMenuItem("About Traffic Solver");
		menu_1.add(menuItem_3);

		menuItem_3.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				appInfos.mainFrame.setLocationRelativeTo(null);
				appInfos.setVisible(true);
			}
		});

		JPanel panel_1 = new JPanel();
		mainFrame.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		mainCanvas = new Canvas();
		panel_1.add(mainCanvas);
	}

	public Canvas getCanvas()
	{
		return mainCanvas;
	}

	public void setWidthHeight(int width, int height)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double monitorWidth = screenSize.getWidth();
		double monitorHeight = screenSize.getHeight();

		if (monitorWidth == width && monitorHeight == height)
		{
			//0,0 signifies top-left corner
			mainCanvas.setBounds(0, 19, width, height);
			mainFrame.setBounds(0, 0, width, height);
		}
		else
		{
			mainCanvas.setBounds(0, 19, width, height);
			//Place window at center of the screen
			mainFrame.setBounds((int) (monitorWidth / 2) - ((int) width / 2), (int) (monitorHeight / 2) - ((int) height / 2), width, height);
		}
	}

	public static void showProgressBar(boolean value)
	{
		progressBar.mainFrame.setVisible(value);
	}

	public static void setProgressBar(String message, int percentageOfCompletion)
	{
		progressBar.actionBeingPerformed.setText(message);

		//Is -1 when an intermediate process doesn't wont to update % value but just set text
		if (percentageOfCompletion != -1) progressBar.progressBar.setValue(percentageOfCompletion);
	}
}