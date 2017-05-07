package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class PlayMenuScreen implements Screen {
    private final ChargeHockeyGame game;

    private final Stage stage;

    PlayMenuScreen(final ChargeHockeyGame game) {
        this.game = game;

        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);
        stage.setDebugAll(true);

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("back_button", "clicked");
                game.setScreen(game.menu_screen);
            }
        });
        back_button.pad(10);

        TextButton easy_button = new TextButton("EASY", game.skin);
        easy_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("easy_button", "clicked");
            }
        });
        easy_button.pad(10);

        TextButton medium_button = new TextButton("MEDIUM", game.skin);
        medium_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("medium_button", "clicked");
            }
        });
        medium_button.pad(10);

        TextButton hard_button = new TextButton("HARD", game.skin);
        hard_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("hard_button", "clicked");
            }
        });
        hard_button.pad(10);

        TextButton custom_button = new TextButton("CUSTOM", game.skin);
        custom_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("custom_button", "clicked");

                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.CUSTOM));
            }
        });
        custom_button.pad(10);

        Table table = new Table();
        table.setFillParent(true);
        table.pad(10 * ChargeHockeyGame.DENSITY);

        Value twidth = Value.percentWidth(0.5f, table);
        table.add(back_button).pad(15).size(Value.percentHeight(2f, easy_button), Value.percentHeight(1f, easy_button)).expandX().left().top().row();
        table.add().expand().row();
        table.add(easy_button).pad(15).uniform().width(twidth).fillY().row();
        table.add(medium_button).pad(15).uniform().width(twidth).fillY().row();
        table.add(hard_button).pad(15).uniform().width(twidth).fillY().row();
        table.add(custom_button).pad(15).uniform().width(twidth).fillY().row();
        table.add().expand();

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
        stage.getViewport().update(width, height);
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
