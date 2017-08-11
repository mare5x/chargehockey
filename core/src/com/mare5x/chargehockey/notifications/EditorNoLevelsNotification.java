package com.mare5x.chargehockey.notifications;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.mare5x.chargehockey.ChargeHockeyGame;


/** "ADD A NEW LEVEL USING '+'.\nREMOVE A LEVEL USING '-'. */
public class EditorNoLevelsNotification extends Notification {
    public EditorNoLevelsNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        Label add_label = new Label("ADD A NEW LEVEL USING ", game.skin, "borderless");
        add_label.setWrap(true);
        Label remove_label = new Label("REMOVE A LEVEL USING ", game.skin, "borderless");
        remove_label.setWrap(true);

        Image add_img = new Image(game.skin.getRegion("add_green64"));
        Image remove_img = new Image(game.skin.getRegion("remove_green64"));

        add(add_label).minWidth(getWidth() * 0.5f).width(getWidth() * 0.8f).maxWidth(getWidth() * 0.9f).padRight(15).expandX().fill();
        add(add_img).size(Value.percentHeight(1, add_label)).expandX().center();
        row();
        add(remove_label).minWidth(getWidth() * 0.5f).width(getWidth() * 0.8f).maxWidth(getWidth() * 0.9f).padRight(15).expandX().fill();
        add(remove_img).size(Value.percentHeight(1, remove_label)).expandX().center();
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
