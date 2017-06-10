package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class GameMenuScreen implements Screen {
    private final Stage stage;
    private final InputMultiplexer input_multiplexer;

    GameMenuScreen(final ChargeHockeyGame game, final Screen parent_screen) {
        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);

        TextButton return_button = new TextButton("RETURN TO GAME", game.skin);
        return_button.pad(10);
        return_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(parent_screen);
            }
        });

        TextButton restart_button = new TextButton("RESTART LEVEL", game.skin);
        restart_button.pad(10);
        restart_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((GameScreen) (parent_screen)).restart_level();
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

        Table table = new Table();
        table.setFillParent(true);

        table.add(return_button).pad(15).width(Value.percentWidth(0.6f, table)).uniform().fillX().row();
        table.add(restart_button).pad(15).uniform().fillX().row();
        table.add(main_menu_button).pad(15).uniform().fillX().row();
        table.add(settings_button).pad(15).uniform().fillX();

        stage.addActor(table);

        InputAdapter back_key_processor = new InputAdapter() {  // same as return button
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    game.setScreen(parent_screen);
                }
                return true;
            }
        };
        input_multiplexer = new InputMultiplexer(stage, back_key_processor);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input_multiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);

        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
