package com.mare5x.chargehockey;


import com.badlogic.gdx.backends.android.AndroidApplication;

class AndroidChargeHockeyGame extends ChargeHockeyGame {
    private final AndroidApplication activity;

    AndroidChargeHockeyGame(AndroidApplication activity) {
        this.activity = activity;
    }

    @Override
    PermissionTools get_permission_tools() {
        return new AndroidPermissionTools(activity);
    }

    @Override
    FilePicker get_file_picker() {
        return new AndroidFilePicker(this);
    }

    @Override
    FilePicker get_file_picker(FilePicker.FileFilter filter) {
        return new AndroidFilePicker(this, filter);
    }
}
