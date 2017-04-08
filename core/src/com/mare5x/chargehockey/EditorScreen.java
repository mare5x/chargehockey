package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class EditorScreen implements Screen {
    final public ChargeHockeyGame game;

    final private List<String> list;
    final InputDialog input_dialog;

    private Stage stage;

    public EditorScreen(final ChargeHockeyGame game) {
        this.game = game;

        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);

        input_dialog = new InputDialog("ADD LEVEL", game.skin);

        list = new List<String>(game.skin);
        init_list_items();
        ScrollPane scroll_pane = new ScrollPane(list);

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("back_button", "clicked");
                game.setScreen(game.menu_screen);
            }
        });
        back_button.pad(10);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("play_button", "clicked");
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
                int selected_idx = list.getSelectedIndex();
                if (selected_idx != -1) {
                    list.getItems().removeIndex(selected_idx);
                }
            }
        });
        remove_button.pad(10);

        Table left_subtable = new Table();

        left_subtable.add(back_button).pad(15).row();
        left_subtable.add(add_button).pad(15).row();
        left_subtable.add(remove_button).pad(15);

        Table table = new Table();
        table.setFillParent(true);

        table.add(left_subtable).pad(15);
        table.add(scroll_pane).pad(15).padRight(50).expand().fill();
        table.row();
        table.add(play_button).pad(15).colspan(2);

        stage.addActor(table);

        stage.setDebugAll(true);
    }

    private void init_list_items() {
        list.setItems("item1", "item2", "asdfasdfasdfsadfsdf");
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
        private TextField name_input;

        public InputDialog(String title, Skin skin) {
            super(title, skin);

            name_input = new TextField("LEVEL NAME", game.skin);
            getContentTable().add(name_input).pad(15).expand().fill();

            Button cancel_button = new Button(game.skin, "cancel");
            cancel_button.pad(10);
            Button confirm_button = new Button(game.skin, "confirm");
            confirm_button.pad(10);

            getButtonTable().pad(5);

            button(cancel_button, 0);
            button(confirm_button, 1);
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
            if (object.equals(1)) {
                list.getItems().add(name_input.getText());
            }

            name_input.getOnscreenKeyboard().show(false);
            hide();
        }
    }
}
