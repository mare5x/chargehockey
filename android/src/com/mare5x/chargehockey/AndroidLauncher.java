package com.mare5x.chargehockey;

import android.content.pm.PackageManager;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    private AndroidChargeHockeyGame game;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
        config.useCompass = false;

        game = new AndroidChargeHockeyGame(this);
		initialize(game, config);
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionTools.STORAGE_PERMISSION_CODE: {
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
