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


class SettingsScreen implements Screen {
    private final Stage stage;
    private final SettingsFile settings_file;

    SettingsScreen(final ChargeHockeyGame game, final Screen parent_screen) {
        settings_file = new SettingsFile();

        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);
        stage.setDebugAll(true);

        Button back_button = new Button(game.skin, "back");
        back_button.pad(10);
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(parent_screen);
            }
        });

        final Button velocity_checkbox = new Button(game.skin, "checkbox");
        velocity_checkbox.setChecked(settings_file.get(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        velocity_checkbox.pad(10);
        velocity_checkbox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings_file.put(SETTINGS_KEY.SHOW_VELOCITY_VECTOR, velocity_checkbox.isChecked());
            }
        });
        final TextButton velocity_vector_text = new TextButton("SHOW VELOCITY VECTOR", game.skin);
        velocity_vector_text.pad(10);
        velocity_vector_text.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                velocity_checkbox.toggle();
                settings_file.put(SETTINGS_KEY.SHOW_VELOCITY_VECTOR, velocity_checkbox.isChecked());
            }
        });

        final Button acceleration_checkbox = new Button(game.skin, "checkbox");
        acceleration_checkbox.setChecked(settings_file.get(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        acceleration_checkbox.pad(10);
        acceleration_checkbox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings_file.put(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR, acceleration_checkbox.isChecked());
            }
        });
        final TextButton acceleration_vector_text = new TextButton("SHOW ACCELERATION VECTOR", game.skin);
        acceleration_vector_text.pad(10);
        acceleration_vector_text.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                acceleration_checkbox.toggle();
                settings_file.put(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR, acceleration_checkbox.isChecked());
            }
        });

        velocity_vector_text.getLabel().setWrap(true);
        acceleration_vector_text.getLabel().setWrap(true);

        Table table = new Table();
        table.setFillParent(true);

        table.add(back_button).pad(15).size(Value.percentWidth(0.3f, table), Value.percentWidth(0.15f, table)).left().row();
        table.add().expand().colspan(2).row();
        table.add(velocity_vector_text).pad(15).width(Value.percentWidth(0.6f, table)).fillX().uniform();
        table.add(velocity_checkbox).pad(15).size(Value.percentWidth(0.125f, table)).right().row();
        table.add(acceleration_vector_text).pad(15).width(Value.percentWidth(0.6f, table)).fillX().uniform();
        table.add(acceleration_checkbox).pad(15).size(Value.percentWidth(0.125f, table)).right().row();
        table.add().colspan(2).expand();

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
        settings_file.save();
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        settings_file.save();
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
