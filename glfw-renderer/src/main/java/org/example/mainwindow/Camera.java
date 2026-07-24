package org.example.mainwindow;

import jsettlers.graphics.map.draw.DrawConstants;


/**
 * this class handles all camera movement.
 */
public class Camera {

    public final float mapWidth; // Map width in screen pixels (tiles * DrawConstants.DISTANCE_X)
    public final float mapHeight;  // Map height in screen pixels (tiles * DrawConstants.DISTANCE_Y)
    public final float incline;  // Parallelogram side incline: DISTANCE_X / 2 / DISTANCE_Y
    public float offsetX;
    public float offsetY;
    public float pendingPanX;
    public float pendingPanY;
    public float previousCursorX;
    public float previousCursorY;
    public boolean rmbPressed;
    public boolean mmbPressed;
    public boolean keyUpPressed;
    public boolean keyDownPressed;
    public boolean keyLeftPressed;
    public boolean keyRightPressed;


    /**
     * @param tilesWidth  map width in tiles
     * @param tilesHeight map height in tiles
     */
    public Camera(int tilesWidth, int tilesHeight) {

        this.mapWidth = tilesWidth * DrawConstants.DISTANCE_X;
        this.mapHeight = tilesHeight * DrawConstants.DISTANCE_Y;
        this.incline = DrawConstants.DISTANCE_X / 2.00f / DrawConstants.DISTANCE_Y;
        this.offsetX = 0;
        this.offsetY = 0;
        this.previousCursorX = 0;
        this.previousCursorY = 0;
        this.pendingPanX = 0;
        this.pendingPanY = 0;
        this.rmbPressed = false;
        this.mmbPressed = false;
        this.keyUpPressed = false;
        this.keyDownPressed = false;
        this.keyLeftPressed = false;
        this.keyRightPressed = false;

        return;
    }


    public float clamp(float min, float max, float value) {

        // todo: inline this function at its call sites

        if (min > max) {
            return (min + max) / 2.00f;
        }

        if (value < min) {
            return min;
        }

        return Math.min(value, max);
    }


    /**
     * Returns how much of {@code deltaY} can be applied without leaving the map.
     * Uses the same convention as legacy {@code ScreenPosition}: offset = -screenCenter.
     */
    public float clampToMapHeight(float deltaY, float viewHeight) {

        float padding = 100.00f;
        float topBorder = this.mapHeight + padding;
        float bottomBorder = 0.00f - padding;

        float minCameraCenterY = bottomBorder + viewHeight / 2.00f;
        float maxCameraCenterY = topBorder - viewHeight / 2.00f;

        float proposedCameraY = -(this.offsetY + deltaY);
        float clampedCameraY = this.clamp(minCameraCenterY, maxCameraCenterY, proposedCameraY);

        return (-clampedCameraY) - this.offsetY;
    }


    /**
     * Returns how much of {@code deltaX} can be applied without leaving the map.
     * X limits depend on the current Y (isometric parallelogram), so apply Y first.
     */
    public float clampToMapWidth(float deltaX, float viewWidth, float viewHeight) {

        float centerY = -this.offsetY;
        float minY = centerY - viewHeight / 2.00f;
        float maxY = minY + viewHeight;

        float left = this.incline * minY;
        float right = this.incline * maxY + this.mapWidth;
        float minCenterX = left + viewWidth / 2.00f;
        float maxCenterX = right - viewWidth / 2.00f;

        float proposedCenterX = -(this.offsetX + deltaX);
        float clampedCenterX = this.clamp(minCenterX, maxCenterX, proposedCenterX);

        return (-clampedCenterX) - this.offsetX;
    }


    public void updateCameraPosition(
        long frameDuration,
        float canvasWidth,
        float canvasHeight,
        float viewportWidth,
        float viewportHeight) {

        float viewScale = viewportWidth / canvasWidth;

        float vectorX = 0.00f;
        float vectorY = 0.00f;

        if (this.keyUpPressed) vectorY    -= 1.00f;
        if (this.keyDownPressed) vectorY  += 1.00f;
        if (this.keyLeftPressed) vectorX  += 1.00f;
        if (this.keyRightPressed) vectorX -= 1.00f;

        // handle keyboard pan
        if (vectorX != 0 || vectorY != 0) {

            final float frameDurationMs = frameDuration / 1_000_000.00f;
            final float cameraSpeed = 1.00f;  // units per second

            float vectorMagnitude = (float) Math.sqrt(vectorX * vectorX + vectorY * vectorY);

            float normalX = vectorX / vectorMagnitude;
            float normalY = vectorY / vectorMagnitude;

            float distance = cameraSpeed * frameDurationMs;

            float deltaX = normalX * distance;
            float deltaY = normalY * distance;

            // delta y needs to be applied before delta x
            // x bounds depend on the current vertical position
            this.offsetY += this.clampToMapHeight(deltaY, canvasHeight);
            this.offsetX += this.clampToMapWidth(deltaX, canvasWidth, canvasHeight);
        }

        // handle mouse pan
        this.offsetY += this.clampToMapHeight(this.pendingPanY / viewScale, canvasHeight);
        this.offsetX += this.clampToMapWidth(this.pendingPanX / viewScale, canvasWidth, canvasHeight);
        this.pendingPanX = 0;
        this.pendingPanY = 0;

        // note: delta x and y need to be cumulative based on all input: keyboard, mmb, rmb

        return;
    }
}