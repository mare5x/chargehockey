package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.editor.CustomMenuScreen;
import com.mare5x.chargehockey.game.PlayMenuScreen;
import com.mare5x.chargehockey.settings.SettingsFile;
import com.mare5x.chargehockey.settings.SettingsScreen;

import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.MAX_BUTTON_WIDTH;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;


public class MenuScreen extends BaseMenuScreen {
    public MenuScreen(final ChargeHockeyGame game) {
        super(game);

        // use an ImageButton for "fit" scaling
        ImageButton play_button = new ImageButton(game.skin, "play");
        play_button.getImageCell().grow();
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new PlayMenuScreen(game));
            }
        });

        TextButton edit_button = make_text_button("CUSTOM EDITOR");
        edit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new CustomMenuScreen(game));
            }
        });

        TextButton settings_button = make_text_button("SETTINGS");
        settings_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new SettingsScreen(game, MenuScreen.this));
            }
        });

        TextButton exit_button = make_text_button("EXIT");
        exit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
            }
        });

        table.add(play_button).pad(CELL_PAD).prefSize(MAX_BUTTON_WIDTH).minSize(MIN_BUTTON_HEIGHT).row();
        add_text_button(edit_button).row();
        add_text_button(settings_button).row();
        add_text_button(exit_button);
    }

    @Override
    protected void back_key_pressed() {
        game.exit();
    }

    @Override
    public void show() {
        super.show();

        SettingsFile.apply_global_settings();
    }

    @Override
    public void hide() {

    }
}
