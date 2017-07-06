package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


class GameMenuScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;
    private final GameScreen parent_screen;

    GameMenuScreen(final ChargeHockeyGame game, final GameScreen parent_screen, final Level level) {
        super(game);

        this.game = game;
        this.parent_screen = parent_screen;

        TextButton return_button = new TextButton("PLAY", game.skin);
        return_button.pad(10);
        return_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(parent_screen);
            }
        });

        TextButton edit_button = new TextButton("EDIT", game.skin);
        edit_button.pad(10);
        edit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EditorScreen(game, level));
                parent_screen.dispose();
            }
        });

        TextButton restart_button = new TextButton("RESTART LEVEL", game.skin);
        restart_button.pad(10);
        restart_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parent_screen.restart_level();
            }
        });

        TextButton main_menu_button = new TextButton("MAIN MENU", game.skin);
        main_menu_button.pad(10);
        main_menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu_screen);
                parent_screen.dispose();
            }
        });

        TextButton settings_button = new TextButton("SETTINGS", game.skin);
        settings_button.pad(10);
        settings_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, parent_screen));
            }
        });

        table.add(return_button).pad(15).width(Value.percentWidth(0.6f, table)).uniform().fillX().row();
        if (level.get_type() == LEVEL_TYPE.CUSTOM) table.add(edit_button).pad(15).uniform().fillX().row();
        table.add(restart_button).pad(15).uniform().fillX().row();
        table.add(main_menu_button).pad(15).uniform().fillX().row();
        table.add(settings_button).pad(15).uniform().fillX();
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(parent_screen);
    }

    @Override
    public void hide() {
        dispose();
    }
}
