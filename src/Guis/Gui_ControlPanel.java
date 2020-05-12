package Guis;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

import org.lwjgl.opengl.DisplayMode;

import Display.DisplayManager;
import Render_engine.EngineData;
import Threads.Executor;
import TrafficLogic.RoadData;
import TrafficLogic.VehiclesManager;
import net.miginfocom.swing.MigLayout;

public class Gui_ControlPanel
{
	private static ImageIcon playSim = new ImageIcon(Gui_ControlPanel.class.getClassLoader().getResource("img/icons/btn_play_simulation.png"));
	private static ImageIcon stopSim = new ImageIcon(Gui_ControlPanel.class.getClassLoader().getResource("img/icons/btn_stop_simulation.png"));
	public static JFrame mainFrame;
	public static volatile JButton btnRestoreDefaultSettings, btnResetTraffic, btnReBuildRoad, btnApplySettings;
	public static JComboBox<?> windowComboBox, aAComboBox, solveModeComboBox;
	public static JTabbedPane tabbedPane;
	public static volatile JButton simButton;
	public static volatile JLabel fps;
	public static Color red = new Color(1, 0, 0);
	public static Color black = new Color(0, 0, 0);

	private static HashMap<String, ArrayList<Object>> generalWidgetMap = new HashMap<String, ArrayList<Object>>();//Can be modified with sim running
	private static HashMap<String, ArrayList<Object>> trafficWidgetMap = new HashMap<String, ArrayList<Object>>();//Modified only when "Reset traffic" is pressed
	private static HashMap<String, ArrayList<Object>> roadWidgetMap = new HashMap<String, ArrayList<Object>>();//Modified only when "Reset road" is pressed
	private static HashMap<String, ArrayList<Object>> graphicsWidgetMap = new HashMap<String, ArrayList<Object>>();

	public static JLabel renderingLED;
	private static int cont = 0;

