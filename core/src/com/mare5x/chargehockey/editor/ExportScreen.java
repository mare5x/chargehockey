package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;

import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelector;
import com.mare5x.chargehockey.notifications.TextNotification;

import java.util.Locale;

class ExportScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;
    private final LevelSelector selector ;

    private final FilePickerScreen.FilePickerCallback export_all_callback;
    private final FilePickerScreen.FilePickerCallback export_selected_callback;
    private static final FilePicker.FileFilter filter = new FilePicker.FileFilter() {
        @Override
        public boolean is_valid(FileHandle path) {
            return path.isDirectory();
        }
    };

    ExportScreen(final ChargeHockeyGame game) {
        super(game);

        this.game = game;
        selector = new LevelSelector(game, LEVEL_TYPE.CUSTOM);

        export_all_callback = new FilePickerScreen.FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                export_all(path);
            }
        };

        export_selected_callback = new FilePickerScreen.FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                export_selected(path);
            }
        };

        TextButton export_button = new TextButton("EXPORT SELECTED", game.skin);
        export_button.pad(10);
        export_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!selector.is_selected()) { // something has to be selected
                    show_notification("FIRST, SELECT A LEVEL");
                    return;
                }

                game.setScreen(new FilePickerScreen(game, ExportScreen.this, export_selected_callback, filter));
            }
        });

        TextButton export_all_button = new TextButton("EXPORT ALL", game.skin);
        export_all_button.pad(10);
        export_all_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new FilePickerScreen(game, ExportScreen.this, export_all_callback, filter));
            }
        });

        add_back_button(game.skin);
        table.add(selector.get_selector_table()).pad(15).expand().fill().row();
        table.add(export_button).pad(15).width(Value.percentWidth(0.6f, table)).row();
        table.add(export_all_button).pad(15).width(Value.percentWidth(0.6f, table));

        if (selector.is_empty()) {
            TextNotification notification = new TextNotification(game, stage, "NO CUSTOM LEVELS YET CREATED.\nCREATE OR IMPORT CUSTOM LEVELS USING THE CUSTOM EDITOR.");
            notification.show(3);
        }
    }

    private void show_notification(String message) {
        TextNotification notification = new TextNotification(game, stage, message);
        notification.show();
    }

    private void export_all(FileHandle target) {
        try {
            LevelSelector.get_levels_dir_fhandle(LEVEL_TYPE.CUSTOM).copyTo(target);
            show_notification(String.format(Locale.US, "EXPORTED TO: %s", target.file().getAbsolutePath()));
        } catch (GdxRuntimeException e) {
            e.printStackTrace();
            show_notification("FAILED TO EXPORT");
        }
    }

    /** NOTE: Assumes that a level is currently selected. */
    private void export_selected(FileHandle target) {
        String level_name = selector.get_selected_name();
        target = target.child(level_name);

        try {
            LevelSelector.get_level_dir_fhandle(LEVEL_TYPE.CUSTOM, level_name).copyTo(target);
            show_notification(String.format(Locale.US, "EXPORTED TO: %s", target.file().getAbsolutePath()));
        } catch (GdxRuntimeException e) {
            e.printStackTrace();
            show_notification("FAILED TO EXPORT");
        }
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(new CustomMenuScreen(game));
        dispose();
    }

    @Override
    public void hide() {
    }
}