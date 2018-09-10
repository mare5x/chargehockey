package com.mare5x.chargehockey.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

/** This stage handles drawing and event handling of pucks and charges. The distinction is
 * necessary because charges are drawn in screen coordinates whereas the pucks are drawn in world
 * coordinates. */
public class GameStage extends Stage {
    private final Group puck_group = new Group();
    private final Group charge_group = new Group();
    private final Stage screen_stage;
    private boolean debug = false;
    private ShapeRenderer debug_renderer;

    GameStage(Viewport viewport, Batch batch, Stage screen_stage) {
        super(viewport, batch);
        this.screen_stage = screen_stage;

        addActor(puck_group);
        addActor(charge_group);
    }

    public void add_puck(Actor actor) {
        puck_group.addActor(actor);
    }

    public void add_charge(Actor actor) {
        charge_group.addActor(actor);
    }

    @Override
    public void setDebugAll(boolean debugAll) {
        super.setDebugAll(debugAll);
        this.debug = debugAll;
    }

    @Override
    public void draw() {
//        super.draw();
        draw_pucks();
        draw_charges();

        if (debug) draw_debug();
    }

    private void draw_debug() {
        if (debug_renderer == null) {
            debug_renderer = new ShapeRenderer();
            debug_renderer.setAutoShapeType(true);
        }

        getRoot().debugAll();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        debug_renderer.setProjectionMatrix(getCamera().combined);
        debug_renderer.begin();
        puck_group.drawDebug(debug_renderer);
        debug_renderer.end();

        debug_renderer.setProjectionMatrix(screen_stage.getCamera().combined);
        debug_renderer.begin();
        charge_group.drawDebug(debug_renderer);
        debug_renderer.end();
    }

    private void draw_pucks() {
        Batch batch = getBatch();
        batch.setProjectionMatrix(getCamera().combined);
        batch.begin();
        puck_group.draw(batch, 1);
        batch.end();
    }

    private void draw_charges() {
        Batch batch = getBatch();
        batch.setProjectionMatrix(screen_stage.getCamera().combined);
        batch.begin();
        charge_group.draw(batch, 1);
        batch.end();
    }
}
