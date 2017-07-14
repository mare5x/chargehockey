package com.mare5x.chargehockey;


import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

class ExportScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;

    ExportScreen(ChargeHockeyGame game) {
        super(game);

        this.game = game;
        final LevelSelector selector = new LevelSelector(game, LEVEL_TYPE.CUSTOM);

        TextButton export_button = new TextButton("EXPORT SELECTED", game.skin);
        export_button.pad(10);
        export_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        TextButton export_all_button = new TextButton("EXPORT ALL", game.skin);
        export_all_button.pad(10);
        export_all_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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
