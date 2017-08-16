package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.game.CameraController;


public class ChargeActor extends Actor {
    public enum CHARGE {
        POSITIVE('p'),
        NEGATIVE('n'),
        PUCK('k');

        private char code;  // item abbreviation code used when writing to files
        CHARGE(char code) {
            this.code = code;
        }

        public static CHARGE from_code(char code) {
            switch (code) {
                case 'p': return POSITIVE;
                case 'n': return NEGATIVE;
                case 'k': return PUCK;
            }
            return null;
        }

        public char code() {
            return code;
        }
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
        public void drag(ChargeActor charge) {}
    }

    private final CHARGE charge_type;
    final Sprite sprite;

    private static float SIZE = 1.3f;  // min = 0.5, max = 2
    private float radius = SIZE / 2f;
    private static final float WEIGHT = 9.1e-31f;  // kg
    private static final float ABS_CHARGE = 1.6e-19f;  // Coulombs

    public ChargeActor(final ChargeHockeyGame game, CHARGE charge_type, final DragCallback drag_callback) {
        super();

        this.charge_type = charge_type;

        float charge_size = SIZE;

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
                charge_size = PuckActor.SIZE;  // make pucks always have the same size
                break;
        }

        set_size(charge_size);

        if (drag_callback != null) {
            DragListener drag_listener = new DragListener() {
                @Override
                public void drag(InputEvent event, float dx, float dy, int pointer) {
                    moveBy(dx - getTouchDownX(), dy - getTouchDownY());
                    drag_callback.drag(ChargeActor.this);
                }

                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer) {
                    Rectangle camera_rect = CameraController.get_camera_rect((OrthographicCamera) getStage().getCamera());
                    // if the actor was dragged below the camera into the bottom hud part, remove it
                    if (get_y() < camera_rect.getY() || !ChargeHockeyGame.WORLD_RECT.contains(get_x(), get_y()))
                        drag_callback.out_of_bounds(ChargeActor.this);
                }
            };
            drag_listener.setTapSquareSize(-1);

            addListener(drag_listener);
        }
    }

    public void reset_size() {
        set_size(SIZE);
    }

    private void set_size(float size) {
        // save the center position
        float x = get_x();
        float y = get_y();

        radius = size / 2f;

        setSize(size, size);
        set_position(x, y);  // re-center
        sprite.setOriginCenter();
        setOrigin(Align.center);
    }

    public float get_x() {
        return getX() + radius;
    }

    public float get_y() {
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
    public void setSize(float width, float height) {
        super.setSize(width, height);
        sprite.setSize(width, height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        sprite.draw(batch, parentAlpha);
    }

    public float get_charge() {
        return ABS_CHARGE * get_direction();
    }

    public float get_abs_charge() {
        return ABS_CHARGE;
    }

    private int get_direction() {
        return charge_type == CHARGE.POSITIVE || charge_type == CHARGE.PUCK ? 1 : -1;
    }

    CHARGE get_type() {
        return charge_type;
    }

    public float get_weight() {
        return WEIGHT;
    }

    /** Returns the vector from puck to this charge, taking the charge's polarity into account. */
    public Vector2 get_vec(ChargeActor puck) {
        return new Vector2(puck.get_x(), puck.get_y()).sub(get_x(), get_y()).scl(get_direction());
    }

    public ChargeState get_state() {
        return new ChargeState(charge_type, get_x(), get_y());
    }

    /** Circular collision detection with a rectangle. */
    public boolean intersects(Rectangle rectangle) {
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

    public boolean size_changed() {
        return !MathUtils.isEqual(radius * 2, SIZE, 0.001f);
    }

    public static void set_charge_size(float size) {
        SIZE = size;
    }
}
