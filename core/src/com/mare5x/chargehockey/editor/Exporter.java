package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.editor.FilePickerScreen.FilePickerCallback;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.menus.BaseMenuScreen;


/** Class for exporting levels.
 *  Deals with picking the destination using a file picker.
 */
class Exporter {
    private static final FilePicker.FileFilter export_filter = new FilePicker.FileFilter() {
        @Override
        public boolean is_valid(FileHandle path) {
            return path.isDirectory();
        }
    };

    interface ExporterCallback {
        // path is the level directory if exporting a single level and the parent directory otherwise
        void on_success(FileHandle path);
        void on_failure(FileHandle path);
    }

    private final ChargeHockeyGame game;
    private final BaseMenuScreen parent_screen;
    private final ExporterCallback exporter_callback;

    Exporter(ChargeHockeyGame game, BaseMenuScreen parent_screen, ExporterCallback exporter_callback) {
        this.game = game;
        this.parent_screen = parent_screen;
        this.exporter_callback = exporter_callback;
    }

    private void show_file_picker(FilePickerCallback on_result) {
        parent_screen.set_screen_permission_check(new FilePickerScreen(game, parent_screen, on_result, export_filter));
    }

    void export(final String level_name) {
        show_file_picker(new FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                if (export(level_name, path))
                    exporter_callback.on_success(path.child(level_name));
                else
                    exporter_callback.on_failure(path.child(level_name));
            }
        });
    }

    /* Export a single custom level to the destination. Returns true on success. */
    private boolean export(String level_name, FileHandle dest) {
        dest = dest.child(level_name);

        try {
            Level.get_level_dir_fhandle(Level.LEVEL_TYPE.CUSTOM, level_name).copyTo(dest);
            return true;
        } catch (GdxRuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    void export(final Array<String> levels) {
        show_file_picker(new FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                if (export(levels, path))
                    exporter_callback.on_success(path);
                else
                    exporter_callback.on_failure(path);
            }
        });
    }

    /* Export multiple levels to the destination, which must be a directory. */
    private boolean export(Array<String> levels, FileHandle dest) {
        boolean success = true;
        for (String level_name : levels) {
            success &= export(level_name, dest);
        }
        return success;
    }

    void export_all() {
        show_file_picker(new FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                if (export_all(path))
                    exporter_callback.on_success(path);
                else
                    exporter_callback.on_failure(path);
            }
        });
    }

    /* Export all custom levels to the destination. Returns true on success. */
    private boolean export_all(FileHandle dest) {
        try {
            Level.get_levels_dir_fhandle(Level.LEVEL_TYPE.CUSTOM).copyTo(dest);
            return true;
        } catch (GdxRuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }
}
