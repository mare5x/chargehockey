package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.Gdx;
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
import com.mare5x.chargehockey.level.Grid;
import com.mare5x.chargehockey.settings.GameDefaults;

import static com.mare5x.chargehockey.settings.GameDefaults.PHYSICS_EPSILON;


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

    /** Helper for updating the charge and camera when dragging a charge close to the edge of the
     * screen. Usage: (1) enter_drag_area() (2) update() (3) exit_drag_area()
     */
    public static class ChargeDragAreaHelper {
        private static final float DRAG_SPEED = 15.0f;
        private static Vector2 tmp_vec = new Vector2();
        private ChargeActor charge;
        private final SymmetryToolActor symmetry_tool;

        public ChargeDragAreaHelper(SymmetryToolActor symmetry_tool) {
            this.symmetry_tool = symmetry_tool;
        }

        public void update(float delta) {
            if (charge != null && charge.getStage() != null)
                in_drag_area(delta);
        }

        void in_drag_area(float delta) {
            // The camera follows the dragged charge off the screen.
            OrthographicCamera camera = (OrthographicCamera) charge.getStage().getCamera();
            tmp_vec.set(charge.get_x(), charge.get_y()).sub(camera.position.x, camera.position.y);
            Vector2 delta_pos = tmp_vec.nor().scl(DRAG_SPEED).scl(delta).scl(camera.zoom);
            camera.translate(delta_pos);
            charge.moveBy(delta_pos.x, delta_pos.y);

            // move the partner symmetrically
            ChargeActor partner = charge.get_partner();
            if (partner != null && symmetry_tool.is_enabled()) {
                symmetry_tool.get_symmetrical_pos(tmp_vec.set(charge.get_x(), charge.get_y()));
                partner.set_position(tmp_vec.x, tmp_vec.y);
            }
        }

        public void enter_drag_area(ChargeActor charge) {
            this.charge = charge;
            if (!Gdx.graphics.isContinuousRendering())
                Gdx.graphics.setContinuousRendering(true);
        }

        public void exit_drag_area() {
            this.charge = null;
            if (Gdx.graphics.isContinuousRendering())
                Gdx.graphics.setContinuousRendering(false);
        }
    }

    // TODO fix this mess
    public abstract static class DragCallback {
        // Out of bounds means out of the Grid OR in the charge zone
        public abstract void out_of_bounds(ChargeActor charge, boolean dragged);  // dragged tells whether charge was dragged physically or by symmetry
        public abstract void drag(ChargeActor charge);
        public void drag_started(ChargeActor charge) {}
        public void enter_charge_zone(ChargeActor charge) {}
        public void exit_charge_zone(ChargeActor charge) {}
        public void enter_drag_area(ChargeActor charge) {}
        public void exit_drag_area(ChargeActor charge) {}
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
    private static final float WEIGHT = 1;  // kg
    private static final float ABS_CHARGE = 1;  // Coulombs

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
                private final float max_zoom = CameraController.ZoomLevel.MAX_ZOOM;
                private final float min_zoom = CameraController.ZoomLevel.MIN_ZOOM;

                private boolean in_charge_zone = false;
                private boolean in_drag_area = false;

                // used to ignore edge dragging if starting in the charge zone
                // true as long as the charge is in the charge zone
                private boolean started_in_charge_zone = false;

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

                    // Charge zone and drag area checking when draggin is determined based on the
                    // finger's (pointer's) position, not the center of the charge
                    if (check_in_charge_zone(event.getStageX(), event.getStageY())) {
                        if (!in_charge_zone) {
                            in_charge_zone = true;
                            drag_callback.enter_charge_zone(ChargeActor.this);
                        }
                    } else {
                        if (in_charge_zone) {
                            started_in_charge_zone = false;
                            in_charge_zone = false;
                            drag_callback.exit_charge_zone(ChargeActor.this);
                        }
                    }

                    // ignore if the dragging is still in the initial charge zone
                    if (!started_in_charge_zone) {
                        if (check_in_drag_area(event.getStageX(), event.getStageY())) {
                            if (!in_drag_area) {
                                in_drag_area = true;
                                drag_callback.enter_drag_area(ChargeActor.this);
                            }
                        } else {
                            if (in_drag_area) {
                                in_drag_area = false;
                                drag_callback.exit_drag_area(ChargeActor.this);
                            }
                        }
                    }
                }

                @Override
                public void dragStart(InputEvent event, float x, float y, int pointer) {
                    started_in_charge_zone = check_in_charge_zone(event.getStageX(), event.getStageY());

                    drag_callback.drag_started(ChargeActor.this);
                    if (partner != null)
                        drag_callback.drag_started(partner);
                }

                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer) {
                    if (check_in_charge_zone(event.getStageX(), event.getStageY())) {
                        in_charge_zone = false;
                        drag_callback.exit_charge_zone(ChargeActor.this);
                    }
                    if (check_in_drag_area(event.getStageX(), event.getStageY())) {
                        in_drag_area = false;
                        drag_callback.exit_drag_area(ChargeActor.this);
                    }

                    if (check_out_of_bounds(event.getStageX(), event.getStageY()))
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
    public Vector2 get_vec_from(Vector2 puck_pos) {
        return puck_pos.sub(get_x(), get_y()).scl(get_direction());
    }

    public ChargeState get_state() {
        return new ChargeState(charge_type, get_x(), get_y());
    }

    /** Circular collision detection with a rectangle. If the circle and rectangle intersect,
     *  the displacement vector is returned in 'intersection'.
     *  @param norm the normal vector of the closest collision point. */
    public boolean intersects(Rectangle rectangle, Vector2 intersection, Vector2 norm) {
        float center_x = get_x();
        float center_y = get_y();

        // find the closest rectangle point to the circle (charge)
        float closest_x = MathUtils.clamp(center_x, rectangle.x, rectangle.x + rectangle.width);
        float closest_y = MathUtils.clamp(center_y, rectangle.y, rectangle.y + rectangle.height);

        float dx = intersection.x = closest_x - center_x;
        float dy = intersection.y = closest_y - center_y;
        // the intersection vector points from the edge of the circle to the closest collision point
        // towards the center, which in this case also acts as the normal vector
        tmp_vec.set(intersection).nor().scl(radius);
        intersection.sub(tmp_vec);

        norm.set(intersection).nor();

        // if the distance from circle to rectangle is less than the circle's radius, there is an intersection
        return (radius*radius) - (dx*dx + dy*dy) > PHYSICS_EPSILON;  // epsilon
    }

    public boolean size_changed() {
        return !MathUtils.isEqual(radius * 2, SIZE, PHYSICS_EPSILON);
    }

    private Vector2 get_screen_coordinates() {
        return get_screen_coordinates(get_x(), get_y());
    }

    /** Returns the screen coordinates in tmp_vec. */
    private Vector2 get_screen_coordinates(float world_x, float world_y) {
        return getStage().stageToScreenCoordinates(tmp_vec.set(world_x, world_y));
    }

    /** Out of bounds means out of the Grid OR in the charge zone.
     * Careful: out of world checking uses the charge's center and in charge zone checking uses
     * the finger/pointer position!
     */
    private boolean check_out_of_bounds(float world_x, float world_y) {
        return check_out_of_world() || check_in_charge_zone(world_x, world_y);
    }

    private boolean check_in_charge_zone(float world_x, float world_y) {
        return GameDefaults.CHARGE_ZONE_RECT.contains(get_screen_coordinates(world_x, world_y));
    }

    private boolean check_in_drag_area(float world_x, float world_y) {
        return !GameDefaults.CHARGE_DRAG_RECT.contains(get_screen_coordinates(world_x, world_y));
    }

    public boolean check_out_of_world() {
        return !Grid.WORLD_RECT.contains(get_x(), get_y());
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
