package com.mare5x.chargehockey.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
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
import com.mare5x.chargehockey.level.Grid.GRID_ITEM;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.LevelFrameBuffer;

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

    public static class CollisionData {
        GRID_ITEM item = GRID_ITEM.NULL;
        Vector2 intersection = new Vector2();  // a vector telling you how much to move the puck to stop colliding
        Vector2 norm = new Vector2();  // the normal vector of the collision point on item

        public CollisionData reset() {
            item = GRID_ITEM.NULL;
            norm.setZero();
            intersection.setZero();
            return this;
        }

        boolean valid() {
            return item != GRID_ITEM.NULL;
        }

        public CollisionData set(CollisionData collision) {
            item = collision.item;
            intersection.set(collision.intersection);
            norm.set(collision.norm);
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

    private static final float E_CONST = 1.1e-10f;
    private static float GAME_SPEED = 1;  // game speed set by the user in settings
    private static final float dt = 0.01f;
    private float dt_accumulator = 0;  // http://gafferongames.com/game-physics/fix-your-timestep/

    private final Vector2 force_vec = new Vector2();
    private final Vector2[] vec_cache;
    {
        vec_cache = new Vector2[4];
        for (int i = 0; i < vec_cache.length; ++i)
            vec_cache[i] = new Vector2();
    }
    private final Vector2 tmp_vec = vec_cache[0];
    private final Rectangle tmp_rect = new Rectangle();
    private final CollisionData tmp_collision = new CollisionData();

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

            CollisionData collision = puck.get_collision();
            if (collision.item == GRID_ITEM.GOAL)
                goals++;
//            else if (collision.item == GRID_ITEM.WALL)
//                return GAME_RESULT.LOSS;
        }
        if (goals == puck_actors.size)
            return GAME_RESULT.WIN;
        else
            return GAME_RESULT.IN_PROGRESS;
    }

    /** Priority: wall > goal > ... */
    private CollisionData get_collision(PuckActor puck) {
        int row = (int) (puck.getY());
        int col = (int) (puck.getX());
        CollisionData bottom_left = check_collision(puck, row, col);
        if (bottom_left.item == GRID_ITEM.WALL) return bottom_left;

        col = (int) (puck.getRight());
        CollisionData bottom_right = check_collision(puck, row, col);
        if (bottom_right.item == GRID_ITEM.WALL) return bottom_right;

        row = (int) (puck.getTop());
        CollisionData top_right = check_collision(puck, row, col);
        if (top_right.item == GRID_ITEM.WALL) return top_right;

        col = (int) (puck.getX());
        CollisionData top_left = check_collision(puck, row, col);
        if (top_left.item == GRID_ITEM.WALL) return top_left;

        // Goal
        if (bottom_left.valid()) return bottom_left;
        if (bottom_right.valid()) return bottom_right;
        if (top_right.valid()) return top_right;
        if (top_left.valid()) return top_left;
        return tmp_collision.reset();
    }

    /** Check collision between puck and the tile at row,col. */
    private CollisionData check_collision(PuckActor puck, int row, int col) {
        GRID_ITEM grid_item = level.get_grid_item(row, col);
        CollisionData collision_data = new CollisionData();
        /*
        // add a bit of leniency: walls have a smaller size than goals (without the white wall border)
        if (grid_item == GRID_ITEM.WALL) {
            tmp_rect.x = col + LevelFrameBuffer.ONE_TX;
            tmp_rect.y = row + LevelFrameBuffer.ONE_TX;
            tmp_rect.width = LevelFrameBuffer.GRID_TILE_SIZE - 2 * LevelFrameBuffer.ONE_TX;
            tmp_rect.height = LevelFrameBuffer.GRID_TILE_SIZE - 2 * LevelFrameBuffer.ONE_TX;
            if (puck.intersects(tmp_rect, collision_data.intersection, collision_data.norm))
                collision_data.item = grid_item;
        }

        else if (grid_item == GRID_ITEM.GOAL || grid_item == GRID_ITEM.BOUNCER)  {
        */
        if (grid_item != GRID_ITEM.NULL) {
            tmp_rect.x = col;
            tmp_rect.y = row;
            tmp_rect.width = LevelFrameBuffer.GRID_TILE_SIZE;
            tmp_rect.height = LevelFrameBuffer.GRID_TILE_SIZE;
            if (puck.intersects(tmp_rect, collision_data.intersection, collision_data.norm))
                collision_data.item = grid_item;
        }
        return collision_data;
    }

    void blink_collided_pucks() {
        for (PuckActor puck : puck_actors) {
            if (puck.get_collision().item == GRID_ITEM.WALL || puck.check_out_of_world()) {
                puck.set_puck_alpha(1);
                puck.set_vector_alpha(0.35f);
                puck.start_blinking();
            }
        }
    }

    /** Move puck by dx,dy, handling any collisions on the way to the destination. */
    private void move_puck(PuckActor puck, float dx, float dy) {
        float start_x = puck.get_x();
        float start_y = puck.get_y();
        float end_x = start_x + dx;
        float end_y = start_y + dy;

        int sign_x = dx >= 0 ? 1 : -1;
        int sign_y = dy >= 0 ? 1 : -1;
        float step_x = sign_x * 0.5f;
        float step_y = sign_y * 0.5f;

        // To prevent collisions going unnoticed if the objects are moving too fast,
        // multisample and check additional points on the trajectory (linear path from source to
        // destination point). Wall bouncing
        float x = start_x, y = start_y;
        float prev_x = x, prev_y = y;
        while (x != end_x || y != end_y) {
            x = (step_x > 0) ? Math.min(x + step_x, end_x) : Math.max(x + step_x, end_x);
            y = (step_y > 0) ? Math.min(y + step_y, end_y) : Math.max(y + step_y, end_y);
            puck.set_position(x, y);

            // Binary search vs Vector math benchmark results:
            // Vector math requires significantly less iterations for the most part and it
            // is usually 100% faster. Binary search on the other hand requires a constant number
            // of iterations and the results are predictable. Sometimes binary search is faster
            // because the vector math might move in very small steps.

            // On a collision, adjust the puck's position to be touching the hit object.
            CollisionData collision = get_collision(puck);
            puck.set_collision(collision);
            int iters = 0;
            // A puck can collide with multiple tiles, so fix them all
            while (collision.valid()) {
                puck.set_collision(collision);  // store the latest collision data for proper collision resolution
                // If the math vector based approach is taking too long, it's likely faster to finish
                // the job using binary search.
                if (iters > 12) {
                    Vector2 low = vec_cache[1].set(prev_x, prev_y);
                    Vector2 high = vec_cache[2].set(x, y);
                    Vector2 mid = vec_cache[3];
                    while (mid.set(high).sub(low).len2() > PHYSICS_EPSILON * PHYSICS_EPSILON) {
                        mid.set(low).add(high).scl(0.5f);
                        puck.set_position(mid.x, mid.y);
                        collision = get_collision(puck);
                        if (collision.valid()) {
                            puck.set_collision(collision);
                            high.set(mid);
                        }
                        else
                            low.set(mid);
                    }
                    break;
                }
                // project the intersection vector onto a normalized vector of the puck's velocity
                // vector: only move the puck on the path that the puck is moving on!
                Vector2 intersection = collision.intersection;
                Vector2 displacement = vec_cache[1].set(puck.get_velocity()).nor();
//                displacement.scl(displacement.dot(tmp_vec));
                displacement.scl(displacement.dot(intersection)).nor().scl(intersection.len());
                puck.moveBy(displacement.x, displacement.y);
                collision = get_collision(puck);
                ++iters;
            }
            if (puck.get_collision().valid())
                break;

            prev_x = x;
            prev_y = y;
        }

        if (PuckActor.get_trace_path())
            puck.save_path_position();
    }

    private void update_pucks(float delta) {
        // can't use nested iterators: https://github.com/libgdx/libgdx/wiki/Collections
        for (int i = 0; i < puck_actors.size; i++) {
            PuckActor puck = puck_actors.get(i);

            if (puck.get_collision().item == GRID_ITEM.GOAL || puck.get_collision().item == GRID_ITEM.WALL)
                continue;

            float weight = puck.get_weight();
            Vector2 velocity_vec = puck.get_velocity();
            Vector2 acceleration_vec = puck.get_acceleration();

            float dx = delta * (velocity_vec.x + delta * acceleration_vec.x / 2);  // x = v * t
            float dy = delta * (velocity_vec.y + delta * acceleration_vec.y / 2);  // average velocity
            move_puck(puck, dx, dy);

            calc_net_force(puck);
            tmp_vec.x = force_vec.x / weight;  // a = F / m
            tmp_vec.y = force_vec.y / weight;

            velocity_vec.x += delta * (tmp_vec.x + acceleration_vec.x) / 2;  // v = v0 + a * t
            velocity_vec.y += delta * (tmp_vec.y + acceleration_vec.y) / 2;

            CollisionData collision = puck.get_collision();
            if (collision.item == GRID_ITEM.BOUNCER) {
                // Reflect the puck's velocity based on the normal vector of the collision.
                // The incoming angle is the same as the outgoing angle.
                Vector2 norm = collision.norm;  // directly change the norm vector since it's used only here
                norm.scl(velocity_vec.scl(-1).dot(norm)).sub(velocity_vec).scl(2);
                velocity_vec.add(norm);
            }

            puck.set_velocity(velocity_vec.x, velocity_vec.y);
            puck.set_acceleration(tmp_vec.x, tmp_vec.y);
        }
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
        float dist_squared = Math.max(PuckActor.SIZE * PuckActor.SIZE, tmp_vec.len2());
        float force_magnitude = (puck.get_abs_charge() * charge.get_abs_charge()) / (dist_squared * E_CONST);
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
