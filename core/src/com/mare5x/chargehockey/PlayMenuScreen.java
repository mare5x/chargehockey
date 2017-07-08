package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


class PlayMenuScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;

    PlayMenuScreen(final ChargeHockeyGame game) {
        super(game);

        this.game = game;

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu_screen);
            }
        });
        back_button.pad(10);

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

        Value twidth = Value.percentWidth(0.5f, table);
        table.add(back_button).pad(15).size(Value.percentWidth(0.3f, table), Value.percentWidth(0.15f, table)).expandX().left().top().row();
        table.add().expand().row();
        table.add(easy_button).pad(15).uniform().width(twidth).fillY().row();
        table.add(medium_button).pad(15).uniform().width(twidth).fillY().row();
        table.add(hard_button).pad(15).uniform().width(twidth).fillY().row();
        table.add(custom_button).pad(15).uniform().width(twidth).fillY().row();
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
