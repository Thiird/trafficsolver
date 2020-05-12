package Guis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window.Type;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

public class Gui_ExecutionVisualization
{
	public static JFrame mainFrame;
	private static JTextField txtRoadPhase0, txtRoadPhase1, txtRoadPhase2, txtRoadPhase3, txtRoadPhase4, txtRoadPhase5, txtRoadPhase6, txtRoadSubPhase0, txtRoadSubPhase1, txtRoadSubPhase2;

	public Gui_ExecutionVisualization()
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
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setResizable(false);
		mainFrame.setType(Type.UTILITY);
		mainFrame.setTitle("Execution Visualization");
		mainFrame.setBounds(100, 100, 350, 480);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(null);

		JPanel SimExecution = new JPanel();
		SimExecution.setBorder(new LineBorder(Color.LIGHT_GRAY));
		SimExecution.setBounds(10, 11, 301, 60);
		SimExecution.setLayout(null);
		mainFrame.getContentPane().add(SimExecution);

		JLabel lblRoadPhase = new JLabel("Phase:");
		lblRoadPhase.setLocation(14, 6);
		lblRoadPhase.setSize(34, 20);
		SimExecution.add(lblRoadPhase);

		//Road Phase
		txtRoadPhase0 = new JTextField();
		SimExecution.add(txtRoadPhase0);
		txtRoadPhase0.setBounds(62, 6, 20, 20);
		txtRoadPhase0.setText("0");
		txtRoadPhase0.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadPhase0.setColumns(10);
		txtRoadPhase0.setEditable(false);

		txtRoadPhase1 = new JTextField();
		SimExecution.add(txtRoadPhase1);
		txtRoadPhase1.setBounds(96, 6, 20, 20);
		txtRoadPhase1.setText("1");
		txtRoadPhase1.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadPhase1.setColumns(10);
		txtRoadPhase1.setEditable(false);

		txtRoadPhase2 = new JTextField();
		SimExecution.add(txtRoadPhase2);
		txtRoadPhase2.setBounds(130, 6, 20, 20);
		txtRoadPhase2.setText("2");
		txtRoadPhase2.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadPhase2.setColumns(10);
		txtRoadPhase2.setEditable(false);
		txtRoadPhase3 = new JTextField();
		SimExecution.add(txtRoadPhase3);
		txtRoadPhase3.setBounds(164, 6, 20, 20);
		txtRoadPhase3.setText("3");
		txtRoadPhase3.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadPhase3.setColumns(10);
		txtRoadPhase3.setEditable(false);

		txtRoadPhase4 = new JTextField();
		txtRoadPhase4.setBounds(198, 6, 20, 20);
		SimExecution.add(txtRoadPhase4);
		txtRoadPhase4.setText("4");
		txtRoadPhase4.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadPhase4.setColumns(10);
		txtRoadPhase4.setEditable(false);

		txtRoadPhase5 = new JTextField();
		txtRoadPhase5.setBounds(232, 6, 20, 20);
		SimExecution.add(txtRoadPhase5);
		txtRoadPhase5.setText("5");
		txtRoadPhase5.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadPhase5.setColumns(10);
		txtRoadPhase5.setEditable(false);

		txtRoadPhase6 = new JTextField();
		txtRoadPhase6.setBounds(266, 6, 20, 20);
		SimExecution.add(txtRoadPhase6);
		txtRoadPhase6.setText("6");
		txtRoadPhase6.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadPhase6.setEditable(false);
		txtRoadPhase6.setColumns(10);

		//Road Sub phase
		JLabel lblSubRoadPhase = new JLabel("SubPhase:");
		lblSubRoadPhase.setLocation(8, 104);
		lblSubRoadPhase.setSize(42, 20);
		lblSubRoadPhase.setBounds(6, 35, 51, 14);
		SimExecution.add(lblSubRoadPhase);

		txtRoadSubPhase0 = new JTextField();
		txtRoadSubPhase0.setBounds(62, 32, 20, 20);
		SimExecution.add(txtRoadSubPhase0);
		txtRoadSubPhase0.setText("0");
		txtRoadSubPhase0.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadSubPhase0.setColumns(10);
		txtRoadSubPhase0.setEditable(false);

		txtRoadSubPhase1 = new JTextField();
		txtRoadSubPhase1.setBounds(96, 32, 20, 20);
		SimExecution.add(txtRoadSubPhase1);
		txtRoadSubPhase1.setText("1");
		txtRoadSubPhase1.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadSubPhase1.setColumns(10);
		txtRoadSubPhase1.setEditable(false);

