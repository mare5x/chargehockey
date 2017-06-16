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
import java.util.Locale;


enum LEVEL_TYPE {
    EASY, NORMAL, HARD, CUSTOM
}


class Level {
    private final String name;
    private final LEVEL_TYPE level_type;

    private final Grid grid;
    private Array<Vector2> puck_positions = new Array<Vector2>();

    Level(final String level_name, final LEVEL_TYPE level_type) {
        this.name = level_name;
        this.level_type = level_type;

        this.grid = new Grid();

        if (LevelSelector.level_file_exists(level_type, level_name))
            load_level();
        else {
            save_level();  // this will create an empty/default valid level file
        }
    }

    final String get_name() {
        return name;
    }

    void set_item(int row, int col, GRID_ITEM item) {
        grid.set_item(row, col, item);
    }

    final GRID_ITEM get_grid_item(int row, int col) {
        return grid.get_item(row, col);
    }

    /** LEVEL SAVE STRUCTURE
     * WIDTH HEIGHT
     * WIDTH * HEIGHT GRID ITEM CODES (ONE LINE STRING)
     * N PUCKS
     * X Y
     * */
    void save_level(Array<ChargeActor> puck_actors) {
        puck_positions.clear();
        for (ChargeActor puck : puck_actors) {
            puck_positions.add(new Vector2(puck.getX(), puck.getY()));
        }
        save_level();
    }

    /** Save the grid and puck positions of the currently loaded level. */
    void save_level() {
        Gdx.app.log("Level", "saving level data");

        FileHandle file = LevelSelector.get_level_grid_fhandle(level_type, name);
        Writer writer = file.writer(false, "UTF-8");

        try {
            writer.write(String.format(Locale.US, "%d %d\n", grid.get_width(), grid.get_height()));
            writer.write(grid.get_grid_string() + "\n");

            writer.write(String.format(Locale.US, "%d\n", puck_positions.size));
            for (Vector2 pos : puck_positions) {
                writer.write(String.format(Locale.US, "%f %f\n", pos.x, pos.y));
            }
        } catch (IOException e) {
            file.delete();
            throw new GdxRuntimeException("Error writing save file", e);
        } finally {
            StreamUtils.closeQuietly(writer);
        }
    }

    /** Loads the grid and puck positions of the current level. */
    private void load_level() {
        Gdx.app.log("Level", "loading level");

        FileHandle file = LevelSelector.get_level_grid_fhandle(level_type, name);
        if (!file.exists())
            return;

        BufferedReader reader = file.reader(256, "UTF-8");
        try {
            String[] size = reader.readLine().split(" ");
            int width = Integer.parseInt(size[0]), height = Integer.parseInt(size[1]);
            grid.set_size(width, height);

            String grid_str = reader.readLine();
            grid.load_from_grid_string(grid_str);

            int n_pucks = Integer.parseInt(reader.readLine());
            puck_positions = new Array<Vector2>(n_pucks);

            String[] pos;
            for (int i = 0; i < n_pucks; i++) {
                pos = reader.readLine().split(" ");
                puck_positions.add(new Vector2(Float.parseFloat(pos[0]), Float.parseFloat(pos[1])));
            }
        } catch (IOException e) {
            throw new GdxRuntimeException("Error reading level file", e);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    /** Charge save data structure:
     * N (number of charges (lines))
     * CHARGE_TYPE X Y
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

    final Array<Vector2> get_puck_positions() {
        return puck_positions;
    }
}
