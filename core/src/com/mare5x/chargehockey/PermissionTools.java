package com.mare5x.chargehockey;


public interface PermissionTools {
    interface RequestCallback {
        void granted();
        void denied();
    }

    int STORAGE_PERMISSION_CODE = 1;

    boolean check_storage_permission();

    void request_storage_permission(RequestCallback callback);
}
