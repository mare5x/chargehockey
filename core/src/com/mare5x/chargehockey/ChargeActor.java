package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;


public class ChargeActor extends Actor {
    public enum CHARGE {
        POSITIVE, NEGATIVE, PUCK
    }

    public static class ChargeState {
        public CHARGE type;
        public float x, y;

        public ChargeState(CHARGE type, float x, float y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public abstract static class DragCallback {
        public abstract void out_of_bounds(ChargeActor charge);
        void drag(ChargeActor charge) {}
    }

    private final CHARGE charge_type;
    final Sprite sprite;

    private static final float SIZE = 1.33f;  // TODO make this adjustable in settings
    private float radius = 0.5f;
    private static final float WEIGHT = 9.1e-31f;  // kg
    private static final float ABS_CHARGE = 1.6e-19f;  // Coulombs

    public ChargeActor(final ChargeHockeyGame game, CHARGE charge_type, final DragCallback drag_callback) {
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

        set_size(SIZE);

        if (drag_callback != null) {
            DragListener drag_listener = new DragListener() {
                @Override
                public void drag(InputEvent event, float dx, float dy, int pointer) {
                    moveBy(dx - getTouchDownX(), dy - getTouchDownY());
                    drag_callback.drag(ChargeActor.this);
                }

                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer) {
                    // if the actor was dragged out the stage, remove it
                    if (!getStage().getCamera().frustum.pointInFrustum(get_x(), get_y(), 0) || !ChargeHockeyGame.WORLD_RECT.contains(get_x(), get_y())) {
                        drag_callback.out_of_bounds(ChargeActor.this);
                    }
                }
            };
            drag_listener.setTapSquareSize(getWidth() / 8);

            addListener(drag_listener);
        }
    }

    void set_size(float size) {
        radius = size / 2f;

        sprite.setSize(size, size);
        setBounds(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
        sprite.setOriginCenter();
        setOrigin(Align.center);
    }

    float get_x() {
        return getX() + radius;
    }

    float get_y() {
        return getY() + radius;
    }

    public void set_position(float center_x, float center_y) {
        setPosition(center_x - radius, center_y - radius);
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
    public void setPosition(float x, float y, int alignment) {
        super.setPosition(x, y, alignment);
        sprite.setPosition(getX(), getY());
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        super.setBounds(x, y, width, height);
        sprite.setBounds(x, y, width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        sprite.draw(batch, parentAlpha);
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

    /** Returns the vector from puck to this charge, taking the charge's polarity into account. */
    Vector2 get_vec(ChargeActor puck) {
        return new Vector2(puck.get_x(), puck.get_y()).sub(get_x(), get_y()).scl(get_direction());
    }

    ChargeState get_state() {
        return new ChargeState(charge_type, get_x(), get_y());
    }

    /** Circular collision detection with a rectangle. */
    boolean intersects(Rectangle rectangle) {
        float center_x = get_x();
        float center_y = get_y();

        // find the closest rectangle point to the circle (charge)
        float closest_x = MathUtils.clamp(center_x, rectangle.x, rectangle.x + rectangle.width);
        float closest_y = MathUtils.clamp(center_y, rectangle.y, rectangle.y + rectangle.height);

        float dx = center_x - closest_x;
        float dy = center_y - closest_y;

        // if the distance from circle to rectangle is less than the circle's radius, there is an intersection
        return (dx * dx + dy * dy) < (radius * radius);
    }
}