	public Gui_ControlPanel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}

		this.initializeWidgetsMap();
		this.initializeGui();
	}

	private void initializeWidgetsMap()
	{
		generalWidgetMap.put(JLabel.class.toString(), new ArrayList<Object>());
		generalWidgetMap.put(JTextField.class.toString(), new ArrayList<Object>());
		generalWidgetMap.put(JSlider.class.toString(), new ArrayList<Object>());
		generalWidgetMap.put(JCheckBox.class.toString(), new ArrayList<Object>());
		generalWidgetMap.put(JComboBox.class.toString(), new ArrayList<Object>());

		trafficWidgetMap.put(JLabel.class.toString(), new ArrayList<Object>());
		trafficWidgetMap.put(JTextField.class.toString(), new ArrayList<Object>());
		trafficWidgetMap.put(JSlider.class.toString(), new ArrayList<Object>());
		trafficWidgetMap.put(JCheckBox.class.toString(), new ArrayList<Object>());
		trafficWidgetMap.put(JComboBox.class.toString(), new ArrayList<Object>());

		roadWidgetMap.put(JLabel.class.toString(), new ArrayList<Object>());
		roadWidgetMap.put(JTextField.class.toString(), new ArrayList<Object>());
		roadWidgetMap.put(JSlider.class.toString(), new ArrayList<Object>());
		roadWidgetMap.put(JCheckBox.class.toString(), new ArrayList<Object>());
		roadWidgetMap.put(JComboBox.class.toString(), new ArrayList<Object>());

		graphicsWidgetMap.put(JLabel.class.toString(), new ArrayList<Object>());
		graphicsWidgetMap.put(JTextField.class.toString(), new ArrayList<Object>());
		graphicsWidgetMap.put(JSlider.class.toString(), new ArrayList<Object>());
		graphicsWidgetMap.put(JCheckBox.class.toString(), new ArrayList<Object>());
		graphicsWidgetMap.put(JComboBox.class.toString(), new ArrayList<Object>());
	}

	private void initializeGui()
	{
		//Tooltip popup timing settings
		javax.swing.ToolTipManager.sharedInstance().setInitialDelay(5000);
		javax.swing.ToolTipManager.sharedInstance().setDismissDelay(7000);

		mainFrame = new JFrame();
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setResizable(false);
		mainFrame.setType(Type.UTILITY);
		mainFrame.setTitle("Control Panel");
		mainFrame.setBounds(100, 100, 500, 320);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(null);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(8, 2, 300, 289);
		mainFrame.getContentPane().add(tabbedPane);

		JPanel panel_road_settings = new JPanel();
		panel_road_settings.setBorder(null);
		tabbedPane.addTab("Road Grid Settings", null, panel_road_settings, null);
		panel_road_settings.setLayout(null);

		//Grid width
		JLabel lblWidth = new JLabel("Width");
		lblWidth.setToolTipText("Width of the road specified in blocks");
		lblWidth.setBounds(11, 5, 28, 14);
		lblWidth.setName("lblWidth");
		panel_road_settings.add(lblWidth);

		JTextField textField_0 = new JTextField();
		textField_0.setBounds(133, 27, 28, 20);
		textField_0.setColumns(10);
		textField_0.setName("I_gridWidth");
		panel_road_settings.add(textField_0);

		JSlider slider_0 = new JSlider();
		slider_0.setMinorTickSpacing(1);
		slider_0.setBounds(11, 24, 118, 26);
		slider_0.setName("I_gridWidth");
		panel_road_settings.add(slider_0);
		slider_0.addChangeListener(new CustomChangeListener(textField_0, slider_0, lblWidth));

		roadWidgetMap.get(lblWidth.getClass().toString()).add(lblWidth);
		roadWidgetMap.get(textField_0.getClass().toString()).add(textField_0);
		roadWidgetMap.get(slider_0.getClass().toString()).add(slider_0);

		//Grid Height
		JLabel lblHeight = new JLabel("Height");
		lblHeight.setToolTipText("Height of the road specified in blocks");
		lblHeight.setBounds(11, 55, 31, 14);
		lblHeight.setName("lblHeight");
		panel_road_settings.add(lblHeight);

		JTextField textField_1 = new JTextField();
		textField_1.setBounds(133, 77, 28, 20);
		textField_1.setColumns(10);
		textField_1.setName("I_gridHeight");
		panel_road_settings.add(textField_1);

		JSlider slider_1 = new JSlider();
		slider_1.setMinorTickSpacing(1);
		slider_1.setBounds(11, 74, 118, 26);
		slider_1.setName("I_gridHeight");
		panel_road_settings.add(slider_1);
		slider_1.addChangeListener(new CustomChangeListener(textField_1, slider_1, lblHeight));

		roadWidgetMap.get(lblHeight.getClass().toString()).add(lblHeight);
		roadWidgetMap.get(textField_1.getClass().toString()).add(textField_1);
		roadWidgetMap.get(slider_1.getClass().toString()).add(slider_1);

		//Roads percentage
		JLabel lblRoads = new JLabel("Roads amount");
		lblRoads.setToolTipText("Amount of road to create in available space");
		lblRoads.setBounds(11, 105, 77, 14);
		lblRoads.setName("lblRoads");
		panel_road_settings.add(lblRoads);

		JTextField textField_2 = new JTextField();
		textField_2.setBounds(133, 127, 33, 20);
		textField_2.setColumns(10);
		textField_2.setName("F_roadsAmount");
		panel_road_settings.add(textField_2);

		JSlider slider_2 = new JSlider();
		slider_2.setMinorTickSpacing(1);
		slider_2.setBounds(11, 124, 118, 26);
		slider_2.setName("F_roadsAmount");
		panel_road_settings.add(slider_2);

		slider_2.addChangeListener(new CustomChangeListener(textField_2, slider_2, lblRoads));

		roadWidgetMap.get(lblRoads.getClass().toString()).add(lblRoads);
		roadWidgetMap.get(textField_2.getClass().toString()).add(textField_2);
		roadWidgetMap.get(slider_2.getClass().toString()).add(slider_2);

		//Obstacles percentage
		JLabel lblObstaclesPercentage = new JLabel("Obstacles amount");
		lblObstaclesPercentage.setToolTipText("Amount of obstacles to create in available space");
		lblObstaclesPercentage.setBounds(11, 155, 95, 14);
		lblObstaclesPercentage.setName("lblObstaclesPercentage");
		panel_road_settings.add(lblObstaclesPercentage);

		JTextField textField_3 = new JTextField();
		textField_3.setColumns(10);
		textField_3.setBounds(133, 177, 33, 20);
		textField_3.setName("F_obstaclesAmount");
		panel_road_settings.add(textField_3);

		JSlider slider_3 = new JSlider();
		slider_3.setMinorTickSpacing(1);
		slider_3.setBounds(11, 174, 118, 26);
		slider_3.setName("F_obstaclesAmount");
		panel_road_settings.add(slider_3);
		slider_3.addChangeListener(new CustomChangeListener(textField_3, slider_3, lblObstaclesPercentage));

		roadWidgetMap.get(lblObstaclesPercentage.getClass().toString()).add(lblObstaclesPercentage);
		roadWidgetMap.get(textField_3.getClass().toString()).add(textField_3);
		roadWidgetMap.get(slider_3.getClass().toString()).add(slider_3);

		//Road blocks offset
		JLabel lblRoadBlocksOffset = new JLabel("RoadBlock offset");
		lblRoadBlocksOffset.setToolTipText("Amount of space between grid blocks (real time)");
		lblRoadBlocksOffset.setBounds(11, 205, 95, 14);
		lblRoadBlocksOffset.setName("lblRoadBlocksOffset");
		panel_road_settings.add(lblRoadBlocksOffset);

		JTextField textField_4 = new JTextField();
		textField_4.setBackground(Color.WHITE);
		textField_4.setColumns(10);
		textField_4.setBounds(133, 227, 33, 20);
		textField_4.setName("F_roadBlocksOffset");

		JSlider slider_4 = new JSlider();
		slider_4.setMinorTickSpacing(1);
		slider_4.setBounds(11, 224, 118, 26);
		slider_4.setName("F_roadBlocksOffset");
		panel_road_settings.add(slider_4);
		slider_4.addChangeListener(new CustomChangeListener(textField_4, slider_4, lblRoadBlocksOffset));

		panel_road_settings.add(textField_4);

		generalWidgetMap.get(lblRoadBlocksOffset.getClass().toString()).add(lblRoadBlocksOffset);
		generalWidgetMap.get(textField_4.getClass().toString()).add(textField_4);
		generalWidgetMap.get(slider_4.getClass().toString()).add(slider_4);

		JPanel panel_simulation_settings = new JPanel();
		panel_simulation_settings.setBorder(null);
		tabbedPane.addTab("Simulation settings", null, panel_simulation_settings, null);
		panel_simulation_settings.setLayout(null);

		//Vehicle spawn rate
		JLabel lblVehicleSpawnRate = new JLabel("Vehicles spawn rate");
		lblVehicleSpawnRate.setToolTipText("Number of vehicles created each minute");
		lblVehicleSpawnRate.setBounds(11, 9, 103, 14);
		lblVehicleSpawnRate.setName("lblVehicleSpawnRate");
		panel_simulation_settings.add(lblVehicleSpawnRate);

		JTextField textField_5 = new JTextField();
		textField_5.setBounds(140, 35, 32, 20);
		textField_5.setColumns(10);
		textField_5.setName("I_vehiclesPerMinute");
		panel_simulation_settings.add(textField_5);

		JSlider slider_5 = new JSlider();
		slider_5.setMinorTickSpacing(1);
		slider_5.setBounds(11, 32, 118, 26);
		slider_5.setName("I_vehiclesPerMinute");
		panel_simulation_settings.add(slider_5);
		slider_5.addChangeListener(new CustomChangeListener(textField_5, slider_5, lblVehicleSpawnRate));

		generalWidgetMap.get(lblVehicleSpawnRate.getClass().toString()).add(lblVehicleSpawnRate);
		generalWidgetMap.get(textField_5.getClass().toString()).add(textField_5);
		generalWidgetMap.get(slider_5.getClass().toString()).add(slider_5);

		//Solver rate
		JLabel lblSolveDelay = new JLabel("Solver delay");
		lblSolveDelay.setToolTipText("Delay between after each RoadPhase execution");
		lblSolveDelay.setBounds(11, 67, 69, 20);
		lblSolveDelay.setName("txtpnSolveRate");
		panel_simulation_settings.add(lblSolveDelay);

		JTextField textField_6 = new JTextField();
		textField_6.setColumns(10);
		textField_6.setBounds(140, 98, 32, 20);
		textField_6.setName("I_solverDelay");
		panel_simulation_settings.add(textField_6);

		JSlider slider_6 = new JSlider();
		slider_6.setMinorTickSpacing(1);
		slider_6.setBounds(11, 96, 118, 24);
		slider_6.setName("I_solverDelay");
		panel_simulation_settings.add(slider_6);
		slider_6.addChangeListener(new CustomChangeListener(textField_6, slider_6, lblSolveDelay));

		generalWidgetMap.get(lblSolveDelay.getClass().toString()).add(lblSolveDelay);
		generalWidgetMap.get(textField_6.getClass().toString()).add(textField_6);
		generalWidgetMap.get(slider_6.getClass().toString()).add(slider_6);

		//Vehicle cruise velocity
		JLabel lblVehicleCruiseVelocity = new JLabel("Vehicle cruise velocity");
		lblVehicleCruiseVelocity.setBounds(11, 129, 116, 20);
		lblVehicleCruiseVelocity.setName("lblVehicleCruiseVelocity");
		panel_simulation_settings.add(lblVehicleCruiseVelocity);

		JTextField textField_7 = new JTextField();
		textField_7.setName("F_cruiseVelocity");
		textField_7.setColumns(10);
		textField_7.setBounds(140, 160, 32, 20);
		panel_simulation_settings.add(textField_7);

		JSlider slider_7 = new JSlider();
		slider_7.setMinorTickSpacing(1);
		slider_7.setName("F_cruiseVelocity");
		slider_7.setBounds(11, 158, 118, 24);
		slider_7.addChangeListener(new CustomChangeListener(textField_7, slider_7, lblVehicleCruiseVelocity));
		panel_simulation_settings.add(slider_7);

		generalWidgetMap.get(lblVehicleCruiseVelocity.getClass().toString()).add(lblVehicleCruiseVelocity);
		generalWidgetMap.get(textField_7.getClass().toString()).add(textField_7);
		generalWidgetMap.get(slider_7.getClass().toString()).add(slider_7);

		//Transition limit
		JLabel lblTransitLimit = new JLabel("Transits per direction limit");
		lblTransitLimit.setBounds(11, 191, 122, 20);
		lblTransitLimit.setName("lblTransitLimit");
		panel_simulation_settings.add(lblTransitLimit);

		JTextField textField_8 = new JTextField();
		textField_8.setName("I_transitLimit");
		textField_8.setColumns(10);
		textField_8.setBounds(140, 222, 32, 20);
		panel_simulation_settings.add(textField_8);

		JSlider slider_8 = new JSlider();
		slider_8.setMinorTickSpacing(1);
		slider_8.setName("I_transitLimit");
		slider_8.setBounds(11, 220, 118, 24);
		slider_8.addChangeListener(new CustomChangeListener(textField_8, slider_8, lblTransitLimit));
		panel_simulation_settings.add(slider_8);

		trafficWidgetMap.get(lblTransitLimit.getClass().toString()).add(lblTransitLimit);
		trafficWidgetMap.get(textField_8.getClass().toString()).add(textField_8);
		trafficWidgetMap.get(slider_8.getClass().toString()).add(slider_8);

		//Solve Mode

		//GRAPHICS TAB
		JPanel panel_graphics_settings = new JPanel();
		panel_graphics_settings.setBorder(null);
		tabbedPane.addTab("Graphics Settings", null, panel_graphics_settings, null);
		panel_graphics_settings.setLayout(null);

		//Window resolution
		JLabel lblWindowResolution = new JLabel("Window mode");
		lblWindowResolution.setToolTipText("width x height x bitPerPixel @Refresh rate");
		lblWindowResolution.setBounds(10, 15, 88, 14);
		lblWindowResolution.setName("lblWindowResolution");
		panel_graphics_settings.add(lblWindowResolution);

		windowComboBox = new JComboBox();
		windowComboBox.setBounds(108, 11, 151, 22);
		windowComboBox.setModel(new DefaultComboBoxModel(getScreenResolutions())); //Set values
		windowComboBox.setSelectedIndex(windowComboBox.getComponentCount() - 1);
		windowComboBox.setName("windowResolution");
		panel_graphics_settings.add(windowComboBox);

		windowComboBox.setSelectedIndex(0);

		//Anti-aliasing
		JCheckBox chckbxAntialiasing = new JCheckBox("Anti-Aliasing");
		chckbxAntialiasing.setEnabled(false);
		chckbxAntialiasing.setToolTipText("Enables Anti-Aliasing (if supported)");
		chckbxAntialiasing.setBounds(10, 44, 97, 23);
		chckbxAntialiasing.setName("antiAliasing");
		chckbxAntialiasing.addChangeListener(new CustomChangeListener(null, chckbxAntialiasing, null));
		chckbxAntialiasing.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				aAComboBox.setEnabled(!aAComboBox.isEnabled());
			}
		});
		panel_graphics_settings.add(chckbxAntialiasing);

		aAComboBox = new JComboBox();
		aAComboBox.setEnabled(false);
		aAComboBox.setToolTipText("Anti-Aliasing Samples");
		aAComboBox.setBounds(108, 44, 41, 22);
		aAComboBox.setName("aASamplesIndex");
		panel_graphics_settings.add(aAComboBox);

		setAASamples(chckbxAntialiasing, aAComboBox);

		aAComboBox.setSelectedIndex(0);

		//Buttons
		JPanel pnlSimActions = new JPanel();
		pnlSimActions.setBorder(new LineBorder(Color.LIGHT_GRAY));
		pnlSimActions.setBounds(316, 13, 170, 118);
		mainFrame.getContentPane().add(pnlSimActions);
		pnlSimActions.setLayout(new MigLayout("", "[103px][153px]", "[23px][][][]"));

		btnRestoreDefaultSettings = new JButton("Restore Default Settings");
		btnRestoreDefaultSettings.setToolTipText("Restores default settings in all tabs");
		btnRestoreDefaultSettings.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				EngineData.restoreDefaultSettings();

				applyDefaultWidgetsValues(generalWidgetMap);
				applyDefaultWidgetsValues(trafficWidgetMap);
				applyDefaultWidgetsValues(roadWidgetMap);
			}
		});

		pnlSimActions.add(btnRestoreDefaultSettings, "cell 0 0, growx, aligny center");

		btnResetTraffic = new JButton("Reset traffic");
		btnResetTraffic.setToolTipText("Deletes all vehicles");
		btnResetTraffic.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				btnResetTraffic.setEnabled(false);

				//Save road settings, then rebuild road
				saveSettingsOfWidgetSet(trafficWidgetMap);
				//saveSettingFromGivenTab(1);
				EngineData.updateTrafficVariables();

				RoadData.toResetTraffic = true;
			}
		});

		btnResetTraffic.setEnabled(false);

		pnlSimActions.add(btnResetTraffic, "cell 0 1,growx");

		btnReBuildRoad = new JButton("ReBuild road");
		btnReBuildRoad.setToolTipText("Build a road with the latest saved settings");
		btnReBuildRoad.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (RoadData.isRebuilding.getCount() == 0)
				{
					//Save road settings, then rebuild road
					saveSettingsOfWidgetSet(generalWidgetMap);
					saveSettingsOfWidgetSet(roadWidgetMap);
					saveSettingsOfWidgetSet(trafficWidgetMap);

					RoadData.toRebuild = true;
				}
			}
		});

		btnReBuildRoad.setEnabled(false);

		pnlSimActions.add(btnReBuildRoad, "cell 0 2,growx");

		btnApplySettings = new JButton("Apply Settings");
		btnApplySettings.setToolTipText("Apply settings in all tabs");
		btnApplySettings.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveSettingsOfWidgetSet(generalWidgetMap);
				saveSettingsOfWidgetSet(graphicsWidgetMap);
				//saveSettingFromGivenTab(1);
				//saveSettingFromGivenTab(2);

				EngineData.updateGeneralVariables();

				VehiclesManager.creationReferenceTime = System.currentTimeMillis();
			}
		});

		pnlSimActions.add(btnApplySettings, "cell 0 3, growx, aligny center");

		tabbedPane.setSelectedIndex(1);

		//RIGHT SIDE OF WINDOW--------------------------------------------------------
		JPanel pnlSimOptions = new JPanel();
		pnlSimOptions.setBorder(new LineBorder(Color.LIGHT_GRAY));
		pnlSimOptions.setBounds(316, 144, 170, 60);
		mainFrame.getContentPane().add(pnlSimOptions);
		pnlSimOptions.setLayout(null);

		JLabel lblSolveMode = new JLabel("Solve mode:");
		lblSolveMode.setBounds(10, 10, 61, 14);
		pnlSimOptions.add(lblSolveMode);
		lblSolveMode.setToolTipText("Basic: how it is right now \n Advanced: oh yeah");
		lblSolveMode.setName("lblSolveMode");

		trafficWidgetMap.get(lblSolveMode.getClass().toString()).add(lblSolveMode);

		solveModeComboBox = new JComboBox();
		solveModeComboBox.setName("solveMode");
		solveModeComboBox.setBounds(80, 6, 80, 22);
		solveModeComboBox.setModel(new DefaultComboBoxModel(new String[] { "I like traffic", "Basic", "Advanced" }));
		solveModeComboBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				GuiActions.updateLabelColor(solveModeComboBox, lblSolveMode);
			}
		});
		trafficWidgetMap.get(solveModeComboBox.getClass().toString()).add(solveModeComboBox);
		pnlSimOptions.add(solveModeComboBox);

		//Direction gizmo
		JCheckBox chckbxShowDirectionsGizmo = new JCheckBox("Show Directions Gizmo");
		chckbxShowDirectionsGizmo.setToolTipText("Show Directions Gizmo");
		chckbxShowDirectionsGizmo.setName("dirGizmo");
		chckbxShowDirectionsGizmo.setBounds(10, 31, 139, 23);
		chckbxShowDirectionsGizmo.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if (chckbxShowDirectionsGizmo.isSelected())
				{
					EngineData.directionsGizmo.setToRender(true);
				}
				else
				{
					EngineData.directionsGizmo.setToRender(false);
				}
			}
		});
		pnlSimOptions.add(chckbxShowDirectionsGizmo);

		JPanel pnlSimPlayStop = new JPanel();
		pnlSimPlayStop.setBorder(new LineBorder(Color.LIGHT_GRAY));
		pnlSimPlayStop.setBounds(320, 217, 100, 60);
		mainFrame.getContentPane().add(pnlSimPlayStop);
		pnlSimPlayStop.setLayout(null);

		JLabel lblSimulationState = new JLabel("Simulation state:");
		lblSimulationState.setBounds(8, 5, 80, 14);
		pnlSimPlayStop.add(lblSimulationState);

		//Simulation start/stop button
		JLabel lblStopped = new JLabel("Paused");
		lblStopped.setBounds(52, 32, 42, 14);
		pnlSimPlayStop.add(lblStopped);

		simButton = new JButton("");
		simButton.setBounds(8, 24, 30, 30);
		simButton.setToolTipText("Run or Pause the Simulation");
		simButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		simButton.setIcon(playSim);
		simButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Executor.runSimulation = !Executor.runSimulation;

				if (Executor.runSimulation)
				{
					simButton.setIcon(stopSim);
					lblStopped.setText("Running");

					if (RoadData.isRebuilding.getCount() == 0)
					{
						btnResetTraffic.setEnabled(true);
						btnReBuildRoad.setEnabled(true);
						btnApplySettings.setEnabled(true);

						//VehiclesManager.creationReferenceTime = System.currentTimeMillis();//TODO ci va o è roba legacy?
					}
				}
				else
				{
					simButton.setIcon(playSim);
					lblStopped.setText("Paused");

					btnResetTraffic.setEnabled(false);
					btnReBuildRoad.setEnabled(false);
				}
			}
		});

		pnlSimPlayStop.add(simButton);

		//--------------------------------------------------------

		setSlidersMinMax(generalWidgetMap);
		setSlidersMinMax(trafficWidgetMap);
		setSlidersMinMax(roadWidgetMap);

		textField_0.addActionListener(new CustomActionLister(textField_0, slider_0, slider_0.getMinimum(), slider_0.getMaximum()));
		textField_0.addFocusListener(new CustomFocusListener(textField_0, slider_0, slider_0.getMinimum(), slider_0.getMaximum()));

		textField_1.addActionListener(new CustomActionLister(textField_1, slider_1, slider_1.getMinimum(), slider_1.getMaximum()));
		textField_1.addFocusListener(new CustomFocusListener(textField_1, slider_1, slider_1.getMinimum(), slider_1.getMaximum()));

		textField_2.addActionListener(new CustomActionLister(textField_2, slider_2, 0f, 1f));
		textField_2.addFocusListener(new CustomFocusListener(textField_2, slider_2, 0f, 1f));

		textField_3.addActionListener(new CustomActionLister(textField_3, slider_3, 0f, 1f));
		textField_3.addFocusListener(new CustomFocusListener(textField_3, slider_3, 0f, 1f));

		textField_4.addActionListener(new CustomActionLister(textField_4, slider_4, 0f, 1f));
		textField_4.addFocusListener(new CustomFocusListener(textField_4, slider_4, 0f, 1f));

		textField_5.addActionListener(new CustomActionLister(textField_5, slider_5, slider_5.getMinimum(), slider_5.getMaximum()));
		textField_5.addFocusListener(new CustomFocusListener(textField_5, slider_5, slider_5.getMinimum(), slider_5.getMaximum()));

		textField_6.addActionListener(new CustomActionLister(textField_6, slider_6, slider_6.getMinimum(), slider_6.getMaximum()));
		textField_6.addFocusListener(new CustomFocusListener(textField_6, slider_6, slider_6.getMinimum(), slider_6.getMaximum()));

		textField_7.addActionListener(new CustomActionLister(textField_7, slider_7, 0f, 0.5f));
		textField_7.addFocusListener(new CustomFocusListener(textField_7, slider_7, 0f, 0.5f));

		textField_8.addActionListener(new CustomActionLister(textField_8, slider_8, slider_8.getMinimum(), slider_8.getMaximum()));
		textField_8.addFocusListener(new CustomFocusListener(textField_8, slider_8, slider_8.getMinimum(), slider_8.getMaximum()));

		renderingLED = new JLabel("R");
		renderingLED.setOpaque(true);
		renderingLED.setFont(new Font("Tahoma", Font.PLAIN, 13));
		renderingLED.setForeground(Color.BLACK);
		renderingLED.setToolTipText("If flashing, TS is rendering, otherwise app has crashed :((");
		renderingLED.setHorizontalAlignment(SwingConstants.CENTER);
		renderingLED.setBounds(447, 225, 20, 20);
		renderingLED.setBackground(Color.RED);
		mainFrame.getContentPane().add(renderingLED);

		fps = new JLabel("- fps");
		fps.setHorizontalAlignment(SwingConstants.CENTER);
		fps.setBounds(435, 256, 45, 15);
		mainFrame.getContentPane().add(fps);

		applyDefaultWidgetsValues(generalWidgetMap);
		applyDefaultWidgetsValues(trafficWidgetMap);
		applyDefaultWidgetsValues(roadWidgetMap);
	}

	public static void changeState()
	{
		if (cont == 15)
		{
			if (renderingLED.getBackground().equals(GuiActions.redColor)) renderingLED.setBackground(GuiActions.greyColor);
			else if (renderingLED.getBackground().equals(GuiActions.greyColor)) renderingLED.setBackground(GuiActions.redColor);

			cont = 0;
		}
		else
		{
			cont++;
		}
	}

	private static void setSlidersMinMax(HashMap<String, ArrayList<Object>> map)
	{
		JSlider slider = null;
		Object[] values = null;

		for (Object widget : (ArrayList<Object>) map.get("class javax.swing.JSlider"))
		{
			slider = ((JSlider) widget);
			values = EngineData.currentSettings.get(((JSlider) widget).getName().substring(2));

			if (slider.getName().startsWith("F"))
			{
				slider.setMinimum(0);
				slider.setMaximum(100);
			}
			else
			{
				slider.setMinimum((int) values[1]);
				slider.setMaximum((int) values[2]);
			}
		}
	}

	public static void applyDefaultWidgetsValues(HashMap<String, ArrayList<Object>> map)
	{//Applies default settings to given set of GUI's widgets

		HashMap<String, Object[]> tempMapReference = EngineData.defaultSettings;//Easier to read
		Object[] values = null;

		for (String widgetClass : map.keySet())
			for (Object widget : (ArrayList<Object>) map.get(widgetClass))
			{
				if (widgetClass.equals("class javax.swing.JTextField"))
				{
					if (((JTextField) widget).getName().startsWith("F"))
					{
						((JTextField) widget).setText(Float.toString((float) tempMapReference.get(((JTextField) widget).getName().substring(2))[0]));
					}
					else if (((JTextField) widget).getName().startsWith("I"))
					{
						((JTextField) widget).setText(Integer.toString((int) tempMapReference.get(((JTextField) widget).getName().substring(2))[0]));
					}
				}
				else if (widgetClass.equals("class javax.swing.JSlider"))
				{
					values = tempMapReference.get(((JSlider) widget).getName().substring(2));

					if (((JSlider) widget).getName().startsWith("F"))
					{
						((JSlider) widget).setValue((int) ((((JSlider) widget).getMaximum() * (float) (values[0])) / (float) values[2]));
					}
					else if (((JSlider) widget).getName().startsWith("I"))
					{
						((JSlider) widget).setValue((int) ((((JSlider) widget).getMaximum() * (int) (values[0])) / (int) values[2]));
					}
				}
				else if (widgetClass.equals("class javax.swing.JComboBox"))
				{
					((JComboBox<?>) widget).setSelectedIndex((int) tempMapReference.get(((JComboBox<?>) widget).getName())[0]);
				}
				else if (widgetClass.equals("class javax.swing.JCheckBox"))
				{
					((JCheckBox) widget).setSelected((boolean) tempMapReference.get(((JCheckBox) widget).getName())[0]);

					((JCheckBox) widget).setForeground(GuiActions.blackColor);
				}
			}
	}

	public static void saveSettingsOfWidgetSet(HashMap<String, ArrayList<Object>> map)
	{//If widget values has changed saves the new value

		HashMap<String, Object[]> tempMapReference = EngineData.currentSettings;

		//Save values
		for (String widgetClass : map.keySet())
		{

			for (Object wdjt : map.get(widgetClass))
			{
				if (wdjt instanceof JLabel)
				{

					((JLabel) wdjt).setForeground(GuiActions.blackColor);
				}
				else if (wdjt instanceof JTextField)
				{
					JTextField comp = (JTextField) wdjt;
					/*
					if ((int) tempMapReference.get(comp.getName()) != Integer.parseInt(((JTextField) comp).getText()))
					{
						tempMapReference.put(comp.getName(), Integer.parseInt(((JTextField) comp).getText()));
					}*/

					if (comp.getName().startsWith("I"))
					{//Integer value

						if ((int) tempMapReference.get(comp.getName().substring(2))[0] != Integer.parseInt(comp.getText()))
						{
							tempMapReference.get(comp.getName().substring(2))[0] = Integer.parseInt(comp.getText());
						}
					}
					else if (comp.getName().startsWith("F"))
					{//Float value
						if ((float) tempMapReference.get(comp.getName().substring(2))[0] != Float.parseFloat(comp.getText()))
						{
							tempMapReference.get(comp.getName().substring(2))[0] = Float.parseFloat(comp.getText());
						}
					}
				}
				else if (wdjt instanceof JCheckBox)
				{
					JCheckBox comp = (JCheckBox) wdjt;

					if (((boolean) tempMapReference.get(comp.getName())[0]) != comp.isSelected())
					{
						tempMapReference.get(comp.getName())[0] = comp.isSelected();
					}

					comp.setForeground(GuiActions.blackColor);
				}
				else if (wdjt instanceof JComboBox)
				{
					JComboBox comp = (JComboBox) wdjt;

					if (((int) (tempMapReference.get(comp.getName())[0])) != comp.getSelectedIndex())
					{//Window resolution, AA samples or solveMode, record the index not the value
						tempMapReference.get(comp.getName())[0] = comp.getSelectedIndex();
					}

					comp.setForeground(GuiActions.blackColor);
				}
			}
		}
	}

	private static void setAASamples(JCheckBox checkBox, JComboBox<?> comboBox)
	{
		if (DisplayManager.availableAASamples.size() != 0)
		{
			comboBox.setModel(new DefaultComboBoxModel(DisplayManager.availableAASamples.toArray(new String[DisplayManager.availableAASamples.size()])));
		}
		else
		{
			checkBox.setEnabled(false);
			comboBox.setEnabled(false);
		}
	}

	private static HashMap<Class<?>, ArrayList<Component>> findAllChildren(JComponent component)
	{//https://stackoverflow.com/questions/17421869/how-to-get-the-name-of-textarea-inside-a-jtabbedpane
		//Returns all widgets that have a name, because I have set a name only on widgets that must be checked when saving settings (or label to color them black when saving settings)

		HashMap<Class<?>, ArrayList<Component>> ChildComponents = new HashMap<Class<?>, ArrayList<Component>>();

		ChildComponents.put(JLabel.class, new ArrayList<Component>());
		ChildComponents.put(JTextField.class, new ArrayList<Component>());
		ChildComponents.put(JCheckBox.class, new ArrayList<Component>());
		ChildComponents.put(JComboBox.class, new ArrayList<Component>());

		for (Component comp : component.getComponents())
		{
			if (comp.getName() != null)
			{
				if (comp instanceof JLabel)
				{
					ChildComponents.get(JLabel.class).add(comp);
				}
				else if (comp instanceof JTextField)
				{
					ChildComponents.get(JTextField.class).add(comp);
				}
				else if (comp instanceof JCheckBox)
				{
					ChildComponents.get(JCheckBox.class).add(comp);
				}
				else if (comp instanceof JComboBox)
				{
					ChildComponents.get(JComboBox.class).add(comp);
				}
			}
		}

		return ChildComponents;
	}

	private String[] getScreenResolutions()
	{
		String[] avaibleModes = new String[DisplayManager.fullScreenModes.size()];
		int cont = 0;

		for (DisplayMode mode : DisplayManager.fullScreenModes)
		{
			avaibleModes[cont] = mode.toString();

			cont++;
		}

		return avaibleModes;
	}

	public void setVisible(boolean isVisible)
	{
		mainFrame.setVisible(isVisible);
	}
}