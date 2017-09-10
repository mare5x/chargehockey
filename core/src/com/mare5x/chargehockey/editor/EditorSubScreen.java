package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.game.GameScreen;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

class EditorSubScreen extends BaseMenuScreen {
    private final EditorScreen parent_screen;

    EditorSubScreen(final ChargeHockeyGame game, final EditorScreen parent_screen) {
        super(game);

        this.parent_screen = parent_screen;

        TextButton resume_button = make_text_button("RESUME");
        resume_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
            }
        });

        TextButton play_button = make_text_button("PLAY");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Level level = parent_screen.get_level();
                parent_screen.dispose();  // first dispose so that the level changes get written before setting the new screen
                set_screen(new GameScreen(game, level));
            }
        });

        TextButton clear_button = make_text_button("CLEAR LEVEL");
        clear_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parent_screen.clear_level();
            }
        });

        // todo HELP screen

        TextButton selector_button = make_text_button("SELECT LEVEL");
        selector_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Level level = parent_screen.get_level();
                parent_screen.dispose();  // first dispose so that the level changes get written before setting the new screen
                set_screen(new EditorMenuScreen(game, level));
            }
        });

        TextButton menu_button = make_text_button("MAIN MENU");
        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(game.menu_screen);
                parent_screen.dispose();
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
        add_text_button(play_button).row();
        add_text_button(clear_button).row();
        add_text_button(selector_button).row();
        add_text_button(menu_button).row();
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
