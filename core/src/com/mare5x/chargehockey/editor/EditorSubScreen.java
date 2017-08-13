package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.game.GameScreen;

class EditorSubScreen extends BaseMenuScreen {
    private final EditorScreen parent_screen;

    EditorSubScreen(final ChargeHockeyGame game, final EditorScreen parent_screen) {
        super(game);

        this.parent_screen = parent_screen;

        TextButton return_button = make_text_button("EDIT");
        return_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
            }
        });

        TextButton play_button = make_text_button("PLAY");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, parent_screen.get_level()));
                parent_screen.dispose();
            }
        });

        TextButton selector_button = make_text_button("SELECT LEVEL");
        selector_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EditorMenuScreen(game));
                parent_screen.dispose();
            }
        });

        TextButton menu_button = make_text_button("MAIN MENU");
        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu_screen);
                parent_screen.dispose();
            }
        });

        add_text_button(return_button).row();
        add_text_button(play_button).row();
        add_text_button(selector_button).row();
        add_text_button(menu_button).row();
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(parent_screen);
    }

    @Override
    public void hide() {
        dispose();
    }
}
