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
import com.mare5x.chargehockey.actors.ChargeActor;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.menus.ScrollableMenuScreen;
import com.mare5x.chargehockey.settings.SettingsFile.SETTINGS_KEY;

import java.util.Locale;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;


public class SettingsScreen extends ScrollableMenuScreen {
    private final Screen parent_screen;

    private final SettingsFile settings_file;

    private final SettingCheckBox acceleration_checkbox, velocity_checkbox, trace_path_checkbox, forces_checkbox, game_grid_lines;
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

        game_grid_lines = new SettingCheckBox(game, "SHOW GRID", settings_file.getBoolean(SETTINGS_KEY.GAME_GRID_LINES));
        velocity_checkbox = new SettingCheckBox(game, "SHOW VELOCITY VECTOR", settings_file.getBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        acceleration_checkbox = new SettingCheckBox(game, "SHOW ACCELERATION VECTOR", settings_file.getBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        forces_checkbox = new SettingCheckBox(game, "SHOW FORCE VECTORS", settings_file.getBoolean(SETTINGS_KEY.SHOW_FORCE_VECTORS));
        trace_path_checkbox = new SettingCheckBox(game, "TRACE PUCK PATH", settings_file.getBoolean(SETTINGS_KEY.TRACE_PATH));

        add_back_button();
        table.add().expand().row();

        table.defaults().pad(CELL_PAD).prefWidth(Value.percentWidth(0.8f, table)).maxWidth(Value.percentWidth(0.8f, table));
        table.add(charge_size_slider).row();
        table.add(game_speed_slider).row();
        table.add(game_grid_lines).row();
        table.add(velocity_checkbox).row();
        table.add(acceleration_checkbox).row();
        table.add(forces_checkbox).row();
        table.add(trace_path_checkbox).row();

//        BaseMenuScreen.add_button_to_table(table, defaults_button).padLeft(Value.percentWidth(0.1f, table)).left().row();
        table.add(defaults_button).minHeight(MIN_BUTTON_HEIGHT).row();

        table.add().expand();
    }

    @Override
    protected void back_key_pressed() {
        set_screen(parent_screen);
    }

    private void load_defaults() {
        SettingsFile.initialize(settings_file.get_preferences(), true);

        charge_size_slider.set_value(settings_file.getFloat(SETTINGS_KEY.CHARGE_SIZE));
        game_speed_slider.set_value(settings_file.getFloat(SETTINGS_KEY.GAME_SPEED));
        game_grid_lines.set_checked(settings_file.getBoolean(SETTINGS_KEY.GAME_GRID_LINES));
        velocity_checkbox.set_checked(settings_file.getBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        acceleration_checkbox.set_checked(settings_file.getBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        forces_checkbox.set_checked(settings_file.getBoolean(SETTINGS_KEY.SHOW_FORCE_VECTORS));
        trace_path_checkbox.set_checked(settings_file.getBoolean(SETTINGS_KEY.TRACE_PATH));
    }

    private void save() {
        Gdx.app.log("SettingsScreen", "saving preferences");
        settings_file.put(SETTINGS_KEY.GAME_SPEED, game_speed_slider.get_value());
        settings_file.put(SETTINGS_KEY.CHARGE_SIZE, charge_size_slider.get_value());
        settings_file.put(SETTINGS_KEY.GAME_GRID_LINES, game_grid_lines.is_checked());
        settings_file.put(SETTINGS_KEY.SHOW_VELOCITY_VECTOR, velocity_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR, acceleration_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.SHOW_FORCE_VECTORS, forces_checkbox.is_checked());
        settings_file.put(SETTINGS_KEY.TRACE_PATH, trace_path_checkbox.is_checked());
        settings_file.save();
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

    /** Encapsulates a unified checkbox. */
    private static class SettingCheckBox extends Table {
        private final Button checkbox;
        private final TextButton text_button;

        SettingCheckBox(final ChargeHockeyGame game, String label, boolean checked) {
            super();

            checkbox = new Button(game.skin, "checkbox");
            checkbox.setChecked(checked);
            checkbox.pad(ACTOR_PAD);

            text_button = BaseMenuScreen.make_text_button(game, label, true);
            text_button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    checkbox.toggle();
                }
            });

            BaseMenuScreen.add_button_to_table(this, text_button).pad(0);
            add(checkbox).space(CELL_PAD).size(MIN_BUTTON_HEIGHT).expandX().right();
        }

        void set_checked(boolean checked) {
            checkbox.setChecked(checked);
        }

        boolean is_checked() {
            return checkbox.isChecked();
        }
    }

    /** A unified horizontal slider. */
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

            add(label).fill().prefWidth(GameDefaults.MIN_DIMENSION * 0.4f).minWidth(50);
            add(slider).space(CELL_PAD).expandX().fillX();

            set_knob_size_impl(MIN_BUTTON_HEIGHT);
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

        private void set_knob_size_impl(float size) {
            // work around knob size
            slider.getStyle().knob.setMinHeight(size);
            slider.getStyle().knob.setMinWidth(size);
        }

        void set_knob_size(float size) {
            set_knob_size_impl(size);
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
        private final Image charge;

        ChargeSettingSlider(ChargeHockeyGame game, float current) {
            super(game, ChargeActor.MIN_SIZE, ChargeActor.MAX_SIZE, current);

            charge = new Image(game.skin.getDrawable("sprite_charge_pos"));

            set_label_format("CHARGE SIZE: %.1f");

            Label size_label = new Label("CHARGE SIZE:", game.skin);
            size_label.setWrap(true);
            size_label.setAlignment(Align.center);

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

            add(size_label).fillX().minHeight(MIN_BUTTON_HEIGHT).prefWidth(GameDefaults.MIN_DIMENSION * 0.4f).minWidth(50);
            add(charge).space(CELL_PAD).size(ChargeActor.BASE_CHARGE_SIZE * ChargeActor.MAX_SIZE).row();
            add(label).fillX().minHeight(MIN_BUTTON_HEIGHT).prefWidth(GameDefaults.MIN_DIMENSION * 0.4f).minWidth(50);
            add(slider).space(CELL_PAD).expandX().fillX();

            set_charge_size_impl(ChargeActor.BASE_CHARGE_SIZE * ChargeActor.MAX_SIZE);
        }

        private void scale_charge() {
            charge.setScale(slider.getValue() / slider.getMaxValue());
        }

        private void set_charge_size_impl(float size) {
            // necessary hack to make the charge be centered in the cell
            charge.setSize(size, size);
            charge.setOrigin(Align.center);
            charge.setAlign(Align.center);
        }

        @Override
        void set_knob_size(float size) {
            super.set_knob_size(size);

            set_charge_size_impl(size);
        }
    }
}
