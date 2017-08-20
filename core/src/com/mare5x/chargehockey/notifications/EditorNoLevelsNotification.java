package com.mare5x.chargehockey.notifications;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.mare5x.chargehockey.ChargeHockeyGame;


/** "ADD A NEW LEVEL USING '+'.\nREMOVE A LEVEL USING '-'. */
public class EditorNoLevelsNotification extends Notification {
    private static final float img_size = ChargeHockeyGame.FONT_SIZE * ChargeHockeyGame.DENSITY * 1.5f;

    public EditorNoLevelsNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        Label add_label = get_label("ADD A NEW LEVEL USING ");
        Label remove_label = get_label("REMOVE A LEVEL USING ");

        Image add_img = new Image(game.skin.getRegion("add_up"));
        Image remove_img = new Image(game.skin.getRegion("remove_up"));

        columnDefaults(0).width(get_label_width());
        add(add_label).space(15);
        add(add_img).size(img_size);
        row();
        add(remove_label).space(15);
        add(remove_img).size(img_size);
    }

    private Value get_label_width() {
        return new Value() {
            @Override
            public float get(Actor context) {
                return Math.min(context.getWidth(), getMaxWidth() * 0.9f - img_size);
            }
        };
    }

    @Override
    public void show() {
        show(2.5f);
    }

    @Override
    public void show(float time) {
        stage.addActor(this);
        addAction(get_action(time));
        pack();
    }
}
