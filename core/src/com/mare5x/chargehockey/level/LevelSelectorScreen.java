package com.mare5x.chargehockey.level;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.GameScreen;
import com.mare5x.chargehockey.PlayMenuScreen;
import com.mare5x.chargehockey.notifications.TextNotification;


public class LevelSelectorScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;

    public LevelSelectorScreen(final ChargeHockeyGame game, Level.LEVEL_TYPE level_type) {
        super(game);

        this.game = game;

        final LevelSelector level_selector = new LevelSelector(game, level_type);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Level level = level_selector.load_selected_level();
                if (level != null) {
                    game.setScreen(new GameScreen(game, level));
                } else {
                    TextNotification notification = new TextNotification(game, stage, "FIRST, SELECT THE LEVEL YOU WISH TO PLAY");
                    notification.show(2);
                }
            }
        });
        play_button.pad(10);

        table.pad(10 * ChargeHockeyGame.DENSITY);

        add_back_button(game.skin);
        table.add(level_selector.get_selector_table()).pad(15).expand().fill().row();
        table.add(play_button).pad(15).size(Value.percentWidth(0.3f, table));

        if (level_selector.is_empty()) {
            TextNotification notification = new TextNotification(game, stage, "NO CUSTOM LEVELS YET CREATED.\nCREATE OR IMPORT CUSTOM LEVELS USING THE CUSTOM EDITOR.");
            notification.show(3);
        }
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(new PlayMenuScreen(game));
    }

    @Override
    public void hide() {
        dispose();
    }
}