package com.mare5x.chargehockey;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.mare5x.chargehockey.editor.PermissionTools;

// https://developer.android.com/training/permissions/requesting.html
class AndroidPermissionTools implements PermissionTools {
    private final AndroidApplication activity;

    private RequestCallback request_callback;

    AndroidPermissionTools(AndroidApplication activity) {
        this.activity = activity;
    }

    @Override
    public boolean check_storage_permission() {
        // sdk 22 or lower grants permission during app install, later versions during runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    public void request_storage_permission(RequestCallback callback) {
        if (callback == null)
            return;

        request_callback = callback;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AndroidLauncher.STORAGE_PERMISSION_CODE);
        }
    }

    RequestCallback get_last_request_callback() {
        return request_callback;
    }
}
