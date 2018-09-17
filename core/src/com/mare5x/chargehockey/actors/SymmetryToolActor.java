package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Grid;


// todo make it be actually centered
public class SymmetryToolActor extends Actor {
    public static class SymmetryToolState {
        public float center_x, center_y, rotation;

        public SymmetryToolState(float x, float y, float rotation) {
            this.center_x = x;
            this.center_y = y;
            this.rotation = rotation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SymmetryToolState that = (SymmetryToolState) o;

            return MathUtils.isEqual(that.center_x, center_x, 0.01f) &&
                    MathUtils.isEqual(that.center_y, center_y, 0.01f) &&
                    MathUtils.isEqual(that.rotation, rotation, 0.01f);
        }
    }

    // Actor position is in world coordinates. The sprite sizes and positions are in screen
    // coordinates. (Like ChargeActor);
    // Actor getX() and getY() represent the center of the axis in world coordinates.

    private static final float knob_size = 0.75f;  // relative units

    private final float screen_knob_size = to_screen(knob_size);
    private final float screen_axis_w = to_screen(0.05f);

    private final Sprite symmetry_axis;
    private final Sprite move_knob, rotate_knob;

    private boolean move_knob_active = false;
    private boolean rotate_knob_active = false;

    private final Vector2 tmp_vec = new Vector2();

    public SymmetryToolActor(ChargeHockeyGame game) {
        symmetry_axis = game.create_sprite("px_green");
        move_knob = game.create_sprite("ui_vertical_knob");
        rotate_knob = game.create_sprite("ui_rotate_knob");

        move_knob.setSize(screen_knob_size, screen_knob_size);
        rotate_knob.setSize(screen_knob_size, screen_knob_size);

        set_center_position(Grid.WORLD_WIDTH / 2f, Grid.WORLD_HEIGHT / 2f);

        DragListener drag_listener = new DragListener() {
            OrthographicCamera camera;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                get_screen_coordinates(event.getStageX(), event.getStageY());
                if (move_knob.getBoundingRectangle().contains(tmp_vec)) {
                    move_knob_active = true;
                    return super.touchDown(event, x, y, pointer, button);
                } else if (rotate_knob.getBoundingRectangle().contains(tmp_vec)) {
                    rotate_knob_active = true;
                    return super.touchDown(event, x, y, pointer, button);
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                move_knob_active = false;
                rotate_knob_active = false;
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                camera = (OrthographicCamera) getStage().getCamera();
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (move_knob_active) {
                    x = event.getStageX();
                    y = event.getStageY();

                    float round_x = MathUtils.round(x);
                    float round_y = MathUtils.round(y);

                    if (Math.abs(x - round_x) < 0.2f * Math.max(0.5f, camera.zoom))
                        x = round_x;
                    if (Math.abs(y - round_y) < 0.2f * Math.max(0.5f, camera.zoom))
                        y = round_y;

                    // if i use moveby, the movement rockets off
                    set_center_position(x, y);
                } else if (rotate_knob_active) {
                    float deg = MathUtils.radiansToDegrees * MathUtils.atan2(event.getStageY() - get_center_y(), event.getStageX() - get_center_x());
                    float step_deg = MathUtils.round(deg / 15f) * 15;
                    if (Math.abs(deg - step_deg) < 5 * Math.max(0.5f, camera.zoom))
                        setRotation(step_deg);
                    else
                        setRotation(deg);
                }
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                // handle out of bounds
                if (!Grid.WORLD_RECT.contains(get_center_x(), get_center_y())) {
                    x = MathUtils.clamp(get_center_x(), 1, Grid.WORLD_WIDTH - 1);
                    y = MathUtils.clamp(get_center_y(), 1, Grid.WORLD_HEIGHT - 1);
                    set_center_position(x, y);
                }
            }
        };
        drag_listener.setTapSquareSize(0);

        addListener(drag_listener);
    }

    public boolean is_enabled() {
        return isVisible();
    }

    public void set_enabled(boolean enabled) {
        setVisible(enabled);
    }

