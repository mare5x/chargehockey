package com.mare5x.chargehockey.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.StreamUtils;
import com.mare5x.chargehockey.actors.ChargeActor;
import com.mare5x.chargehockey.actors.ChargeActor.CHARGE;
import com.mare5x.chargehockey.actors.ChargeActor.ChargeState;
import com.mare5x.chargehockey.actors.SymmetryToolActor.SymmetryToolState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;


public class Level {
    public enum LEVEL_TYPE {
        EASY, MEDIUM, HARD, CUSTOM
    }

    /** AUTO is the save file that gets written to automatically and is the currently loaded save.
     * QUICKSAVE is the save file that is used only when explicitly called. (aka quicksave)
     * All save types are written to their own files.
     */
    public enum SAVE_TYPE {
        AUTO, QUICKSAVE
    }

    public static final String DEFAULT_HEADER = "0\n";

    private final String name;
    private final LEVEL_TYPE level_type;

    private final Grid grid;
    private Array<ChargeState> puck_states = new Array<ChargeState>();  // (x, y) of the puck's center

    private SymmetryToolState symmetry_tool_state;

    private boolean level_finished = false;

    Level(final String level_name, final LEVEL_TYPE level_type) {
        this.name = level_name;
        this.level_type = level_type;

        this.grid = new Grid();

        if (LevelSelector.level_file_exists(level_type, level_name))
            load_level();
        else
            empty_level();
    }

    public final LEVEL_TYPE get_type() {
        return level_type;
    }

    public final String get_name() {
        return name;
    }

    public boolean get_level_finished() {
        return level_finished;
    }

    public void set_level_finished(boolean finished) {
        level_finished = finished;
    }

    public SymmetryToolState get_symmetry_tool_state() {
        return symmetry_tool_state;
    }

    public void set_symmetry_tool_state(SymmetryToolState state) {
        symmetry_tool_state = state;
    }

    public void set_item(int row, int col, Grid.GRID_ITEM item) {
        grid.set_item(row, col, item);
    }

    public final Grid.GRID_ITEM get_grid_item(int row, int col) {
        return grid.get_item(row, col);
    }

    public void clear_grid() {
        grid.clear();
    }

    public boolean save_file_exists() {
        return LevelSelector.get_level_save_fhandle(level_type, name, SAVE_TYPE.AUTO).exists();
    }

    /** Creates a new empty level file. */
    private void empty_level() {
        save_level(new Array<ChargeActor>(0));
    }

