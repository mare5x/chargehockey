package com.mare5x.chargehockey.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.ChargeActor;
import com.mare5x.chargehockey.actors.ChargeActor.CHARGE;
import com.mare5x.chargehockey.actors.ChargeActor.ChargeState;
import com.mare5x.chargehockey.actors.ForcePuckActor;
import com.mare5x.chargehockey.actors.PuckActor;
import com.mare5x.chargehockey.actors.SymmetryToolActor;
import com.mare5x.chargehockey.level.Grid;
import com.mare5x.chargehockey.level.Grid.GRID_ITEM;
import com.mare5x.chargehockey.level.Level;

import static com.mare5x.chargehockey.settings.GameDefaults.PHYSICS_EPSILON;


public class GameLogic {
    private enum GAME_RESULT {
        WIN, LOSS, IN_PROGRESS
    }

    interface UIInterface {
        void result_win();
        void result_loss();
        ChargeActor.DragCallback get_charge_drag_callback();
    }

    private static class RectBounds {
        float top, left, bottom, right;
    }

    private static class Intersection {
        GRID_ITEM item = GRID_ITEM.NULL;
        Vector2 pos = new Vector2();  // the center of the puck, so that it's touching the RectBounds
        Vector2 normal = new Vector2();
        Vector2 intersection = new Vector2();  // the point of intersection between the puck and the RectBounds
        float time;

        Intersection() { }

        Intersection(float x, float y, float time, float norm_x, float norm_y, float intersection_x, float intersection_y) {
            pos.set(x, y);
            normal.set(norm_x, norm_y);
            intersection.set(intersection_x, intersection_y);
            this.time = time;
        }

        boolean valid() {
            return item != GRID_ITEM.NULL && time > 0;
        }

        Intersection reset() {
            item = GRID_ITEM.NULL;
            pos.setZero();
            normal.setZero();
            intersection.setZero();
            time = 0;
            return this;
        }

        Intersection set(Intersection other) {
            item = other.item;
            pos.set(other.pos);
            normal.set(other.normal);
            intersection.set(other.intersection);
            time = other.time;
            return this;
        }
    }

    private final ChargeHockeyGame game;
    private final Level level;
    private final UIInterface ui_interface;

    private final SymmetryToolActor symmetry_tool;

    private final Stage game_stage;

    private boolean is_playing = false;
    private boolean charge_state_changed = false;

    private static float GAME_SPEED = 1;  // game speed set by the user in settings
    private static final float dt = 0.01f;
    private float dt_accumulator = 0;  // http://gafferongames.com/game-physics/fix-your-timestep/

    private static final float FORCE_FACTOR = 100;

    private final Vector2 force_vec = new Vector2();
    // Use a cache to prevent the garbage collector from running.
    private final Vector2[] vec_cache;
    {
        vec_cache = new Vector2[4];
        for (int i = 0; i < vec_cache.length; ++i)
            vec_cache[i] = new Vector2();
    }
    private final Intersection[] intersection_cache;
    {
        intersection_cache = new Intersection[9];
        for (int i = 0; i < intersection_cache.length; ++i)
            intersection_cache[i] = new Intersection();
    }
    private final Vector2 tmp_vec = vec_cache[0];
    private final RectBounds tmp_bounds = new RectBounds();

    private final Array<ChargeActor> charge_actors = new Array<ChargeActor>();
    private final Array<PuckActor> puck_actors = new Array<PuckActor>();
    
    private final Array<ForcePuckActor> initial_pucks = new Array<ForcePuckActor>();

    GameLogic(ChargeHockeyGame game, Stage game_stage, Level level, UIInterface ui_interface, SymmetryToolActor symmetry_tool) {
        this.game = game;
        this.game_stage = game_stage;
        this.level = level;
        this.ui_interface = ui_interface;
        this.symmetry_tool = symmetry_tool;

        for (ChargeState state : level.get_puck_states()) {
            add_puck(state.x, state.y);
            if (state.partner != null)
                add_puck(state.partner.x, state.partner.y);
        }
    }

