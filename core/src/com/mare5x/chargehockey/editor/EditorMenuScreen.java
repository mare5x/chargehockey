package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.LevelSelector;
import com.mare5x.chargehockey.notifications.EditorNoLevelsNotification;
import com.mare5x.chargehockey.notifications.TextNotification;


class EditorMenuScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;

    private final InputDialog input_dialog;
    private final LevelSelector level_selector;

    private enum DIALOG_BUTTON {
        CANCEL, CONFIRM
    }

    EditorMenuScreen(final ChargeHockeyGame game) {
        super(game);

        this.game = game;

        input_dialog = new InputDialog("ADD LEVEL", game.skin);

        level_selector = new LevelSelector(game, LEVEL_TYPE.CUSTOM);

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
            }
        });
        back_button.pad(10);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Level level = level_selector.load_selected_level();
                if (level != null) {
                    game.setScreen(new EditorScreen(game, level));
                } else if (level_selector.is_empty()) {
                    EditorNoLevelsNotification notification = new EditorNoLevelsNotification(game, stage);
                    notification.show();
                } else {
                    TextNotification notification = new TextNotification(game, stage, "FIRST, SELECT THE LEVEL YOU WISH TO EDIT");
                    notification.show(2);
                }
            }
        });
        play_button.pad(10);

        Button add_button = new Button(game.skin, "add");
        add_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                input_dialog.show(stage);
            }
        });
        add_button.pad(10);

        Button remove_button = new Button(game.skin, "remove");
        remove_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                level_selector.remove_selected_level();
            }
        });
        remove_button.pad(10);

        Table left_subtable = new Table();

        Value twidth = Value.percentWidth(0.8f, left_subtable);

        left_subtable.add(back_button).padBottom(30).size(twidth, Value.percentWidth(0.4f, left_subtable)).expandX().top().row();
        left_subtable.add(add_button).padBottom(30).size(twidth).expand().row();
        left_subtable.add(remove_button).size(twidth).expand();

        table.pad(50 * ChargeHockeyGame.DENSITY, 15 * ChargeHockeyGame.DENSITY, 50 * ChargeHockeyGame.DENSITY, 15 * ChargeHockeyGame.DENSITY);

        table.add(left_subtable).pad(15).width(Value.percentWidth(0.25f, table)).expandY().fillY();
        table.add(level_selector.get_selector_table()).pad(15).expand().fill().row();
        table.add(play_button).pad(15).colspan(2).size(Value.percentWidth(0.3f, table));

        if (level_selector.is_empty()) {
            EditorNoLevelsNotification notification = new EditorNoLevelsNotification(game, stage);
            notification.show();
        }
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(new CustomMenuScreen(game));
    }

    @Override
    public void hide() {
        dispose();
    }

    private class InputDialog extends Dialog {
        private final TextField name_input;

        InputDialog(String title, Skin skin) {
            super(title, skin);

            setModal(true);
            setResizable(false);
            setMovable(false);

            pad(15 * ChargeHockeyGame.DENSITY);
            padTop(game.skin.getFont("font").getLineHeight() * 2);

//            getTitleTable().pad(game.skin.getFont("font").getLineHeight());
//            getTitleTable().clear();

            getContentTable().pad(15 * ChargeHockeyGame.DENSITY);

            name_input = new TextField("LEVEL NAME", game.skin);
            getContentTable().add(name_input).width(Value.percentWidth(0.8f, this)).expandX();

            Button cancel_button = new Button(game.skin, "cancel");
            cancel_button.pad(10);
            Button confirm_button = new Button(game.skin, "confirm");
            confirm_button.pad(10);

            button(cancel_button, DIALOG_BUTTON.CANCEL);
            button(confirm_button, DIALOG_BUTTON.CONFIRM);

            Value size = percent_width(0.2f);
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

        private Value percent_width(final float percent) {
            return new Value() {
                @Override
                public float get(Actor context) {
                    return percent * stage.getWidth();
                }
            };
        }
    }
}
