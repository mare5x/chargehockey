package com.mare5x.chargehockey;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.mare5x.chargehockey.editor.Exporter;
import com.mare5x.chargehockey.editor.FilePickerScreen;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

import java.io.FileNotFoundException;
import java.io.OutputStream;

public class AndroidExporter extends Exporter {
    private final AndroidApplication activity;
    private final AndroidChargeHockeyGame game;
    private FilePickerScreen.FilePickerCallback file_picker_callback;

    public AndroidExporter(AndroidApplication activity, AndroidChargeHockeyGame game) {
        this.activity = activity;
        this.game = game;
    }

    @Override
    protected void show_file_picker(BaseMenuScreen parent_screen, String name, FilePickerScreen.FilePickerCallback on_result) {
        // Intent.ACTION_CREATE_DOCUMENT was added in API 19; it doesn't require permissions.
        if (Build.VERSION.SDK_INT >= 19) {
            file_picker_callback = on_result;
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("application/zip");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_TITLE, "chargehockey_export_" + name + ".zip");
            activity.startActivityForResult(intent, AndroidLauncher.EXPORT_PICKER_CODE);
        } else {
            // API 22 or lower grants permission during app install, later versions during runtime
            parent_screen.set_screen(new FilePickerScreen(game, parent_screen, on_result, export_filter));
            //parent_screen.set_screen_permission_check(new FilePickerScreen(game, parent_screen, on_result, export_filter));
        }
    }

    // Called on intent activity result.
    void file_picker_result_callback(Uri uri) {
        try {
            OutputStream stream = activity.getContentResolver().openOutputStream(uri, "w");
            file_picker_callback.write_result(stream, uri.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        file_picker_callback = null;
    }
}