    private void add_puck(float x, float y) {
        PuckActor puck = new PuckActor(game);
        puck.set_position(x, y);

        puck_actors.add(puck);
        game_stage.addActor(puck);

        ForcePuckActor initial_puck = new ForcePuckActor(game);
        initial_puck.set_position(x, y);
        initial_puck.set_alpha(1);
        initial_puck.setVisible(false);
        initial_pucks.add(initial_puck);
        game_stage.addActor(initial_puck);
    }

    /** Place a charge at the center of the screen, keeping the symmetry tool in mind. */
    void place_charge(CHARGE charge_type) {
        place_charge(charge_type, game_stage.getCamera().position.x, game_stage.getCamera().position.y, false);
    }

    /** Place a charge at the given position, keeping the symmetry tool in mind. */
    void place_charge(CHARGE charge_type, float x, float y) {
        place_charge(charge_type, x, y, true);
    }

    /** Place a charge at the given position, keeping the symmetry tool and dragging in mind.
     *  Dragging determines whether to perform out of bounds checking now or when dragging is finished. */
    private void place_charge(CHARGE charge_type, float x, float y, boolean dragged) {
        ChargeActor charge1;
        if (symmetry_tool.is_enabled()) {
            charge1 = add_charge(charge_type, x, y);
            symmetry_tool.get_symmetrical_pos(tmp_vec.set(charge1.get_x(), charge1.get_y()));
            ChargeActor charge2 = add_charge(charge_type, tmp_vec.x, tmp_vec.y);

            charge1.set_partner(charge2);
            charge2.set_partner(charge1);

            if (!dragged && charge2.check_out_of_world())
                remove_charge(charge2);
        } else {
            charge1 = add_charge(charge_type, x, y);
        }

        if (!dragged && charge1.check_out_of_world())
            remove_charge(charge1);
    }

    private ChargeActor add_charge(CHARGE charge_type, float x, float y) {
        ChargeActor charge = new ChargeActor(game, charge_type, ui_interface.get_charge_drag_callback(), symmetry_tool);
        charge.set_position(x, y);

        charge_actors.add(charge);
        game_stage.addActor(charge);

        for (PuckActor puck : puck_actors)
            puck.set_force(charge, calc_force(puck, charge));
        for (ForcePuckActor puck : initial_pucks)
            puck.set_force(charge, calc_force(puck, charge));

        charge_state_changed = true;

        return charge;
    }

    void remove_charge(ChargeActor charge) {
        if (charge instanceof PuckActor)
            return;

        ChargeActor partner = charge.get_partner();
        if (partner != null) {
            partner.set_partner(null);
            charge.set_partner(null);
        }
        charge_actors.removeValue(charge, true);
        charge.clear();
        charge.remove();

        for (PuckActor puck : puck_actors) {
            puck.remove_force(charge);
        }
        for (ForcePuckActor puck : initial_pucks) {
            puck.remove_force(charge);
        }

        charge_state_changed = true;
    }

    void update(float delta) {
        if (!is_playing())
            return;

        dt_accumulator += delta;

        if (PuckActor.get_trace_path() && (dt_accumulator >= dt)) {
            reset_pucks_history();
        }

        while (dt_accumulator >= dt) {
            update_pucks(dt);

            handle_game_result();

            dt_accumulator -= dt * (1f / GAME_SPEED);
        }
    }

    private void handle_game_result() {
        GAME_RESULT result = get_game_result();

        if (result == GAME_RESULT.IN_PROGRESS)
            return;

        if (result == GAME_RESULT.WIN) {
            ui_interface.result_win();
        } else {  // LOSS
            ui_interface.result_loss();
        }
    }

    private void reset_pucks_history() {
        for (PuckActor puck : puck_actors) {
            puck.reset_trace_path_history();
        }
    }

    private GAME_RESULT get_game_result() {
        int goals = 0;
        for (PuckActor puck : puck_actors) {
            if (puck.check_out_of_world())
                return GAME_RESULT.LOSS;

            GRID_ITEM collision_item = puck.get_collision();
            if (collision_item == GRID_ITEM.GOAL)
                goals++;
//            else if (collision_item == GRID_ITEM.WALL)
//                return GAME_RESULT.LOSS;
        }
        if (goals == puck_actors.size)
            return GAME_RESULT.WIN;
        else
            return GAME_RESULT.IN_PROGRESS;
    }

