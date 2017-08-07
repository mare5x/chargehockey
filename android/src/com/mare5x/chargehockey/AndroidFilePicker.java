package com.mare5x.chargehockey;

import android.os.Environment;

import com.badlogic.gdx.files.FileHandle;


class AndroidFilePicker extends FilePicker {
    AndroidFilePicker(ChargeHockeyGame game) {
        super(game);
    }

    AndroidFilePicker(ChargeHockeyGame game, FileFilter filter) {
        super(game, filter);
    }

    @Override
    FileHandle get_root_path() {
        return new FileHandle(Environment.getRootDirectory().getParentFile());
    }
}