    /** Returns the position of pos mirrored on the other side of the symmetrical axis. */
    public Vector2 get_symmetrical_pos(Vector2 pos) {
        // Project pos onto a unit vector on the rotation axis then scale it by 2 and add it to the
        // original pos.
        float pos_x = pos.x;
        float pos_y = pos.y;
        pos.sub(get_center_x(), get_center_y());
        tmp_vec.x = (float) Math.cos(getRotation() * MathUtils.degreesToRadians);
        tmp_vec.y = (float) Math.sin(getRotation() * MathUtils.degreesToRadians);
        tmp_vec.scl(pos.dot(tmp_vec)).sub(pos).scl(2);
        return pos.set(pos_x, pos_y).add(tmp_vec);
    }

    private float get_center_x() {
        return getX();
    }

    private float get_center_y() {
        return getY();
    }

    private void set_center_position(float x, float y) {
        setPosition(x, y);
    }

    private void update_bounds() {
        int screen_height = getStage().getViewport().getScreenHeight();
        int screen_width = getStage().getViewport().getScreenWidth();
        float axis_width = screen_width * screen_height;

        Vector2 center_screen_pos = get_screen_coordinates(get_center_x(), get_center_y());
        symmetry_axis.setBounds(-axis_width / 2, center_screen_pos.y - screen_axis_w / 2, axis_width, screen_axis_w);
        move_knob.setPosition(center_screen_pos.x - screen_knob_size / 2, center_screen_pos.y - screen_knob_size / 2);

        float cos = MathUtils.cosDeg(getRotation());
        float sin = MathUtils.sinDeg(getRotation());
        rotate_knob.setPosition(center_screen_pos.x - screen_knob_size / 2 + cos * 3 * screen_knob_size,
                                center_screen_pos.y - screen_knob_size / 2 + sin * 3 * screen_knob_size);

        symmetry_axis.setOrigin(center_screen_pos.x - symmetry_axis.getX(), screen_axis_w / 2);
        move_knob.setOriginCenter();
        rotate_knob.setOriginCenter();
        setOrigin(Align.center);
    }

    @Override
    public void setRotation(float degrees) {
        super.setRotation(degrees);
        symmetry_axis.setRotation(degrees);
        rotate_knob.setRotation(degrees);
        move_knob.setRotation(degrees);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        update_bounds();
        symmetry_axis.draw(batch, parentAlpha);
        move_knob.draw(batch, parentAlpha);
        rotate_knob.draw(batch, parentAlpha);
    }

    @Override
    protected void drawDebugBounds(ShapeRenderer shapes) {
        update_bounds();
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.ORANGE);
        shapes.rect(symmetry_axis.getX(), symmetry_axis.getY(), symmetry_axis.getWidth(), symmetry_axis.getHeight());
        shapes.rect(move_knob.getX(), move_knob.getY(), move_knob.getWidth(), move_knob.getHeight());
        shapes.rect(rotate_knob.getX(), rotate_knob.getY(), rotate_knob.getWidth(), rotate_knob.getHeight());
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        // hit detection for the move and rotate knobs
        if (touchable && !isTouchable()) return null;
        localToStageCoordinates(tmp_vec.set(x, y));  // to world
        get_screen_coordinates(tmp_vec.x, tmp_vec.y);
        if (move_knob.getBoundingRectangle().contains(tmp_vec)) return this;
        if (rotate_knob.getBoundingRectangle().contains(tmp_vec)) return this;
        return null;
    }

    public SymmetryToolState get_state() {
        return new SymmetryToolState(get_center_x(), get_center_y(), getRotation());
    }

    public void set_state(SymmetryToolState state) {
        set_center_position(state.center_x, state.center_y);
        setRotation(state.rotation);
    }

    /** Returns the screen coordinates in tmp_vec. */
    private Vector2 get_screen_coordinates(float world_x, float world_y) {
        getStage().stageToScreenCoordinates(tmp_vec.set(world_x, world_y));
        tmp_vec.y = getStage().getViewport().getScreenHeight() - tmp_vec.y - 1;
        return tmp_vec;
    }

    static private float to_screen(float relative) {
        return relative * ChargeActor.BASE_CHARGE_SIZE;
    }
}
