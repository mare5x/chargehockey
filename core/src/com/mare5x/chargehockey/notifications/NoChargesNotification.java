package com.mare5x.chargehockey.notifications;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.mare5x.chargehockey.ChargeHockeyGame;


public class NoChargesNotification extends Notification {
    private static final float img_size = ChargeHockeyGame.FONT_SIZE * ChargeHockeyGame.DENSITY * 1.5f;

    public NoChargesNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        Label text_label = get_label("FIRST, ADD SOME: ");

        Image pos_charge = new Image(game.sprites.findRegion("charge_pos"));
        Image neg_charge = new Image(game.sprites.findRegion("charge_neg"));

        add(text_label).width(get_label_width());
        add(pos_charge).space(15).size(img_size);
        add(neg_charge).size(img_size);
    }

    private Value get_label_width() {
        return new Value() {
            @Override
            public float get(Actor context) {
                return Math.min(context.getWidth(), getMaxWidth() * 0.9f - 2 * img_size);
            }
        };
    }
}
