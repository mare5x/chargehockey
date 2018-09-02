package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.utils.Array;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.game.GameLogic;


public class PuckActor extends ForcePuckActor {
    private final Sprite velocity_sprite, acceleration_sprite;
    private final Sprite path_px;

    private final Vector2 velocity_vec = new Vector2(), acceleration_vec = new Vector2();

    private static boolean draw_velocity = false, draw_acceleration = false;
    private static boolean trace_path = false;

    private Array<Vector2> trace_path_history = new Array<Vector2>(16);  // holds the puck's position history for the past render frame

    private GameLogic.CollisionData collision = new GameLogic.CollisionData();

    private RepeatAction blink_collision_action = null;

    public PuckActor(ChargeHockeyGame game) {
        this(game, CHARGE.PUCK, null);
    }

    private PuckActor(ChargeHockeyGame game, CHARGE charge_type, DragCallback drag_callback) {
        super(game, charge_type, drag_callback);

        velocity_sprite = new Sprite(vector_region);
        velocity_sprite.setColor(game.skin.getColor("green"));
        acceleration_sprite = new Sprite(vector_region);
        acceleration_sprite.setColor(game.skin.getColor("purple"));

        path_px = new Sprite(game.skin.getRegion("pixels/px_white"));
        path_px.setSize(getWidth() / 5, getHeight() / 5);
        path_px.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);

        reset_vectors();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // draw the vectors
        if (draw_velocity) velocity_sprite.draw(batch);
        if (draw_acceleration) acceleration_sprite.draw(batch);

        // draw the sprite after the vectors, so it's on top
        if (blink_collision_action != null)
            sprite.setColor(getColor());
        super.draw(batch, parentAlpha);
    }

    public void draw_trace_path_history(Batch batch) {
        for (Vector2 point : trace_path_history) {
            path_px.setPosition(point.x - path_px.getWidth() / 2, point.y - path_px.getHeight() / 2);
            path_px.draw(batch);
        }
    }

    public void reset_trace_path_history() {
        trace_path_history.clear();
    }

    public void save_path_position() {
        trace_path_history.add(new Vector2(get_x(), get_y()));
    }

    private void reset_vectors() {
        velocity_vec.setZero();
        acceleration_vec.setZero();

        velocity_sprite.setSize(0, 0);
        acceleration_sprite.setSize(0, 0);

        super.reset_sprites();
    }

    public void reset() {
        stop_blinking();
        reset_trace_path_history();
        reset_vectors();
        collision.reset();
    }

    public Vector2 get_velocity() {
        return velocity_vec;
    }

    public Vector2 get_acceleration() {
        return acceleration_vec;
    }

    public void set_velocity(float x, float y) {
        velocity_vec.set(x, y);
        if (draw_velocity) {
            float len = Math.min(velocity_vec.len() / 2, _MAX_LENGTH);
            prepare_vector_sprite(velocity_sprite, velocity_vec, len);
        }
    }

    public void set_acceleration(float x, float y) {
        acceleration_vec.set(x, y);
        if (draw_acceleration) {
            float len = Math.min(acceleration_vec.len() / 4, _MAX_LENGTH);
            prepare_vector_sprite(acceleration_sprite, acceleration_vec, len);
        }
    }

    public static void set_draw_velocity(boolean draw) {
        draw_velocity = draw;
    }

    public static void set_draw_acceleration(boolean draw) {
        draw_acceleration = draw;
    }

    public static void set_trace_path(boolean val) {
        trace_path = val;
    }

    public static boolean get_trace_path() {
        return trace_path;
    }

    public GameLogic.CollisionData get_collision() {
        return collision;
    }

    public void set_collision(GameLogic.CollisionData collision) {
        this.collision = collision;
    }

    public void start_blinking() {
        blink_collision_action = Actions.forever(Actions.sequence(Actions.color(Color.RED, 0.5f), Actions.color(Color.WHITE, 0.5f)));
        addAction(blink_collision_action);
    }

    private void stop_blinking() {
        if (blink_collision_action != null)
            removeAction(blink_collision_action);
        blink_collision_action = null;
        setColor(Color.WHITE);
        sprite.setColor(getColor());
    }
}
