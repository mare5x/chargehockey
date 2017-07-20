package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;


class TextNotification extends Notification {
    TextNotification(ChargeHockeyGame game, Stage stage, String message) {
        super(game, stage);

        Label text_label = new Label(message, game.skin, "borderless");
        text_label.setWrap(true);

        add(text_label).width(get_text_width()).fill().center();
    }

    private Value get_text_width() {
        return new Value() {
            @Override
            public float get(Actor context) {
                return Math.min(context.getWidth(), getWidth());
            }
        };
    }

    @Override
    void show() {
        stage.addActor(this);
        addAction(get_action());
        pack();
    }
}
