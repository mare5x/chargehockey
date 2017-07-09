package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;


class PuckActor extends ChargeActor {
    private final TextureAtlas.AtlasRegion vector_region;
    private final Sprite velocity_sprite, acceleration_sprite;
    private final Sprite path_px;

    private final Vector2 velocity_vec = new Vector2(), acceleration_vec = new Vector2();

    private final ObjectMap<ChargeActor, Sprite> force_sprites = new ObjectMap<ChargeActor, Sprite>();

    private static boolean draw_velocity = false, draw_acceleration = false, draw_forces = false;
    private static boolean trace_path = false;

    private Array<Vector2> trace_path_history = new Array<Vector2>(16);  // holds the puck's position history for the past render frame

    // vector sprite settings
    private static final float _MAX_LENGTH = ChargeHockeyGame.WORLD_WIDTH;
    private static final float _MIN_VEC_HEIGHT = 0.6f;
    private static final float _VEC_HEIGHT_SCL = 1.5f;  // manually check blank_vector.png arrow's tail height

    // force vector colors
    private static final Color POS_RED = new Color(1, 0, 0, 1);
    private static final Color NEG_BLUE = new Color(0, 0.58f, 1, 1);

    private GRID_ITEM collision = GRID_ITEM.NULL;

    private RepeatAction blink_collision_action = null;

    PuckActor(ChargeHockeyGame game, CHARGE charge_type, DragCallback drag_callback) {
        super(game, charge_type, drag_callback);

        removeListener(getListeners().first());  // disables dragging

        vector_region = game.sprites.findRegion("blank_vector");
        velocity_sprite = new Sprite(vector_region);
        velocity_sprite.setColor(game.skin.getColor("green"));
        acceleration_sprite = new Sprite(vector_region);
        acceleration_sprite.setColor(game.skin.getColor("purple"));

        path_px = new Sprite(game.skin.getRegion("pixels/px_white"));
        path_px.setSize(getWidth() / 5, getHeight() / 5);

        reset_vectors();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // draw the vectors
        if (draw_velocity) velocity_sprite.draw(batch);
        if (draw_acceleration) acceleration_sprite.draw(batch);
        if (draw_forces) {
            for (Sprite force_sprite : force_sprites.values())
                force_sprite.draw(batch);
        }

        // draw the sprite after the vectors, so it's on top
        if (blink_collision_action != null)
            sprite.setColor(getColor());
        super.draw(batch, parentAlpha);
    }

    void draw_trace_path_history(Batch batch) {
        for (Vector2 point : trace_path_history) {
            path_px.setPosition(point.x, point.y);
            path_px.draw(batch);
        }
    }

    void reset_trace_path_history() {
        trace_path_history.clear();
    }

    void save_path_position() {
        trace_path_history.add(new Vector2(getX(Align.center), getY(Align.center)));
    }

    private void reset_vectors() {
        velocity_vec.setZero();
        acceleration_vec.setZero();

        velocity_sprite.setSize(0, 0);
        acceleration_sprite.setSize(0, 0);

        for (Sprite force_sprite : force_sprites.values())
            force_sprite.setSize(0, 0);
    }

    void reset() {
        stop_blinking();
        reset_trace_path_history();
        reset_vectors();
        set_collision(GRID_ITEM.NULL);
    }

    Vector2 get_velocity() {
        return velocity_vec;
    }

    Vector2 get_acceleration() {
        return acceleration_vec;
    }

    private void prepare_vector_sprite(Sprite sprite, Vector2 vector, float length) {
        if (MathUtils.isZero(length, 10e-5f)) {
            sprite.setSize(0, 0);
            return;
        }
        float height = Math.max(length / (_MAX_LENGTH), _MIN_VEC_HEIGHT);  // sprite width
        height *= _VEC_HEIGHT_SCL;
        sprite.setBounds(getX(Align.center), getY(Align.center) - height / 2, length, height);
        sprite.setOrigin(0, height / 2);  // rotate around the center of the puck
        sprite.setRotation(vector.angle());
    }

    void set_velocity(float x, float y) {
        velocity_vec.set(x, y);
        if (draw_velocity) {
            float len = Math.min(velocity_vec.len() / 2, _MAX_LENGTH);
            prepare_vector_sprite(velocity_sprite, velocity_vec, len);
        }
    }

    void set_acceleration(float x, float y) {
        acceleration_vec.set(x, y);
        if (draw_acceleration) {
            float len = Math.min(acceleration_vec.len() / 4, _MAX_LENGTH);
            prepare_vector_sprite(acceleration_sprite, acceleration_vec, len);
        }
    }

    private Sprite get_force_sprite(ChargeActor charge, Vector2 force) {
        Sprite force_sprite = force_sprites.get(charge, new Sprite(vector_region));
        float len = Math.min(force.scl(1 / get_weight()).len(), _MAX_LENGTH);
        prepare_vector_sprite(force_sprite, force, len);
        if (charge.get_type() == CHARGE.POSITIVE)
            force_sprite.setColor(POS_RED);
        else
            force_sprite.setColor(NEG_BLUE);
        force_sprite.setAlpha(Math.min(len / _MAX_LENGTH, 0.8f));
        return force_sprite;
    }

    static void set_draw_velocity(boolean draw) {
        draw_velocity = draw;
    }

    static void set_draw_acceleration(boolean draw) {
        draw_acceleration = draw;
    }

    static void set_draw_forces(boolean draw) {
        draw_forces = draw;
    }

    static boolean get_draw_forces() {
        return draw_forces;
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

    void start_blinking() {
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

    void set_force(ChargeActor charge, Vector2 force_vec) {
        force_sprites.put(charge, get_force_sprite(charge, force_vec));
    }

    void remove_force(ChargeActor charge) {
        force_sprites.remove(charge);
    }
}
