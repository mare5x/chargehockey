package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.ChargeHockeyGame;

class FilePickerScreen extends BaseMenuScreen {
    interface FilePickerCallback {
        void on_result(FileHandle path);
    }

    private final Screen parent_screen;

    public FilePickerScreen(ChargeHockeyGame game, Screen parent_screen, final FilePickerCallback callback) {
        this(game, parent_screen, callback, null);
    }

    FilePickerScreen(ChargeHockeyGame game, Screen parent_screen, final FilePickerCallback callback, FilePicker.FileFilter filter) {
        super(game);

        this.parent_screen = parent_screen;

        final Label path_label = new Label("", game.skin);
        path_label.setFontScale(0.75f);
        path_label.setWrap(true);

        final FilePicker file_picker;
        if (filter != null)
            file_picker = game.get_file_picker(filter);
        else
            file_picker = game.get_file_picker();
        file_picker.set_event_listener(new FilePicker.EventListener() {
            @Override
            public void dir_changed(FileHandle path) {
                path_label.setText(path.file().getAbsolutePath());
            }
        });
        path_label.setText(file_picker.get_current_path().file().getAbsolutePath());

        // get file storage access permission on android
        PermissionTools permission_tools = game.get_permission_tools();
        if (!permission_tools.check_storage_permission()) {
            permission_tools.request_storage_permission(new PermissionTools.RequestCallback() {
                public void granted() {
                    file_picker.refresh();
                }

                public void denied() {
                    back_key_pressed();
                }
            });
        }

        TextButton back_button = make_text_button("BACK", false);
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                file_picker.show_parent_dir();
            }
        });

        TextButton result_button = make_text_button("SELECT", false);
        result_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FileHandle selected_path = file_picker.get_selected_path();
                if (selected_path.file().isFile() && file_picker.is_valid(selected_path))
                    callback.on_result(selected_path);
                else
                    callback.on_result(file_picker.get_current_path());
                back_key_pressed();
            }
        });

        add_back_button(2);
        table.add(path_label).colspan(2).pad(15).width(Value.percentWidth(1, table)).row();
        table.add(file_picker.get_display()).colspan(2).pad(15).expand().fill().row();
        table.add(back_button).pad(15).fillX();
        table.add(result_button).pad(15).expandX().fillX();
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
