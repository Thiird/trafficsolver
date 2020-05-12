package Display;

import java.util.ArrayList;

import javax.swing.UnsupportedLookAndFeelException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import Guis.Gui_ControlPanel;
import Guis.Gui_MainWindow;

public class DisplayManager
{
	public static String appVersion = "1.1";
	private static Clock clock;
	private static int fps = 0;
	private static double referenceTime = 0; //In seconds
	private static double elapsedTime = 0; //In seconds

	private static int width = 640;
	private static int height = 480;
	private static final int FPS_CAP = 60;
	private static final ContextAttribs attribs = new ContextAttribs(3, 3).withForwardCompatible(true).withProfileCore(true); //TODO OpenGL 3.3? maybe leave that for compatibility
	public static ArrayList<DisplayMode> fullScreenModes = new ArrayList<DisplayMode>();
	public static ArrayList<String> availableAASamples;

	public static int aASamplesIndex = 0;
	public static int currentDisplayModeIndex = 0;
	public static int maxAASamples = -1;
	public static boolean isVSyncAvailable = false;
	public static int currentAASamples = 8;

	private static Gui_MainWindow window;

	public DisplayManager() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, LWJGLException
	{
		getContextCapabilities();
		loadFullScreenDisplayModes();
		initDefaultDisplayMode();
		loadAASamples();

		window = new Gui_MainWindow();

		window.setWidthHeight(width, height);

		Display.setParent(window.getCanvas());

		//Set parent windows before creating actual opengl window, otherwise little glitch
		initGLDisplay();

		Display.setFullscreen(false);

		//Render in the whole display
		GL11.glViewport(0, 0, width, height);

		clock = new Clock();
		referenceTime = clock.getFrameTimeSeconds();
	}

	private static void initDefaultDisplayMode()
	{//Takes the first available display mode that supports full screen, and uses it

		String defaultDMode = fullScreenModes.get(0).toString();

		width = Integer.parseInt(defaultDMode.split("x")[0].replaceAll(" ", ""));
		height = Integer.parseInt(defaultDMode.split("x")[1].replaceAll(" ", ""));
	}

	private static void initGLDisplay()
	{
		try
		{
			Display.create(new PixelFormat(8, 8, 0, currentAASamples).withSamples(currentAASamples).withDepthBits(16), attribs);

			Display.setDisplayMode(fullScreenModes.get(currentDisplayModeIndex));

			Display.setTitle("[Traffic Solver - ©Stefano Nicolis]");
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
	}

	public static void setDisplayMode(int width, int height, boolean fullscreen)
	{

		// return if requested DisplayMode is already set
		if ((Display.getDisplayMode().getWidth() == width) && (Display.getDisplayMode().getHeight() == height) && (Display.isFullscreen() == fullscreen))
		{
			return;
		}

		try
		{
			DisplayMode targetDisplayMode = null;

			if (fullscreen)
			{
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;

				for (int i = 0; i < modes.length; i++)
				{
					DisplayMode current = modes[i];

					if ((current.getWidth() == width) && (current.getHeight() == height))
					{
						if ((targetDisplayMode == null) || (current.getFrequency() >= freq))
						{
							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel()))
							{
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against the 
						// original display mode then it's probably best to go for this one
						// since it's most likely compatible with the monitor
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) && (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency()))
						{
							targetDisplayMode = current;
							break;
						}
					}
				}
			}
			else
			{
				targetDisplayMode = new DisplayMode(width, height);
			}

			if (targetDisplayMode == null)
			{
				//System.out.println("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);

		}
		catch (LWJGLException e)
		{
			//System.out.println("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
		}
	}

	public static void updateWindowResolution()
	{
		currentDisplayModeIndex = Gui_ControlPanel.windowComboBox.getSelectedIndex();

		String newDMode = fullScreenModes.get(currentDisplayModeIndex).toString();

		width = Integer.parseInt(newDMode.split("x")[0].replaceAll(" ", ""));
		height = Integer.parseInt(newDMode.split("x")[1].replaceAll(" ", ""));

		GL11.glViewport(0, 0, width, height);

		window.getCanvas().setSize(width, height);
		Gui_MainWindow.mainFrame.setSize(width, height);
		Gui_MainWindow.mainFrame.setLocationRelativeTo(null);

		try
		{
			Display.setDisplayMode(fullScreenModes.get(currentDisplayModeIndex));
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}

		Display.update();//Otherwise freeze
	}

	public void loadAASamples()
	{
		availableAASamples = new ArrayList<String>();

		if (maxAASamples != -1)
		{
			int tempSamples = 1;

			while (true)
			{
				if (tempSamples <= maxAASamples)
				{
					availableAASamples.add(Integer.toString(tempSamples));

					tempSamples *= 2;
				}
				else
				{
					break;
				}
			}
		}
	}

	private static void getContextCapabilities()
	{//http://forum.lwjgl.org/index.php?topic=4078.0  ---  last post
		//Create a dummy context to check if system supports AA

		PixelFormat format = new PixelFormat(32, 0, 24, 8, 0);

		try
		{
			Pbuffer pb = new Pbuffer(width, height, format, null);

			pb.makeCurrent();

			//Check for Anti-Aliasing
			if (GLContext.getCapabilities().GL_ARB_multisample)
			{
				DisplayManager.maxAASamples = GL11.glGetInteger(GL30.GL_MAX_SAMPLES);
			}

			pb.destroy();
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
	}

	public static void update()
	{
		Display.sync(FPS_CAP);

		Display.update();

		clock.update();

		updateName();
	}

	private void loadFullScreenDisplayModes()
	{
		DisplayMode[] modes = null;

		try
		{
			modes = Display.getAvailableDisplayModes();
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}

		for (int i = 0; i < modes.length; i++)
		{
			if (modes[i].isFullscreenCapable())
			{
				fullScreenModes.add(modes[i]);
			}
		}
	}

	private static int updateName()
	{
		//If 1s has passed, update fps count on display title
		elapsedTime += clock.getFrameTimeSeconds();

		if (elapsedTime - referenceTime >= 1)
		{
			Gui_ControlPanel.fps.setText(fps + " fps");

			//Reset fps count
			fps = 0;

			referenceTime = clock.getFrameTimeSeconds();

			elapsedTime = 0f;
		}
		else
		{
			fps++;
		}

		return fps;
	}

	public static void closeDisplay()
	{
		Display.destroy();
	}

	public static int getWindowWidth()
	{
		return width;
	}

	public static int getWindowHeight()
	{
		return height;
	}
}