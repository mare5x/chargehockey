package com.mare5x.chargehockey.notifications;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.mare5x.chargehockey.ChargeHockeyGame;

import static com.mare5x.chargehockey.menus.BaseMenuScreen.MIN_BUTTON_HEIGHT;

public class EditorPaintTipNotification extends Notification {
    public EditorPaintTipNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        Label message = get_label("TIP: LONG PRESS A TILE TO START PAINTING");
        Image paint_img = new Image(game.skin.getDrawable("edit_on"));

        add(message).space(15).width(get_label_width());
        add(paint_img).space(15).size(MIN_BUTTON_HEIGHT);
    }

    private Value get_label_width() {
        return new Value() {
            @Override
            public float get(Actor context) {
                return Math.min(context.getWidth(), getMaxWidth() * 0.8f);
            }
        };
    }
}
