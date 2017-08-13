package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.game.PlayMenuScreen;
import com.mare5x.chargehockey.settings.SettingsFile;
import com.mare5x.chargehockey.settings.SettingsScreen;
import com.mare5x.chargehockey.editor.CustomMenuScreen;


public class MenuScreen extends BaseMenuScreen {
    public MenuScreen(final ChargeHockeyGame game) {
        super(game);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new PlayMenuScreen(game));
            }
        });

        TextButton edit_button = make_text_button("CUSTOM EDITOR");
        edit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CustomMenuScreen(game));
            }
        });

        TextButton settings_button = make_text_button("SETTINGS");
        settings_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, MenuScreen.this));
            }
        });

        table.add(play_button).expandX().pad(15).size(Value.percentWidth(0.6f, table)).row();
        add_text_button(edit_button).row();
        add_text_button(settings_button);
    }

    @Override
    protected void back_key_pressed() {
        Gdx.app.exit();
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
