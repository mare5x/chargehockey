package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelector;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

import java.util.Locale;

class ExportScreen extends BaseMenuScreen {
    private final LevelSelector selector;

    ExportScreen(final ChargeHockeyGame game) {
        super(game);

        selector = new LevelSelector(game, LEVEL_TYPE.CUSTOM);
        selector.set_multiple_select(true);

        final Exporter exporter = new Exporter(game, this, new Exporter.ExporterCallback() {
            @Override
            public void on_success(FileHandle path) {
                show_notification(String.format(Locale.US, "EXPORTED TO: %s", path.file().getAbsolutePath()));
            }

            @Override
            public void on_failure(FileHandle path) {
                show_notification("FAILED TO EXPORT");
            }
        });

        TextButton export_button = make_text_button("EXPORT SELECTED");
        export_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (custom_levels_check()) {
                    // something has to be selected
                    if (selector.is_selected())
                        exporter.export(selector.get_selected_names());
                    else
                        show_notification("FIRST, SELECT A LEVEL");
                }
            }
        });

        TextButton export_all_button = make_text_button("EXPORT ALL");
        export_all_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (custom_levels_check())
                    exporter.export_all();
            }
        });

        add_back_button();
        table.add(selector.get_selector_table()).pad(15).expand().fill().row();
        add_text_button(export_button).row();
        add_text_button(export_all_button);

        custom_levels_check();
    }

    private boolean custom_levels_check() {
        if (selector.is_empty()) {
            show_notification("NO CUSTOM LEVELS YET CREATED.\nCREATE OR IMPORT CUSTOM LEVELS USING THE CUSTOM EDITOR.", 3);
            return false;
        }
        return true;
    }

    @Override
    protected void back_key_pressed() {
        set_screen(new CustomMenuScreen(game), true);
    }

    @Override
    public void hide() { }
}
