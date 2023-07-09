package com.mare5x.chargehockey;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.files.FileHandle;
import com.mare5x.chargehockey.editor.FilePickerScreen;
import com.mare5x.chargehockey.editor.Importer;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class AndroidImporter extends Importer {

    private final AndroidApplication activity;
    private final ChargeHockeyGame game;
    private BaseMenuScreen parent_screen = null;

    AndroidImporter(AndroidApplication activity, ChargeHockeyGame game) {
        this.activity = activity;
        this.game = game;
    }

    @Override
    protected void run(BaseMenuScreen parent_screen) {
        // Intent.ACTION_OPEN_DOCUMENT was added in API 19; it doesn't require permissions.
        if (Build.VERSION.SDK_INT >= 19) {
            this.parent_screen = parent_screen;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/zip");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            activity.startActivityForResult(intent, AndroidLauncher.IMPORT_PICKER_CODE);
        } else {
            // API 22 or lower grants permission during app install, later versions during runtime
            parent_screen.set_screen_permission_check(new FilePickerScreen(game, parent_screen, new FilePickerScreen.FilePickerCallback() {
                @Override
                public void on_result(FileHandle path) {
                    handle_result(parent_screen, handle_import(path));
                }
            }, import_filter));

        }
    }

    // Called on intent activity result.
    void file_picker_result_callback(Uri uri) {
        try {
            InputStream stream = activity.getContentResolver().openInputStream(uri);
            handle_result(parent_screen, import_zip(stream));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        parent_screen = null;
    }
}
