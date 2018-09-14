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


public class NoChargesNotification extends Notification {
    public NoChargesNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        Label text_label = get_label("FIRST, ADD SOME: ");

        Image pos_charge = new Image(game.skin.getRegion("sprite_charge_pos"));
        pos_charge.setScaling(Scaling.fit);
        Image neg_charge = new Image(game.skin.getRegion("sprite_charge_neg"));
        neg_charge.setScaling(Scaling.fit);

        add(text_label).width(get_label_width());
        add(pos_charge).space(CELL_PAD).size(IMAGE_FONT_SIZE);
        add(neg_charge).size(IMAGE_FONT_SIZE);
    }

    private Value get_label_width() {
        return new Value() {
            @Override
            public float get(Actor context) {
                return Math.min(context.getWidth(), getMaxWidth() * 0.9f - 2 * IMAGE_FONT_SIZE);
            }
        };
    }
}
