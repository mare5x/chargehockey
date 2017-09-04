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


public class GameLogic {
    private enum GAME_RESULT {
        WIN, LOSS, IN_PROGRESS
    }

    interface ResultCallback {
        void win();
        void loss();
    }

    private final ChargeHockeyGame game;
    private final Level level;
    private final ResultCallback result_callback;

    private final SymmetryToolActor symmetry_tool;

    private final Stage game_stage;

    private boolean is_playing = false;
    private boolean charge_state_changed = false;

    private static final float MIN_DIST = PuckActor.RADIUS;  // how many units apart can two charges be when calculating the force? (avoids infinite forces)
    private static final float E_CONST = 1.1e-10f;
    private static float GAME_SPEED = 1;  // game speed (force scalar) set by the user in settings
    private static final float dt = 0.01f;
    private float dt_accumulator = 0;  // http://gafferongames.com/game-physics/fix-your-timestep/

    private final Vector2 force_vec = new Vector2(), tmp_vec = new Vector2();

    private final Array<ChargeActor> charge_actors = new Array<ChargeActor>();
    private final Array<PuckActor> puck_actors = new Array<PuckActor>();
    
    private final Array<ForcePuckActor> initial_pucks = new Array<ForcePuckActor>();

    GameLogic(ChargeHockeyGame game, Stage game_stage, Level level, ResultCallback result_callback, SymmetryToolActor symmetry_tool) {
        this.game = game;
        this.game_stage = game_stage;
        this.level = level;
        this.result_callback = result_callback;
        this.symmetry_tool = symmetry_tool;

        for (Vector2 pos : level.get_puck_positions()) {
            PuckActor puck = new PuckActor(game);
            puck.set_position(pos.x, pos.y);

            puck_actors.add(puck);
            game_stage.addActor(puck);

            ForcePuckActor initial_puck = new ForcePuckActor(game);
            initial_puck.set_position(pos.x, pos.y);
            initial_puck.set_alpha(1);
            initial_puck.setVisible(false);
            initial_pucks.add(initial_puck);
            game_stage.addActor(initial_puck);
        }

        load_charge_state(Level.SAVE_TYPE.AUTO);
    }

    /** Add a charge of type charge_type to the center of the camera position. If the symmetry tool is
     * enabled, it appropriately connects the two added charges. */
    void add_charge(CHARGE charge_type) {
        if (symmetry_tool.is_enabled()) {
            ChargeActor charge1 = add_charge(charge_type, game_stage.getCamera().position.x, game_stage.getCamera().position.y);
            symmetry_tool.get_symmetrical_pos(tmp_vec.set(charge1.get_x(), charge1.get_y()));
            ChargeActor charge2 = add_charge(charge_type, tmp_vec.x, tmp_vec.y);

            charge1.set_partner(charge2);
            charge2.set_partner(charge1);

            if (charge2.check_out_of_bounds())
                remove_charge(charge2);
        } else {
            add_charge(charge_type, game_stage.getCamera().position.x, game_stage.getCamera().position.y);
        }
    }

    private ChargeActor add_charge(CHARGE charge_type, float x, float y) {
        ChargeActor charge = new ChargeActor(game, charge_type, new ChargeActor.DragCallback() {
            @Override
            public void out_of_bounds(ChargeActor charge) {
                remove_charge(charge);
            }

            @Override
            public void drag(ChargeActor charge) {
                if (PuckActor.get_draw_forces()) {
                    for (PuckActor puck : puck_actors)
                        puck.set_force(charge, calc_force(puck, charge));
                    for (ForcePuckActor puck : initial_pucks)
                        puck.set_force(charge, calc_force(puck, charge));
                }
            }

            @Override
            public void drag_started(ChargeActor charge) {
                charge_state_changed = true;
            }
        }, symmetry_tool);
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

    private void remove_charge(ChargeActor charge) {
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

            update_collisions();

            handle_game_result();

            dt_accumulator -= dt * (1f / GAME_SPEED);
        }
    }

    private void handle_game_result() {
        GAME_RESULT result = get_game_result();

        if (result == GAME_RESULT.IN_PROGRESS)
            return;

        if (result == GAME_RESULT.WIN) {
            result_callback.win();
        } else {  // LOSS
            result_callback.loss();
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
            if (check_out_of_bounds(puck))
                return GAME_RESULT.LOSS;

            GRID_ITEM collision = puck.get_collision();
            if (collision == GRID_ITEM.GOAL)
                goals++;
            else if (collision == GRID_ITEM.WALL)
                return GAME_RESULT.LOSS;
        }
        if (goals == puck_actors.size)
            return GAME_RESULT.WIN;
        else
            return GAME_RESULT.IN_PROGRESS;
    }

    private boolean check_out_of_bounds(PuckActor puck) {
        float x = puck.get_x();  // center
        float y = puck.get_y();
        return x < 0 || x > ChargeHockeyGame.WORLD_WIDTH || y < 0 || y > ChargeHockeyGame.WORLD_HEIGHT;
    }

