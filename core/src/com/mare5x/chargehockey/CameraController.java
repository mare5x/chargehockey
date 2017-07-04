package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;


class CameraController extends GestureDetector.GestureAdapter {
    private enum ZoomLevel {
        MIN(1.6f),
        LEVEL1(0.8f),
        LEVEL2(0.4f),
        LEVEL3(0.2f),
        MAX(0.1f);

        private float amount;

        ZoomLevel(float amount) {
            this.amount = amount;
        }

        float get_amount() {
            return amount;
        }

        /** Returns the next zoom level after the current one, or MIN/MAX if at the end. */
        ZoomLevel get_next() {
            int next_ord = MathUtils.clamp(this.ordinal() + 1, MIN.ordinal(), MAX.ordinal());
            return ZoomLevel.values()[next_ord];
        }

        /** Get the appropriate ZoomLevel based on the given camera zoom. */
        static ZoomLevel get(float zoom) {
            if (zoom < (LEVEL3.amount + MAX.amount) / 2) return MAX;
            if (between(zoom, (LEVEL3.amount + MAX.amount) / 2, (LEVEL2.amount + LEVEL3.amount) / 2)) return LEVEL3;
            if (between(zoom, (LEVEL2.amount + LEVEL3.amount) / 2, (LEVEL1.amount + LEVEL2.amount) / 2)) return LEVEL2;
            if (between(zoom, (LEVEL1.amount + LEVEL2.amount) / 2, (MIN.amount + LEVEL1.amount) / 2)) return LEVEL1;
            else return MIN;
        }

        /** Check whether a <= x < b. */
        static private boolean between(float x, float a, float b) {
            return x >= a && x < b;
        }
    }

    private static final int BORDER = 16;
    private final OrthographicCamera camera;
    private final Stage stage;

    private boolean continuous_rendering = Gdx.graphics.isContinuousRendering();  // continuous rendering as defined by external code

    private final Vector2 tmp_coords = new Vector2();
    private final Vector2 velocity = new Vector2();

    private boolean is_stopping = false;

    private boolean is_moving_to_target = false;
    private final Vector2 target_pos = new Vector2();
    private final Vector2 start_pos = new Vector2();
    private static final float move_to_duration = 0.6f;  // in seconds
    private float time_passed = 0;
    private Interpolation move_to_interpolator = null;

    private boolean double_tap_zoom = false;
    private boolean zoom_started = false;
    private boolean is_zooming = false;
    private float zoom_target_val = -1;
    private float prev_zoom_distance = 0;

    CameraController(OrthographicCamera camera, Stage stage) {
        this.camera = camera;
        this.stage = stage;
    }

    Rectangle get_camera_rect() {
        float x = camera.position.x - camera.viewportWidth / 2f * camera.zoom;
        float y = camera.position.y - camera.viewportHeight / 2f * camera.zoom;
        return new Rectangle(x, y, camera.viewportWidth, camera.viewportHeight);
    }

    boolean point_in_view(float x, float y) {
        return camera.frustum.pointInFrustum(x, y, 0);
    }

    void update(float delta) {
        if (is_zooming) {
            zoom_to(zoom_target_val);
        }

        if (is_moving_to_target) {
            move_to(target_pos.x, target_pos.y, move_to_interpolator, delta);
        } else if (is_stopping) {
            stop_movement(false);
        } else if (velocity.isZero()) {
            restore_rendering();
        } else {
            update_vel(delta);
        }

        camera.update();
    }

    /** Move the camera based on the current velocity. */
    private void update_vel(float delta) {
        float delta_x = velocity.x * delta;
        float delta_y = velocity.y * delta;

        move_by(delta_x, delta_y);
        handle_out_of_bounds();

        velocity.scl(0.9f);  // smooth camera fling movement
        if (Math.abs(velocity.x) < 0.1f) {
            velocity.x = 0;
        }
        if (Math.abs(velocity.y) < 0.1f) {
            velocity.y = 0;
        }
    }

    private void move_by(float delta_x, float delta_y) {
        float translate_x  = -delta_x / camera.viewportWidth * camera.zoom;
        float translate_y = delta_y / camera.viewportHeight * camera.zoom;

        if (translate_x != 0 || translate_y != 0)
            camera.translate(translate_x, translate_y);
    }

    private void handle_out_of_bounds() {
        // camera.position is in the center of the camera

        float xw = camera.position.x - camera.viewportWidth / 2f * camera.zoom;
        float xe = camera.position.x + camera.viewportWidth / 2f * camera.zoom;
        float yn = camera.position.y + camera.viewportHeight / 2f * camera.zoom;
        float ys = camera.position.y - camera.viewportHeight / 2f * camera.zoom;

        float target_x = camera.position.x, target_y = camera.position.y;
        if (xw < -BORDER * camera.zoom) {
            target_x += -(BORDER / 2) - xw;
            velocity.x = 0;
        }
        else if (xe > ChargeHockeyGame.WORLD_WIDTH + BORDER * camera.zoom) {
            target_x += ChargeHockeyGame.WORLD_WIDTH + ((BORDER / 2) * camera.zoom) - xe;
            velocity.x = 0;
        }
        if (ys < -BORDER * camera.zoom) {
            target_y += -(BORDER / 2) - ys;
            velocity.y = 0;
        }
        else if (yn > ChargeHockeyGame.WORLD_HEIGHT + BORDER * camera.zoom) {
            target_y += ChargeHockeyGame.WORLD_HEIGHT + ((BORDER / 2) * camera.zoom) - yn;
            velocity.y = 0;
        }
        if (target_x != camera.position.x || target_y != camera.position.y)
            move_to(target_x, target_y);
    }