    void blink_collided_pucks() {
        for (PuckActor puck : puck_actors) {
            if (puck.get_collision() == GRID_ITEM.WALL || puck.check_out_of_world()) {
                puck.set_puck_alpha(1);
                puck.set_vector_alpha(0.35f);
                puck.start_blinking();
            }
        }
    }

    private Intersection get_nearest_intersection(Vector2 current, Vector2 start, Vector2 stop) {
        // Collision check only the 9 nearest tiles around the puck.
        Intersection intersections[] = intersection_cache;
        final int[] sign_x = { 0, 1, 0, -1, 0, 1, -1, -1, 1 };
        final int[] sign_y = { 0, 0, 1, 0, -1, 1, 1, -1, -1 };
        float r = PuckActor.RADIUS + PHYSICS_EPSILON;
        for (int i = 0; i < 9; ++i) {
            int row = (int) (current.y + sign_y[i] * r);
            int col = (int) (current.x + sign_x[i] * r);

            intersections[i].reset();

            GRID_ITEM item = level.get_grid_item(row, col);
            if (item == GRID_ITEM.NULL)
                continue;

            RectBounds bounds = tmp_bounds;
            bounds.left = col;
            bounds.right = col + Grid.UNIT;
            bounds.bottom = row;
            bounds.top = row + Grid.UNIT;

            Intersection intersection = get_intersection(bounds, start, stop);
            if (intersection != null)
                intersections[i].set(intersection);
            intersections[i].item = item;
        }

        // Pick the collision that happened first.
        float min_time = Float.MAX_VALUE;
        Intersection nearest_intersection = null;
        for (Intersection intersection : intersections) {
            if (intersection.valid() && intersection.time < min_time) {
                min_time = intersection.time;
                nearest_intersection = intersection;
            }
        }
        return (nearest_intersection != null ? nearest_intersection : intersections[0].reset());
    }

