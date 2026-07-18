package org.example.mainwindow;

import go.graphics.UIPoint;
import go.graphics.event.GOEventHandlerProvider;
import go.graphics.event.interpreter.AbstractEventConverter;
import org.example.events.CursorEvent;
import org.example.events.MouseEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.Point;


/**
 * Converts GLFW mouse input into GO events for the game engine.
 */
public class GLFWGOEventConverter extends AbstractEventConverter {

    private static final int MOUSE_MOVE_THRESHOLD = 10;
    private static final double MOUSE_TIME_THRESHOLD = 5;

    private boolean leftButtonDown;


    public GLFWGOEventConverter(GOEventHandlerProvider provider) {

        super(provider);
        this.leftButtonDown = false;

        this.addReplaceRule(
            new EventReplacementRule(
                ReplacableEvent.DRAW,
                Replacement.COMMAND_SELECT,
                MOUSE_TIME_THRESHOLD,
                MOUSE_MOVE_THRESHOLD
            )
        );

        return;
    }


    public void onMouseButton(Application application, MouseEvent event) {

        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }

        Point canvasCursor = application.getCursorPosition();
        UIPoint position = new UIPoint(
            canvasCursor.x,
            application.canvas.height - canvasCursor.y
        );

        if (event.action() == GLFW.GLFW_PRESS) {
            this.leftButtonDown = true;
            this.startDraw(position);
        }

        else if (event.action() == GLFW.GLFW_RELEASE) {
            this.leftButtonDown = false;
            this.endDraw(position);
        }

        return;
    }


    public void onCursorMove(Application application, CursorEvent event) {

        if (this.leftButtonDown == false && drawStarted() == false) {
            return;
        }

        Point canvasCursor = application.getCursorPosition();
        UIPoint position = new UIPoint(
            canvasCursor.x,
            application.canvas.height - canvasCursor.y
        );

        this.updateDrawPosition(position);
        return;
    }
}