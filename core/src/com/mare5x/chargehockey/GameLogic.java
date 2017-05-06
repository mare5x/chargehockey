package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;


class GameLogic {
    private static final float E_CONST = 1.1e-10f;

    private final ChargeHockeyGame game;
    private final Stage game_stage;
    private final Level level;
    private final GameScreen game_screen;

    private boolean is_playing = false;

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

        // temp TODO make this an option in the settings menu
        PuckActor.set_draw_acceleration(true);
        PuckActor.set_draw_velocity(true);
    }

    // Add a charge of type charge_type to the center of the camera position.
    final ChargeActor add_charge(CHARGE charge_type) {
        ChargeActor charge = new ChargeActor(game, charge_type, this);
        charge.setPosition(game_stage.getCamera().position.x, game_stage.getCamera().position.y);

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

            if (check_puck_out_of_bounds()) {
                game_screen.toggle_playing();
                return;
            }

            GRID_ITEM collision = get_collision();
            if (collision == GRID_ITEM.GOAL) {
                game_screen.toggle_playing();
                return;
            } else if (collision == GRID_ITEM.WALL) {
                game_screen.toggle_playing();
                return;
            }

            dt_accumulator -= dt;
        }
    }

    private boolean check_puck_out_of_bounds() {
        for (PuckActor puck : puck_actors) {
            float x = puck.getX();
            float y = puck.getY();
            if (x < 0 || x > ChargeHockeyGame.WORLD_WIDTH || y < 0 || y > ChargeHockeyGame.WORLD_HEIGHT)
                return true;
        }
        return false;
    }

    private GRID_ITEM get_collision() {
        for (PuckActor puck : puck_actors) {
            GRID_ITEM grid_item = level.get_grid_item((int) (puck.getY()), (int) (puck.getX()));  // row = y, col = x
            if (grid_item == GRID_ITEM.GOAL || grid_item == GRID_ITEM.WALL)
                return grid_item;
            grid_item = level.get_grid_item((int) (puck.getY()), (int) (puck.getRight()));  // row = y, col = x
            if (grid_item == GRID_ITEM.GOAL || grid_item == GRID_ITEM.WALL)
                return grid_item;
            grid_item = level.get_grid_item((int) (puck.getTop()), (int) (puck.getX()));  // row = y, col = x
            if (grid_item == GRID_ITEM.GOAL || grid_item == GRID_ITEM.WALL)
                return grid_item;
        }
        return GRID_ITEM.NULL;
    }

    private void update_pucks(float delta) {
        // can't use nested iterators: https://github.com/libgdx/libgdx/wiki/Collections
        for (int i = 0; i < puck_actors.size; i++) {
            PuckActor puck = puck_actors.get(i);

            float weight = puck.get_weight();
            Vector2 velocity_vec = puck.get_velocity();
            Vector2 acceleration_vec = puck.get_acceleration();

            float dx = delta * (velocity_vec.x + delta * acceleration_vec.x / 2);  // x = v * t
            float dy = delta * (velocity_vec.y + delta * acceleration_vec.y / 2);  // average velocity
            puck.moveBy(dx, dy);  // TODO make speed adjustable (so its slower on higher zoom levels ...)

            for (int j = 0; j < charge_actors.size; j++) {
                ChargeActor charge = charge_actors.get(j);
                while (puck.overlaps(charge)) {
                    System.out.println("move");
                    puck.moveBy(dx, dy);

                    // make sure that the puck doesn't bypass an obstacle
                    GRID_ITEM collision = get_collision();
                    if (collision == GRID_ITEM.GOAL || collision == GRID_ITEM.WALL) {
                        puck.reset_vectors();
                        return;
                    }
                }
            }

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
            puck_actors.get(i).setPosition(pos.x, pos.y);
            puck_actors.get(i).reset_vectors();
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
}
