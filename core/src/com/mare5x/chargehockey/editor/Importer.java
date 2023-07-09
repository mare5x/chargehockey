package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/* Class that handles importing custom levels. */
public abstract class Importer {
    protected static final FilePicker.FileFilter import_filter = new FilePicker.FileFilter() {
        @Override
        public boolean is_valid(FileHandle path) {
            // On desktop we allow importing not only zip files.
            String ext = path.extension();
            return path.isDirectory() || ext.equals("grid") || ext.equals("save") || ext.equals("csave") || ext.equals("zip");
        }
    };

    protected abstract void run(BaseMenuScreen parent_screen);

    protected void handle_result(BaseMenuScreen parent_screen, String msg) {
        parent_screen.show_notification(msg);
    }

    // Import a previously exported zip file containing custom levels.
    protected String import_zip(InputStream stream) {
        Array<String> import_list = new Array<>();
        try (ZipInputStream zip_stream = new ZipInputStream(stream)) {
            ZipEntry entry = zip_stream.getNextEntry();
            while (entry != null) {
                Gdx.app.log("Importer", entry.getName());
                FileHandle dst = Level.get_levels_dir_fhandle(Level.LEVEL_TYPE.CUSTOM)
                        .child(entry.getName());
                OutputStream output = null;

                // Copy zip entries creating folders as necessary.
                // The assumption is that we are importing only valid (exported) files.
                String ext = dst.extension();
                if (!dst.exists() && (ext.equals("grid") || ext.equals("save") || ext.equals("csave"))) {
                    try {
                        output = dst.write(false);
                        StreamUtils.copyStream(zip_stream, output);
                        if (ext.equals("grid")) {
                            import_list.add(dst.nameWithoutExtension());
                        }
                    } catch (Exception ex) {
                        Gdx.app.error("IMPORTER", "ERROR IMPORTING SAVE FILE!", ex);
                    } finally {
                        StreamUtils.closeQuietly(output);
                    }
                }

                entry = zip_stream.getNextEntry();
            }
            zip_stream.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return import_list_message(import_list);
    }

    private String import_list_message(Array<String> import_list) {
        if (import_list.size > 0) {
            if (import_list.size < 10)
                if (import_list.size == 1)
                    return String.format(Locale.US, "IMPORTED LEVEL: %s", import_list.toString(", "));
                else
                    return String.format(Locale.US, "IMPORTED %d LEVELS: %s", import_list.size, import_list.toString(", "));
            else
                return String.format(Locale.US, "IMPORTED %d LEVELS", import_list.size);
        } else {
            return "NOTHING IMPORTED";
        }
    }

    /* Returns a message string suitable for display. */
    protected String handle_import(FileHandle path) {
        if (path.extension().equals("zip")) {
            return import_zip(path.read());
        }

        if (path.isDirectory()) {
            Array<String> import_list = new Array<>();
            for (FileHandle child : path.list()) {
                if (import_level(child) || import_dir(child)) {
                    import_list.add(child.nameWithoutExtension());
                }
            }
            return import_list_message(import_list);
        } else {
            String extension = path.extension();
            if (extension.equals("grid")) {
                path.copyTo(Level.get_level_grid_fhandle(LEVEL_TYPE.CUSTOM, path.nameWithoutExtension()));
                return String.format(Locale.US, "IMPORTED %s", path.name());
            } else if (extension.equals("save")) {
                if (import_save(path, Level.SAVE_TYPE.AUTO))
                    return String.format(Locale.US, "IMPORTED %s", path.name());
                else
                    return "CAN'T IMPORT SAVE FILE. MAKE SURE THE LEVEL EXISTS.";
            } else if (extension.equals("csave")) {
                if (import_save(path, Level.SAVE_TYPE.QUICKSAVE))
                    return String.format(Locale.US, "IMPORTED %s", path.name());
                else
                    return "CAN'T IMPORT SAVE FILE. MAKE SURE THE LEVEL EXISTS.";
            } else {
                return "YOU MUST PICK A VALID IMPORT LOCATION";
            }
        }
    }

    /** Attempts to import path, which must be a directory.
     * It doesn't import anything other than .grid and .save files. */
    private boolean import_dir(FileHandle path) {
        if (path.isDirectory()) {
            String name = path.name();
            FileHandle grid_path = path.child(name + ".grid");
            if (grid_path.exists())
                return import_level(grid_path);
        }
        return false;
    }

    private boolean import_level(FileHandle grid_path) {
        if (grid_path.extension().equals("grid")) {
            String name = grid_path.nameWithoutExtension();
            grid_path.copyTo(Level.get_level_grid_fhandle(LEVEL_TYPE.CUSTOM, name));

            FileHandle save_file = grid_path.sibling(name + ".save");
            if (save_file.exists())
                import_save(save_file, Level.SAVE_TYPE.AUTO);

            save_file = grid_path.sibling(name + ".csave");
            if (save_file.exists())
                import_save(save_file, Level.SAVE_TYPE.QUICKSAVE);

            return true;
        }
        return false;
    }

    /** NOTE: Assumes that path points to an existing, valid .save file. */
    private boolean import_save(FileHandle path, Level.SAVE_TYPE save_type) {
        String name = path.nameWithoutExtension();
        if (Level.get_level_grid_fhandle(LEVEL_TYPE.CUSTOM, name).exists()) {  // a save file without a grid would be useless
            FileHandle save_path = Level.get_level_save_fhandle(LEVEL_TYPE.CUSTOM, name, save_type);

            // manually copy the save file, but first reset the save file header, so that the
            // level completion flag gets reset. this is necessary because once the flag is set, it's 'permanent'

            BufferedReader reader = path.reader(256, "UTF-8");
            Writer writer = save_path.writer(false, "UTF-8");
            try {
                writer.write(Level.DEFAULT_HEADER);
                reader.readLine();  // skip first header line
                String line = reader.readLine();
                while (line != null) {
                    writer.write(line + "\n");
                    line = reader.readLine();
                }
            } catch (IOException e) {
                Gdx.app.error("IMPORTER", "ERROR IMPORTING SAVE FILE!", e);
            } finally {
                StreamUtils.closeQuietly(writer);
                StreamUtils.closeQuietly(reader);
            }

            return true;
        }
        return false;
    }
}
