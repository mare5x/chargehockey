package com.mare5x.chargehockey.game;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.notifications.TextNotification;
import com.mare5x.chargehockey.settings.SettingsScreen;
import com.mare5x.chargehockey.editor.EditorScreen;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelectorScreen;
import com.mare5x.chargehockey.menus.BaseMenuScreen;


class GameMenuScreen extends BaseMenuScreen {
    private final GameScreen parent_screen;

    GameMenuScreen(final ChargeHockeyGame game, final GameScreen parent_screen, final Level level) {
        super(game);

        this.parent_screen = parent_screen;

        TextButton return_button = make_text_button("PLAY");
        return_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(parent_screen);
            }
        });

        TextButton edit_button = make_text_button("EDIT");
        edit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EditorScreen(game, level));
                parent_screen.dispose();
            }
        });

        TextButton save_button = make_text_button("SAVE CURRENT STATE");
        save_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parent_screen.save_charge_state(Level.SAVE_TYPE.CUSTOM);
            }
        });

        TextButton load_button = make_text_button("LOAD LAST SAVE");
        load_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!parent_screen.load_charge_state(Level.SAVE_TYPE.CUSTOM)) {
                    // probably the file doesn't exist
                    TextNotification notification = new TextNotification(game, stage, "FAILED TO LOAD LAST SAVE");
                    notification.show();
                }
            }
        });

        TextButton restart_button = make_text_button("RESTART LEVEL");
        restart_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parent_screen.restart_level();
            }
        });

        TextButton selector_button = make_text_button("SELECT LEVEL");
        selector_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectorScreen(game, level.get_type()));
                parent_screen.dispose();
            }
        });

        TextButton main_menu_button = make_text_button("MAIN MENU");
        main_menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu_screen);
                parent_screen.dispose();
            }
        });

        TextButton settings_button = make_text_button("SETTINGS");
        settings_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game, parent_screen));
            }
        });

        table.add(return_button).pad(15).width(Value.percentWidth(0.6f, table)).uniformX().fillX().row();
        if (level.get_type() == LEVEL_TYPE.CUSTOM) table.add(edit_button).pad(15).uniformX().fillX().row();
        table.add(save_button).pad(15).uniformX().fillX().row();
        table.add(load_button).pad(15).uniformX().fillX().row();
        table.add(restart_button).pad(15).uniformX().fillX().row();
        table.add(selector_button).pad(15).uniformX().fillX().row();
        table.add(main_menu_button).pad(15).uniformX().fillX().row();
        table.add(settings_button).pad(15).uniformX().fillX();
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
