package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.files.FileHandle;
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
        parent_screen.set_screen(new FilePickerScreen(game, parent_screen, on_result, export_filter));
    }

    void export(final String level_name) {
        show_file_picker(new FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                export(level_name, path);
            }
        });
    }

    /* Export a single custom level to the destination. */
    private void export(String level_name, FileHandle dest) {
        dest = dest.child(level_name);

        try {
            Level.get_level_dir_fhandle(Level.LEVEL_TYPE.CUSTOM, level_name).copyTo(dest);
            exporter_callback.on_success(dest);
        } catch (GdxRuntimeException e) {
            e.printStackTrace();
            exporter_callback.on_failure(dest);
        }
    }

    void export(String[] levels) {

    }

    void export_all() {
        show_file_picker(new FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                export_all(path);
            }
        });
    }

    /* Export all custom levels to the destination. */
    private void export_all(FileHandle dest) {
        try {
            Level.get_levels_dir_fhandle(Level.LEVEL_TYPE.CUSTOM).copyTo(dest);
            exporter_callback.on_success(dest);
        } catch (GdxRuntimeException e) {
            e.printStackTrace();
            exporter_callback.on_failure(dest);
        }
    }
}
