package Guis;

import java.awt.Font;
import java.awt.Window.Type;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Gui_KeyboardShortcuts
{
	public JFrame mainFrame;
	private JLabel themShortcuts;

	public Gui_KeyboardShortcuts()
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
		this.mainFrame.setTitle("Keyboard Shortcuts");
		this.mainFrame.setType(Type.UTILITY);
		this.mainFrame.setBounds(100, 100, 370, 180);
		this.mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.mainFrame.getContentPane().setLayout(null);
		this.mainFrame.setLocationRelativeTo(null);

		themShortcuts = new JLabel(
				"<html>\r\nQ: With simulation not running, compute a single step<br>\r\nA: Spawn a Vehicle on a random route (if there is space)<br>\r\nS: With a Vehicle selected, block it/release it<br>\r\nR: With simulation running, Rebuild Road<br>\r\nT: With simulation running: reset Traffic<br>\r\nC: Reset camera position to last saved one<br>\r\nF: Save current camera position<br>\r\nP: Pause/Run simulation<br>\r\n<html>");
		themShortcuts.setFont(new Font("Tahoma", Font.PLAIN, 13));
		themShortcuts.setVerticalAlignment(SwingConstants.TOP);
		themShortcuts.setBounds(10, 11, 344, 129);
		mainFrame.getContentPane().add(themShortcuts);
	}

	public void setVisible(boolean isVisible)
	{
		this.mainFrame.setVisible(isVisible);
	}
}