    /** Priority: wall > goal > ... */
    private GRID_ITEM get_collision(PuckActor puck) {
        int row = (int) (puck.getY());
        int col = (int) (puck.getX());
        GRID_ITEM bottom_left = check_collision(puck, row, col);
        if (bottom_left == GRID_ITEM.WALL)
            return bottom_left;

        col = (int) (puck.getRight());
        GRID_ITEM bottom_right = check_collision(puck, row, col);
        if (bottom_right == GRID_ITEM.WALL)
            return bottom_right;

        row = (int) (puck.getTop());
        GRID_ITEM top_right = check_collision(puck, row, col);
        if (top_right == GRID_ITEM.WALL)
            return top_right;

        col = (int) (puck.getX());
        GRID_ITEM top_left = check_collision(puck, row, col);
        if (top_left == GRID_ITEM.WALL)
            return top_left;

        if (bottom_left != GRID_ITEM.NULL) return bottom_left;
        if (bottom_right != GRID_ITEM.NULL) return bottom_right;
        if (top_right != GRID_ITEM.NULL) return top_right;
        if (top_left != GRID_ITEM.NULL) return top_left;

        return GRID_ITEM.NULL;
    }

    private GRID_ITEM check_collision(PuckActor puck, int row, int col) {
        GRID_ITEM grid_item = level.get_grid_item(row, col);
        // add a bit of leniency: walls have a smaller size than goals
        if (grid_item == GRID_ITEM.WALL && puck.intersects(
                new Rectangle(col + LevelFrameBuffer.ONE_TX,
                              row + LevelFrameBuffer.ONE_TX,
                              LevelFrameBuffer.GRID_TILE_SIZE - 3 * LevelFrameBuffer.ONE_TX,
                              LevelFrameBuffer.GRID_TILE_SIZE - 3 * LevelFrameBuffer.ONE_TX)))
            return grid_item;
        else if (grid_item == GRID_ITEM.GOAL && puck.intersects(
                new Rectangle(col, row,
                              LevelFrameBuffer.GRID_TILE_SIZE, LevelFrameBuffer.GRID_TILE_SIZE)))
            return grid_item;
        return GRID_ITEM.NULL;
    }

    private void update_collisions() {
        for (PuckActor puck : puck_actors) {
            if (!is_collision(puck.get_collision())) {
                puck.set_collision(get_collision(puck));
            }
        }
    }

    void blink_collided_pucks() {
        for (PuckActor puck : puck_actors) {
            if (puck.get_collision() == GRID_ITEM.WALL || check_out_of_bounds(puck)) {
                puck.set_puck_alpha(1);
                puck.set_vector_alpha(0.35f);
                puck.start_blinking();
            }
        }
    }

    private void update_pucks(float delta) {
        // can't use nested iterators: https://github.com/libgdx/libgdx/wiki/Collections
        for (int i = 0; i < puck_actors.size; i++) {
            PuckActor puck = puck_actors.get(i);

            if (is_collision(puck.get_collision()))
                continue;

            float weight = puck.get_weight();
            Vector2 velocity_vec = puck.get_velocity();
            Vector2 acceleration_vec = puck.get_acceleration();

            float dx = delta * (velocity_vec.x + delta * acceleration_vec.x / 2);  // x = v * t
            float dy = delta * (velocity_vec.y + delta * acceleration_vec.y / 2);  // average velocity
            puck.moveBy(dx, dy);

            if (PuckActor.get_trace_path())
                puck.save_path_position();

            calc_net_force(puck);
            tmp_vec.x = force_vec.x / weight;  // a = F / m
            tmp_vec.y = force_vec.y / weight;

            velocity_vec.x += delta * (tmp_vec.x + acceleration_vec.x) / 2;  // v = v0 + a * t
            velocity_vec.y += delta * (tmp_vec.y + acceleration_vec.y) / 2;
            puck.set_velocity(velocity_vec.x, velocity_vec.y);

            puck.set_acceleration(tmp_vec.x, tmp_vec.y);
        }
    }

    private Vector2 calc_net_force(final PuckActor puck) {
        force_vec.setZero();
        // calculate the net force on the puck
        for (ChargeActor charge : charge_actors) {
            apply_force(puck, charge);
        }
        return force_vec;
    }

    /** Returns a force vector of the force between puck and charge. */
    private Vector2 calc_force(ChargeActor puck, ChargeActor charge) {
        Vector2 vec = charge.get_vec(puck);
        float dist_squared = vec.len2();
        dist_squared = dist_squared < MIN_DIST * MIN_DIST ? MIN_DIST * MIN_DIST : dist_squared;
        float val = (puck.get_abs_charge() * charge.get_abs_charge()) / (dist_squared * E_CONST);
        return vec.scl(val);
    }

    /** Applies the force between puck and charge to the resultant force_vec. */
    private void apply_force(PuckActor puck, ChargeActor charge) {
        Vector2 vec = calc_force(puck, charge);
        force_vec.add(vec);
        if (PuckActor.get_draw_forces())
            puck.set_force(charge, vec);
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
        for (int i = 0; i < level.get_puck_positions().size; i++) {
            final Vector2 pos = level.get_puck_positions().get(i);
            PuckActor puck = puck_actors.get(i);
            puck.set_position(pos.x, pos.y);
            puck.reset();
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
            add_charge(charge_state.type, charge_state.x, charge_state.y);
        }
        return true;
    }

    boolean has_charges() {
        return charge_actors.size > 0;
    }

    private static boolean is_collision(final GRID_ITEM collision) {
        return collision == GRID_ITEM.WALL || collision == GRID_ITEM.GOAL;
    }

    public static void set_game_speed(float value) {
        GAME_SPEED = value;
    }
}
