package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


class MenuScreen extends BaseMenuScreen {
    MenuScreen(final ChargeHockeyGame game) {
        super(game);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new PlayMenuScreen(game));
            }
        });

        TextButton edit_button = new TextButton("CUSTOM EDITOR", game.skin);
        edit_button.pad(10);
        edit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EditorMenuScreen(game));
            }
        });

        TextButton settings_button = new TextButton("SETTINGS", game.skin);
        settings_button.pad(10);
        settings_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, MenuScreen.this));
            }
        });

        table.add(play_button).expandX().pad(15).size(Value.percentWidth(0.6f, table)).row();
        table.add(edit_button).pad(15).width(Value.percentWidth(0.6f, table)).fillX().row();
        table.add(settings_button).pad(15).width(Value.percentWidth(0.6f, table)).fillX();
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
