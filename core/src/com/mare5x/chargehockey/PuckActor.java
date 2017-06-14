package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;


class PuckActor extends ChargeActor {
    private final Sprite velocity_sprite, acceleration_sprite;
    private final Sprite path_px;

    private final Vector2 velocity_vec = new Vector2(), acceleration_vec = new Vector2();

    private static boolean draw_velocity = false, draw_acceleration = false;
    private static boolean trace_path = false;

    private static final float _MAX_LENGTH = ChargeHockeyGame.WORLD_WIDTH;
    private static final float _MIN_VEC_HEIGHT = 0.6f;
    private static final float _VEC_HEIGHT_SCL = 1.5f;  // manually check blank_vector.png arrow's tail height

    private GRID_ITEM collision = GRID_ITEM.NULL;

    PuckActor(ChargeHockeyGame game, CHARGE charge_type, DragCallback drag_callback) {
        super(game, charge_type, drag_callback);

        removeListener(getListeners().first());  // disables dragging

        velocity_sprite = new Sprite(game.sprites.findRegion("blank_vector"));
        velocity_sprite.setColor(game.skin.getColor("green"));
        acceleration_sprite = new Sprite(game.sprites.findRegion("blank_vector"));
        acceleration_sprite.setColor(game.skin.getColor("purple"));

        path_px = new Sprite(game.skin.getRegion("pixels/px_white"));
        path_px.setSize(getWidth() / 5, getHeight() / 5);

        reset_vectors();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (draw_velocity) velocity_sprite.draw(batch);
        if (draw_acceleration) acceleration_sprite.draw(batch);
    }

    void draw_trace_path_point(Batch batch) {
        path_px.setPosition(getX(Align.center), getY(Align.center));
        path_px.draw(batch);
    }

    void reset_vectors() {
        velocity_vec.setZero();
        acceleration_vec.setZero();

        velocity_sprite.setSize(0, 0);
        acceleration_sprite.setSize(0, 0);
    }

    Vector2 get_velocity() {
        return velocity_vec;
    }

    Vector2 get_acceleration() {
        return acceleration_vec;
    }

    void set_velocity(float x, float y) {
        velocity_vec.set(x, y);
        if (draw_velocity) {
            if (velocity_vec.isZero(10e-5f)) {
                velocity_sprite.setSize(0, 0);
                return;
            }
            float len = Math.min(velocity_vec.len() / 2, _MAX_LENGTH);
            float height = Math.max(len / (_MAX_LENGTH), _MIN_VEC_HEIGHT);  // sprite width
            height *= _VEC_HEIGHT_SCL;
            velocity_sprite.setBounds(getX(Align.center), getY(Align.center) - height / 2, len, height);
            velocity_sprite.setOrigin(0, height / 2);  // rotate around the center of the puck
            velocity_sprite.setRotation(velocity_vec.angle());
        }
    }

    void set_acceleration(float x, float y) {
        acceleration_vec.set(x, y);
        if (draw_acceleration) {
            if (acceleration_vec.isZero(10e-5f)) {
                acceleration_sprite.setSize(0, 0);
                return;
            }
            float len = Math.min(acceleration_vec.len() / 4, _MAX_LENGTH);
            float height = Math.max(len / (_MAX_LENGTH), _MIN_VEC_HEIGHT);  // sprite width
            height *= _VEC_HEIGHT_SCL;
            acceleration_sprite.setBounds(getX(Align.center), getY(Align.center) - height / 2, len, height);
            acceleration_sprite.setOrigin(0, height / 2);  // rotate around the center of the puck
            acceleration_sprite.setRotation(acceleration_vec.angle());
        }
    }

    static void set_draw_velocity(boolean draw) {
        draw_velocity = draw;
    }

    static void set_draw_acceleration(boolean draw) {
        draw_acceleration = draw;
    }

    static void set_trace_path(boolean val) {
        trace_path = val;
    }

    static boolean get_trace_path() {
        return trace_path;
    }

    GRID_ITEM get_collision() {
        return collision;
    }

    void set_collision(GRID_ITEM collision) {
        this.collision = collision;
    }
}
