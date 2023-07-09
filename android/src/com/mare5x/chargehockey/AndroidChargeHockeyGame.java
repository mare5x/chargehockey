package com.mare5x.chargehockey;


import com.badlogic.gdx.backends.android.AndroidApplication;
import com.mare5x.chargehockey.editor.Exporter;
import com.mare5x.chargehockey.editor.FilePicker;
import com.mare5x.chargehockey.editor.Importer;
import com.mare5x.chargehockey.editor.PermissionTools;

class AndroidChargeHockeyGame extends ChargeHockeyGame {
    private final AndroidApplication activity;
    private AndroidPermissionTools permission_tools = null;
    private AndroidExporter exporter = null;
    private AndroidImporter importer = null;

    AndroidChargeHockeyGame(AndroidApplication activity) {
        this.activity = activity;
    }

    @Override
    public PermissionTools get_permission_tools() {
        if (permission_tools == null) {
            permission_tools = new AndroidPermissionTools(activity);
        }
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
        if (exporter == null) {
            exporter = new AndroidExporter(activity, this);
        }
        return exporter;
    }

    @Override
    public Importer get_importer() {
        if (importer == null) {
            importer = new AndroidImporter(activity, this);
        }
        return importer;
    }
}
