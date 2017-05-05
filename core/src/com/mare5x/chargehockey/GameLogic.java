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

    private boolean is_playing = false;

    private static final float dt = 0.004f;

    private final Vector2 force_vec = new Vector2(), puck_vec = new Vector2();
    private final Vector2 velocity_vec = new Vector2(), acceleration_vec = new Vector2(), new_acceleration_vec = new Vector2();

    private Array<ChargeActor> charge_actors;
    private Array<ChargeActor> puck_actors;
    private final Array<Vector2> initial_puck_positions;

    GameLogic(ChargeHockeyGame game, Stage game_stage, Level level) {
        this.game = game;
        this.game_stage = game_stage;
        this.level = level;

        charge_actors = new Array<ChargeActor>();
        puck_actors = new Array<ChargeActor>();
        initial_puck_positions = level.get_puck_positions();

        // replace puck positions in level with null items and instead place puck charge actors
        for (Vector2 pos : initial_puck_positions) {
            level.set_item((int) (pos.y), (int) (pos.x), GRID_ITEM.NULL);

            ChargeActor puck = new ChargeActor(game, CHARGE.PUCK);
            puck.setPosition(pos.x, pos.y);

            puck_actors.add(puck);
            game_stage.addActor(puck);
        }
    }

    // Add a charge of type charge_type to the center of the camera position.
    final ChargeActor add_charge(CHARGE charge_type) {
        ChargeActor charge = new ChargeActor(game, charge_type);
        charge.setPosition(game_stage.getCamera().position.x, game_stage.getCamera().position.y);

        charge_actors.add(charge);
        game_stage.addActor(charge);

        return charge;
    }

    void update(float delta) {
        if (!is_playing())
            return;

        update_pucks(dt);

        // TODO check collisions
        if (is_game_won()) {
            set_playing(false);
            return;
        }
    }

    private void update_pucks(float delta) {
        for (ChargeActor puck : puck_actors) {
            float weight = puck.get_weight();

            float dx = delta * (velocity_vec.x + delta * acceleration_vec.x / 2);  // x = v * t
            float dy = delta * (velocity_vec.y + delta * acceleration_vec.y / 2);  // average velocity
            puck.moveBy(dx, dy);

//            for (ChargeActor charge : charge_actors) {
//                if (puck.overlaps(charge)) {
//                    puck.moveBy(dx, dy);
//                    System.out.println("move");
//                }
//            }

            calc_net_force(puck);
            new_acceleration_vec.x = force_vec.x / weight;  // a = F / m
            new_acceleration_vec.y = force_vec.y / weight;

            velocity_vec.x += delta * (new_acceleration_vec.x + acceleration_vec.x) / 2;  // v = v0 + a * t
            velocity_vec.y += delta * (new_acceleration_vec.y + acceleration_vec.y) / 2;

            acceleration_vec.set(new_acceleration_vec);
        }
    }

    private Vector2 calc_net_force(final ChargeActor puck) {
        force_vec.setZero();
        puck_vec.set(puck.getX(Align.center), puck.getY(Align.center));
        // calculate the net force on the puck
        for (ChargeActor charge : charge_actors) {
            apply_force(puck, charge);
        }
        return force_vec;
    }

    private void apply_force(ChargeActor puck, ChargeActor charge) {
        Vector2 vec = charge.get_vec(puck_vec);
        float dist = vec.len();
        float val = (puck.get_abs_charge() * charge.get_abs_charge()) / (dist * dist * E_CONST);
        vec.scl(val);
        force_vec.add(vec);
    }

    void set_playing(boolean value) {
        // reset positions if changing to playing from not playing
        if (value && !is_playing)
            reset_pucks();
        is_playing = value;
        if (is_playing) {
            Gdx.graphics.setContinuousRendering(true);
        }
        else {
            Gdx.graphics.setContinuousRendering(false);
        }
        Gdx.graphics.requestRendering();
    }

    private void reset_pucks() {
        for (int i = 0; i < initial_puck_positions.size; i++) {
            final Vector2 pos = initial_puck_positions.get(i);
            puck_actors.get(i).setPosition(pos.x, pos.y);
        }
        velocity_vec.setZero();
        acceleration_vec.setZero();
        new_acceleration_vec.setZero();
        force_vec.setZero();
        puck_vec.setZero();
    }

    boolean is_playing() {
        return is_playing;
    }

    // TODO
    boolean is_game_won() {
        return false;
    }
}