    // Based on:
    // https://stackoverflow.com/questions/18704999/how-to-fix-circle-and-rectangle-overlap-in-collision-response/18790389#18790389
    // Implementation of above in ./CircleRectangle.jar
    // start - the position of the puck at a point where we know it doesn't collide with anything
    // end - the target position of the puck along a straight line from start
    private Intersection get_intersection(RectBounds bounds, Vector2 start, Vector2 end) {
        float r = PuckActor.RADIUS;

        // If it's impossible for the circle to touch the bounding box:
        if (    (Math.max(start.x, end.x) + r < bounds.left)  ||
                (Math.min(start.x, end.x) - r > bounds.right) ||
                (Math.min(start.y, end.y) - r > bounds.top)   ||
                (Math.max(start.y, end.y) + r < bounds.bottom))
            return null;

        final float dx = end.x - start.x;
        final float dy = end.y - start.y;
        float corner_x = Float.MAX_VALUE;
        float corner_y = Float.MAX_VALUE;

        // The circle goes through the left edge from left to right
        if (start.x - r < bounds.left && end.x + r > bounds.left) {
            // time is a scalar of the distance travelled, in range [0, 1]
            // the 'time' it takes the circle to hit the left side
            float left_time = (bounds.left - r - start.x) / dx;
            if (left_time >= 0.0f && left_time <= 1.0f) {
                float left_y = dy * left_time + start.y;
                if (left_y <= bounds.top && left_y >= bounds.bottom)
                    return new Intersection(dx * left_time + start.x, left_y, left_time, -1, 0, bounds.left, left_y);
            }
            corner_x = bounds.left;
        }

        // The circle goes through the right edge from right to left
        if (start.x + r > bounds.right && end.x - r < bounds.right) {
            float right_time = (start.x - (bounds.right + r)) / -dx;
            if (right_time >= 0.0f && right_time <= 1.0f) {
                float right_y = dy * right_time + start.y;
                if (right_y <= bounds.top && right_y >= bounds.bottom)
                    return new Intersection(dx * right_time + start.x, right_y, right_time, 1, 0, bounds.right, right_y);
            }
            corner_x = bounds.right;
        }

        // The circle goes through the top edge from the top down
        if (start.y + r > bounds.top && end.y - r < bounds.top) {
            float top_time = (start.y - (bounds.top + r)) / -dy;
            if (top_time >= 0.0f && top_time <= 1.0f) {
                float top_x = dx * top_time + start.x;
                if (top_x >= bounds.left && top_x <= bounds.right)
                    return new Intersection(top_x, dy * top_time + start.y, top_time, 0, 1, top_x, bounds.top);
            }
            corner_y = bounds.top;
        }

        // The circle goes through the bottom edge from the bottom up
        if (start.y - r < bounds.bottom && end.y + r > bounds.bottom) {
            float bot_time = ((bounds.bottom - r) - start.y) / dy;
            if (bot_time > 0.0f && bot_time <= 1.0f) {
                float bot_x = dx * bot_time + start.x;
                if (bot_x >= bounds.left && bot_x <= bounds.right)
                    return new Intersection(bot_x, dy * bot_time + start.y, bot_time, 0, -1, bot_x, bounds.bottom);
            }
            corner_y = bounds.bottom;
        }

        if (corner_x == Float.MAX_VALUE && corner_y == Float.MAX_VALUE)
            return null;

        if (corner_x != Float.MAX_VALUE && corner_y == Float.MAX_VALUE)
            corner_y = (dy < 0.0f ? bounds.bottom : bounds.top);
        if (corner_y != Float.MAX_VALUE && corner_x == Float.MAX_VALUE)
            corner_x = (dx > 0.0f ? bounds.right : bounds.left);

        /* Solve the triangle between the start, corner, and intersection point.
         *
         *           +-----------T-----------+
         *           |                       |
         *          L|                       |R
         *           |                       |
         *           C-----------B-----------+
         *          / \
         *         /   \r     _.-E
         *        /     \ _.-'
         *       /    _.-I
         *      / _.-'
         *     S-'
         *
         * S = start of circle's path
         * E = end of circle's path
         * LTRB = sides of the rectangle
         * I = {ix, iY} = point at which the circle intersects with the rectangle
         * C = corner of intersection (and collision point)
         * C=>I (r) = {nx, ny} = radius and intersection normal
         * S=>C = cornerdist
         * S=>I = intersectionDistance
         * S=>E = lineLength
         * <S = innerAngle
         * <I = angle1
         * <C = angle2
         */

        double corner_dx = corner_x - start.x;
        double corner_dy = corner_y - start.y;
        double corner_dist = Math.hypot(corner_dx, corner_dy);
        double line_length = Math.hypot(dx, dy);
        // Rearrange vector dot product formula to get the angle:
        double inner_angle = Math.acos((corner_dx * dx + corner_dy * dy) / (line_length * corner_dist));

        // If the circle is too close, no intersection.
        if (corner_dist < r)
            return null;

        // If inner angle is zero, it's going to hit the corner straight on.
        if (inner_angle == 0.0f) {
            float time = (float) ((corner_dist - r) / line_length);

            if (time > 1.0f || time < 0.0f)
                return null;

            Intersection intersection = new Intersection();
            intersection.pos.x = time * dx + start.x;
            intersection.pos.y = time * dy + start.y;
            intersection.time = time;
            intersection.normal.x = (float) (corner_dx / corner_dist);
            intersection.normal.y = (float) (corner_dy / corner_dist);
            intersection.intersection.set(corner_x, corner_y);
            return intersection;
        }

        double inner_angle_sin = Math.sin(inner_angle);
        double angle_1_sin = inner_angle_sin * corner_dist / r;  // the law of sines

        if (Math.abs(angle_1_sin) > 1.0f)
            return null;

        double angle1 = Math.PI - Math.asin(angle_1_sin);
        double angle2 = Math.PI - inner_angle - angle1;
        double intersection_dist = r * Math.sin(angle2) / inner_angle_sin;  // the law of sines

        float time = (float) (intersection_dist / line_length);

        if (time > 1.0f || time < 0.0f)
            return null;

        Intersection intersection = new Intersection();
        intersection.pos.x = time * dx + start.x;
        intersection.pos.y = time * dy + start.y;
        intersection.time = time;
        intersection.normal.x = (intersection.pos.x - corner_x) / r;
        intersection.normal.y = (intersection.pos.y - corner_y) / r;
        intersection.intersection.set(corner_x, corner_y);

        return intersection;
    }

    private void move_puck(PuckActor puck, float dx, float dy) {
        move_puck(puck, dx, dy, true);
    }

