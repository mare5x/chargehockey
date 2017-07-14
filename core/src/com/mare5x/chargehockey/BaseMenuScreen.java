package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;


abstract class BaseMenuScreen implements Screen {
    final Stage stage;
    final InputMultiplexer input_multiplexer;

    protected final Table table;

    BaseMenuScreen(final ChargeHockeyGame game) {
        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);

        table = new Table(game.skin);
        table.setFillParent(true);

        stage.addActor(table);

        InputAdapter back_key_processor = new InputAdapter() {  // same as return button
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    back_key_pressed();
                }
                return true;
            }
        };
        input_multiplexer = new InputMultiplexer(stage, back_key_processor);
    }

    void add_back_button(Skin skin) {
        Button back_button = new Button(skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
            }
        });
        back_button.pad(10);

        table.add(back_button).pad(15).expandX().size(Value.percentWidth(0.3f, table), Value.percentWidth(0.15f, table)).left().top().row();
    }

    abstract protected void back_key_pressed();

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
    abstract public void hide();

    @Override
    public void dispose() {
        stage.dispose();
    }
}
