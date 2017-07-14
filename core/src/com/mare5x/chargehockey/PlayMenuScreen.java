package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


class PlayMenuScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;

    PlayMenuScreen(final ChargeHockeyGame game) {
        super(game);

        this.game = game;

        TextButton easy_button = new TextButton("EASY", game.skin);
        easy_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.EASY));
            }
        });
        easy_button.pad(10);

        TextButton medium_button = new TextButton("MEDIUM", game.skin);
        medium_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.MEDIUM));
            }
        });
        medium_button.pad(10);

        TextButton hard_button = new TextButton("HARD", game.skin);
        hard_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.HARD));
            }
        });
        hard_button.pad(10);

        TextButton custom_button = new TextButton("CUSTOM", game.skin);
        custom_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, LEVEL_TYPE.CUSTOM));
            }
        });
        custom_button.pad(10);

        table.pad(10 * ChargeHockeyGame.DENSITY);

        Value twidth = Value.percentWidth(0.6f, table);
        add_back_button(game.skin);
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
