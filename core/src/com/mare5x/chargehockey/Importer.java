package com.mare5x.chargehockey;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import java.util.Locale;


/** Class that handles importing custom levels. */
class Importer {
    private final ChargeHockeyGame game;
    private final Stage stage;

    private TextNotification notification = null;

    private static final FilePicker.FileFilter import_filter = new FilePicker.FileFilter() {
        @Override
        public boolean is_valid(FileHandle path) {
            return path.isDirectory() || path.extension().equals("grid") || path.extension().equals("save");
        }
    };

    Importer(ChargeHockeyGame game, Stage stage) {
        this.game = game;
        this.stage = stage;
    }

    static FilePicker.FileFilter get_filter() {
        return import_filter;
    }

    void handle_import(FileHandle path) {
        if (path.isDirectory()) {
            Array<String> import_list = new Array<String>();
            for (FileHandle child : path.list()) {
                if (import_level(child) || import_dir(child)) {
                    import_list.add(child.nameWithoutExtension());
                }
            }
            if (import_list.size > 0) {
                show_notification(String.format(Locale.US, "IMPORTED %s", import_list.toString(", ")));
            } else {
                show_notification("YOU MUST PICK A VALID IMPORT LOCATION");
            }
        } else if (path.extension().equals("grid")) {
            String name = path.nameWithoutExtension();
            path.copyTo(LevelSelector.get_level_grid_fhandle(LEVEL_TYPE.CUSTOM, name));
            show_notification(String.format(Locale.US, "IMPORTED %s", name));
        } else if (path.extension().equals("save")) {
            if (import_save(path)) {
                show_notification(String.format(Locale.US, "IMPORTED %s", path.nameWithoutExtension()));
            } else {
                show_notification("CAN'T IMPORT SAVE FILE. MAKE SURE THE LEVEL EXISTS.");
            }
        } else {
            show_notification("YOU MUST PICK A VALID IMPORT LOCATION");
        }
    }

    /** Attempts to import path, which must be a directory.
     * It doesn't import anything other than .grid and .save files. */
    private boolean import_dir(FileHandle path) {
        if (path.isDirectory()) {
            String name = path.name();
            FileHandle grid_path = path.child(name + ".grid");
            if (grid_path.exists())
                return import_level(grid_path);
        }
        return false;
    }

    private boolean import_level(FileHandle grid_path) {
        if (grid_path.extension().equals("grid")) {
            String name = grid_path.nameWithoutExtension();
            grid_path.copyTo(LevelSelector.get_level_grid_fhandle(LEVEL_TYPE.CUSTOM, name));

            FileHandle save_file = grid_path.sibling(name + ".save");
            if (save_file.exists())
                import_save(save_file);

            return true;
        }
        return false;
    }

    /** NOTE: Assumes that path points to an existing, valid .save file. */
    private boolean import_save(FileHandle path) {
        String name = path.nameWithoutExtension();
        if (LevelSelector.get_level_grid_fhandle(LEVEL_TYPE.CUSTOM, name).exists()) {  // a save file without a grid would be useless
            path.copyTo(LevelSelector.get_level_save_fhandle(LEVEL_TYPE.CUSTOM, name));
            return true;
        }
        return false;
    }

    private void show_notification(String message) {
        if (notification != null)
            notification.remove();  // get rid of the old notification
        notification = new TextNotification(game, stage, message);
        notification.show();
    }
}
