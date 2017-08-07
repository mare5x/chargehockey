package com.mare5x.chargehockey;


class DesktopChargeHockeyGame extends ChargeHockeyGame {
    @Override
    PermissionTools get_permission_tools() {
        return new DesktopPermissionTools();
    }

    @Override
    FilePicker get_file_picker() {
        return new DesktopFilePicker(this);
    }

    @Override
    FilePicker get_file_picker(FilePicker.FileFilter filter) {
        return new DesktopFilePicker(this, filter);
    }
}
