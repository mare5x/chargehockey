package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Grid;
import com.mare5x.chargehockey.level.GridCache;


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

    private static final float length = (float) Math.hypot(Grid.WORLD_WIDTH, Grid.WORLD_HEIGHT);
    private static final float knob_size = 1.75f;  // world units
    private float axis_w;

    private final Sprite symmetry_axis;
    private final Sprite move_knob, rotate_knob;

    private boolean move_knob_active = false;
    private boolean rotate_knob_active = false;

    private final Vector2 tmp_v = new Vector2();

    public SymmetryToolActor(ChargeHockeyGame game) {
        symmetry_axis = new Sprite(game.skin.getRegion("pixels/px_green"));
        move_knob = new Sprite(game.skin.getRegion("vertical_knob"));
        rotate_knob = new Sprite(game.skin.getRegion("rotate_knob"));

        setBounds((Grid.WORLD_WIDTH - length) / 2f, Grid.WORLD_HEIGHT / 2f - knob_size / 2f, length, knob_size);
        move_knob.setSize(knob_size, knob_size);
        rotate_knob.setSize(knob_size, knob_size);

        update_size(1);
        set_knob_position();

        DragListener drag_listener = new DragListener() {
            OrthographicCamera camera;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                localToStageCoordinates(tmp_v.set(x, y));
                if (move_knob.getBoundingRectangle().contains(tmp_v)) {
                    move_knob_active = true;
                    return super.touchDown(event, x, y, pointer, button);
                } else if (rotate_knob.getBoundingRectangle().contains(tmp_v)) {
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
                    setPosition(x - length / 2f, y - knob_size / 2f);
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
        tmp_v.x = (float) Math.cos(getRotation() * MathUtils.degreesToRadians);
        tmp_v.y = (float) Math.sin(getRotation() * MathUtils.degreesToRadians);
        tmp_v.scl(pos.dot(tmp_v)).sub(pos).scl(2);
        return pos.set(pos_x, pos_y).add(tmp_v);
    }

    private float get_center_x() {
        return getX() + length / 2f;
    }

    private float get_center_y() {
        return getY() + knob_size / 2f;
    }

    private void set_center_position(float x, float y) {
        setPosition(x - length / 2f, y - knob_size / 2f);
    }

    private void set_knob_position() {
        float cos = MathUtils.cosDeg(getRotation());
        float sin = MathUtils.sinDeg(getRotation());
        float center_x = get_center_x();
        float center_y = get_center_y();
        move_knob.setPosition(center_x - knob_size / 2f, center_y - knob_size / 2f);
        rotate_knob.setPosition(center_x + cos * 3 * knob_size - knob_size / 2f, center_y + sin * 3 * knob_size - knob_size / 2f);
    }

    public void update_size(float zoom) {
        axis_w = GridCache.calculate_grid_line_size(zoom) * 1.5f;
        symmetry_axis.setBounds(getX(), get_center_y() - axis_w / 2f, length, axis_w);

        symmetry_axis.setOriginCenter();
        move_knob.setOriginCenter();
        rotate_knob.setOriginCenter();
        setOrigin(Align.center);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        symmetry_axis.setPosition(x, get_center_y() - axis_w / 2f);
        set_knob_position();
    }

    @Override
    public void moveBy(float x, float y) {
        super.moveBy(x, y);
        symmetry_axis.translate(x, y);
        move_knob.translate(x, y);
        rotate_knob.translate(x, y);
    }

    @Override
    public void setRotation(float degrees) {
        super.setRotation(degrees);
        symmetry_axis.setRotation(degrees);
        rotate_knob.setRotation(degrees);
        move_knob.setRotation(degrees);
        set_knob_position();
    }

    @Override
    public void rotateBy(float amountInDegrees) {
        super.rotateBy(amountInDegrees);
        symmetry_axis.rotate(amountInDegrees);
        set_knob_position();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        symmetry_axis.draw(batch, parentAlpha);
        move_knob.draw(batch, parentAlpha);
        rotate_knob.draw(batch, parentAlpha);
    }

    public SymmetryToolState get_state() {
        return new SymmetryToolState(get_center_x(), get_center_y(), getRotation());
    }

    public void set_state(SymmetryToolState state) {
        set_center_position(state.center_x, state.center_y);
        setRotation(state.rotation);
    }
}
