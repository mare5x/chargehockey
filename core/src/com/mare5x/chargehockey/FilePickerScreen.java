package com.mare5x.chargehockey;


import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

class FilePickerScreen extends BaseMenuScreen {
    interface FilePickerCallback {
        void on_result(FileHandle path);
    }

    private final ChargeHockeyGame game;
    private final Screen parent_screen;

    FilePickerScreen(ChargeHockeyGame game, Screen parent_screen, final FilePickerCallback callback) {
        this(game, parent_screen, callback, null);
    }

    FilePickerScreen(ChargeHockeyGame game, Screen parent_screen, final FilePickerCallback callback, FilePicker.FileFilter filter) {
        super(game);

        this.game = game;
        this.parent_screen = parent_screen;

        final Label path_label = new Label("", game.skin);
        path_label.setFontScale(0.75f);
        path_label.setWrap(true);

        final FilePicker file_picker;
        if (filter != null)
            file_picker = new FilePicker(game, filter);
        else
            file_picker = new FilePicker(game);
        file_picker.set_event_listener(new FilePicker.EventListener() {
            @Override
            public void dir_changed(FileHandle path) {
                path_label.setText(path.file().getAbsolutePath());
            }
        });
        path_label.setText(file_picker.get_selected_path().file().getAbsolutePath());

        TextButton up_button = new TextButton("UP", game.skin);
        up_button.pad(10);
        up_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                file_picker.show_parent_dir();
            }
        });

        TextButton result_button = new TextButton("SELECT", game.skin);
        result_button.pad(10);
        result_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.on_result(file_picker.get_selected_path());
                back_key_pressed();
            }
        });

        add_back_button(game.skin, 2);
        table.add(path_label).colspan(2).pad(15).width(Value.percentWidth(1, table)).row();
        table.add(file_picker.get_display()).colspan(2).pad(15).expand().fill().row();
        table.add(up_button).pad(15).width(Value.percentWidth(0.15f, table)).fill();
        table.add(result_button).pad(15).expandX().fill();
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
