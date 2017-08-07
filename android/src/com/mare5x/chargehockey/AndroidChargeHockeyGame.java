package com.mare5x.chargehockey;


import com.badlogic.gdx.backends.android.AndroidApplication;
import com.mare5x.chargehockey.editor.FilePicker;
import com.mare5x.chargehockey.editor.PermissionTools;

class AndroidChargeHockeyGame extends ChargeHockeyGame {
    private final AndroidApplication activity;

    AndroidChargeHockeyGame(AndroidApplication activity) {
        this.activity = activity;
    }

    @Override
    public PermissionTools get_permission_tools() {
        return new AndroidPermissionTools(activity);
    }

    @Override
    public FilePicker get_file_picker() {
        return new AndroidFilePicker(this);
    }

    @Override
    public FilePicker get_file_picker(FilePicker.FileFilter filter) {
        return new AndroidFilePicker(this, filter);
    }
}
