package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mare5x.chargehockey.editor.FilePickerScreen.FilePickerCallback;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/** Class for exporting levels.
 *  Deals with picking the destination using a file picker.
 */
public abstract class Exporter {
    protected static final FilePicker.FileFilter export_filter = new FilePicker.FileFilter() {
        @Override
        public boolean is_valid(FileHandle path) {
            return path.isDirectory();
        }
    };

    public interface ExporterCallback {
        // path is the level directory if exporting a single level and the parent directory otherwise
        void on_success(FileHandle path);
        void on_failure(FileHandle path);
    }

    protected abstract void show_file_picker(BaseMenuScreen parent_screen, String name, FilePickerCallback on_result);

    void export(final String level_name, BaseMenuScreen parent_screen, final ExporterCallback callback) {
        show_file_picker(parent_screen, level_name, new FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                if (path.isDirectory()) {
                    path = path.child("chargehockey_export_" + level_name + ".zip");
                }
                if (export(level_name, path.write(false)))
                    callback.on_success(path);
                else
                    callback.on_failure(path);
            }

            @Override
            public void write_result(OutputStream stream, String path) {
                if (export(level_name, stream))
                    callback.on_success(new FileHandle(path));
                else
                    callback.on_failure(new FileHandle(path));
            }
        });
    }

    /* Export a single custom level to the destination. Returns true on success. */
    private boolean export(String level_name, OutputStream stream) {
        try {
            FileHandle src = Level.get_level_dir_fhandle(Level.LEVEL_TYPE.CUSTOM, level_name);
            ZipOutputStream zos = new ZipOutputStream(stream);
            zip_dir(src, zos, level_name + "/");
            zos.close();
            stream.close();
            return true;
        } catch (GdxRuntimeException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    void export(final Array<String> levels, BaseMenuScreen parent_screen, final ExporterCallback callback) {
        show_file_picker(parent_screen, Integer.toString(levels.size), new FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                if (path.isDirectory()) {
                    path = path.child("chargehockey_export_" + levels.size + ".zip");
                }
                if (export(levels, path.write(false)))
                    callback.on_success(path);
                else
                    callback.on_failure(path);
            }

            @Override
            public void write_result(OutputStream stream, String path) {
                if (export(levels, stream))
                    callback.on_success(new FileHandle(path));
                else
                    callback.on_failure(new FileHandle(path));
            }
        });
    }

    /* Export multiple levels to the destination, which must be a directory. */
    private boolean export(Array<String> levels, OutputStream stream) {
        ZipOutputStream zos = new ZipOutputStream(stream);
        try {
            for (String level_name : levels) {
                FileHandle src = Level.get_level_dir_fhandle(Level.LEVEL_TYPE.CUSTOM, level_name);
                zip_dir(src, zos, level_name + "/");
            }
            zos.close();
            stream.close();
        } catch (GdxRuntimeException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void export_all(BaseMenuScreen parent_screen, final ExporterCallback callback) {
        show_file_picker(parent_screen, "all", new FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                if (path.isDirectory()) {
                    path = path.child("chargehockey_export.zip");
                }
                if (export_all(path.write(false)))
                    callback.on_success(path);
                else
                    callback.on_failure(path);
            }

            @Override
            public void write_result(OutputStream stream, String path) {
                if (export_all(stream))
                    callback.on_success(new FileHandle(path));
                else
                    callback.on_failure(new FileHandle(path));
            }
        });
    }

    /* Export all custom levels to the destination. Returns true on success. */
    private boolean export_all(OutputStream stream) {
        try {
            FileHandle src = Level.get_levels_dir_fhandle(Level.LEVEL_TYPE.CUSTOM);
            ZipOutputStream zos = new ZipOutputStream(stream);
            zip_dir(src, zos, "");
            zos.close();
            stream.close();
            return true;
        } catch (GdxRuntimeException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void zip_dir(FileHandle dir, ZipOutputStream zos, String path) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;

        // N.B. we don't create explicit entries for directories
        for (FileHandle child : dir.list()) {
            if (child.isDirectory()) {
                zip_dir(child, zos, path + child.name() + "/");
                continue;
            }
            InputStream fis = child.read();
            zos.putNextEntry(new ZipEntry(path + child.name()));
            while ((bytesRead = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, bytesRead);
            }
            zos.closeEntry();
            fis.close();
        }
    }
}
