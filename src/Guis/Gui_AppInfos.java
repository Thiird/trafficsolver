package Guis;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import Display.DisplayManager;

public class Gui_AppInfos
{
	private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	public JFrame mainFrame;

	public Gui_AppInfos()
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
		this.mainFrame = new JFrame();
		this.mainFrame.setResizable(false);
		this.mainFrame.setType(Type.UTILITY);
		this.mainFrame.getContentPane().setBackground(Color.WHITE);
		this.mainFrame.setTitle("About Traffic Solver - v" + DisplayManager.appVersion);
		this.mainFrame.setBounds(100, 100, 480, 400);
		this.mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.mainFrame.getContentPane().setLayout(null);

		JLabel lblTrafficSolverID = new JLabel();
		lblTrafficSolverID.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblTrafficSolverID.setBackground(Color.WHITE);
		lblTrafficSolverID.setBounds(12, 114, 453, 76);
		lblTrafficSolverID.setText(
				"<html>Traffic Solver is a software simulation of vehicles in a road network. <br> Its purpose is to show that it is possible to implement the basic road principles in a software simulation. <br> Written in Java, with the LWJGL 2 OpenGL API. <html>");
		mainFrame.getContentPane().add(lblTrafficSolverID);

		JLabel lblappLogo = new JLabel("\"app logo\"");
		lblappLogo.setIcon(new ImageIcon(Gui_AppInfos.class.getClassLoader().getResource("img/app_logo/TS_logo.png")));

		lblappLogo.setToolTipText(
				"<html>There's a 0.00000025% chance for the misterious vehicle to spawn.<br>\r\nAre you going to catch it? You probably already missed it....<br>\r\nAnyway, like in all 3D videogames, you can cheat!<br>\r\n Send me an email entitled:</br>\r\n\"TRAFFIC SOLVER-JOB OFFER-YOU ARE AMAZING-MEMES ARE GREAT-SCIENCE4LIFE\",<br>\r\n and I will send you a screenshot of what it is.Hint in the TS logo.<br>\r\n Good luck soldier!<br>\r\n (Don't forget to clean your room)<br></html>\r\n</html>");
		lblappLogo.setBounds(11, 9, 453, 96);
		mainFrame.getContentPane().add(lblappLogo);

		JLabel lblAuthor = new JLabel();
		lblAuthor.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblAuthor.setText("Application written by Stefano Nicolis.");
		lblAuthor.setBounds(11, 278, 250, 22);
		mainFrame.getContentPane().add(lblAuthor);

		JLabel lblSteMail = new JLabel("stenicolis@gmail.com");
		lblSteMail.setToolTipText("Click to copy to clipboard");
		lblSteMail.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblSteMail.setBounds(47, 340, 136, 22);
		lblSteMail.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				clipboard.setContents(new StringSelection(lblSteMail.getText()), null);
			}
		});
		mainFrame.getContentPane().add(lblSteMail);

		JLabel lblGmailLogo = new JLabel("");
		lblGmailLogo.setToolTipText(" dont spam plz thx cya");
		lblGmailLogo.setIcon(new ImageIcon(Gui_AppInfos.class.getClassLoader().getResource("img/icons/gmail2.png")));
		lblGmailLogo.setBounds(17, 340, 20, 22);
		mainFrame.getContentPane().add(lblGmailLogo);

		JLabel lblVimeoLogo = new JLabel("");
		lblVimeoLogo.setToolTipText("Remember to like, subscribe and see you later!...oh wait, this is not youtube...");
		lblVimeoLogo.setIcon(new ImageIcon(Gui_AppInfos.class.getClassLoader().getResource("img/icons/vimeo2.png")));
		lblVimeoLogo.setBounds(17, 309, 20, 22);
		mainFrame.getContentPane().add(lblVimeoLogo);

		JLabel txtpnThanksToThinmatrix = new JLabel();
		txtpnThanksToThinmatrix.setText("Thanks to ThinMatrix for creating a YouTube series about LWJGL 2.\r\n");
		txtpnThanksToThinmatrix.setFont(new Font("Tahoma", Font.PLAIN, 15));
		txtpnThanksToThinmatrix.setBounds(12, 195, 454, 22);
		mainFrame.getContentPane().add(txtpnThanksToThinmatrix);

		JLabel lblDevTimeFrame = new JLabel();
		lblDevTimeFrame.setToolTipText("First big project");
		lblDevTimeFrame.setText("Developing time-frame was October 2017 - May 2020.");
		lblDevTimeFrame.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblDevTimeFrame.setBounds(13, 221, 360, 22);
		mainFrame.getContentPane().add(lblDevTimeFrame);

		JLabel lblAndYou = new JLabel();
		lblAndYou.setToolTipText("When they say \"and you\" at the end of the game credits :')");
		lblAndYou.setText("Thank You for checking out Traffic Solver!");
		lblAndYou.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblAndYou.setBounds(12, 249, 291, 22);
		mainFrame.getContentPane().add(lblAndYou);

		JLabel lblPresentationVideo = new JLabel();
		lblPresentationVideo.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblPresentationVideo.setText("<html><a href=''>Presentation video</a></html>");
		lblPresentationVideo.setForeground(Color.BLUE.darker());
		lblPresentationVideo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblPresentationVideo.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				try
				{
					Desktop.getDesktop().browse(new URI("https://vimeo.com/414509171"));
				}
				catch (IOException | URISyntaxException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		lblPresentationVideo.setBounds(47, 311, 123, 23);
		mainFrame.getContentPane().add(lblPresentationVideo);
	}

	public void setVisible(boolean isVisible)
	{
		this.mainFrame.setVisible(isVisible);
	}
}