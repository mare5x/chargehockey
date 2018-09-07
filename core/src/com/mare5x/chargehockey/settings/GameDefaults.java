package com.mare5x.chargehockey.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

/* Defines shared default values to be used by other parts of the game. */
public class GameDefaults {
    public static float MIN_DIMENSION = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    public static float DENSITY = Gdx.graphics.getDensity();
    public static final float FONT_SIZE = 24;  // dp units
    public static final float IMAGE_FONT_SIZE = DENSITY * FONT_SIZE * 1.5f;  // in-line image with text

    // currently image based Buttons have a fixed size and do not scale
    // use ImageButtons instead if you want them to scale

    public static float MIN_BUTTON_SIZE = 50 * DENSITY;  // 63 * DPI == 1 cm
    public static float IMAGE_BUTTON_SIZE = MIN_DIMENSION * 0.125f;
    public static float MIN_BUTTON_HEIGHT = MIN_DIMENSION * 0.1f;
    public static float MAX_BUTTON_WIDTH = MIN_DIMENSION * 0.6f;
    public static float ACTOR_PAD = MIN_DIMENSION * 0.01f;
    public static float CELL_PAD = MIN_DIMENSION * 0.015f;

    public static final String CHARGE_ZONE_BG = "pixels/px_grey_opaque";
    public static final String CHARGE_ZONE_ACTIVE_BG = "pixels/px_darkgrey_opaque";
    // NOTE: Screen coordinates' origin is at the top left corner of the screen! (x - right, y - down)
    // the height of the rectangle at the bottom of the screen from where charges are added and to
    // where they must be dragged to remove them
    public static final float CHARGE_ZONE_HEIGHT = IMAGE_BUTTON_SIZE * 1.5f;  // in px
    // Defines the area of where charges are dragged to get removed (the bottom of the screen).
    public static Rectangle CHARGE_ZONE_RECT = new Rectangle();  // in screen coordinates (px)
    // Defines the area outside of which charge dragging or tile painting moves the camera.
    public static Rectangle CHARGE_DRAG_RECT = new Rectangle();  // in screen coordinates (px)
    private static float CHARGE_DRAG_EDGE = CHARGE_ZONE_HEIGHT / 2;  // in px
    public static final float CHARGE_DRAG_SPEED = 15.0f;

    public static final long SECONDS_TO_NANOS = 1000000000L;

    public static final float PHYSICS_EPSILON = 0.001f;

    public static void resize(int screen_width, int screen_height) {
        // for now we only care about the top y position
        // make the width and height larger in case the charge is dragged 'off' the screen
        CHARGE_ZONE_RECT.x = -screen_width;
        CHARGE_ZONE_RECT.y = screen_height - CHARGE_ZONE_HEIGHT;
        CHARGE_ZONE_RECT.width = screen_width * 3;
        CHARGE_ZONE_RECT.height = CHARGE_ZONE_HEIGHT * 2;

        CHARGE_DRAG_RECT.x = CHARGE_DRAG_EDGE;
        CHARGE_DRAG_RECT.y = CHARGE_DRAG_EDGE;
        CHARGE_DRAG_RECT.width = screen_width - 2 * CHARGE_DRAG_EDGE;
        CHARGE_DRAG_RECT.height = screen_height - CHARGE_DRAG_EDGE - CHARGE_DRAG_EDGE / 2;
    }
}
