package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;


class NoChargesNotification extends Notification {
    private final Sprite pos_charge, neg_charge;
    private final Label text_label;

    NoChargesNotification(ChargeHockeyGame game, Stage stage) {
        super(game, stage);

        text_label = new Label("ADD SOME  ", game.skin, "borderless");

        float charge_size = text_label.getHeight();
        float charge_total_width = 2 * charge_size + charge_size / 2;

        text_label.setPosition(getX() + getWidth() / 2 - text_label.getWidth() / 2 - charge_total_width / 2,
                               getY() + getHeight() / 2 - text_label.getHeight() / 2);

        pos_charge = game.sprites.createSprite("pos_red64");
        pos_charge.setBounds(text_label.getX(Align.right), text_label.getY(), charge_size, charge_size);
        neg_charge = game.sprites.createSprite("neg_blue64");
        neg_charge.setBounds(pos_charge.getX() + pos_charge.getWidth() * 1.5f, pos_charge.getY(), charge_size, charge_size);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float alpha = getColor().a;  // the action changes alpha through getColor

        super.draw(batch, alpha * parentAlpha);

        text_label.draw(batch, parentAlpha * alpha);
        pos_charge.draw(batch, alpha);
        neg_charge.draw(batch, alpha);
    }

    @Override
    void show() {
        stage.addActor(this);
        addAction(get_action());
    }
}
