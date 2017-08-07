package com.mare5x.chargehockey.settings;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.mare5x.chargehockey.GameLogic;
import com.mare5x.chargehockey.LevelFrameBuffer;
import com.mare5x.chargehockey.PuckActor;


public class SettingsFile {
    public enum SETTINGS_KEY {
        SHOW_VELOCITY_VECTOR,
        SHOW_ACCELERATION_VECTOR,
        SHOW_FORCE_VECTORS,
        GAME_SPEED,
        TRACE_PATH,
        GRID_LINES
    }

    private static final String FILE_NAME = "mare5x.chargehockey.settings";

    private final Preferences prefs;

    public SettingsFile() {
        prefs = Gdx.app.getPreferences(FILE_NAME);
    }

    // Initialize the preferences file with the default key, value pairs.
    // NOTE: if the keys already exist, does nothing!
    public static void initialize() {
        Preferences prefs = Gdx.app.getPreferences(FILE_NAME);
        if (prefs.contains(SETTINGS_KEY.values()[0].name()))
            return;
        prefs.putBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR.name(), false);
        prefs.putBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR.name(), false);
        prefs.putBoolean(SETTINGS_KEY.SHOW_FORCE_VECTORS.name(), true);
        prefs.putFloat(SETTINGS_KEY.GAME_SPEED.name(), 1f);
        prefs.putBoolean(SETTINGS_KEY.TRACE_PATH.name(), true);
        prefs.putBoolean(SETTINGS_KEY.GRID_LINES.name(), true);
        prefs.flush();
    }

    public boolean getBoolean(SETTINGS_KEY key) {
        return prefs.getBoolean(key.name());
    }

    public float getFloat(SETTINGS_KEY key) {
        return prefs.getFloat(key.name());
    }

    public void put(SETTINGS_KEY key, boolean val) {
        prefs.putBoolean(key.name(), val);
    }

    public void put(SETTINGS_KEY key, float val) {
        prefs.putFloat(key.name(), val);
    }

    public void save() {
        prefs.flush();
    }

    /** Convenience method to set a single setting and apply the changes. */
    public static void set_setting(SETTINGS_KEY key, boolean val){
        SettingsFile settings = new SettingsFile();
        settings.put(key, val);
        settings.save();
        apply_global_settings(settings);
    }

    /** Convenience method to set a single setting and apply the changes. */
    public static void set_setting(SETTINGS_KEY key, float val){
        SettingsFile settings = new SettingsFile();
        settings.put(key, val);
        settings.save();
        apply_global_settings(settings);
    }

    /** Applies static/global-level settings. */
    public static void apply_global_settings() {
        SettingsFile settings = new SettingsFile();
        apply_global_settings(settings);
    }

    public static void apply_global_settings(SettingsFile settings) {
        PuckActor.set_draw_velocity(settings.getBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        PuckActor.set_draw_acceleration(settings.getBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        PuckActor.set_draw_forces(settings.getBoolean(SETTINGS_KEY.SHOW_FORCE_VECTORS));
        PuckActor.set_trace_path(settings.getBoolean(SETTINGS_KEY.TRACE_PATH));
        GameLogic.set_game_speed(settings.getFloat(SETTINGS_KEY.GAME_SPEED));
        LevelFrameBuffer.set_grid_lines_setting(settings.getBoolean(SETTINGS_KEY.GRID_LINES));
    }
}