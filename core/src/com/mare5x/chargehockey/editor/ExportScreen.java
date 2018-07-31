package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelector;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

import java.util.Locale;

class ExportScreen extends BaseMenuScreen {
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

        TextButton export_button = make_text_button("EXPORT SELECTED");
        export_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!selector.is_empty()) {
                    // something has to be selected
                    if (selector.is_selected())
                        set_screen(new FilePickerScreen(game, ExportScreen.this, export_selected_callback, filter));
                    else
                        show_notification("FIRST, SELECT A LEVEL");
                } else
                    show_notification("NO CUSTOM LEVELS YET CREATED.\nCREATE OR IMPORT CUSTOM LEVELS USING THE CUSTOM EDITOR.", 3);
            }
        });

        TextButton export_all_button = make_text_button("EXPORT ALL");
        export_all_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!selector.is_empty())
                    set_screen(new FilePickerScreen(game, ExportScreen.this, export_all_callback, filter));
                else
                    show_notification("NO CUSTOM LEVELS YET CREATED.\nCREATE OR IMPORT CUSTOM LEVELS USING THE CUSTOM EDITOR.", 3);
            }
        });

        add_back_button();
        table.add(selector.get_selector_table()).pad(15).expand().fill().row();
        add_text_button(export_button).row();
        add_text_button(export_all_button);

        if (selector.is_empty()) {
            show_notification("NO CUSTOM LEVELS YET CREATED.\nCREATE OR IMPORT CUSTOM LEVELS USING THE CUSTOM EDITOR.", 3);
        }
    }

    private void export_all(FileHandle target) {
        try {
            Level.get_levels_dir_fhandle(LEVEL_TYPE.CUSTOM).copyTo(target);
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
            Level.get_level_dir_fhandle(LEVEL_TYPE.CUSTOM, level_name).copyTo(target);
            show_notification(String.format(Locale.US, "EXPORTED TO: %s", target.file().getAbsolutePath()));
        } catch (GdxRuntimeException e) {
            e.printStackTrace();
            show_notification("FAILED TO EXPORT");
        }
    }

    @Override
    protected void back_key_pressed() {
        set_screen(new CustomMenuScreen(game), true);
    }

    @Override
    public void hide() {

    }
}
