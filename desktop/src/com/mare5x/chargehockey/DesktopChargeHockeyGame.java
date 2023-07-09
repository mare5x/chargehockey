package com.mare5x.chargehockey;


import com.badlogic.gdx.files.FileHandle;
import com.mare5x.chargehockey.editor.Exporter;
import com.mare5x.chargehockey.editor.FilePicker;
import com.mare5x.chargehockey.editor.FilePickerScreen;
import com.mare5x.chargehockey.editor.Importer;
import com.mare5x.chargehockey.editor.PermissionTools;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

import java.io.InputStream;

class DesktopChargeHockeyGame extends ChargeHockeyGame {
    @Override
    public PermissionTools get_permission_tools() {
        return new DesktopPermissionTools();
    }

    @Override
    public FilePicker get_file_picker() {
        return new DesktopFilePicker(this);
    }

    @Override
    public FilePicker get_file_picker(FilePicker.FileFilter filter) {
        return new DesktopFilePicker(this, filter);
    }

    @Override
    public Exporter get_exporter() {
        return new Exporter() {
            @Override
            protected void show_file_picker(BaseMenuScreen parent_screen, String name, FilePickerScreen.FilePickerCallback on_result) {
                parent_screen.set_screen(new FilePickerScreen(DesktopChargeHockeyGame.this, parent_screen, on_result, export_filter));
            }
        };
    }

    @Override
    public Importer get_importer() {
        return new Importer() {
            @Override
            protected void run(BaseMenuScreen parent_screen) {
                parent_screen.set_screen_permission_check(new FilePickerScreen(DesktopChargeHockeyGame.this, parent_screen, new FilePickerScreen.FilePickerCallback() {
                    @Override
                    public void on_result(FileHandle path) {
                        handle_result(parent_screen, handle_import(path));
                    }
                }, import_filter));
            }
        };
    }
}
