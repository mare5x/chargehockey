package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.Locale;


class SettingsScreen implements Screen {
    private final Stage stage;
    private final InputMultiplexer input_multiplexer;

    private final SettingsFile settings_file;

    private final Button acceleration_checkbox, velocity_checkbox, trace_path_checkbox;
    private final Slider game_speed_slider;

    SettingsScreen(final ChargeHockeyGame game, final Screen parent_screen) {
        settings_file = new SettingsFile();

        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);

        Button back_button = new Button(game.skin, "back");
        back_button.pad(10);
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(parent_screen);
            }
        });

        final Label game_speed_label = new Label("GAME SPEED: ", game.skin);
        game_speed_label.setWrap(true);
        game_speed_label.setAlignment(Align.center);
        game_speed_slider = new Slider(0.1f, 1.5f, 0.1f, false, game.skin);
        game_speed_slider.setValue(settings_file.getFloat(SETTINGS_KEY.GAME_SPEED));
        game_speed_label.setText(String.format(Locale.US, "GAME SPEED: %.1f", game_speed_slider.getValue()));
        game_speed_slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game_speed_label.setText(String.format(Locale.US, "GAME SPEED: %.1f", game_speed_slider.getValue()));
            }
        });

        velocity_checkbox = new Button(game.skin, "checkbox");
        velocity_checkbox.setChecked(settings_file.getBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        velocity_checkbox.pad(10);

        final TextButton velocity_vector_text = new TextButton("SHOW VELOCITY VECTOR", game.skin);
        velocity_vector_text.pad(10);
        velocity_vector_text.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                velocity_checkbox.toggle();
            }
        });

        acceleration_checkbox = new Button(game.skin, "checkbox");
        acceleration_checkbox.setChecked(settings_file.getBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        acceleration_checkbox.pad(10);

        final TextButton acceleration_vector_text = new TextButton("SHOW ACCELERATION VECTOR", game.skin);
        acceleration_vector_text.pad(10);
        acceleration_vector_text.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                acceleration_checkbox.toggle();
            }
        });

        trace_path_checkbox = new Button(game.skin, "checkbox");
        trace_path_checkbox.setChecked(settings_file.getBoolean(SETTINGS_KEY.TRACE_PATH));
        trace_path_checkbox.pad(10);

        final TextButton trace_path_text = new TextButton("TRACE PATH?", game.skin);
        trace_path_text.pad(10);
        trace_path_text.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                trace_path_checkbox.toggle();
            }
        });

        velocity_vector_text.getLabel().setWrap(true);
        acceleration_vector_text.getLabel().setWrap(true);
        trace_path_text.getLabel().setWrap(true);

        Table table = new Table();
        table.setFillParent(true);

        table.add(back_button).pad(15).size(Value.percentWidth(0.3f, table), Value.percentWidth(0.15f, table)).left().row();

        table.add().expand().colspan(2).row();

        final Table slider_table = new Table();
        slider_table.add(game_speed_label).pad(15).width(Value.percentWidth(0.4f, table)).height(Value.percentHeight(1, velocity_vector_text)).fill().center();
        slider_table.add(game_speed_slider).pad(15).width(Value.percentWidth(0.4f, table)).height(Value.percentHeight(1, velocity_checkbox)).expandX().fillX().row();
        table.add(slider_table).colspan(2).pad(15).row();

        table.add(velocity_vector_text).pad(15).width(Value.percentWidth(0.6f, table)).fillX();
        table.add(velocity_checkbox).pad(15).size(Value.percentWidth(0.125f, table)).row();

        table.add(acceleration_vector_text).pad(15).width(Value.percentWidth(0.6f, table)).fillX();
        table.add(acceleration_checkbox).pad(15).size(Value.percentWidth(0.125f, table)).row();

        table.add(trace_path_text).pad(15).width(Value.percentWidth(0.6f, table)).fillX();
        table.add(trace_path_checkbox).pad(15).size(Value.percentWidth(0.125f, table)).row();

        table.add().colspan(2).expand();

        stage.addActor(table);

        InputAdapter back_key_processor = new InputAdapter() {
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

    private void save() {
        Gdx.app.log("SettingsScreen", "saving preferences");
        settings_file.put(SETTINGS_KEY.GAME_SPEED, game_speed_slider.getValue());
        settings_file.put(SETTINGS_KEY.SHOW_VELOCITY_VECTOR, velocity_checkbox.isChecked());
        settings_file.put(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR, acceleration_checkbox.isChecked());
        settings_file.put(SETTINGS_KEY.TRACE_PATH, trace_path_checkbox.isChecked());
        settings_file.save();
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

        // work around knob size
        game_speed_slider.getStyle().knob.setMinHeight(width * 0.125f);
        game_speed_slider.getStyle().knob.setMinWidth(width * 0.125f);

        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {
        save();
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        save();
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
