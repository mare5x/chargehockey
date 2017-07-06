package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


class LevelSelectorScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;

    LevelSelectorScreen(final ChargeHockeyGame game, LEVEL_TYPE level_type) {
        super(game);

        this.game = game;

        final LevelSelector level_selector = new LevelSelector(game, level_type);

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu_screen);
            }
        });
        back_button.pad(10);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Level level = level_selector.load_selected_level();
                if (level != null) {
                    game.setScreen(new GameScreen(game, level));
                }
            }
        });
        play_button.pad(10);

//        table.pad(50 * ChargeHockeyGame.DENSITY, 15 * ChargeHockeyGame.DENSITY, 50 * ChargeHockeyGame.DENSITY, 15 * ChargeHockeyGame.DENSITY);

        table.add(back_button).pad(15).expandX().size(Value.percentWidth(0.3f, table), Value.percentWidth(0.15f, table)).left().top().row();
        table.add(level_selector.get_selector_table()).pad(15).expand().fill().row();
        table.add(play_button).pad(15).size(Value.percentWidth(0.3f, table));
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
