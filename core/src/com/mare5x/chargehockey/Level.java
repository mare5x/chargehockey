package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;


enum LEVEL_TYPE {
    EASY, NORMAL, HARD, CUSTOM
}


class Level {
    private final ChargeHockeyGame game;

    private final String name;
    private final LEVEL_TYPE level_type;

    private final ObjectMap<GRID_ITEM, Sprite> grid_sprites;

    private final Grid grid;

    Level(final ChargeHockeyGame game, final String level_name, final LEVEL_TYPE level_type) {
        this.game = game;
        this.name = level_name;
        this.level_type = level_type;

        this.grid = new Grid();

        Sprite null_sprite = game.sprites.createSprite("grid_null");
        null_sprite.setSize(1, 1);

        Sprite wall_sprite = game.sprites.createSprite("grid_wall");
        wall_sprite.setSize(1, 1);

        Sprite goal_sprite = game.sprites.createSprite("grid_goal");
        goal_sprite.setSize(1, 1);

        Sprite puck_sprite = game.sprites.createSprite("puck");
        puck_sprite.setSize(1, 1);

        grid_sprites = new ObjectMap<GRID_ITEM, Sprite>(GRID_ITEM.values.length);
        grid_sprites.put(GRID_ITEM.NULL, null_sprite);
        grid_sprites.put(GRID_ITEM.WALL, wall_sprite);
        grid_sprites.put(GRID_ITEM.GOAL, goal_sprite);
        grid_sprites.put(GRID_ITEM.PUCK, puck_sprite);

        load_grid_from_data(read_level_data(level_name));
    }

    final String get_name() {
        return name;
    }

    void set_item(int row, int col, GRID_ITEM item) {
        grid.set_item(row, col, item);
    }

    final Sprite get_item_sprite(GRID_ITEM item) {
        return grid_sprites.get(item);
    }

    final GRID_ITEM get_grid_item(int row, int col) {
        return grid.get_item(row, col);
    }

    static byte[] get_empty_level_data() {
        byte[] level_data = new byte[ChargeHockeyGame.WORLD_WIDTH * ChargeHockeyGame.WORLD_HEIGHT];
        Arrays.fill(level_data, (byte) GRID_ITEM.NULL.ordinal());

        return level_data;
    }

    private byte[] get_level_data() {
        return grid.get_byte_data();
    }

    // Assumes that the level file exists!
    private byte[] read_level_data(String level_name) {
        FileHandle file = LevelSelector.get_level_grid_fhandle(level_type, level_name);

        return file.readBytes();
    }

    void save_grid() {
        Gdx.app.log("Level", "saving grid");

        FileHandle file = LevelSelector.get_level_grid_fhandle(level_type, name);

        file.writeBytes(get_level_data(), false);
    }

    void load_grid_from_data(byte[] level_data) {
        Gdx.app.log("Level", "loading grid");

        grid.from_byte_data(level_data);
    }

    /*
    Charge save data structure:
    N (number of charges (lines))
    CHARGE_TYPE X Y
     */
    void save_charge_state(Array<ChargeActor> charge_actors) {
        Gdx.app.log("Level", "saving charge state");

        FileHandle file = LevelSelector.get_level_save_fhandle(level_type, name);
        Writer writer = file.writer(false, "UTF-8");

        try {
            writer.write(String.valueOf(charge_actors.size) + "\n");
            for (ChargeActor charge : charge_actors) {
                ChargeState state = charge.get_state();

                writer.write(String.format(Locale.US, "%s %f %f\n", state.type.name(), state.x, state.y));
            }
        } catch (IOException e) {
            file.delete();
            throw new GdxRuntimeException("Error writing file", e);
        } finally {
            StreamUtils.closeQuietly(writer);
        }
    }

    Array<ChargeState> load_charge_state() {
        Gdx.app.log("Level", "loading charge state");

        FileHandle file = LevelSelector.get_level_save_fhandle(level_type, name);
        if (!file.exists()) {
            return null;
        }

        BufferedReader reader = file.reader(256, "UTF-8");
        try {
            int n = Integer.parseInt(reader.readLine());
            Array<ChargeState> states = new Array<ChargeState>(n);

            for (int i = 0; i < n; i++) {
                String[] split = reader.readLine().split(" ");
                states.add(new ChargeState(CHARGE.valueOf(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2])));
            }

            return states;
        } catch (IOException e) {
            throw new GdxRuntimeException("Error reading file", e);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    // x = col, y = row
    Array<Vector2> get_puck_positions() {
        // I'm manually checking the positions, since this functions should only get called once per level load.
        Array<Vector2> positions = new Array<Vector2>();

        for (int row = 0; row < grid.get_height(); row++) {
            for (int col = 0; col < grid.get_width(); col++) {
                if (grid.get_item(row, col) == GRID_ITEM.PUCK) {
                    positions.add(new Vector2(col, row));
                }
            }
        }

        return positions;
    }
}
