package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;

public class BaseGestureAdapter extends GestureDetector.GestureAdapter {
    private static final int BORDER = 16;
    private float prev_zoom_distance = 0;
    private final OrthographicCamera camera;

    public BaseGestureAdapter(OrthographicCamera camera) {
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

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX / camera.viewportWidth * camera.zoom * 2, deltaY / camera.viewportHeight * camera.zoom * 2);

        // camera.position is in the center of the camera

        float xw = camera.position.x - camera.viewportWidth / 2f * camera.zoom;
        float xe = camera.position.x + camera.viewportWidth / 2f * camera.zoom;
        float yn = camera.position.y + camera.viewportHeight / 2f * camera.zoom;
        float ys = camera.position.y - camera.viewportHeight / 2f * camera.zoom;

        if (xw < -BORDER * camera.zoom)
            camera.translate(-BORDER - xw, 0);
        else if (xe > ChargeHockeyGame.WORLD_WIDTH + BORDER * camera.zoom)
            camera.translate(ChargeHockeyGame.WORLD_WIDTH + (BORDER * camera.zoom) - xe, 0);
        if (ys < -BORDER * camera.zoom)
            camera.translate(0, -BORDER - ys);
        else if (yn > ChargeHockeyGame.WORLD_HEIGHT + BORDER * camera.zoom)
            camera.translate(0, ChargeHockeyGame.WORLD_HEIGHT + (BORDER * camera.zoom) - yn);

        return true;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float new_zoom_distance = distance - initialDistance;
        float amount = (new_zoom_distance - prev_zoom_distance) / camera.viewportHeight;
        prev_zoom_distance = new_zoom_distance;

        camera.zoom -= amount * 0.2f;

        if (camera.zoom < 0.1f)
            camera.zoom = 0.1f;
        else if (camera.zoom > 1.8f)
            camera.zoom = 1.8f;

        return true;
    }

    @Override
    public void pinchStop() {
        prev_zoom_distance = 0;
    }
}