    /** Move puck by dx,dy, handling any collisions on the way to the destination. */
    private void move_puck(PuckActor puck, float dx, float dy, boolean trace_path) {
        if (puck.get_collision() == GRID_ITEM.GOAL) return;

        float start_x = puck.get_x();
        float start_y = puck.get_y();
        float end_x = start_x + dx;
        float end_y = start_y + dy;

        int sign_x = dx >= 0 ? 1 : -1;
        int sign_y = dy >= 0 ? 1 : -1;
        float step_x = sign_x * 0.5f;
        float step_y = sign_y * 0.5f;

        // Find the tile the puck intersects with first:
        Intersection intersection = intersection_cache[0].reset();
        float x = start_x, y = start_y;
        while (x != end_x || y != end_y) {
            x = (step_x > 0) ? Math.min(x + step_x, end_x) : Math.max(x + step_x, end_x);
            y = (step_y > 0) ? Math.min(y + step_y, end_y) : Math.max(y + step_y, end_y);
            intersection = get_nearest_intersection(vec_cache[1].set(x, y), vec_cache[2].set(start_x, start_y), vec_cache[3].set(end_x, end_y));
            if (intersection.valid())
                break;
        }

        puck.set_collision(intersection.item);

        if (intersection.valid()) {
            // On a collision, adjust the puck's position to be touching the hit object.
            puck.set_position(intersection.pos.x, intersection.pos.y);

            Vector2 velocity = puck.get_velocity();
            float remaining_time = 1.0f - intersection.time;
            float dot = dx * intersection.normal.x + dy * intersection.normal.y;
            float new_dx = dx - 2 * intersection.normal.x * dot;
            float new_dy = dy - 2 * intersection.normal.y * dot;
            // Reflect the velocity and move the puck the remaining distance.
            // Moving the puck the remaining distance usually causes the puck to gain energy when
            // bouncing.
            velocity.sub(intersection.normal.scl(2 * intersection.normal.dot(velocity)));
            move_puck(puck, new_dx * remaining_time, new_dy * remaining_time, false);
        } else {
            puck.set_position(end_x, end_y);
        }

        if (trace_path && PuckActor.get_trace_path())
            puck.save_path_position();
    }

    private void update_pucks(float delta) {
        // can't use nested iterators: https://github.com/libgdx/libgdx/wiki/Collections
        for (int i = 0; i < puck_actors.size; i++) {
            PuckActor puck = puck_actors.get(i);
            integrate(puck, delta);
        }
    }

    private void integrate(PuckActor puck, float delta) {
        if (puck.get_collision() == GRID_ITEM.GOAL)
            return;

        float weight = puck.get_weight();
        Vector2 velocity_vec = puck.get_velocity();

        // Semi-implicit euler integration

        calc_net_force(puck);
        tmp_vec.x = force_vec.x / weight;  // a = F / m
        tmp_vec.y = force_vec.y / weight;
        puck.set_acceleration(tmp_vec.x, tmp_vec.y);

        velocity_vec.x += delta * tmp_vec.x;
        velocity_vec.y += delta * tmp_vec.y;

        float dx = delta * velocity_vec.x;  // x = v * t
        float dy = delta * velocity_vec.y;
        move_puck(puck, dx, dy);

        puck.set_velocity(velocity_vec.x, velocity_vec.y);
    }

    /** Update the puck's force vectors which depend on 'charge' */
    void update_puck_vectors(ChargeActor charge) {
        if (PuckActor.get_draw_forces()) {
            for (PuckActor puck : puck_actors)
                puck.set_force(charge, calc_force(puck, charge));
            for (ForcePuckActor puck : initial_pucks)
                puck.set_force(charge, calc_force(puck, charge));
        }
    }

    /** Calculate the net force on the puck. The answer is in force_vec. */
    private void calc_net_force(final PuckActor puck) {
        force_vec.setZero();
        for (ChargeActor charge : charge_actors)
            apply_force(puck, charge);
    }