		txtRoadSubPhase2 = new JTextField();
		txtRoadSubPhase2.setBounds(130, 32, 20, 20);
		SimExecution.add(txtRoadSubPhase2);
		txtRoadSubPhase2.setText("2");
		txtRoadSubPhase2.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoadSubPhase2.setColumns(10);
		txtRoadSubPhase2.setEditable(false);

		JLabel lblPhasesDescription = new JLabel(
				"<html>\r\nTraffic Solver is structed like a simulation, it divides\r\nthe computation in atomic passes, called steps.<br>\r\nEach step is computed separately from the next or the previous.<br>\r\n\r\nThe vehicle movement illusion is given by the fast\r\ncomputation of multiple steps in series.<br>\r\n\r\nEach step is divided in 6 Phases, called RoadPhases.<br>\r\nEach RoadPhase has 1 to 3 subPhases.<br>\r\n<br>\r\nRoadPhases are:<br>\r\n0)Ts checks for road rebuild or traffic reset requests<br>\r\n1)Intersection Managers solve the intersection<br>\r\n2)Vehicles compute next step and move<br>\r\n3)IMs check for vehicle transition on intersection<br>\r\n4)Ts checks if its time to spawn new vehicles<br>\r\n5)Ts checks for vehicles to delete<br>\r\n6)Ts checks if user has modified the RoadBlock Offset<br>\r\n<br>\r\nTo actually see the phases computing, up the Simulation Stepping settings in the 'Simulation Tab' to a value of 250.\r\n<br>\r\n<br>\r\nTS = Traffic Solver | IM = Intersection Managers\r\n<html>");
		lblPhasesDescription.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPhasesDescription.setVerticalAlignment(SwingConstants.TOP);
		lblPhasesDescription.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPhasesDescription.setBounds(10, 82, 323, 360);
		mainFrame.getContentPane().add(lblPhasesDescription);
	}

	public static void setRoadPhase(int currentStatus)
	{
		if (currentStatus >= 0 && currentStatus <= 6)
		{
			for (int i = 0; i < 77; i++)
			{
				switch (i)
				{
					case 0:
						if (i == currentStatus) txtRoadPhase0.setBackground(GuiActions.redColor);
						else txtRoadPhase0.setBackground(GuiActions.greyColor);
						break;
					case 1:
						if (i == currentStatus) txtRoadPhase1.setBackground(GuiActions.redColor);
						else txtRoadPhase1.setBackground(GuiActions.greyColor);
						break;
					case 2:
						if (i == currentStatus) txtRoadPhase2.setBackground(GuiActions.redColor);
						else txtRoadPhase2.setBackground(GuiActions.greyColor);
						break;
					case 3:
						if (i == currentStatus) txtRoadPhase3.setBackground(GuiActions.redColor);
						else txtRoadPhase3.setBackground(GuiActions.greyColor);
						break;
					case 4:
						if (i == currentStatus) txtRoadPhase4.setBackground(GuiActions.redColor);
						else txtRoadPhase4.setBackground(GuiActions.greyColor);
						break;
					case 5:
						if (i == currentStatus) txtRoadPhase5.setBackground(GuiActions.redColor);
						else txtRoadPhase5.setBackground(GuiActions.greyColor);
						break;
					case 6:
						if (i == currentStatus) txtRoadPhase6.setBackground(GuiActions.redColor);
						else txtRoadPhase6.setBackground(GuiActions.greyColor);
						break;
				}
			}
		}
	}

	public static void setRoadSubPhase(int currentStatus)
	{
		if (currentStatus >= 0 && currentStatus <= 2)
		{
			for (int i = 0; i < 4; i++)
			{
				switch (i)
				{
					case 0:
						if (i == currentStatus) txtRoadSubPhase0.setBackground(GuiActions.redColor);
						else txtRoadSubPhase0.setBackground(GuiActions.greyColor);
						break;
					case 1:
						if (i == currentStatus) txtRoadSubPhase1.setBackground(GuiActions.redColor);
						else txtRoadSubPhase1.setBackground(GuiActions.greyColor);
						break;
					case 2:
						if (i == currentStatus) txtRoadSubPhase2.setBackground(GuiActions.redColor);
						else txtRoadSubPhase2.setBackground(GuiActions.greyColor);
						break;
				}
			}
		}
	}

	public static void setVisible(boolean flag)
	{
		mainFrame.setVisible(flag);
	}
}