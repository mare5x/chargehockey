package com.mare5x.chargehockey.game;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelectorScreen;
import com.mare5x.chargehockey.menus.BaseMenuScreen;


public class PlayMenuScreen extends BaseMenuScreen {
    public PlayMenuScreen(final ChargeHockeyGame game) {
        super(game);

        TextButton easy_button = make_text_button("EASY");
        easy_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.EASY));
            }
        });

        TextButton medium_button = make_text_button("MEDIUM");
        medium_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.MEDIUM));
            }
        });

        TextButton hard_button = make_text_button("HARD");
        hard_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.HARD));
            }
        });

        TextButton custom_button = make_text_button("CUSTOM");
        custom_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.CUSTOM));
            }
        });

        Value twidth = Value.percentWidth(0.6f, table);
        add_back_button();
        table.add().expand().row();
        table.add(easy_button).pad(15).uniform().width(twidth).row();
        table.add(medium_button).pad(15).uniform().width(twidth).row();
        table.add(hard_button).pad(15).uniform().width(twidth).row();
        table.add(custom_button).pad(15).uniform().width(twidth).row();
        table.add().expand();
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(game.menu_screen);
    }

    @Override
    public void hide() {
        dispose();
    }
}
