package com.mare5x.chargehockey.settings;

import com.badlogic.gdx.Gdx;

/* Defines shared default values to be used by other parts of the game. */
public class GameDefaults {
    public static float MIN_DIMENSION = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    public static float DENSITY = Gdx.graphics.getDensity();
    public static final float FONT_SIZE = 24;  // dp units
    public static final float IMAGE_FONT_SIZE = DENSITY * FONT_SIZE * 1.5f;  // in-line image with text

    // currently image based Buttons have a fixed size and do not scale
    // use ImageButtons instead if you want them to scale

    public static float MIN_BUTTON_SIZE = 50 * DENSITY;
    public static float IMAGE_BUTTON_SIZE = MIN_DIMENSION * 0.125f;
    public static float MIN_BUTTON_HEIGHT = MIN_DIMENSION * 0.1f;
    public static float MAX_BUTTON_WIDTH = MIN_DIMENSION * 0.6f;
    public static float ACTOR_PAD = MIN_DIMENSION * 0.01f;
    public static float CELL_PAD = MIN_DIMENSION * 0.015f;

    // the height of the rectangle at the bottom of the screen from where charges are added and to
    // where they must be dragged to remove them
    public static final float CHARGE_ZONE_HEIGHT = IMAGE_BUTTON_SIZE * 1.5f;  // in px
    public static float CHARGE_ZONE_PERCENT_HEIGHT = CHARGE_ZONE_HEIGHT / Gdx.graphics.getHeight(); // 0.15f;
    public static final String CHARGE_ZONE_BG = "pixels/px_grey_opaque";
    public static final String CHARGE_ZONE_ACTIVE_BG = "pixels/px_darkgrey_opaque";

    public static void resize(int screen_width, int screen_height) {
        CHARGE_ZONE_PERCENT_HEIGHT = CHARGE_ZONE_HEIGHT / screen_height;
    }
}
