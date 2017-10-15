package com.mare5x.chargehockey.game;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.editor.EditorScreen;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelectorScreen;
import com.mare5x.chargehockey.menus.ScrollableMenuScreen;
import com.mare5x.chargehockey.settings.SettingsScreen;


class GameMenuScreen extends ScrollableMenuScreen {
    private final GameScreen parent_screen;

    GameMenuScreen(final ChargeHockeyGame game, final GameScreen parent_screen, final Level level) {
        super(game);

        this.parent_screen = parent_screen;

        TextButton resume_button = make_text_button("RESUME");
        resume_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(parent_screen);
            }
        });

        TextButton edit_button = make_text_button("EDIT");
        edit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new EditorScreen(game, level));
                parent_screen.dispose();
            }
        });

        TextButton save_button = make_text_button("QUICKSAVE");
        save_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parent_screen.save_charge_state(Level.SAVE_TYPE.QUICKSAVE);
                show_notification("QUICKSAVED");
            }
        });

        TextButton load_button = make_text_button("LOAD QUICKSAVE");
        load_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!parent_screen.load_charge_state(Level.SAVE_TYPE.QUICKSAVE)) {
                    // probably the file doesn't exist
                    show_notification("FAILED TO LOAD QUICKSAVE");
                } else {
                    set_screen(parent_screen);
                }
            }
        });

        TextButton restart_button = make_text_button("RESTART LEVEL");
        restart_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parent_screen.restart_level();
                set_screen(parent_screen);
            }
        });

        TextButton selector_button = make_text_button("SELECT LEVEL");
        selector_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new LevelSelectorScreen(game, level));
                parent_screen.dispose();
            }
        });

        TextButton main_menu_button = make_text_button("MAIN MENU");
        main_menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(game.menu_screen);
                parent_screen.dispose();
            }
        });

        TextButton settings_button = make_text_button("SETTINGS");
        settings_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new SettingsScreen(game, parent_screen));
            }
        });

        TextButton exit_button = make_text_button("EXIT");
        exit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parent_screen.dispose();
                game.exit();
            }
        });

        add_text_button(resume_button).row();
        if (level.get_type() == LEVEL_TYPE.CUSTOM) add_text_button(edit_button).row();
        add_text_button(save_button).row();
        add_text_button(load_button).row();
        add_text_button(restart_button).row();
        add_text_button(selector_button).row();
        add_text_button(main_menu_button).row();
        add_text_button(settings_button).row();
        add_text_button(exit_button);
    }

    @Override
    protected void back_key_pressed() {
        set_screen(parent_screen);
    }

    @Override
    public void hide() {
        dispose();
    }
}