    /** Returns a force vector of the force between puck and charge in tmp_vec.
     *  Coulomb's law. */
    private Vector2 calc_force(ChargeActor puck, ChargeActor charge) {
        charge.get_vec_from(tmp_vec.set(puck.get_x(), puck.get_y()));
        // how many units apart can two charges be when calculating the force? (avoids infinite forces)
        final float MIN_DIST = PuckActor.SIZE * PuckActor.SIZE * 2;
        float dist_squared = tmp_vec.len2();
        float force_magnitude = FORCE_FACTOR * (puck.get_abs_charge() * charge.get_abs_charge()) / Math.max(MIN_DIST, dist_squared);
        if (dist_squared <= MIN_DIST)  // small workaround for energy preservation
            force_magnitude *= dist_squared / MIN_DIST;
        return tmp_vec.nor().scl(force_magnitude);
    }

    /** Applies the force between puck and charge to the resultant force_vec. */
    private void apply_force(PuckActor puck, ChargeActor charge) {
        calc_force(puck, charge);
        force_vec.add(tmp_vec);
        if (PuckActor.get_draw_forces())
            puck.set_force(charge, tmp_vec);
    }
    
    private void show_initial_pucks(boolean visibility) {
        for (int i = 0; i < initial_pucks.size; i++) {
            initial_pucks.get(i).setVisible(visibility);

            // when showing initial pucks, lower the alpha of current pucks
            if (visibility)
                puck_actors.get(i).set_alpha(0.35f);
            else
                puck_actors.get(i).set_alpha(1);
        }
    }

    void set_playing(boolean playing) {
        // reset positions if changing to playing from not playing
        if (playing && !is_playing) {
            reset_pucks();
        }

        show_initial_pucks(!playing);

        dt_accumulator = 0;
        is_playing = playing;
        Gdx.graphics.setContinuousRendering(playing);
        Gdx.graphics.requestRendering();
    }

    boolean is_playing() {
        return is_playing;
    }

    private void reset_pucks() {
        PuckActor puck;
        int puck_idx = 0;
        for (final ChargeState state : level.get_puck_states()) {
            puck = puck_actors.get(puck_idx);
            puck.set_position(state.x, state.y);
            puck.reset();

            if (state.partner != null) {
                puck_idx++;

                puck = puck_actors.get(puck_idx);
                puck.set_position(state.partner.x, state.partner.y);
                puck.reset();
            }
            puck_idx++;
        }
        tmp_vec.setZero();
        force_vec.setZero();
    }

    private void reset_charges() {
        for (ChargeActor charge : charge_actors) {
            charge.clear();
            charge.remove();
        }
        charge_actors.clear();

        charge_state_changed = true;
    }

    /** Resets the state of the loaded level to its initial state (without the charges). */
    void reset() {
        reset_pucks();
        for (ForcePuckActor puck : initial_pucks)
            puck.clear_sprites();
        reset_charges();

        charge_state_changed = true;
    }

    /** If necessary, resizes all charges to their new set size.
     * Use this after changing the SettingsFile. */
    void handle_charge_size_change() {
        if (charge_actors.size > 0 && charge_actors.first().size_changed()) {
            for (ChargeActor charge : charge_actors)
                charge.reset_size();
        }
    }

    /** Returns whether the position of charges has changed since the last call to this method or
     * if this is the first call, returns true. */
    boolean charge_state_changed() {
        boolean ret_val = charge_state_changed;
        charge_state_changed = false;
        return ret_val;
    }

    void update_charge_state() {
        charge_state_changed = true;
    }

    final Array<PuckActor> get_pucks() {
        return puck_actors;
    }

    final Array<ChargeActor> get_charges() {
        return charge_actors;
    }

    /** Returns true on success and false otherwise. */
    boolean load_charge_state(Level.SAVE_TYPE save_type) {
        Array<ChargeState> charge_states = level.load_save_file(save_type);
        if (charge_states == null)
            return false;

        reset();
        for (ChargeState charge_state : charge_states) {
            ChargeActor charge1 = add_charge(charge_state.type, charge_state.x, charge_state.y);
            if (charge_state.partner != null) {
                ChargeActor charge2 = add_charge(charge_state.partner.type, charge_state.partner.x, charge_state.partner.y);
                charge1.set_partner(charge2);
                charge2.set_partner(charge1);
            }
        }
        return true;
    }

    boolean has_charges() {
        return charge_actors.size > 0;
    }

    public static void set_game_speed(float value) {
        GAME_SPEED = value;
    }
}
