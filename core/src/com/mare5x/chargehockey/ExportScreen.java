package com.mare5x.chargehockey;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

class ExportScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;

    private final FilePickerScreen.FilePickerCallback callback;
    private static final FilePicker.FileFilter filter = new FilePicker.FileFilter() {
        @Override
        public boolean is_valid(FileHandle path) {
            return path.isDirectory();
        }
    };

    ExportScreen(final ChargeHockeyGame game) {
        super(game);

        this.game = game;
        final LevelSelector selector = new LevelSelector(game, LEVEL_TYPE.CUSTOM);

        callback = new FilePickerScreen.FilePickerCallback() {
            @Override
            public void on_back() {
                game.setScreen(new ExportScreen(game));
            }

            @Override
            public void on_result(FileHandle path) {

            }
        };

        TextButton export_button = new TextButton("EXPORT SELECTED", game.skin);
        export_button.pad(10);
        export_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!selector.is_selected())  // something has to be selected
                    return;  // todo add a notification explanation

                game.setScreen(new FilePickerScreen(game, callback, filter));
            }
        });

        TextButton export_all_button = new TextButton("EXPORT ALL", game.skin);
        export_all_button.pad(10);
        export_all_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new FilePickerScreen(game, callback, filter));
            }
        });

        add_back_button(game.skin);
        table.add(selector.get_selector_table()).pad(15).expand().fill().row();
        table.add(export_button).pad(15).width(Value.percentWidth(0.6f, table)).row();
        table.add(export_all_button).pad(15).width(Value.percentWidth(0.6f, table));
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(new CustomMenuScreen(game));
    }

    @Override
    public void hide() {
        dispose();
    }
}
