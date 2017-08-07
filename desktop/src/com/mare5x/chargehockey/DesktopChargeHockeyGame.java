package com.mare5x.chargehockey;


import com.mare5x.chargehockey.editor.FilePicker;

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
}
