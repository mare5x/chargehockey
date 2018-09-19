package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.utils.Array;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Grid;

import static com.mare5x.chargehockey.level.LevelFrameBuffer.ONE_TX;


public class PuckActor extends ForcePuckActor {
    private final VectorSprite velocity_sprite, acceleration_sprite;
    private final Sprite path_px;

    private final Vector2 velocity_vec = new Vector2();

    private static boolean draw_velocity = false, draw_acceleration = false;
    private static boolean trace_path = false;

    private static final float TRACE_DIST = 0.4f;  // world units; the distance between each trace point
    private final Vector2 tmp_vec = new Vector2();
    private final Vector2 prev_trace_pos = new Vector2();
    private final Array<Vector2> trace_path_history = new Array<Vector2>();  // holds the puck's position history for the past render frame
    private Array<Vector2> full_position_history;  // used only for debugging

    private Grid.GRID_ITEM collision = Grid.GRID_ITEM.NULL;

    private RepeatAction blink_collision_action = null;

    public PuckActor(ChargeHockeyGame game) {
        this(game, CHARGE.PUCK, null);
    }

    private PuckActor(ChargeHockeyGame game, CHARGE charge_type, DragCallback drag_callback) {
        super(game, charge_type, drag_callback);

        velocity_sprite = new VectorSprite(game);
        velocity_sprite.setColor(game.skin.getColor("ui_green_up"));
        velocity_sprite.setAlpha(0.75f);
        acceleration_sprite = new VectorSprite(game);
        acceleration_sprite.setColor(game.skin.getColor("purple"));
        acceleration_sprite.setAlpha(0.75f);

        path_px = game.create_sprite("px_white");
        path_px.setSize(ONE_TX, ONE_TX);
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

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);

        if (full_position_history == null)
            full_position_history = new Array<Vector2>();

        full_position_history.add(new Vector2(get_x(), get_y()));
        for (int i = 1; i < full_position_history.size; ++i)
            shapes.line(full_position_history.get(i-1), full_position_history.get(i));
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
        if (prev_trace_pos.isZero()) {
            prev_trace_pos.set(get_x(), get_y());
            trace_path_history.add(prev_trace_pos.cpy());
            return;
        }
        float x = get_x();
        float y = get_y();
        while (prev_trace_pos.dst2(x, y) >= TRACE_DIST * TRACE_DIST) {
            prev_trace_pos.add(tmp_vec.set(x, y).sub(prev_trace_pos).nor().scl(TRACE_DIST));
            trace_path_history.add(prev_trace_pos.cpy());
        }
    }

    /** Zeroes out all vectors, but does NOT remove them. */
    private void reset_vectors() {
        velocity_vec.setZero();

        velocity_sprite.zero();
        acceleration_sprite.zero();

        reset_sprites();
    }

    public void reset() {
        stop_blinking();
        reset_trace_path_history();
        prev_trace_pos.setZero();
        if (full_position_history != null) full_position_history.clear();
        reset_vectors();
        collision = Grid.GRID_ITEM.NULL;
    }

    public Vector2 get_velocity() {
        return velocity_vec;
    }

    public void set_velocity(Vector2 velocity) {
        velocity_vec.set(velocity);
        if (draw_velocity) {
            float len = Math.min(velocity_vec.len(), VectorSprite.MAX_LENGTH);
            velocity_sprite.prepare(get_x(), get_y(), velocity_vec.angle(), len);
        }
    }

    public void set_acceleration(Vector2 acceleration_vec) {
        if (draw_acceleration) {
            // the same length as a force vector sprite, which means it accurately shows the net
            // force vector
            float len = Math.min(acceleration_vec.len() * get_weight() * 4, VectorSprite.MAX_LENGTH);
            acceleration_sprite.prepare(get_x(), get_y(), acceleration_vec.angle(), len);
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

    public Grid.GRID_ITEM get_collision() {
        return collision;
    }

    public void set_collision(Grid.GRID_ITEM item) {
        collision = item;
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
