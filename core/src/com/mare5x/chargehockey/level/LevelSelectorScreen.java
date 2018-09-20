package com.mare5x.chargehockey.level;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.game.CustomLevelSelectorScreen;
import com.mare5x.chargehockey.game.GameScreen;
import com.mare5x.chargehockey.game.PlayMenuScreen;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.menus.TableLayout;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;


public class LevelSelectorScreen extends BaseMenuScreen {
    protected final LevelSelector level_selector;

    private LevelSelectorScreen(final ChargeHockeyGame game, Level selected_level) {
        this(game, selected_level.get_type(), selected_level.get_name());
    }

    public LevelSelectorScreen(final ChargeHockeyGame game, Level.LEVEL_TYPE level_type) {
        this(game, level_type, null);
    }

    public LevelSelectorScreen(final ChargeHockeyGame game, Level.LEVEL_TYPE level_type, String selected_level_name) {
        super(game);

        this.level_selector = new LevelSelector(game, level_type);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Level level = level_selector.load_selected_level();
                if (level != null) {
                    set_screen(new GameScreen(game, level));
                } else {
                    show_notification("FIRST, SELECT THE LEVEL YOU WISH TO PLAY", 2);
                }
            }
        });
        play_button.pad(ACTOR_PAD);

        TableLayout level_selector_layout = level_selector.get_table_layout();
        table.add_layout(level_selector_layout);

        add_back_button();
        table.add(level_selector_layout).pad(CELL_PAD).expand().fill().row();
        table.add(play_button).pad(CELL_PAD).size(1.75f * MIN_BUTTON_HEIGHT).padBottom(Value.percentHeight(0.05f, table));

        if (level_selector.is_empty()) {
            show_notification("NO CUSTOM LEVELS YET CREATED.\nCREATE OR IMPORT CUSTOM LEVELS USING THE CUSTOM EDITOR.", 3);
        } else if (selected_level_name != null) {
            // workaround for scrolling to selected level (TableLayout initialized after resize)
            table.resize(stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
            table.validate();
            level_selector.select(selected_level_name);
        }
    }

    @Override
    protected void back_key_pressed() {
        set_screen(new PlayMenuScreen(game));
    }

    @Override
    public void hide() {
        dispose();
    }

    public static LevelSelectorScreen create(ChargeHockeyGame game, Level level) {
        if (level.get_type() == Level.LEVEL_TYPE.CUSTOM)
            return new CustomLevelSelectorScreen(game, level);
        return new LevelSelectorScreen(game, level);
    }
}
