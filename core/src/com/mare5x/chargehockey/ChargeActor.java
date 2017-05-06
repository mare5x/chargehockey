package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;


enum CHARGE {
    POSITIVE, NEGATIVE, PUCK
}


class ChargeActor extends Actor {
    private final CHARGE charge_type;
    private final Sprite sprite;

    private static final float WEIGHT = 9.1e-31f;  // kg
    private static final float ABS_CHARGE = 1.6e-19f;  // Coulombs

    ChargeActor(final ChargeHockeyGame game, CHARGE charge_type) {
        super();

        this.charge_type = charge_type;

        sprite = new Sprite();
        switch (charge_type) {
            case POSITIVE:
                sprite.setRegion(game.sprites.findRegion("pos_red64"));
                break;
            case NEGATIVE:
                sprite.setRegion(game.sprites.findRegion("neg_blue64"));
                break;
            case PUCK:
                sprite.setRegion(game.sprites.findRegion("puck"));
                break;
        }

        sprite.setSize(1, 1);
        setBounds(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
        sprite.setOriginCenter();
        setOrigin(Align.center);

        DragListener drag_listener = new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                moveBy(x, y);
            }
        };
        drag_listener.setTapSquareSize(getWidth() / 4);

        addListener(drag_listener);
    }

    @Override
    public void moveBy(float x, float y) {
        super.moveBy(x, y);
        sprite.translate(x, y);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        sprite.setPosition(x, y);
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        super.setBounds(x, y, width, height);
        sprite.setBounds(x, y, width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        sprite.draw(batch);
    }

    float get_charge() {
        return ABS_CHARGE * get_direction();
    }

    float get_abs_charge() {
        return ABS_CHARGE;
    }

    private int get_direction() {
        return charge_type == CHARGE.POSITIVE || charge_type == CHARGE.PUCK ? 1 : -1;
    }

    CHARGE get_type() {
        return charge_type;
    }

    float get_weight() {
        return WEIGHT;
    }

    Vector2 get_vec(Vector2 puck_vec) {
        return new Vector2(puck_vec).sub(getX(Align.center), getY(Align.center)).scl(get_direction());
    }

    boolean overlaps(ChargeActor charge) {
        return get_rect().overlaps(charge.get_rect());
    }

    private Rectangle get_rect() {
        return sprite.getBoundingRectangle();
    }
}
