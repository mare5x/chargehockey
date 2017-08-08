package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.game.GameScreen;

class EditorSubScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;
    private final EditorScreen parent_screen;

    EditorSubScreen(final ChargeHockeyGame game, final EditorScreen parent_screen) {
        super(game);

        this.game = game;
        this.parent_screen = parent_screen;

        TextButton return_button = new TextButton("EDIT", game.skin);
        return_button.pad(10);
        return_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
            }
        });

        TextButton play_button = new TextButton("PLAY", game.skin);
        play_button.pad(10);
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, parent_screen.get_level()));
                parent_screen.dispose();
            }
        });

        TextButton selector_button = new TextButton("SELECT LEVEL", game.skin);
        selector_button.pad(10);
        selector_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EditorMenuScreen(game));
                parent_screen.dispose();
            }
        });

        TextButton menu_button = new TextButton("MAIN MENU", game.skin);
        menu_button.pad(10);
        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu_screen);
                parent_screen.dispose();
            }
        });

        table.add(return_button).pad(15).width(Value.percentWidth(0.6f, table)).uniform().fillX().row();
        table.add(play_button).pad(15).uniform().fillX().row();
        table.add(selector_button).pad(15).uniform().fillX().row();
        table.add(menu_button).pad(15).uniform().fillX();
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
