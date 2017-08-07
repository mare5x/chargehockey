package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mare5x.chargehockey.editor.FilePicker;


class DesktopFilePicker extends FilePicker {
    DesktopFilePicker(ChargeHockeyGame game) {
        super(game);
    }

    DesktopFilePicker(ChargeHockeyGame game, FileFilter filter) {
        super(game, filter);
    }

    @Override
    public FileHandle get_root_path() {
        return Gdx.files.external("");
    }
}
