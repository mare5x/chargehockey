package com.mare5x.chargehockey.notifications;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Align;
import com.mare5x.chargehockey.ChargeHockeyGame;


public class TextNotification extends Notification {
    public TextNotification(ChargeHockeyGame game, Stage stage, String message) {
        super(game, stage);

        Label text_label = new Label(message, game.skin, "borderless");
        text_label.setWrap(true);
        text_label.setAlignment(Align.center);

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
    public void show() {
        show(DEFAULT_SHOW_TIME);
    }

    public void show(float time) {
        stage.addActor(this);
        addAction(get_action(time));
        pack();
    }
}
