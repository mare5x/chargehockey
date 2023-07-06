package com.mare5x.chargehockey.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;
import com.mare5x.chargehockey.level.Grid;

import static com.mare5x.chargehockey.settings.GameDefaults.SECONDS_TO_NANOS;


// TODO rewrite the whole class using InputAdapter
public class CameraController {
    /** Levels are ordered in descending zoom amount: MIN_LEVEL (0) has MIN_ZOOM (1.55), etc ... */
    public static class ZoomLevel {
        private static final int N_ZOOM_LEVELS = 5;
        private static final float ZOOM_LEVELS[] = new float[N_ZOOM_LEVELS];
        public static final int MAX_LEVEL = N_ZOOM_LEVELS - 1;
        public static final int MIN_LEVEL = 0;
        public static final float MAX_ZOOM;  // 0.05
        public static final float MIN_ZOOM;  // 1.55 
        
        static {
            // cache values
            for (int i = 0; i < N_ZOOM_LEVELS; ++i)
                ZOOM_LEVELS[i] = calc_zoom(MAX_LEVEL - i);
            MAX_ZOOM = ZOOM_LEVELS[MAX_LEVEL];
            MIN_ZOOM = ZOOM_LEVELS[MIN_LEVEL];
        }

        private static float calc_zoom(int opposite_level) {
            // (2^x)/10-0.05 -> look at a graph plot to see
            // but 'flipped'
            return (float) (Math.pow(2.0, opposite_level) / 10.0 - 0.05);
        }

        private static float get_zoom(int level) {
            level = MathUtils.clamp(level, MIN_LEVEL, MAX_LEVEL);
            return ZOOM_LEVELS[level];
        }

        public static float get_zoom(float zoom) {
            return get_zoom(get_level(zoom));
        }
        
        public static int get_level(float zoom) {
            // solve for x in the calc_zoom equation and round it
            return MAX_LEVEL - MathUtils.round(MathUtils.log2(10*zoom + 0.5f));
        }
        
        /** The next level has more zoom (but lower value). */
        private static int get_next_level(int level) {
            return MathUtils.clamp(level + 1, MIN_LEVEL, MAX_LEVEL);
        }

        public static float get_next_zoom(int level) {
            return get_zoom(get_next_level(level));
        }
    }

    private final GestureDetector detector;
    private final CameraControllerImpl controller;

    private boolean continuous_rendering = Gdx.graphics.isContinuousRendering();  // continuous rendering as defined by external code
    private boolean double_tap_zoom = false;

    private boolean long_press_started = false;

    private static Rectangle tmp_rect = new Rectangle();

