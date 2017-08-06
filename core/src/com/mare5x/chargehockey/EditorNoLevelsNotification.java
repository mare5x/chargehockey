package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;


/** "ADD A NEW LEVEL USING '+'.\nREMOVE A LEVEL USING '-'. */
class EditorNoLevelsNotification extends Notification {
    EditorNoLevelsNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        Label add_label = new Label("ADD A NEW LEVEL USING ", game.skin, "borderless");
        Label remove_label = new Label("REMOVE A LEVEL USING ", game.skin, "borderless");

        Image add_img = new Image(game.skin.getRegion("add_green64"));
        Image remove_img = new Image(game.skin.getRegion("remove_green64"));

        add(add_label).padRight(15).expandX().fill();
        add(add_img).size(Value.percentHeight(1, add_label));
        row();
        add(remove_label).padRight(15).expandX().fill();
        add(remove_img).size(Value.percentHeight(1, remove_label));
    }

    @Override
    void show() {
        stage.addActor(this);
        addAction(get_action(2.5f));
        pack();
    }
}
