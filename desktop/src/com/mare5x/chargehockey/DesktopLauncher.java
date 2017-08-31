package com.mare5x.chargehockey;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 450;
        config.height = 800;

		new LwjglApplication(new DesktopChargeHockeyGame(), config);
	}
}
