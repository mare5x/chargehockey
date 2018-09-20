package com.mare5x.chargehockey.game;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.editor.EditorScreen;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.LevelSelector;
import com.mare5x.chargehockey.level.LevelSelectorScreen;
import com.mare5x.chargehockey.menus.NameInputDialog;

public class CustomLevelSelectorScreen extends LevelSelectorScreen {
    private final PlayInputDialog dialog;

    CustomLevelSelectorScreen(ChargeHockeyGame game) {
        this(game, null);
    }

    public CustomLevelSelectorScreen(ChargeHockeyGame game, Level selected_level) {
        super(game, Level.LEVEL_TYPE.CUSTOM, selected_level != null ? selected_level.get_name() : null);

        this.dialog = new PlayInputDialog(stage, "EDIT LEVEL", game.skin);

        level_selector.set_select_on_long_press(true);
        level_selector.set_long_press_callback(new LevelSelector.LongPressCallback() {
            @Override
            public void run(String level_name) {
                dialog.show(level_name);
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (dialog.isVisible())
            dialog.resize();
    }

    private class PlayInputDialog extends NameInputDialog {
        PlayInputDialog(Stage stage, String title, Skin skin) {
            super(stage, title, skin);

            TextButton delete_button = make_text_button("DELETE");
            delete_button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    level_selector.remove_selected_level();
                    hide();
                }
            });

            TextButton edit_button = make_text_button("EDIT");
            edit_button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    final Level level = level_selector.load_selected_level();
                    if (level != null)
                        set_screen(new EditorScreen(game, level));
                    hide();
                }
            });

            Table content_table = getContentTable();  // see inherited defaults()
            content_table.add(delete_button).row();
            content_table.add(edit_button);
        }

        @Override
        public void on_confirm() {
            if (!level_selector.rename_selected_level(get_name())) {
                show_notification("ERROR RENAMING LEVEL");
            }
        }
    }
}
