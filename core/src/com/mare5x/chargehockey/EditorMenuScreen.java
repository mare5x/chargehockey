package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class EditorMenuScreen implements Screen {
    private final ChargeHockeyGame game;

    private final InputDialog input_dialog;
    private final LevelSelector level_selector;

    private final Stage stage;

    private enum DIALOG_BUTTON {
        CANCEL, CONFIRM
    }

    public EditorMenuScreen(final ChargeHockeyGame game) {
        this.game = game;

        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);
        stage.setDebugAll(true);

        input_dialog = new InputDialog("ADD LEVEL", game.skin);

        level_selector = new LevelSelector(game);

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("back_button", "clicked");
                game.setScreen(game.menu_screen);
                dispose();
            }
        });
        back_button.pad(10);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("play_button", "clicked");

                final String name = level_selector.get_selected_name();
                if (!name.equals("")) {
                    game.setScreen(new EditorScreen(game, name));
                    dispose();
                }
            }
        });
        play_button.pad(10);

        Button add_button = new Button(game.skin, "add");
        add_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("add_button", "clicked");

                input_dialog.show(stage);
            }
        });
        add_button.pad(10);

        Button remove_button = new Button(game.skin, "remove");
        remove_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("remove_button", "clicked");
                level_selector.remove_selected_level();
            }
        });
        remove_button.pad(10);

        Table left_subtable = new Table();

        Value twidth = Value.percentWidth(0.8f, left_subtable);

        left_subtable.add(back_button).padBottom(30).size(twidth, Value.percentWidth(0.4f, left_subtable)).expandX().top().row();
        left_subtable.add(add_button).padBottom(30).size(twidth).expand().row();
        left_subtable.add(remove_button).size(twidth).expand();

        Table table = new Table();
        table.setFillParent(true);

        table.pad(50 * game.DENSITY, 15 * game.DENSITY, 50 * game.DENSITY, 15 * game.DENSITY);

        table.add(left_subtable).pad(15).width(Value.percentWidth(0.25f, table)).expandY().fillY();
        table.add(level_selector.get_display()).pad(15).expand().fill();
        table.row();
        table.add(play_button).pad(15).colspan(2).size(Value.percentWidth(0.3f, table));

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private class InputDialog extends Dialog {
        private final TextField name_input;

        public InputDialog(String title, Skin skin) {
            super(title, skin);

            name_input = new TextField("LEVEL NAME", game.skin);
            getContentTable().add(name_input).pad(15 * game.DENSITY).width(Value.percentWidth(0.8f, this)).expandX();

            Button cancel_button = new Button(game.skin, "cancel");
            cancel_button.pad(10);
            Button confirm_button = new Button(game.skin, "confirm");
            confirm_button.pad(10);

            button(cancel_button, DIALOG_BUTTON.CANCEL);
            button(confirm_button, DIALOG_BUTTON.CONFIRM);

            getTitleTable().pad(10 * game.DENSITY);
            getContentTable().pad(10 * game.DENSITY);
            getButtonTable().pad(10 * game.DENSITY);

            Value size = Value.percentHeight(3, name_input);
            getButtonTable().getCell(cancel_button).size(size);
            getButtonTable().getCell(confirm_button).size(size);
        }

        @Override
        public Dialog show(Stage stage) {
            super.show(stage);

            name_input.selectAll();  // select everything, so it's ready to be overwritten
            stage.setKeyboardFocus(name_input);
            name_input.getOnscreenKeyboard().show(true);

            return this;
        }

        @Override
        protected void result(Object object) {
            if (object.equals(DIALOG_BUTTON.CONFIRM)) {
                level_selector.add_level(name_input.getText());
            }

            name_input.getOnscreenKeyboard().show(false);
            hide();
        }
    }
}