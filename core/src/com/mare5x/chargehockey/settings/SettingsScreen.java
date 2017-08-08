package com.mare5x.chargehockey.settings;

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
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.settings.SettingsFile.SETTINGS_KEY;

import java.util.Locale;


public class SettingsScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;
    private final Screen parent_screen;

    private final SettingsFile settings_file;

    private final SettingCheckBox acceleration_checkbox, velocity_checkbox, trace_path_checkbox, forces_checkbox;
    private final SettingSlider game_speed_slider;

    public SettingsScreen(final ChargeHockeyGame game, final Screen parent_screen) {
        super(game);

        this.game = game;
        this.parent_screen = parent_screen;

        settings_file = new SettingsFile();

        game_speed_slider = new SettingSlider(game, 0.1f, 1.5f, settings_file.getFloat(SETTINGS_KEY.GAME_SPEED));
        game_speed_slider.set_label_format("GAME SPEED: %.1f");

        velocity_checkbox = new SettingCheckBox(game, "SHOW VELOCITY VECTOR", settings_file.getBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        acceleration_checkbox = new SettingCheckBox(game, "SHOW ACCELERATION VECTOR", settings_file.getBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        forces_checkbox = new SettingCheckBox(game, "SHOW FORCE VECTORS", settings_file.getBoolean(SETTINGS_KEY.SHOW_FORCE_VECTORS));
        trace_path_checkbox = new SettingCheckBox(game, "TRACE PATH?", settings_file.getBoolean(SETTINGS_KEY.TRACE_PATH));

        add_back_button(game.skin);
        table.add().expand().colspan(2).row();

        game_speed_slider.add_to_table(table);
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
        settings_file.put(SETTINGS_KEY.GAME_SPEED, game_speed_slider.get_value());
        settings_file.put(SETTINGS_KEY.SHOW_VELOCITY_VECTOR, velocity_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR, acceleration_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.SHOW_FORCE_VECTORS, forces_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.TRACE_PATH, trace_path_checkbox.is_checked());
        settings_file.save();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        game_speed_slider.set_knob_size(width * 0.125f);

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
    private static class SettingCheckBox extends Actor {
        private final Button checkbox;
        private final TextButton text_button;

        SettingCheckBox(final ChargeHockeyGame game, String label, boolean checked) {
            checkbox = new Button(game.skin, "checkbox");
            checkbox.setChecked(checked);
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

    /** A unified horizontal slider. Do NOT use it as a Table, but use the add_to_table method. */
    private static class SettingSlider extends Table {
        private final Label label;
        private final Slider slider;

        SettingSlider(final ChargeHockeyGame game, float min, float max, float current) {
            label = new Label("...", game.skin);
            label.setWrap(true);
            label.setAlignment(Align.center);

            slider = new Slider(min, max, 0.1f, false, game.skin);
            slider.setValue(current);
        }

        void add_to_table(Table parent) {
            add(label).pad(15).width(Value.percentWidth(0.4f, parent)).fill().center();
            add(slider).pad(15).width(Value.percentWidth(0.4f, parent)).expandX().fillX().row();
            parent.add(this).colspan(2).row();
        }

        void set_text(String text) {
            label.setText(text);
        }

        float get_value() {
            return slider.getValue();
        }

        void set_knob_size(float size) {
            // work around knob size
            slider.getStyle().knob.setMinHeight(size);
            slider.getStyle().knob.setMinWidth(size);
        }

        /** Registers the built in label to display the formatted format string using the
         * slider's current value. i.e. format must have a single '%f' or '%d' tag. */
        void set_label_format(final String format) {
            set_text(String.format(Locale.US, format, get_value()));
            slider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    set_text(String.format(Locale.US, format, get_value()));
                }
            });
        }
    }
}
