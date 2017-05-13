package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;


class GameLogic {
    private enum GAME_RESULT {
        WIN, LOSS, IN_PROGRESS
    }

    private final ChargeHockeyGame game;
    private final Level level;
    private final GameScreen game_screen;

    private final Stage game_stage;

    private boolean is_playing = false;

    private static final float MIN_DIST = PuckActor.SIZE;  // how many units apart can two charges be when calculating the force? (avoids infinite forces)
    private static final float E_CONST = 1.1e-10f;
    private static float GAME_SPEED = 1;  // game speed (force scalar) set by the user in settings
    private static final float dt = 0.01f;
    private float dt_accumulator = 0;  // http://gafferongames.com/game-physics/fix-your-timestep/

    private final Vector2 force_vec = new Vector2(), puck_vec = new Vector2();
    private final Vector2 tmp_vec = new Vector2();

    private final Array<ChargeActor> charge_actors;
    private final Array<PuckActor> puck_actors;
    private final Array<Vector2> initial_puck_positions;

    GameLogic(ChargeHockeyGame game, Stage game_stage, Level level, GameScreen game_screen) {
        this.game = game;
        this.game_stage = game_stage;
        this.level = level;
        this.game_screen = game_screen;

        charge_actors = new Array<ChargeActor>();
        puck_actors = new Array<PuckActor>();
        initial_puck_positions = level.get_puck_positions();

        // replace puck positions in level with null items and instead place puck actors
        for (Vector2 pos : initial_puck_positions) {
            level.set_item((int) (pos.y), (int) (pos.x), GRID_ITEM.NULL);

            PuckActor puck = new PuckActor(game, CHARGE.PUCK, this);
            puck.setPosition(pos.x, pos.y);

            puck_actors.add(puck);
            game_stage.addActor(puck);
        }

        load_charge_state();
    }

    // Add a charge of type charge_type to the center of the camera position.
    ChargeActor add_charge(CHARGE charge_type) {
        return add_charge(charge_type, game_stage.getCamera().position.x, game_stage.getCamera().position.y);
    }

    private ChargeActor add_charge(CHARGE charge_type, float x, float y) {
        ChargeActor charge = new ChargeActor(game, charge_type, this);
        charge.setPosition(x, y);

        charge_actors.add(charge);
        game_stage.addActor(charge);

        return charge;
    }

    void remove_charge(ChargeActor charge) {
        if (charge instanceof PuckActor)
            return;

        charge_actors.removeValue(charge, true);
        charge.clear();
        charge.remove();
    }

    void update(float delta) {
        if (!is_playing())
            return;

        dt_accumulator += delta;

        while (dt_accumulator >= dt) {
            update_pucks(dt);

            update_collisions();

            GAME_RESULT result = get_game_result();
            switch (result) {
                case WIN:
                    game_screen.toggle_playing();
                    Gdx.app.log("game", "WIN");
                    break;
                case LOSS:
                    game_screen.toggle_playing();
                    Gdx.app.log("game", "LOSS");
                    break;
                case IN_PROGRESS:
                    break;
            }

            dt_accumulator -= dt;
        }
    }

    private GAME_RESULT get_game_result() {
        int goals = 0;
        int walls = 0;
        for (PuckActor puck : puck_actors) {
            if (check_out_of_bounds(puck))
                return GAME_RESULT.LOSS;

            GRID_ITEM collision = puck.get_collision();
            if (collision == GRID_ITEM.GOAL)
                goals++;
            else if (collision == GRID_ITEM.WALL)
                walls++;
        }
        if (walls > 0)
            return GAME_RESULT.LOSS;
        else if (goals == puck_actors.size)
            return GAME_RESULT.WIN;
        else
            return GAME_RESULT.IN_PROGRESS;
    }

    private boolean check_out_of_bounds(PuckActor puck) {
        float x = puck.getX();
        float y = puck.getY();
        return x < 0 || x > ChargeHockeyGame.WORLD_WIDTH || y < 0 || y > ChargeHockeyGame.WORLD_HEIGHT;
    }

    private GRID_ITEM get_collision(PuckActor puck) {
        GRID_ITEM grid_item = level.get_grid_item((int) (puck.getY()), (int) (puck.getX()));  // row = y, col = x
        if (is_collision(grid_item))
            return grid_item;
        grid_item = level.get_grid_item((int) (puck.getY()), (int) (puck.getRight()));  // row = y, col = x
        if (is_collision(grid_item))
            return grid_item;
        grid_item = level.get_grid_item((int) (puck.getTop()), (int) (puck.getX()));  // row = y, col = x
        if (is_collision(grid_item))
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
            puck.moveBy(dx * GAME_SPEED, dy * GAME_SPEED);

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
        puck_vec.set(puck.getX(Align.center), puck.getY(Align.center));
        // calculate the net force on the puck
        for (ChargeActor charge : charge_actors) {
            apply_force(puck, charge);
        }
        return force_vec;
    }

    private void apply_force(PuckActor puck, ChargeActor charge) {
        Vector2 vec = charge.get_vec(puck_vec);
        float dist_squared = vec.len2();
        dist_squared = dist_squared < MIN_DIST * MIN_DIST ? MIN_DIST * MIN_DIST : dist_squared;
        float val = (puck.get_abs_charge() * charge.get_abs_charge()) / (dist_squared * E_CONST);
        vec.scl(val);
        force_vec.add(vec);
    }

    void set_playing(boolean value) {
        // reset positions if changing to playing from not playing
        if (value && !is_playing)
            reset_pucks();
        dt_accumulator = 0;
        is_playing = value;
        if (is_playing) {
            Gdx.graphics.setContinuousRendering(true);
        }
        else {
            Gdx.graphics.setContinuousRendering(false);
        }
        Gdx.graphics.requestRendering();
    }

    boolean is_playing() {
        return is_playing;
    }

    private void reset_pucks() {
        for (int i = 0; i < initial_puck_positions.size; i++) {
            final Vector2 pos = initial_puck_positions.get(i);
            PuckActor puck = puck_actors.get(i);
            puck.setPosition(pos.x, pos.y);
            puck.reset_vectors();
            puck.set_collision(GRID_ITEM.NULL);
        }
        tmp_vec.setZero();
        force_vec.setZero();
        puck_vec.setZero();
    }

    // Resets the state of the loaded level to its initial state.
    void reset() {
        reset_pucks();
        for (ChargeActor charge : charge_actors) {
            charge.clear();
            charge.remove();
        }
        charge_actors.clear();
    }

    void save_charge_state() {
        level.save_charge_state(charge_actors);
    }

    private void load_charge_state() {
        Array<ChargeState> charge_states = level.load_charge_state();
        if (charge_states == null)
            return;

        charge_actors.clear();
        for (ChargeState charge_state : charge_states) {
            add_charge(charge_state.type, charge_state.x, charge_state.y);
        }
    }

    private static boolean is_collision(final GRID_ITEM collision) {
        return collision == GRID_ITEM.WALL || collision == GRID_ITEM.GOAL;
    }

    static void set_game_speed(float value) {
        GAME_SPEED = value;
    }
}
