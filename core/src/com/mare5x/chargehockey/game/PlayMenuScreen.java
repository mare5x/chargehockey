package com.mare5x.chargehockey.game;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelectorScreen;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.tutorial.TutorialScreen;


public class PlayMenuScreen extends BaseMenuScreen {
    public PlayMenuScreen(final ChargeHockeyGame game) {
        super(game);

        TextButton tutorial_button = make_text_button("TUTORIAL");
        tutorial_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new TutorialScreen(game));
            }
        });

        TextButton easy_button = make_text_button("EASY");
        easy_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new LevelSelectorScreen(game, LEVEL_TYPE.EASY));
            }
        });

        TextButton medium_button = make_text_button("MEDIUM");
        medium_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new LevelSelectorScreen(game, LEVEL_TYPE.MEDIUM));
            }
        });

        TextButton hard_button = make_text_button("HARD");
        hard_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new LevelSelectorScreen(game, LEVEL_TYPE.HARD));
            }
        });

        TextButton custom_button = make_text_button("CUSTOM");
        custom_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new LevelSelectorScreen(game, LEVEL_TYPE.CUSTOM));
            }
        });

        add_back_button();
        table.add().expand().row();
        add_text_button(tutorial_button).row();
        add_text_button(easy_button).row();
        add_text_button(medium_button).row();
        add_text_button(hard_button).row();
        add_text_button(custom_button).row();
        table.add().expand();
    }

    @Override
    protected void back_key_pressed() {
        set_screen(game.menu_screen);
    }

    @Override
    public void hide() {
        dispose();
    }
}
