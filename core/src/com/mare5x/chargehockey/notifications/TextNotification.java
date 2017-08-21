package com.mare5x.chargehockey.notifications;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.mare5x.chargehockey.ChargeHockeyGame;


public class TextNotification extends Notification {
    public TextNotification(ChargeHockeyGame game, Stage stage, String message) {
        super(game, stage);

        Label text_label = get_label(message);

        add(text_label).width(get_label_width());
    }

    // this packs the label
    private Value get_label_width() {
        return new Value() {
            @Override
            public float get(Actor context) {
                return Math.min(context.getWidth(), getMaxWidth());
            }
        };
    }
}
