package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.GridCache;


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

    private static final float length = (float) Math.hypot(ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);
    private static final float knob_size = 2;  // world units
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

        axis_w = GridCache.calculate_grid_line_size(1) * 1.5f;

        setBounds((ChargeHockeyGame.WORLD_WIDTH - length) / 2f, ChargeHockeyGame.WORLD_HEIGHT / 2f - knob_size / 2f, length, knob_size);
        symmetry_axis.setBounds(getX(), get_center_y() - axis_w / 2f, length, axis_w);
        move_knob.setSize(knob_size, knob_size);
        rotate_knob.setSize(knob_size, knob_size);

        symmetry_axis.setOriginCenter();
        move_knob.setOriginCenter();
        rotate_knob.setOriginCenter();
        setOrigin(Align.center);

        set_knob_position();

        DragListener drag_listener = new DragListener() {
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
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (move_knob_active) {
                    x = event.getStageX() - getTouchDownX();
                    y = event.getStageY() - getTouchDownY();

                    // if i use moveby, the movement rockets off
                    setPosition(x, y);

                } else if (rotate_knob_active) {
                    float deg = MathUtils.radiansToDegrees * MathUtils.atan2(event.getStageY() - get_center_y(), event.getStageX() - get_center_x());
                    setRotation(deg);
                }
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                // handle out of bounds
                if (!ChargeHockeyGame.WORLD_RECT.contains(get_center_x(), get_center_y())) {
                    x = MathUtils.clamp(get_center_x(), 1, ChargeHockeyGame.WORLD_WIDTH - 1);
                    y = MathUtils.clamp(get_center_y(), 1, ChargeHockeyGame.WORLD_HEIGHT - 1);
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

    /** Orthogonal projection of pos coordinates to the given slope (k1).
     * NOTE: k1 must not be 0 or Infinity. */
    private Vector2 orthogonal_projection(Vector2 pos, float k1) {
        float center_x = get_center_x();
        float center_y = get_center_y();
        pos.sub(center_x, center_y);

        float k2 = -1 / k1;
        float n = pos.y - k2 * pos.x;

        float new_x = n / (k1 - k2);
        float new_y = k1 * new_x;

        return pos.set(new_x, new_y).add(center_x, center_y);
    }

    /** Returns the position of pos mirrored on the other side of the symmetrical axis. */
    public Vector2 get_symmetrical_pos(Vector2 pos) {
        float rotation = getRotation();
        if (rotation % 90 == 0) {
            if (rotation % 180 == 0) {  // 0 or 180 only y
                pos.y -= 2 * (pos.y - get_center_y());
                return pos;
            }
            // 90
            pos.x -= 2 * (pos.x - get_center_x());
            return pos;
        }
        float k1 = (float) Math.tan(rotation * MathUtils.degreesToRadians);
        orthogonal_projection(tmp_v.set(pos), k1);
        tmp_v.sub(pos).scl(2);
        return pos.add(tmp_v);
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
        symmetry_axis.setBounds(getX(), getY() + knob_size / 2f - axis_w / 2f, length, axis_w);

        symmetry_axis.setOriginCenter();
        move_knob.setOriginCenter();
        rotate_knob.setOriginCenter();
        setOrigin(Align.center);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        symmetry_axis.setPosition(x, y + knob_size / 2f - axis_w / 2f);
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
