package com.mare5x.chargehockey;


import com.badlogic.gdx.backends.android.AndroidApplication;
import com.mare5x.chargehockey.editor.Exporter;
import com.mare5x.chargehockey.editor.FilePicker;
import com.mare5x.chargehockey.editor.PermissionTools;

class AndroidChargeHockeyGame extends ChargeHockeyGame {
    private final AndroidPermissionTools permission_tools;
    private final AndroidExporter exporter;

    AndroidChargeHockeyGame(AndroidApplication activity) {
        permission_tools = new AndroidPermissionTools(activity);
        exporter = new AndroidExporter(activity, this);
    }

    @Override
    public PermissionTools get_permission_tools() {
        // permission_tools must not be newly allocated since the object holds the result callback
        return permission_tools;
    }

    @Override
    public FilePicker get_file_picker() {
        return new AndroidFilePicker(this);
    }

    @Override
    public FilePicker get_file_picker(FilePicker.FileFilter filter) {
        return new AndroidFilePicker(this, filter);
    }

    @Override
    public Exporter get_exporter() {
        return exporter;
    }
}
