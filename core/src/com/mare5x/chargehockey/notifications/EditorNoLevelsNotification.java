package com.mare5x.chargehockey.notifications;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Scaling;
import com.mare5x.chargehockey.ChargeHockeyGame;

import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.IMAGE_FONT_SIZE;


public class EditorNoLevelsNotification extends Notification {
    public EditorNoLevelsNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        Label add_label = get_label("ADD A NEW LEVEL USING ");

        Image add_img = new Image(game.skin.getRegion("ui_add_up"));
        add_img.setScaling(Scaling.fit);

        columnDefaults(0).width(get_label_width()).space(CELL_PAD);
        columnDefaults(1).size(IMAGE_FONT_SIZE).space(CELL_PAD);
        add(add_label);
        add(add_img);
    }

    private Value get_label_width() {
        return new Value() {
            @Override
            public float get(Actor context) {
                return Math.min(context.getWidth(), getMaxWidth() * 0.9f - IMAGE_FONT_SIZE);
            }
        };
    }

    @Override
    public void show() {
        show(2.5f);
    }
}
