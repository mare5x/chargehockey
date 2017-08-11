package com.mare5x.chargehockey.notifications;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.mare5x.chargehockey.ChargeHockeyGame;


public class NoChargesNotification extends Notification {

    public NoChargesNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        Label text_label = new Label("FIRST, ADD SOME  ", game.skin, "borderless");

        Image pos_charge = new Image(game.sprites.findRegion("pos_red64"));
        Image neg_charge = new Image(game.sprites.findRegion("neg_blue64"));

        add(text_label).padRight(15).expandX().fill();
        add(pos_charge).padRight(15).size(Value.percentHeight(1, text_label)).expandX().center();
        add(neg_charge).size(Value.percentHeight(1, text_label)).expandX().center();
    }

    @Override
    public void show(float time) {
        stage.addActor(this);
        addAction(get_action(time));
        pack();
    }
}