    private void move_to(float x, float y) {
        move_to(x, y, null, 0);
    }

    private void move_to(float x, float y, Interpolation interpolator) {
        move_to(x, y, interpolator, 0);
    }

    private void move_to(float x, float y, Interpolation interpolator, float delta) {
        if (camera.position.epsilonEquals(x, y, 0, 0.01f)) {
            is_moving_to_target = false;
            velocity.setZero();
            restore_rendering();
            return;
        }
        Gdx.graphics.setContinuousRendering(true);

        is_moving_to_target = true;
        move_to_interpolator = interpolator;

        if (target_pos.x != x || target_pos.y != y) {
            start_pos.set(camera.position.x, camera.position.y);
            target_pos.set(x, y);
            time_passed = 0;
        }

        if (interpolator == null) {
            camera.position.x += (x - camera.position.x) * 0.125f;
            camera.position.y += (y - camera.position.y) * 0.125f;
        } else {
            time_passed += delta;
            time_passed = Math.min(time_passed, move_to_duration);
            float alpha = interpolator.apply(time_passed / move_to_duration);

            tmp_coords.set(target_pos).sub(start_pos).scl(alpha);

            camera.position.set(start_pos, 0).add(tmp_coords.x, tmp_coords.y, 0);
        }
    }

    void set_rendering(boolean value) {
        continuous_rendering = value;
    }

    private void restore_rendering() {
        boolean render = continuous_rendering || is_moving();
        if (Gdx.graphics.isContinuousRendering() != render)
            Gdx.graphics.setContinuousRendering(render);
    }

    private void stop_movement(boolean force_stop) {
        is_moving_to_target = false;

        if (force_stop || velocity.isZero(0.1f)) {
            is_stopping = false;
            is_zooming = false;
            velocity.setZero();
        } else {
            is_stopping = true;
            velocity.scl(0.6f);
            move_by(velocity.x * Gdx.graphics.getDeltaTime(), velocity.y * Gdx.graphics.getDeltaTime());
        }
    }

    void set_double_tap_zoom(boolean val) {
        double_tap_zoom = val;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        stage.screenToStageCoordinates(tmp_coords.set(x, y));

        stop_movement(false);

        // double tap to zoom in/out
        if (double_tap_zoom && count == 2) {
            zoom_to(get_next_zoom_level());
            move_to(tmp_coords.x, tmp_coords.y);  // x and y must be in stage coordinates!
        }

        return true;
    }

    private float get_next_zoom_level() {
        ZoomLevel zoom_level = ZoomLevel.get(camera.zoom);
        if (zoom_level == ZoomLevel.MAX) {
            // if not completely at max zoom, zoom to max
            if (MathUtils.isEqual(camera.zoom, ZoomLevel.MAX.get_amount(), 0.05f)) {
                return ZoomLevel.LEVEL2.get_amount();
            } else {
                return ZoomLevel.MAX.get_amount();
            }
        } else if (zoom_level == ZoomLevel.MIN) {
            return ZoomLevel.LEVEL1.get_amount();
        } else {
            return zoom_level.get_next().get_amount();
        }
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        velocity.setZero();
        is_moving_to_target = false;

        restore_rendering();

        move_by(deltaX, deltaY);

        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        handle_out_of_bounds();

        return true;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        Gdx.graphics.setContinuousRendering(true);
        is_moving_to_target = false;

        velocity.set(velocityX, velocityY);

        return true;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        float initial_dst = initialPointer1.dst(initialPointer2);
        float dst = pointer1.dst(pointer2);

        float new_zoom_distance = dst - initial_dst;
        if (Math.abs(new_zoom_distance) < 3f)  // ignore very small zoom
            return true;

        float amount = (new_zoom_distance - prev_zoom_distance) / camera.viewportHeight;
        prev_zoom_distance = new_zoom_distance;

        zoom_to(camera.zoom - (amount * 0.285f));

        // move to center of pinch at the start of pinching
        if (!zoom_started) {
            float center_x = (initialPointer1.x + initialPointer2.x) / 2;
            float center_y = (initialPointer1.y + initialPointer2.y) / 2;

            stage.screenToStageCoordinates(tmp_coords.set(center_x, center_y));

            move_to(tmp_coords.x, tmp_coords.y, Interpolation.pow3Out);
        }

        zoom_started = true;

        return true;
    }

    @Override
    public void pinchStop() {
        zoom_started = false;
        prev_zoom_distance = 0;
        restore_rendering();
    }

    private void zoom_to(float target_val) {
        if (MathUtils.isEqual(target_val, camera.zoom, 0.01f)) {
            is_zooming = false;
            restore_rendering();
            return;
        }
        Gdx.graphics.setContinuousRendering(true);

        zoom_target_val = target_val;
        is_zooming = true;

        camera.zoom += (zoom_target_val - camera.zoom) * 0.1f;

        if (camera.zoom < ZoomLevel.MAX.get_amount())
            camera.zoom = ZoomLevel.MAX.get_amount();
        else if (camera.zoom > ZoomLevel.MIN.get_amount())
            camera.zoom = ZoomLevel.MIN.get_amount();
    }

    boolean is_moving() {
        return is_moving_to_target || is_stopping || is_zooming || !velocity.isZero();
    }

    static int get_grid_line_spacing(float zoom) {
        if (zoom <= 0.6f) return 1;
        else if (zoom <= 1f) return 2;
        else if (zoom <= 1.4f) return 4;
        else return 8;
    }
}