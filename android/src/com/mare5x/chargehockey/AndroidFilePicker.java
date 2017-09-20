package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mare5x.chargehockey.editor.FilePicker;


class AndroidFilePicker extends FilePicker {
    AndroidFilePicker(ChargeHockeyGame game) {
        super(game);
    }

    AndroidFilePicker(ChargeHockeyGame game, FileFilter filter) {
        super(game, filter);
    }

    @Override
    public FileHandle get_root_path() {
//        return new FileHandle(Environment.getRootDirectory().getParentFile());
        return Gdx.files.external("");
    }
}
