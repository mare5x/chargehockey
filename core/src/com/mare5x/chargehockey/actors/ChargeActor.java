package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.game.CameraController;
import com.mare5x.chargehockey.game.GameScreen;


public class ChargeActor extends Actor {
    public enum CHARGE {
        POSITIVE('p'),
        NEGATIVE('n'),
        PUCK('k');

        private char code;  // item abbreviation code used when writing to files
        CHARGE(char code) {
            this.code = code;
        }

        public static CHARGE from_code(char code) {
            switch (code) {
                case 'p': return POSITIVE;
                case 'n': return NEGATIVE;
                case 'k': return PUCK;
            }
            return null;
        }

        public char code() {
            return code;
        }
    }

    public static class ChargeState {
        public CHARGE type;
        public float x, y;
        public ChargeState partner;  // manually set the partner

        public ChargeState(CHARGE type, float x, float y) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.partner = null;
        }
    }

    public abstract static class DragCallback {
        public abstract void out_of_bounds(ChargeActor charge, boolean dragged);  // dragged tells whether charge was dragged physically or by symmetry
        public void drag(ChargeActor charge) {}
        public void drag_started(ChargeActor charge) {}
    }

    private final CHARGE charge_type;
    final Sprite sprite;

    private ChargeActor partner;  // the symmetrical tool partner of this charge (must have the same drag_callback)
    private final Vector2 tmp_vec = new Vector2();

    public static final float MAX_SIZE = 3;
    public static final float MIN_SIZE = 0.5f;
    private static float SIZE = 2;  // the public shared size of all charges set in the settings
    private float radius = SIZE / 2f;  // all 'size' checking, etc uses this
    private float charge_size = 2 * radius;  // this is the effective size of the charge (not necessarily the current size), it's the size the charge gets reset to
    private static final float WEIGHT = 9.1e-31f;  // kg
    private static final float ABS_CHARGE = 1.6e-19f;  // Coulombs

    private static int UID_COUNTER = 0;
    private final int uid = ++UID_COUNTER;  // unique identifier for every charge (used in .save files)

    ChargeActor(final ChargeHockeyGame game, final CHARGE charge_type, final DragCallback drag_callback) {
        this(game, charge_type, drag_callback, null);
    }

    public ChargeActor(final ChargeHockeyGame game, final CHARGE charge_type, final DragCallback drag_callback, final SymmetryToolActor symmetry_tool) {
        super();

        this.charge_type = charge_type;

        sprite = new Sprite();
        switch (charge_type) {
            case POSITIVE:
                sprite.setRegion(game.sprites.findRegion("charge_pos"));
                break;
            case NEGATIVE:
                sprite.setRegion(game.sprites.findRegion("charge_neg"));
                break;
            case PUCK:
                sprite.setRegion(game.sprites.findRegion("puck"));
                charge_size = PuckActor.SIZE;  // make pucks always have the same size
                break;
        }

        reset_size();

        if (drag_callback != null) {
            DragListener drag_listener = new DragListener() {
                private final float max_zoom = CameraController.ZoomLevel.MAX.get_amount();
                private final float min_zoom = CameraController.ZoomLevel.MIN.get_amount();

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    // when a charge is touched, increase its size
                    // adjust is necessary to translate the old x,y into new x,y local coordinates
                    float zoom = ((OrthographicCamera) (getStage().getCamera())).zoom;
                    zoom -= max_zoom;

                    // from 1.1 to 1.6 based on the current zoom
                    float enlarge_factor = 1.1f + (zoom / (min_zoom - max_zoom)) * 0.5f;

                    float adjust = radius * enlarge_factor - radius;
                    boolean ret = super.touchDown(event, x + adjust, y + adjust, pointer, button);

                    // go ahead with the sizing only if the input is valid
                    if (ret) {
                        clearActions();
                        set_size(enlarge_factor * charge_size, true);

                        if (partner != null) {
                            partner.clearActions();
                            partner.set_size(enlarge_factor * charge_size, true);
                        }
                    }
                    return ret;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    set_size(charge_size, true);
                    if (partner != null) {
                        partner.set_size(charge_size, true);
                    }
                    if (!isDragging()) {  // clicked (not dragged)
                        dragStop(event, x, y, pointer);
                        cancel();
                    } else
                        super.touchUp(event, x, y, pointer, button);
                }

                @Override
                public void drag(InputEvent event, float dx, float dy, int pointer) {
                    moveBy(dx - getTouchDownX(), dy - getTouchDownY());

                    // move the partner symmetrically
                    if (partner != null && symmetry_tool.is_enabled()) {
                        symmetry_tool.get_symmetrical_pos(tmp_vec.set(get_x(), get_y()));
                        partner.set_position(tmp_vec.x, tmp_vec.y);
                        drag_callback.drag(partner);
                    }

                    drag_callback.drag(ChargeActor.this);
                }

                @Override
                public void dragStart(InputEvent event, float x, float y, int pointer) {
                    drag_callback.drag_started(ChargeActor.this);
                    if (partner != null)
                        drag_callback.drag_started(partner);
                }

                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer) {
                    if (check_out_of_bounds())
                        drag_callback.out_of_bounds(ChargeActor.this, true);
                    if (partner != null && partner.check_out_of_world())
                        drag_callback.out_of_bounds(partner, false);
                }
            };
            drag_listener.setTapSquareSize(-1);

            addListener(drag_listener);
        }
    }

    public void reset_size() {
        if (charge_type == CHARGE.PUCK)
            charge_size = PuckActor.SIZE;
        else
            charge_size = SIZE;
        set_size(charge_size);
    }

    private void set_size(float size) {
        set_size(size, false);
    }

    /** Correctly sizes the circular actor. If animate is false the sizing happens immediately. */
    private void set_size(float size, boolean animate) {
        if (animate) {
            addAction(Actions.sizeTo(size, size, 0.1f));
        } else {
            setSize(size, size);
        }
    }

    public float get_x() {
        return getX() + radius;
    }

    public float get_y() {
        return getY() + radius;
    }

    public void set_position(float center_x, float center_y) {
        setPosition(center_x - radius, center_y - radius);
    }

    @Override
    public void moveBy(float x, float y) {
        super.moveBy(x, y);
        sprite.translate(x, y);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        sprite.setPosition(x, y);
    }

    @Override
    public void setPosition(float x, float y, int alignment) {
        super.setPosition(x, y, alignment);
        sprite.setPosition(getX(), getY());
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        super.setBounds(x, y, width, height);
        sprite.setBounds(x, y, width, height);
    }

    @Override
    public void setSize(float width, float height) {
        // save the center position
        float x = get_x();
        float y = get_y();

        super.setSize(width, height);
        sprite.setSize(width, height);

        radius = width / 2f;

        set_position(x, y);  // re-center
        sprite.setOriginCenter();
        setOrigin(radius, radius);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        sprite.draw(batch, parentAlpha);
    }

    public float get_charge() {
        return ABS_CHARGE * get_direction();
    }

    public float get_abs_charge() {
        return ABS_CHARGE;
    }

    private int get_direction() {
        return charge_type == CHARGE.POSITIVE || charge_type == CHARGE.PUCK ? 1 : -1;
    }

    CHARGE get_type() {
        return charge_type;
    }

    public float get_weight() {
        return WEIGHT;
    }

    /** Returns the vector from puck to this charge, taking the charge's polarity into account. */
    public Vector2 get_vec(ChargeActor puck) {
        return new Vector2(puck.get_x(), puck.get_y()).sub(get_x(), get_y()).scl(get_direction());
    }

    public ChargeState get_state() {
        return new ChargeState(charge_type, get_x(), get_y());
    }

    /** Circular collision detection with a rectangle. */
    public boolean intersects(Rectangle rectangle) {
        float center_x = get_x();
        float center_y = get_y();

        // find the closest rectangle point to the circle (charge)
        float closest_x = MathUtils.clamp(center_x, rectangle.x, rectangle.x + rectangle.width);
        float closest_y = MathUtils.clamp(center_y, rectangle.y, rectangle.y + rectangle.height);

        float dx = center_x - closest_x;
        float dy = center_y - closest_y;

        // if the distance from circle to rectangle is less than the circle's radius, there is an intersection
        return (dx * dx + dy * dy) < (radius * radius);
    }

    public boolean size_changed() {
        return !MathUtils.isEqual(radius * 2, SIZE, 0.001f);
    }

    private boolean check_out_of_bounds() {
        return check_out_of_world() || check_below_view();
    }

    private boolean check_below_view() {
        Rectangle camera_rect = CameraController.get_camera_rect((OrthographicCamera) getStage().getCamera());
        return get_y() < (camera_rect.getY() + GameScreen.CHARGE_ZONE_PERCENT_HEIGHT * camera_rect.getHeight());
    }

    public boolean check_out_of_world() {
        return !ChargeHockeyGame.WORLD_RECT.contains(get_x(), get_y());
    }

    public void set_partner(ChargeActor charge) {
        partner = charge;
    }

    public ChargeActor get_partner() {
        return partner;
    }

    public int get_id() {
        return uid;
    }

    public static void set_charge_size(float size) {
        SIZE = size;
    }
}