    /** LEVEL GRID SAVE STRUCTURE
     * WIDTH HEIGHT
     * WIDTH * HEIGHT GRID ITEM CODES (ONE LINE STRING)
     * N PUCKS
     * X Y (optional: x y of partner) (partners are only for the editor)
     *
     * Save the grid and puck positions of the currently loaded level. */
    public void save_level(final Array<ChargeActor> puck_actors) {
        Gdx.app.log("Level", "saving level data");

        FileHandle file = LevelSelector.get_level_grid_fhandle(level_type, name);
        Writer writer = file.writer(false, "UTF-8");

        try {
            writer.write(String.format(Locale.US, "%d %d\n", grid.get_width(), grid.get_height()));
            writer.write(grid.get_grid_string() + "\n");

            // update the puck states array here, so the Level can just be passed around instead of reloaded using load_level
            puck_states.clear();

            writer.write(String.format(Locale.US, "%d\n", puck_actors.size));
            IntSet written_ids = new IntSet(puck_actors.size);
            for (ChargeActor puck : puck_actors) {
                if (!written_ids.contains(puck.get_id())) {
                    written_ids.add(puck.get_id());

                    writer.write(String.format(Locale.US, "%f %f", puck.get_x(), puck.get_y()));

                    puck_states.add(new ChargeState(CHARGE.PUCK, puck.get_x(), puck.get_y()));

                    ChargeActor partner = puck.get_partner();
                    if (partner != null) {
                        writer.write(String.format(Locale.US, " %f %f", partner.get_x(), partner.get_y()));

                        written_ids.add(partner.get_id());

                        puck_states.peek().partner = new ChargeState(CHARGE.PUCK, partner.get_x(), partner.get_y());
                    }
                    writer.write("\n");
                }
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
            puck_states = new Array<ChargeState>(n_pucks);
            for (int i = 0; i < n_pucks; i++) {
                String[] pos = reader.readLine().split(" ");

                puck_states.add(new ChargeState(CHARGE.PUCK, Float.parseFloat(pos[0]), Float.parseFloat(pos[1])));

                if (pos.length > 2) {
                    puck_states.peek().partner = new ChargeState(CHARGE.PUCK, Float.parseFloat(pos[2]), Float.parseFloat(pos[3]));
                    i++;
                }
            }
        } catch (IOException e) {
            throw new GdxRuntimeException("Error reading level file", e);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    /** .save structure:
     * HEADER 0/1 FLAG if level finished
     * N (number of charges)
     * CHARGE_TYPE(code) X Y (optional: x y of partner)
     * (optional) symmetry tool x y rotation
     */
    public void write_save_file(SAVE_TYPE save_type, Array<ChargeActor> charge_actors) {
        Gdx.app.log("Level", "saving charge state");

        FileHandle file = LevelSelector.get_level_save_fhandle(level_type, name, save_type);
        Writer writer = file.writer(false, "UTF-8");

        try {
            writer.write(level_finished ? "1\n" : "0\n");

            writer.write(String.valueOf(charge_actors.size) + "\n");
            IntSet written_ids = new IntSet(charge_actors.size);
            for (ChargeActor charge : charge_actors) {
                // partners get written together in the same line, so make sure not to write already written charges
                if (!written_ids.contains(charge.get_id())) {
                    ChargeState state = charge.get_state();
                    writer.write(String.format(Locale.US, "%s %f %f", state.type.code(), state.x, state.y));

                    written_ids.add(charge.get_id());

                    ChargeActor partner = charge.get_partner();
                    if (partner != null) {
                        state = partner.get_state();
                        writer.write(String.format(Locale.US, " %s %f %f", state.type.code(), state.x, state.y));

                        written_ids.add(partner.get_id());
                    }
                    writer.write("\n");
                }
            }

            if (symmetry_tool_state != null) {
                writer.write(String.format(Locale.US, "%f %f %f\n",
                        symmetry_tool_state.center_x, symmetry_tool_state.center_y, symmetry_tool_state.rotation));
            }
        } catch (IOException e) {
            file.delete();
            throw new GdxRuntimeException("Error writing file", e);
        } finally {
            StreamUtils.closeQuietly(writer);
        }
    }

    public Array<ChargeState> load_save_file(SAVE_TYPE save_type) {
        Gdx.app.log("Level", "loading charge state");

        FileHandle file = LevelSelector.get_level_save_fhandle(level_type, name, save_type);
        if (!file.exists()) {
            return null;
        }

        BufferedReader reader = file.reader(256, "UTF-8");
        try {
            level_finished = reader.readLine().equals("1");

            int n = Integer.parseInt(reader.readLine());
            Array<ChargeState> states = new Array<ChargeState>(n);
            for (int i = 0; i < n; i++) {
                String[] split = reader.readLine().split(" ");
                states.add(new ChargeState(CHARGE.from_code(split[0].charAt(0)), Float.parseFloat(split[1]), Float.parseFloat(split[2])));
                if (split.length > 3) {  // this means it has a partner
                    states.peek().partner = new ChargeState(CHARGE.from_code(split[3].charAt(0)), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
                    i++;
                }
            }

            // symmetry tool state
            String symmetry_tool_s = reader.readLine();
            if (symmetry_tool_s != null) {
                String[] split = symmetry_tool_s.split(" ");
                symmetry_tool_state = new SymmetryToolState(Float.parseFloat(split[0]),
                        Float.parseFloat(split[1]), Float.parseFloat(split[2]));
            }

            return states;
        } catch (IOException e) {
            throw new GdxRuntimeException("Error reading file", e);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    /** NOTE: use this only when you want to write the header without changing the rest of
     * the .save file because this method is SLOW and inefficient. */
    public void write_save_header() {
        FileHandle save_file = LevelSelector.get_level_save_fhandle(level_type, name);
        if (!save_file.exists())
            return;

        // writes the new header and copies the old save file to the new temp file, then replaces
        // the old save file

        FileHandle tmp_file = save_file.sibling("tmp");

        BufferedReader reader = save_file.reader(256, "UTF-8");
        Writer writer = tmp_file.writer(false, "UTF-8");
        try {
            // write the header
            writer.write(level_finished ? "1\n" : "0\n");

            reader.readLine();  // skip the header
            String line = reader.readLine();
            while (line != null) {
                writer.write(line + "\n");  // readline trims \n
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new GdxRuntimeException("Error writing header", e);
        } finally {
            StreamUtils.closeQuietly(writer);
            StreamUtils.closeQuietly(reader);
        }

        tmp_file.moveTo(save_file);  // replace the save file
    }

    public final Array<ChargeState> get_puck_states() {
        return puck_states;
    }
}
