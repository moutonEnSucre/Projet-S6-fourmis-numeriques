package openGL.renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;

public class DisplayManager {
	
	private static final int WIDTH = 1920/2;
	private static final int HEIGHT = 800;
	private static final int FPS_CAP = 240;

	private static long lastFrameTime;
	private static float delta; //Frame time in seconds
	private static long deltaMS; //Frame time in milliseconds

	private static long intervalTimer;

	/**
	 * Create a new windows and prepare it to be used as rendering target
	 */
	public static void createDisplay(){		
		ContextAttribs attribs = new ContextAttribs(3,2)
		.withForwardCompatible(true)
		.withProfileCore(true);
		
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
			Display.create(new PixelFormat().withSamples(8), attribs);
			Display.setTitle("Projet APO, Demo moteur graphique");
			GL11.glEnable(GL13.GL_MULTISAMPLE);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		GL11.glViewport(0,0, WIDTH, HEIGHT);
		lastFrameTime = getCurrentTime();
		intervalTimer = getCurrentTime();
		Display.setLocation(WIDTH, 0);
	}

	/**
	 * Draw the GPU's FrameBuffer to the screen
	 */
	public static void updateDisplay(){
		Display.sync(FPS_CAP);
		Display.update();
		long currentTime = getCurrentTime();
		deltaMS = currentTime - lastFrameTime;
		delta = (float)(deltaMS)/ 1000.0f;
		lastFrameTime = getCurrentTime();
		Display.setTitle("Projet APO, Demo moteur graphique       FPS : " + (int)(1f/delta));
	}

	/**
	 * return the last frame duration in seconds
	 * @return last frame duration in seconds
	 */
	public static float getFrameTimeSeconds() {
		return delta;
	}

	/**
	 * return the last frame duration in seconds
	 * @return last frame duration in seconds
	 */
	public static float getFrameTimeMS() {
		return deltaMS;
	}


	/**
	 * Close the display
	 */
	public static void closeDisplay(){
		Display.destroy();
	}

	/**
	 * Return the current time in milliseconds
	 * @return current time in milliseconds
	 */
	public static long getCurrentTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}

	/**
	 * return whether or not a variable interval has passed since the last call of this function
	 * @param ms the interval size in milliseconds
	 * @return has an interval passed
	 */
	public static boolean intervalHasPassed(int ms) {
		if (getCurrentTime() - intervalTimer > ms) {
			intervalTimer = getCurrentTime();
			return true;
		}
		return false;
	}

	/**
	 * Return the time elapsed since the last call to intervalHasPassed(int ms)
	 * @return time passed since last intervalHasPassed(int ms) call
	 */
	public static long timeSinceLastInterval() {
		return getCurrentTime() - intervalTimer;
	}
}