    public CameraController(OrthographicCamera camera, Stage stage) {
        controller = new CameraControllerImpl(camera, stage);
        detector = new GestureDetector(controller) {
            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                if (!Gdx.graphics.isContinuousRendering())
                    Gdx.graphics.setContinuousRendering(true);
                return super.touchDown(x, y, pointer, button);
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                controller.handle_inactivity();
                if (long_press_started && pointer == 0)
                    on_long_press_end();
                return super.touchUp(x, y, pointer, button);
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                controller.zoom_to(controller.camera.zoom + amountY * 0.1f, Interpolation.pow3Out);
                return true;
            }
        };
    }

    public void update(float delta) {
        if (long_press_started) {
//            Vector2 touch_coordinates = controller.get_stage_coordinates(Gdx.input.getX(), Gdx.input.getY());
            on_long_press_held(Gdx.input.getX(), Gdx.input.getY());
        }
        delta = delta > 1 ? 0 : delta;  // after a period of inactivity, make the camera less jerky
        controller.update(delta);
    }

    public void resize(float width, float height) {
        controller.resize(width, height);
    }

    public void set_rendering(boolean value) {
        continuous_rendering = value;
    }

    void set_double_tap_zoom(boolean val) {
        double_tap_zoom = val;
    }

    public GestureDetector get_gesture_detector() {
        return detector;
    }

    private void restore_rendering() {
        boolean render = continuous_rendering || controller.is_moving();
        if (Gdx.graphics.isContinuousRendering() != render)
            Gdx.graphics.setContinuousRendering(render);
    }

    protected boolean point_in_view(float x, float y) {
        return controller.camera.frustum.pointInFrustum(x, y, 0);
    }

    protected boolean is_moving() {
        return controller.is_moving();
    }

    protected void on_long_press_start(float screen_x, float screen_y) {
        long_press_started = true;
    }

    protected void on_long_press_held(float screen_x, float screen_y) {

    }

    protected void on_long_press_end() {
        long_press_started = false;
    }

    protected boolean on_tap(float x, float y, int count, int button) {
        controller.stop_movement(false);

        // double tap to zoom in/out
        if (double_tap_zoom && count == 2) {
            controller.zoom_to(controller.get_next_zoom_level(), Interpolation.pow2Out);
            controller.move_to(x, y, Interpolation.pow2Out);  // x and y must be in stage coordinates!
        }

        return true;
    }

    protected void on_zoom_change(float zoom) { }

    /* Return the Rectangle of the current camera view in world coordinates. */
    public static Rectangle get_camera_rect(OrthographicCamera camera) {
        // can also use camera.frustum.planepoints ...
        float w = camera.viewportWidth * camera.zoom;
        float h = camera.viewportHeight * camera.zoom;
        float x = camera.position.x - w / 2;
        float y = camera.position.y - h / 2;
        return tmp_rect.set(x, y, w, h);
    }

    private class CameraControllerImpl extends GestureDetector.GestureAdapter {
        private static final float dt = 1 / 60f;  // fixed time step
        private float dt_accumulator = 0;

        private static final float BORDER = 24;  // world units (for out_of_bounds, zoom adjusted)

        private static final float EPSILON = 0.01f;

        private final OrthographicCamera camera;
        private final Stage stage;

        private final Vector2 tmp_coords = new Vector2();
        private final Vector2 velocity = new Vector2();

        private boolean is_stopping = false;
        private boolean is_moving_to_target = false;
        private boolean is_zooming = false;

        private final Vector2 target_pos = new Vector2();
        private final Vector2 start_pos = new Vector2();

        private Interpolation move_to_interpolator = null;
        private static final float move_to_duration = 0.6f;  // in seconds
        private float move_to_time = 0;

        private Interpolation zoom_to_interpolator = null;
        private static final float zoom_to_duration = 0.6f;  // in seconds
        private float zoom_to_time = 0;

        private float px_to_zoom;  // conversion factor
        private float max_pinch_distance = 0;  // in pixels
        private Vector2 px_to_world_unit = new Vector2();  // conversion factor

        private Vector2 pinch_target_center = new Vector2();
        private boolean is_pinching = false;
        private float pinch_start_dst = 0;
        private float pinch_start_zoom = 0;
        private long pinch_stop_time = 0;
        private float zoom_target_val = -1;
        private float zoom_to_start_value = 0;

        private long last_action_time = 0;
        private static final float inactivity_duration = 0.6f;  // in seconds

        CameraControllerImpl(OrthographicCamera camera, Stage stage) {
            this.camera = camera;
            this.stage = stage;

            resize(stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        }

        void update(float delta) {
            dt_accumulator += delta;

            while (dt_accumulator >= dt) {
                if (is_zooming)
                    zoom_to(zoom_target_val, zoom_to_interpolator, dt);
                if (is_moving_to_target)
                    move_to(target_pos.x, target_pos.y, move_to_interpolator, dt);
                if (is_stopping)
                    stop_movement(false);
                if (!velocity.isZero(EPSILON))
                    update_vel(dt);

                handle_inactivity();

                camera.update();

                dt_accumulator -= dt;
            }
        }

        void resize(float screen_width, float screen_height) {
            max_pinch_distance = (float) Math.sqrt(screen_width * screen_width + screen_height * screen_height);  // length of diagonal
            float zoom_range = ZoomLevel.MIN_ZOOM - ZoomLevel.MAX_ZOOM;
            px_to_zoom = zoom_range / max_pinch_distance;  // 1 px = px_to_zoom camera.zoom units

            calc_px_to_world_unit(screen_width, screen_height);
        }

        private void calc_px_to_world_unit() {
            calc_px_to_world_unit(stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        }

        private void calc_px_to_world_unit(float screen_width, float screen_height) {
            px_to_world_unit.x = (camera.viewportWidth * camera.zoom * 0.5f) / screen_width;
            px_to_world_unit.y = (camera.viewportHeight * camera.zoom * 0.5f) / screen_height;
        }

        private Vector2 px_to_world_units(Vector2 px) {
//            stage.screenToStageCoordinates(px);
            return px.scl(px_to_world_unit);
        }

        /* Check if either coordinate is NaN */
        private boolean is_nan(Vector2 vec) {
            return vec.x != vec.x || vec.y != vec.y;
        }

        /** Move the camera based on the current velocity. */
        private void update_vel(float delta) {
            float delta_x = velocity.x * delta;
            float delta_y = velocity.y * delta;

            move_by(tmp_coords.set(delta_x, delta_y));

            if (!is_moving_to_target || is_nan(target_pos) && !is_zooming)
                handle_out_of_bounds();

            velocity.scl(0.9f);  // smooth camera fling movement
            if (Math.abs(velocity.x) < 0.1f)
                velocity.x = 0;
            if (Math.abs(velocity.y) < 0.1f)
                velocity.y = 0;
        }

        /** Instantly move the camera by 'amount' in world units. */
        private void move_by(Vector2 amount) {
            camera.translate(amount.scl(-1, 1));
        }

        private void handle_out_of_bounds() {
            // camera.position is in the center of the camera
            Rectangle view = get_camera_rect(camera);
            float xw = view.x;
            float xe = view.x + view.width;
            float yn = view.y + view.height;
            float ys = view.y;

            float border = BORDER * camera.zoom;

            float dx = 0;
            float dy = 0;

            if (xw < -border)
                dx += -(border / 2) - xw;
            else if (xe > Grid.WORLD_WIDTH + border)
                dx += Grid.WORLD_WIDTH + (border / 2) - xe;
            if (ys < -border)
                dy += -(border / 2) - ys;
            else if (yn > Grid.WORLD_HEIGHT + border)
                dy += Grid.WORLD_HEIGHT + (border / 2) - yn;

            if (xw + dx < 0 && xe + dx > Grid.WORLD_WIDTH)
                dx = Grid.WORLD_WIDTH / 2f - camera.position.x;
            if (ys + dy < 0 && yn + dy > Grid.WORLD_HEIGHT)
                dy = Grid.WORLD_HEIGHT / 2f - camera.position.y;

            if (dx != 0 || dy != 0) {
                if (dx != 0) velocity.x = 0;
                if (dy != 0) velocity.y = 0;
                move_to(dx == 0 ? Float.NaN : camera.position.x + dx,
                        dy == 0 ? Float.NaN : camera.position.y + dy,
                        Interpolation.pow3Out);
            }
        }

        private void move_to(float x, float y) {
            move_to(x, y, null, 0);
        }

        private void move_to(float x, float y, Interpolation interpolator) {
            move_to(x, y, interpolator, 0);
        }

        /* If x or y is equal to NaN, no movement will be performed in that direction. */
        private void move_to(float x, float y, Interpolation interpolator, float delta) {
            last_action_time = TimeUtils.nanoTime();

            if (camera.position.epsilonEquals(x, y, 0, EPSILON)) {
                is_moving_to_target = false;
                return;
            }
            if (!Gdx.graphics.isContinuousRendering())
                Gdx.graphics.setContinuousRendering(true);

            // Use Float.compare to test equality because of NaN
            if (!is_moving_to_target || Float.compare(target_pos.x, x) != 0 || Float.compare(target_pos.y, y) != 0 || move_to_interpolator != interpolator) {
                is_moving_to_target = true;
                start_pos.set(camera.position.x, camera.position.y);
                target_pos.set(x, y);
                move_to_time = 0;
                move_to_interpolator = interpolator;
            }

            if (interpolator == null) {
                // fast !isNaN check
                if (x == x) camera.position.x += (x - camera.position.x) * 0.125f;
                if (y == y) camera.position.y += (y - camera.position.y) * 0.125f;
            } else {
                move_to_time += delta;
                move_to_time = Math.min(move_to_time, move_to_duration);
                float alpha = interpolator.apply(move_to_time / move_to_duration);

                tmp_coords.set(target_pos).sub(start_pos).scl(alpha);
                // !isNaN check
                if (tmp_coords.x == tmp_coords.x) camera.position.x = start_pos.x + tmp_coords.x;
                if (tmp_coords.y == tmp_coords.y) camera.position.y = start_pos.y + tmp_coords.y;
            }
        }

        private void stop_movement(boolean force_stop) {
            is_moving_to_target = false;

            if (force_stop || velocity.isZero(EPSILON)) {
                is_stopping = false;
                is_zooming = false;
                velocity.setZero();
            } else {
                is_stopping = true;
                velocity.scl(0.6f);
                move_by(velocity.scl(dt));
            }
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            last_action_time = TimeUtils.nanoTime();

            stage.screenToStageCoordinates(tmp_coords.set(x, y));

            return on_tap(tmp_coords.x, tmp_coords.y, count, button);
        }

        @Override
        public boolean longPress(float x, float y) {
            last_action_time = TimeUtils.nanoTime();

            on_long_press_start(x, y);
            return true;
        }

        private float get_next_zoom_level() {
            int zoom_level = ZoomLevel.get_level(camera.zoom);
            if (zoom_level == ZoomLevel.MAX_LEVEL) {
                // if not completely at the zoom level, zoom to it
                if (!MathUtils.isEqual(ZoomLevel.MAX_ZOOM, camera.zoom, EPSILON))
                    return ZoomLevel.MAX_ZOOM;
                return ZoomLevel.get_zoom(ZoomLevel.N_ZOOM_LEVELS / 2);
            }
            return ZoomLevel.get_next_zoom(zoom_level);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            last_action_time = TimeUtils.nanoTime();

            velocity.setZero();
            is_moving_to_target = false;

            move_by(px_to_world_units(tmp_coords.set(deltaX, deltaY)));

            return true;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            long time = TimeUtils.nanoTime();
            // ignore flings just after pinching
            if (time - pinch_stop_time < 2e7)  // 0.02 seconds
                return true;

            last_action_time = time;

            if (!Gdx.graphics.isContinuousRendering())
                Gdx.graphics.setContinuousRendering(true);
            is_moving_to_target = false;

            px_to_world_units(velocity.set(velocityX, velocityY));

            return true;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            last_action_time = TimeUtils.nanoTime();

            if (!Gdx.graphics.isContinuousRendering())
                Gdx.graphics.setContinuousRendering(true);

            if (!is_pinching) {
                is_pinching = true;
                pinch_start_zoom = camera.zoom;
                pinch_start_dst = initialPointer1.dst(initialPointer2);
                pinch_target_center.set(initialPointer1).add(initialPointer2).scl(0.5f);
                stage.screenToStageCoordinates(pinch_target_center);
            } else {
                float dst = pointer1.dst(pointer2);
                float delta_pinch_dst = dst - pinch_start_dst;

                // use the px_to_zoom conversion factor to determine the right zoom from the pixel delta value
                float target_zoom = MathUtils.clamp(pinch_start_zoom - delta_pinch_dst * px_to_zoom,
                        ZoomLevel.MAX_ZOOM, ZoomLevel.MIN_ZOOM);

                zoom_to(target_zoom, Interpolation.pow3Out);

                // when pinching, move the camera towards the center of where the pinch started (in
                // world coordinates), based on the distance between the fingers that are pinching
                if (!MathUtils.isEqual(camera.zoom, target_zoom, EPSILON)) {
                    Vector2 cur_center = tmp_coords.set(camera.position.x, camera.position.y);
                    Vector2 delta_pos = pinch_target_center.cpy().sub(cur_center).scl(Math.abs(delta_pinch_dst / max_pinch_distance));
                    cur_center.add(delta_pos);
                    move_to(cur_center.x, cur_center.y);
//                    move_by(delta_pos.scl(-1, 1));
                }
            }
            return true;
        }

        @Override
        public void pinchStop() {
            pinch_stop_time = TimeUtils.nanoTime();
            is_pinching = false;
        }

        private void zoom_to(float target_val) {
            zoom_to(target_val, null, 0);
        }

        private void zoom_to(float target_val, Interpolation interpolator) {
            zoom_to(target_val, interpolator, 0);
        }

        private void zoom_to(float target_val, Interpolation interpolator, float delta) {
            last_action_time = TimeUtils.nanoTime();

            if (MathUtils.isEqual(target_val, camera.zoom, EPSILON)) {
                is_zooming = false;
                return;
            }
            if (!Gdx.graphics.isContinuousRendering())
                Gdx.graphics.setContinuousRendering(true);

            if (!is_zooming || zoom_target_val != target_val || zoom_to_interpolator != interpolator) {
                is_zooming = true;
                zoom_target_val = target_val;
                zoom_to_start_value = camera.zoom;
                zoom_to_time = 0;
                zoom_to_interpolator = interpolator;
            }

            if (interpolator == null)
                camera.zoom += (zoom_target_val - camera.zoom) * 0.1f;
            else {
                zoom_to_time += delta;
                zoom_to_time = Math.min(zoom_to_time, zoom_to_duration);
                float alpha = interpolator.apply(zoom_to_time / zoom_to_duration);
                camera.zoom = zoom_to_start_value + (target_val - zoom_to_start_value) * alpha;
            }

            calc_px_to_world_unit();
            on_zoom_change(camera.zoom);
        }

        private void handle_inactivity() {
            long time = TimeUtils.nanoTime();
            if (!is_moving() && time - last_action_time >= inactivity_duration * SECONDS_TO_NANOS) {
                last_action_time = time;
                velocity.setZero();
                restore_rendering();
                handle_out_of_bounds();
            }
        }

        boolean is_moving() {
            return is_moving_to_target || is_stopping || is_zooming || !velocity.isZero(EPSILON) || Gdx.input.isTouched();
        }
    }
}
