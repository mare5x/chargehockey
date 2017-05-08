package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

class PuckActor extends ChargeActor {
    private final Sprite velocity_sprite, acceleration_sprite;

    private final Vector2 velocity_vec = new Vector2(), acceleration_vec = new Vector2();

    private static boolean draw_velocity = false, draw_acceleration = false;

    private GRID_ITEM collision = GRID_ITEM.NULL;

    PuckActor(ChargeHockeyGame game, CHARGE charge_type, GameLogic game_logic) {
        super(game, charge_type, game_logic);

        removeListener(getListeners().first());

        velocity_sprite = new Sprite(game.skin.getRegion("px_green"));
        acceleration_sprite = new Sprite(game.skin.getRegion("px_purple"));

        reset_vectors();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (draw_velocity) velocity_sprite.draw(batch);
        if (draw_acceleration) acceleration_sprite.draw(batch);
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
            velocity_sprite.setBounds(getX(), getY(), velocity_vec.len(), getHeight());
            velocity_sprite.setRotation(velocity_vec.angle());
        }
    }

    void set_acceleration(float x, float y) {
        acceleration_vec.set(x, y);
        if (draw_acceleration) {
            acceleration_sprite.setBounds(getX(), getY(), acceleration_vec.len(), getHeight());
            acceleration_sprite.setRotation(acceleration_vec.angle());
        }
    }

    static void set_draw_velocity(boolean draw) {
        draw_velocity = draw;
    }

    static void set_draw_acceleration(boolean draw) {
        draw_acceleration = draw;
    }

    GRID_ITEM get_collision() {
        return collision;
    }

    void set_collision(GRID_ITEM collision) {
        this.collision = collision;
    }
}
