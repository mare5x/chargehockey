package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
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
    private final ChargeHockeyGame game;
    private final Screen parent_screen;

    private final Stage stage;

    GameMenuScreen(final ChargeHockeyGame game, final Screen parent_screen) {
        this.game = game;
        this.parent_screen = parent_screen;

        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);
        stage.setDebugAll(true);

        TextButton return_button = new TextButton("RETURN TO GAME", game.skin);
        return_button.pad(10);
        return_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("return_button", "clicked");

                game.setScreen(parent_screen);
            }
        });

        TextButton restart_button = new TextButton("RESTART LEVEL", game.skin);
        restart_button.pad(10);
        restart_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("restart_button", "clicked");

                ((GameScreen) (parent_screen)).restart_level();
            }
        });

        TextButton main_menu_button = new TextButton("MAIN MENU", game.skin);
        main_menu_button.pad(10);
        main_menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("main_menu_button", "clicked");

                game.setScreen(game.menu_screen);
                parent_screen.dispose();
            }
        });

        TextButton settings_button = new TextButton("SETTINGS", game.skin);
        settings_button.pad(10);
        settings_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("settings_button", "clicked");
            }
        });

        Table table = new Table();
        table.setFillParent(true);

        table.add(return_button).pad(15).width(Value.percentWidth(0.6f, table)).uniform().fillX().row();
        table.add(restart_button).pad(15).uniform().fillX().row();
        table.add(main_menu_button).pad(15).uniform().fillX().row();
        table.add(settings_button).pad(15).uniform().fillX();

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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