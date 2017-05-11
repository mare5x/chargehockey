package com.mare5x.chargehockey;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;


enum SETTINGS_KEY {
    SHOW_VELOCITY_VECTOR, SHOW_ACCELERATION_VECTOR
}


class SettingsFile {
    private static final String FILE_NAME = "mare5x.chargehockey.settings";

    private final Preferences prefs;

    SettingsFile() {
        prefs = Gdx.app.getPreferences(FILE_NAME);
    }

    // Initialize the preferences file with the default key, value pairs.
    // NOTE: if the keys already exist, does nothing!
    static void initialize() {
        Preferences prefs = Gdx.app.getPreferences(FILE_NAME);
        if (prefs.contains(SETTINGS_KEY.values()[0].name()))
            return;
        prefs.putBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR.name(), false);
        prefs.putBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR.name(), false);
        prefs.flush();
    }

    boolean get(SETTINGS_KEY key) {
        return prefs.getBoolean(key.name(), false);
    }

    void put(SETTINGS_KEY key, boolean val) {
        prefs.putBoolean(key.name(), val);
    }

    void save() {
        prefs.flush();
    }
}
