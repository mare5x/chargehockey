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
import java.util.Locale;
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
        void on_success(String message);
        void on_failure();
    }

    static String export_list_message(Array<String> export_list) {
        if (export_list.size > 0) {
            if (export_list.size < 10)
                if (export_list.size == 1)
                    return String.format(Locale.US, "EXPORTED: %s", export_list.toString(", "));
                else
                    return String.format(Locale.US, "EXPORTED %d LEVELS: %s", export_list.size, export_list.toString(", "));
            else
                return String.format(Locale.US, "EXPORTED %d LEVELS", export_list.size);
        } else {
            return "NOTHING EXPORTED";
        }
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
                    callback.on_success(export_list_message(Array.with(path.file().getAbsolutePath())));
                else
                    callback.on_failure();
            }

            @Override
            public void write_result(OutputStream stream) {
                if (export(level_name, stream))
                    callback.on_success(export_list_message(Array.with(level_name)));
                else
                    callback.on_failure();
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
                    callback.on_success(export_list_message(Array.with(path.file().getAbsolutePath())));
                else
                    callback.on_failure();
            }

            @Override
            public void write_result(OutputStream stream) {
                if (export(levels, stream))
                    callback.on_success(export_list_message(levels));
                else
                    callback.on_failure();
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
                    callback.on_success(export_list_message(Array.with(path.file().getAbsolutePath())));
                else
                    callback.on_failure();
            }

            @Override
            public void write_result(OutputStream stream) {
                if (export_all(stream)) {
                    FileHandle path = Level.get_levels_dir_fhandle(Level.LEVEL_TYPE.CUSTOM);
                    Array<String> exported = new Array<>();
                    for (FileHandle p : path.list())
                        exported.add(p.nameWithoutExtension());
                    callback.on_success(export_list_message(exported));
                }
                else
                    callback.on_failure();
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
