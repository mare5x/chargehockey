package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import java.util.Locale;


class SettingsScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;
    private final Screen parent_screen;

    private final SettingsFile settings_file;

    private final SettingCheckBox acceleration_checkbox, velocity_checkbox, trace_path_checkbox, forces_checkbox;
    private final Slider game_speed_slider;

    SettingsScreen(final ChargeHockeyGame game, final Screen parent_screen) {
        super(game);

        this.game = game;
        this.parent_screen = parent_screen;

        settings_file = new SettingsFile();

        Button back_button = new Button(game.skin, "back");
        back_button.pad(10);
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
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

        velocity_checkbox = new SettingCheckBox(game, "SHOW VELOCITY VECTOR", SETTINGS_KEY.SHOW_VELOCITY_VECTOR);
        acceleration_checkbox = new SettingCheckBox(game, "SHOW ACCELERATION VECTOR", SETTINGS_KEY.SHOW_ACCELERATION_VECTOR);
        forces_checkbox = new SettingCheckBox(game, "SHOW FORCE VECTORS", SETTINGS_KEY.SHOW_FORCE_VECTORS);
        trace_path_checkbox = new SettingCheckBox(game, "TRACE PATH?", SETTINGS_KEY.TRACE_PATH);

        table.add(back_button).pad(15).size(Value.percentWidth(0.3f, table), Value.percentWidth(0.15f, table)).left().row();

        table.add().expand().colspan(2).row();

        Table slider_table = new Table();
        slider_table.add(game_speed_label).pad(15).width(Value.percentWidth(0.4f, table)).fill().center();
        slider_table.add(game_speed_slider).pad(15).width(Value.percentWidth(0.4f, table)).expandX().fillX().row();
        table.add(slider_table).colspan(2).pad(15).row();

        velocity_checkbox.add_to_table(table);
        acceleration_checkbox.add_to_table(table);
        forces_checkbox.add_to_table(table);
        trace_path_checkbox.add_to_table(table);

        table.add().colspan(2).expand();
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(parent_screen);
    }

    private void save() {
        Gdx.app.log("SettingsScreen", "saving preferences");
        settings_file.put(SETTINGS_KEY.GAME_SPEED, game_speed_slider.getValue());
        settings_file.put(SETTINGS_KEY.SHOW_VELOCITY_VECTOR, velocity_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR, acceleration_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.SHOW_FORCE_VECTORS, forces_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.TRACE_PATH, trace_path_checkbox.is_checked());
        settings_file.save();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // work around knob size
        game_speed_slider.getStyle().knob.setMinHeight(width * 0.125f);
        game_speed_slider.getStyle().knob.setMinWidth(width * 0.125f);

        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {
        super.pause();

        save();
    }

    @Override
    public void hide() {
        save();
        SettingsFile.apply_global_settings(settings_file);
        dispose();
    }

    /** Encapsulates a unified checkbox. Use with add_to_table(). DO NOT use it as an Actor. */
    private class SettingCheckBox extends Actor {
        private final Button checkbox;
        private final TextButton text_button;

        SettingCheckBox(final ChargeHockeyGame game, String label, SETTINGS_KEY setting) {
            checkbox = new Button(game.skin, "checkbox");
            checkbox.setChecked(settings_file.getBoolean(setting));
            checkbox.pad(10);

            text_button = new TextButton(label, game.skin);
            text_button.pad(10);
            text_button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    checkbox.toggle();
                }
            });

            text_button.getLabel().setWrap(true);
        }

        void add_to_table(Table parent) {
            parent.add(text_button).pad(15).width(Value.percentWidth(0.6f, parent)).fillX();
            parent.add(checkbox).pad(15).size(Value.percentWidth(0.125f, parent)).row();
        }

        boolean is_checked() {
            return checkbox.isChecked();
        }

        @Override
        public float getHeight() {
            return text_button.getHeight();
        }

        @Override
        public float getWidth() {
            return text_button.getWidth();
        }
    }
}
