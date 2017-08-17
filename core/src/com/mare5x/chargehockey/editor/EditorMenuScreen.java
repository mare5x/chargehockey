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
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelector;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.notifications.EditorNoLevelsNotification;


class EditorMenuScreen extends BaseMenuScreen {
    private final InputDialog input_dialog;
    private final LevelSelector level_selector;

    private enum DIALOG_BUTTON {
        CANCEL, CONFIRM
    }

    EditorMenuScreen(final ChargeHockeyGame game) {
        this(game, null);
    }

    EditorMenuScreen(final ChargeHockeyGame game, final Level selected_level) {
        super(game);

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
                    set_screen(new EditorScreen(game, level));
                } else if (level_selector.is_empty()) {
                    remove_notification();
                    notification = new EditorNoLevelsNotification(game, stage);
                    notification.show();
                } else {
                    show_notification("FIRST, SELECT THE LEVEL YOU WISH TO EDIT", 2);
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

        Value twidth = Value.percentWidth(0.9f, left_subtable);

        left_subtable.add(add_button).padBottom(30).size(twidth).expand().row();
        left_subtable.add(remove_button).size(twidth).expand();

        add_back_button();
        table.add(left_subtable).pad(15).width(Value.percentWidth(0.25f, table)).expandY().fillY();
        table.add(level_selector.get_selector_table()).pad(15).expand().fill().row();
        table.add(play_button).pad(15).colspan(2).size(Value.percentWidth(0.3f, table));

        if (level_selector.is_empty()) {
            remove_notification();
            notification = new EditorNoLevelsNotification(game, stage);
            notification.show();
        } else if (selected_level != null) {
            table.validate();
            level_selector.select(selected_level.get_name());
        }
    }

    @Override
    protected void back_key_pressed() {
        set_screen(new CustomMenuScreen(game));
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

            pad(ChargeHockeyGame.FONT_SIZE * 1.5f * ChargeHockeyGame.DENSITY);

            getTitleTable().clear();

            name_input = new TextField("LEVEL NAME", game.skin);
            getContentTable().add(name_input).space(15).width(get_input_width());

            Button cancel_button = new Button(game.skin, "cancel");
            cancel_button.pad(10);
            Button confirm_button = new Button(game.skin, "confirm");
            confirm_button.pad(10);

            getButtonTable().defaults().size(Value.percentWidth(0.125f, table)).padTop(15).space(15).expandX().center();
            button(cancel_button, DIALOG_BUTTON.CANCEL);
            button(confirm_button, DIALOG_BUTTON.CONFIRM);
        }

        @Override
        public float getPrefWidth() {
            return stage.getWidth() * 0.8f;
        }

        private Value get_input_width() {
            return new Value() {
                @Override
                public float get(Actor context) {
                    return getPrefWidth() - getPadLeft() - getPadRight();
                }
            };
        }

        @Override
        public Dialog show(Stage stage) {
            super.show(stage);

            super.setPosition(stage.getWidth() / 2 - getWidth() / 2, (float) (stage.getHeight() * 0.8 - getHeight()));

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
