package com.mare5x.chargehockey.editor;


public interface PermissionTools {
    interface RequestCallback {
        void granted();
        void denied();
    }

    boolean check_storage_permission();

    void request_storage_permission(RequestCallback callback);
}
