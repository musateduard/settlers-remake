package org.example.mainwindow;

import org.example.events.MouseEvent;
import org.example.events.CursorEvent;
import org.example.events.KeyEvent;
import org.lwjgl.glfw.GLFW;


/**
 * this class handles all camera related functions like panning and zooming.
 */
public class Camera {

    public float offsetX;
    public float offsetY;
    public float prevCursorX;
    public float prevCursorY;
    public boolean lmbPressed;
    public boolean rmbPressed;
    public boolean mmbPressed;
    public boolean keyUpPressed;
    public boolean keyDownPressed;
    public boolean keyLeftPressed;
    public boolean keyRightPressed;


    public Camera() {

        this.offsetX = 0;
        this.offsetY = 0;
        this.prevCursorX = 0;
        this.prevCursorY = 0;
        this.lmbPressed = false;
        this.rmbPressed = false;
        this.mmbPressed = false;
        this.keyUpPressed = false;
        this.keyDownPressed = false;
        this.keyLeftPressed = false;
        this.keyRightPressed = false;

        return;
    }


    /*
    public void handleMouseButtonEvent(MouseEvent event) {

        if (event.action() == GLFW.GLFW_PRESS) {

            switch (event.button()) {

                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> this.rmbPressed = true;
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> this.lmbPressed = true;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> this.mmbPressed = true;

                default -> {
                    // do nothing
                }
            }
        }

        else {

            switch (event.button()) {

                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> this.rmbPressed = false;
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> this.lmbPressed = false;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> this.mmbPressed = false;

                default -> {
                    // do nothing
                }
            }
        }

        return;
    }


    public void handleCursorEvent(CursorEvent event) {

        // note:
        //
        // glfw screen coordinates are calculated from top-left to bottom right
        // opengl coordinates are bottom-left to top-right
        // deltaY needs to be calculated inverted (i.e. previous - current) so that moves up are positive and down are negative

        float deltaX = (float) event.xpos() - this.prevCursorX;
        float deltaY = this.prevCursorY - (float) event.ypos();  // deltaY needs to be inverted so that moves up are positive

        if (this.rmbPressed) {

            // note: when panning using rmb we apply delta negatively to simulate moving the camera

            this.offsetX -= deltaX;
            this.offsetY -= deltaY;
        }

        else if (this.mmbPressed) {

            // note: when panning using mmb we apply delta positively to simulate dragging the map

            this.offsetX += deltaX;
            this.offsetY += deltaY;
        }

        this.prevCursorX = (float) event.xpos();
        this.prevCursorY = (float) event.ypos();

        return;
    }


    public void handleKeyEvent(KeyEvent event) {

        if (event.action() == GLFW.GLFW_PRESS) {

            switch (event.key()) {

                case GLFW.GLFW_KEY_UP -> this.keyUpPressed = true;
                case GLFW.GLFW_KEY_DOWN -> this.keyDownPressed = true;
                case GLFW.GLFW_KEY_LEFT -> this.keyLeftPressed = true;
                case GLFW.GLFW_KEY_RIGHT -> this.keyRightPressed = true;

                default -> {
                    // do nothing
                }
            }
        }

        else if (event.action() == GLFW.GLFW_RELEASE) {

            switch (event.key()) {

                case GLFW.GLFW_KEY_UP -> this.keyUpPressed = false;
                case GLFW.GLFW_KEY_DOWN -> this.keyDownPressed = false;
                case GLFW.GLFW_KEY_LEFT -> this.keyLeftPressed = false;
                case GLFW.GLFW_KEY_RIGHT -> this.keyRightPressed = false;

                default -> {
                    // do nothing
                }
            }
        }

        else {
            // do nothing
        }

        return;
    }
    */


    public void updateCameraPosition(long frameDuration) {

        // update based on key press
        // update based on mouse movement while pressed
        // how do i detect a key was pressed/released within a single frame?

        float vectorX = 0.00f;
        float vectorY = 0.00f;

        if (this.keyUpPressed) {
            vectorY -= 1.00f;
        }

        if (this.keyDownPressed) {
            vectorY += 1.00f;
        }

        if (this.keyLeftPressed) {
            vectorX += 1.00f;
        }

        if (this.keyRightPressed) {
            vectorX -= 1.00f;
        }

        if (vectorX != 0 || vectorY != 0) {

            final float frameDurationMs = frameDuration / 1_000_000.00f;
            final float cameraSpeed = 0.60f;  // units per second

            float vectorMagnitude = (float) Math.sqrt(vectorX * vectorX + vectorY * vectorY);

            float normalX = vectorX / vectorMagnitude;
            float normalY = vectorY / vectorMagnitude;

            float distance = cameraSpeed * frameDurationMs;

            float deltaX = normalX * distance;
            float deltaY = normalY * distance;

            this.offsetX += deltaX;
            this.offsetY += deltaY;
        }

        return;
    }
}