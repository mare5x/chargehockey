package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** Class that draws the level preview along with the scroll pane. */
class PreviewScrollPane extends ScrollPane {
    private final LevelFrameBuffer preview_fbo;

    PreviewScrollPane(List list, Skin skin, final LevelSelector level_selector, final SpriteBatch batch) {
        super(list, skin);

        preview_fbo = new LevelFrameBuffer(null);

        list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final Level level = level_selector.load_selected_level();
                if (level != null) {
                    preview_fbo.set_level(level);
                    preview_fbo.update(batch);
                }
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        batch.setColor(1, 1, 1, parentAlpha);
        float fbo_size = 0.5f * getWidth();
        preview_fbo.render(batch, getX() + getWidth() - fbo_size, getY(), fbo_size, fbo_size);
    }
}
