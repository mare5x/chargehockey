package com.mare5x.chargehockey.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mare5x.chargehockey.ChargeHockeyGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 450;
        config.height = 800;
		new LwjglApplication(new ChargeHockeyGame(), config);
	}
}
