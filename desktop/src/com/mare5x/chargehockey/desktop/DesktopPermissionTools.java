package com.mare5x.chargehockey.desktop;


import com.mare5x.chargehockey.PermissionTools;

class DesktopPermissionTools implements PermissionTools {
    @Override
    public boolean check_storage_permission() {
        return true;
    }

    @Override
    public void request_storage_permission(RequestCallback callback) {
    }
}
