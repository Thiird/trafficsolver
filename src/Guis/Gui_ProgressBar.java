package Guis;

import java.awt.Window.Type;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Gui_ProgressBar
{
	public JFrame mainFrame;
	public JProgressBar progressBar;
	public JLabel actionBeingPerformed;

	public Gui_ProgressBar()
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
		this.mainFrame = new JFrame();
		this.mainFrame.setResizable(false);
		this.mainFrame.setAlwaysOnTop(true);
		this.mainFrame.setTitle("Rebuilding Road Grid");
		this.mainFrame.setType(Type.UTILITY);
		this.mainFrame.setBounds(100, 100, 300, 100);
		this.mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.mainFrame.getContentPane().setLayout(null);
		this.mainFrame.setLocationRelativeTo(null);

		progressBar = new JProgressBar();
		progressBar.setToolTipText("<html>\r\n( . Y . )\r\n</html>");
		progressBar.setBounds(10, 43, 274, 23);
		this.mainFrame.getContentPane().add(progressBar);

		actionBeingPerformed = new JLabel("Your mom");
		actionBeingPerformed.setHorizontalAlignment(SwingConstants.CENTER);
		actionBeingPerformed.setBounds(41, 18, 212, 14);
		mainFrame.getContentPane().add(actionBeingPerformed);
	}
}