package com.mare5x.chargehockey.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.menus.ScrollableMenuScreen;
import com.mare5x.chargehockey.settings.SettingsFile.SETTINGS_KEY;

import java.util.Locale;


// todo add load defaults button confirmation
public class SettingsScreen extends ScrollableMenuScreen {
    private final Screen parent_screen;

    private final SettingsFile settings_file;

    private final SettingCheckBox acceleration_checkbox, velocity_checkbox, trace_path_checkbox, forces_checkbox;
    private final SettingSlider game_speed_slider, charge_size_slider;

    public SettingsScreen(final ChargeHockeyGame game, final Screen parent_screen) {
        super(game);

        this.parent_screen = parent_screen;

        settings_file = new SettingsFile();

        final TextButton defaults_button = make_text_button("RESET TO DEFAULT SETTINGS");
        defaults_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                load_defaults();
            }
        });

        game_speed_slider = new SettingSlider(game, 0.1f, 1.5f, settings_file.getFloat(SETTINGS_KEY.GAME_SPEED));
        game_speed_slider.set_label_format("GAME SPEED: %.1f");

        charge_size_slider = new ChargeSettingSlider(game, settings_file.getFloat(SETTINGS_KEY.CHARGE_SIZE));

        velocity_checkbox = new SettingCheckBox(game, "SHOW VELOCITY VECTOR", settings_file.getBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        acceleration_checkbox = new SettingCheckBox(game, "SHOW ACCELERATION VECTOR", settings_file.getBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        forces_checkbox = new SettingCheckBox(game, "SHOW FORCE VECTORS", settings_file.getBoolean(SETTINGS_KEY.SHOW_FORCE_VECTORS));
        trace_path_checkbox = new SettingCheckBox(game, "TRACE PUCK PATH", settings_file.getBoolean(SETTINGS_KEY.TRACE_PATH));

        add_back_button();
        table.add().expand().row();

        game_speed_slider.add_to_table(table);
        charge_size_slider.add_to_table(table);
        velocity_checkbox.add_to_table(table);
        acceleration_checkbox.add_to_table(table);
        forces_checkbox.add_to_table(table);
        trace_path_checkbox.add_to_table(table);

        table.add(defaults_button).pad(15).minHeight(MIN_BUTTON_HEIGHT).center().fillX().row();

        table.add().expand();
    }

    @Override
    protected void back_key_pressed() {
        set_screen(parent_screen);
    }

    private void load_defaults() {
        SettingsFile.initialize(settings_file.get_preferences(), true);

        game_speed_slider.set_value(settings_file.getFloat(SETTINGS_KEY.GAME_SPEED));
        charge_size_slider.set_value(settings_file.getFloat(SETTINGS_KEY.CHARGE_SIZE));
        velocity_checkbox.set_checked(settings_file.getBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        acceleration_checkbox.set_checked(settings_file.getBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        forces_checkbox.set_checked(settings_file.getBoolean(SETTINGS_KEY.SHOW_FORCE_VECTORS));
        trace_path_checkbox.set_checked(settings_file.getBoolean(SETTINGS_KEY.TRACE_PATH));
    }

    private void save() {
        Gdx.app.log("SettingsScreen", "saving preferences");
        settings_file.put(SETTINGS_KEY.GAME_SPEED, game_speed_slider.get_value());
        settings_file.put(SETTINGS_KEY.CHARGE_SIZE, charge_size_slider.get_value());
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
        charge_size_slider.set_knob_size(width * 0.125f);

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

    /** Encapsulates a unified checkbox. Use with add_to_table(). */
    private static class SettingCheckBox extends Table {
        private final Button checkbox;
        private final TextButton text_button;

        SettingCheckBox(final ChargeHockeyGame game, String label, boolean checked) {
            super();

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

            add(text_button).space(15).minHeight(MIN_BUTTON_HEIGHT).center();
            add(checkbox).space(15).size(MIN_BUTTON_HEIGHT).expandX().center();
        }

        void add_to_table(Table parent) {
            getCell(text_button).width(Value.percentWidth(0.6f, parent));
            parent.add(this).pad(15).fillX().expandX().row();
        }

        void set_checked(boolean checked) {
            checkbox.setChecked(checked);
        }

        boolean is_checked() {
            return checkbox.isChecked();
        }
    }

    /** A unified horizontal slider. Do NOT use it as a Table, but use the add_to_table method. */
    private static class SettingSlider extends Table {
        final Label label;
        final Slider slider;

        SettingSlider(final ChargeHockeyGame game, float min, float max, float current) {
            label = new Label("...", game.skin);
            label.setWrap(true);
            label.setAlignment(Align.center);

            slider = new Slider(min, max, 0.1f, false, game.skin);
            slider.setValue(current);

            // this fixes the scrollpane (ScrollableMenu...) from messing with the slider
            slider.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    event.stop();
                    return false;
                }
            });

            add(label).space(15).fill().center();
            add(slider).space(15).expandX().center();
        }

        void add_to_table(Table parent) {
            getCell(label).width(Value.percentWidth(0.4f, parent));
            getCell(slider).width(Value.percentWidth(0.4f, parent));

            parent.add(this).pad(15).fillX().expandX().row();
        }

        void set_text(String text) {
            label.setText(text);
        }

        void set_value(float value) {
            slider.setValue(value);
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

    /** A slider for setting the charge's visual size. */
    private static class ChargeSettingSlider extends SettingSlider {
        private static final float MIN_SIZE = 0.5f;
        private static final float MAX_SIZE = 2;

        private final Image charge;

        ChargeSettingSlider(ChargeHockeyGame game, float current) {
            super(game, MIN_SIZE, MAX_SIZE, current);

            charge = new Image(game.sprites.findRegion("charge_pos"));

            set_label_format("CHARGE SIZE: %.1f");

            scale_charge();

            slider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    scale_charge();
                }
            });

            // remove parent layout
            clear();
            reset();
            remove();

            add(label).space(15).fill().center();
            add(slider).space(15).expandX().center();
            add(charge).space(15).size(Value.percentHeight(1, slider)).expandX().center();
        }

        private void scale_charge() {
            charge.setScale(0.5f + slider.getPercent() * 0.5f);  // scale includes [0.5, 1.0]
        }

        @Override
        void set_knob_size(float size) {
            super.set_knob_size(size);

            // necessary hack to make the charge be centered in the cell
            charge.setSize(size, size);
            charge.setOrigin(Align.center);
            charge.setAlign(Align.center);
        }

        @Override
        void add_to_table(Table parent) {
            getCell(label).width(Value.percentWidth(0.4f, parent));
            getCell(slider).width(Value.percentWidth(0.3f, parent));
            parent.add(this).pad(15).fillX().expandX().row();
        }
    }
}
