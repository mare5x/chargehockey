package com.mare5x.chargehockey;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

// This is the entry point of the game.
public class AndroidLauncher extends AndroidApplication {
    private AndroidChargeHockeyGame game;

    public static final int STORAGE_PERMISSION_CODE = 1;
    public static final int EXPORT_PICKER_CODE = 2;
    public static final int IMPORT_PICKER_CODE = 3;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
        config.useCompass = false;
        config.useImmersiveMode = false;  // Don't hide the navigation bar

        game = new AndroidChargeHockeyGame(this);
		initialize(game, config);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EXPORT_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                AndroidExporter exporter = (AndroidExporter) game.get_exporter();
                exporter.file_picker_result_callback(uri);
            }
        } else if (requestCode == IMPORT_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                AndroidImporter importer = (AndroidImporter) game.get_importer();
                importer.file_picker_result_callback(uri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_CODE: {
                AndroidPermissionTools permission_tools = (AndroidPermissionTools) game.get_permission_tools();
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission_tools.get_last_request_callback().granted();
                } else {
                    permission_tools.get_last_request_callback().denied();
                }
            }
        }
    }
}
