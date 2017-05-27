package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


class BaseGestureAdapter extends GestureDetector.GestureAdapter {
    private static final int BORDER = 16;
    private final OrthographicCamera camera;

    private final Vector2 velocity = new Vector2();
    private static final float dt = 0.01f;

    private boolean continuous_rendering = Gdx.graphics.isContinuousRendering();

    private boolean is_stopping = false;

    private boolean is_moving_to_target = false;
    private final Vector2 target_pos = new Vector2();

    private boolean is_zooming = false;
    private float zoom_target_val = -1;
//    private final Vector2 zoom_target_pos = new Vector2();  TODO zoom to target position
    private float prev_zoom_distance = 0;

    BaseGestureAdapter(OrthographicCamera camera) {
        this.camera = camera;
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
            move_to(target_pos.x, target_pos.y);
        } else if (is_stopping) {
            stop_movement(false);
        } else if (velocity.isZero()) {
            if (Gdx.graphics.isContinuousRendering() != continuous_rendering) {
                Gdx.graphics.setContinuousRendering(continuous_rendering);
            }
        } else {
            move_vel();

            velocity.scl(0.95f);  // smooth camera fling movement
            if (Math.abs(velocity.x) < 0.1f) {
                velocity.x = 0;
            }
            if (Math.abs(velocity.y) < 0.1f) {
                velocity.y = 0;
            }
        }

        camera.update();
    }

    // Move the camera based on the current velocity.
    private void move_vel() {
        float delta_x = velocity.x * dt;
        float delta_y = velocity.y * dt;

        move_by(delta_x, delta_y);
        handle_out_of_bounds();
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
        if (camera.position.epsilonEquals(x, y, 0, 0.01f)) {
            is_moving_to_target = false;
            velocity.setZero();
            return;
        }
        is_moving_to_target = true;

        target_pos.set(x, y);

        // lerping
        camera.position.x += (x - camera.position.x) * 0.125f;
        camera.position.y += (y - camera.position.y) * 0.125f;
    }

    void set_rendering(boolean value) {
        continuous_rendering = value;
    }

    void stop_movement(boolean force_stop) {
        is_moving_to_target = false;

        if (force_stop || velocity.isZero(0.1f)) {
            is_stopping = false;
            is_zooming = false;
            velocity.setZero();
        } else {
            is_stopping = true;
            velocity.scl(0.6f);
            move_by(velocity.x * dt, velocity.y * dt);
        }
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        stop_movement(false);
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (!continuous_rendering)
            Gdx.graphics.setContinuousRendering(false);
        velocity.setZero();
        is_moving_to_target = false;

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
    public boolean zoom(float initialDistance, float distance) {
        Gdx.graphics.setContinuousRendering(true);

        float new_zoom_distance = distance - initialDistance;
        if (Math.abs(new_zoom_distance) < 0.1f)
            return true;

        float amount = (new_zoom_distance - prev_zoom_distance) / camera.viewportHeight;
        prev_zoom_distance = new_zoom_distance;

        zoom_to(camera.zoom - (amount * 0.285f));

        return true;
    }

    private void zoom_to(float target_val) {
        if (target_val == camera.zoom) {
            is_zooming = false;
            return;
        }

        zoom_target_val = target_val;
        is_zooming = true;

        camera.zoom += (zoom_target_val - camera.zoom) * 0.125f;

        if (camera.zoom < 0.1f)
            camera.zoom = 0.1f;
        else if (camera.zoom > 1.7f)
            camera.zoom = 1.7f;
    }

    @Override
    public void pinchStop() {
        prev_zoom_distance = 0;
        Gdx.graphics.setContinuousRendering(continuous_rendering);
    }

    boolean is_moving() {
        return is_moving_to_target || is_stopping || is_zooming;
    }
}