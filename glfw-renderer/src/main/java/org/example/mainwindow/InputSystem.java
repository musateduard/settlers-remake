package org.example.mainwindow;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Queue;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiMouseButton;
import jsettlers.common.action.EActionType;
import jsettlers.common.action.PointAction;
import jsettlers.common.action.SelectAreaAction;
import jsettlers.common.map.shapes.IMapArea;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.graphics.map.MapContent;
import org.lwjgl.glfw.GLFW;
import org.example.events.CursorEvent;
import org.example.events.InputEvent;
import org.example.events.KeyEvent;
import org.example.events.MouseEvent;
import org.example.events.ResizeEvent;


public class InputSystem {

    private static InputSystem instance = null;
    public final Queue<InputEvent> queue = new ArrayDeque<>();


    private InputSystem() {

        // note: this class is a singleton and is not meant to be instantiated
        // instead you use getInstance() to get the current running instance
        // don't call this method from threads other than main
        // you can also use glfwSetWindowUserPointer to pass an instance to glfw that you can access during the callbacks
        // use glfwGetWindowUserPointer to get the instance pointer
        // for this project a singleton is sufficient

        return;
    }


    public static InputSystem getInstance() {

        if (InputSystem.instance == null) {
            InputSystem.instance = new InputSystem();
        }

        return InputSystem.instance;
    }


    public static void addKeyEvent(long windowId, int key, int scanCode, int action, int modifiers) {

        KeyEvent event = new KeyEvent(windowId, key, scanCode, action, modifiers);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    public static void addMouseButtonEvent(long windowId, int button, int action, int modifiers) {

        MouseEvent event = new MouseEvent(windowId, button, action, modifiers);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    public static void addCursorEvent(long windowId, double offsetX, double offsetY) {

        CursorEvent event = new CursorEvent(windowId, offsetX, offsetY);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    public static void addResizeEvent(long windowId, int newWidth, int newHeight) {

        ResizeEvent event = new ResizeEvent(windowId, newWidth, newHeight);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    static class ApplicationInput {

        static boolean consumeEvent(Application application, InputEvent event) {

            switch (event) {

                case ResizeEvent resize -> {
                    application.resizeWindow(resize);
                    return true;
                }

                default -> {
                    return false;
                }
            }
        }
    }


    static class UserInterfaceInput {

        static boolean handleMouseEvent(MouseEvent mouse) {

            ImGuiIO input = ImGui.getIO();

            if (mouse.action() == GLFW.GLFW_PRESS) {
                input.addMouseButtonEvent(ImGuiMouseButton.Left, true);
            }

            else {
                input.addMouseButtonEvent(ImGuiMouseButton.Left, false);
            }

            return input.getWantCaptureMouse();
        }


        static boolean consumeEvent(Application application, InputEvent event) {

            ImGuiIO input = ImGui.getIO();

            switch (event) {

                case CursorEvent move -> {
                    // CursorEvent needs to be passed right after glfwBackend.newFrame and
                    // before ImGui.newFrame otherwise they get overwritten
                    return input.getWantCaptureMouse();
                }

                case MouseEvent mouse -> {
                    return UserInterfaceInput.handleMouseEvent(mouse);
                }

                case KeyEvent key -> {
                    // todo: add key event to ImGuiIO
                    return input.getWantCaptureKeyboard();
                }

                default -> {
                    return false;
                }
            }
        }
    }


    static class CameraInput {

        static boolean handleKeyEvent(Camera camera, KeyEvent event) {

            switch (event.key()) {

                case GLFW.GLFW_KEY_UP -> {
                    camera.keyUpPressed = event.action() != GLFW.GLFW_RELEASE;
                    return true;
                }

                case GLFW.GLFW_KEY_DOWN -> {
                    camera.keyDownPressed = event.action() != GLFW.GLFW_RELEASE;
                    return true;
                }

                case GLFW.GLFW_KEY_LEFT -> {
                    camera.keyLeftPressed = event.action() != GLFW.GLFW_RELEASE;
                    return true;
                }

                case GLFW.GLFW_KEY_RIGHT -> {
                    camera.keyRightPressed = event.action() != GLFW.GLFW_RELEASE;
                    return true;
                }

                default -> {
                    return false;
                }
            }
        }


        static boolean handleMouseEvent(Camera camera, MouseEvent event) {

            switch (event.button()) {

                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                    camera.rmbPressed = event.action() == GLFW.GLFW_PRESS;
                    return false;  // return false for testing; don't return false unconditionally
                }

                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> {
                    camera.mmbPressed = event.action() == GLFW.GLFW_PRESS;
                    return false;
                }

                default -> {
                    return false;
                }
            }
        }


        static boolean handleCursorEvent(Camera camera, CursorEvent event) {

            // todo: convert coordinates only when passing to opengl
            // use top-left coordinates for everything else

            /*
            note:

            glfw screen coordinates are calculated from top-left to bottom right
            opengl coordinates are bottom-left to top-right
            deltaY needs to be calculated inverted (i.e. previous - current) so that moves up are positive and down are negative
            */

            float deltaX = (float) event.offsetX() - camera.prevCursorX;
            float deltaY = camera.prevCursorY - (float) event.offsetY();  // deltaY needs to be inverted so that moves up are positive

            if (camera.rmbPressed) {

                // note: when panning using rmb we apply delta negatively to simulate moving the camera

                camera.offsetX -= deltaX;
                camera.offsetY -= deltaY;
            }

            else if (camera.mmbPressed) {

                // note: when panning using mmb we apply delta positively to simulate dragging the map

                camera.offsetX += deltaX;
                camera.offsetY += deltaY;
            }

            camera.prevCursorX = (float) event.offsetX();
            camera.prevCursorY = (float) event.offsetY();
            return false;
        }


        static boolean consumeEvent(Camera camera, InputEvent event) {

            switch (event) {

                case KeyEvent key -> {
                    boolean result = CameraInput.handleKeyEvent(camera, key);
                    return result;
                }

                case MouseEvent mouse -> {
                    return CameraInput.handleMouseEvent(camera, mouse);
                }

                case CursorEvent cursor -> {
                    return CameraInput.handleCursorEvent(camera, cursor);
                }

                default -> {
                    return false;
                }
            }
        }
    }


    static class GameInput {

        static void handleMouseEvent(Application application, UserInterface userInterface, MapContent map, MouseEvent event) {

            switch (event.button()) {

                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {

                    Point cursorPosition = application.getCursorPosition();

                    if (event.action() == GLFW.GLFW_PRESS) {
                        userInterface.isLmbPressed = true;
                        userInterface.selectionStartPosition.x = cursorPosition.x;
                        userInterface.selectionStartPosition.y = cursorPosition.y;
                    }

                    else if (event.action() == GLFW.GLFW_RELEASE) {

                        if (userInterface.isLmbPressed) {

                            userInterface.isLmbPressed = false;

                            int deltaX = cursorPosition.x - userInterface.selectionStartPosition.x;
                            int deltaY = cursorPosition.y - userInterface.selectionStartPosition.y;
                            int distanceSq = deltaX * deltaX + deltaY * deltaY;

                            if (distanceSq < 10) {

                                // Treat as point selection
                                ShortPoint2D mapPosition = map.mapContext.getPositionOnScreen(
                                    cursorPosition.x,
                                    (float) application.canvas.height - cursorPosition.y
                                );

                                if (map.mapContext.checkMapCoordinates(mapPosition.x, mapPosition.y)) {
                                    PointAction singleSelect = new PointAction(EActionType.SELECT_POINT, mapPosition);
                                    map.fireAction(singleSelect);
                                }
                            }

                            else {

                                int canvasHeight = application.canvas.height;

                                // Treat as area selection
                                int startX = Math.min(userInterface.selectionStartPosition.x, cursorPosition.x);
                                int startY = Math.min(canvasHeight - userInterface.selectionStartPosition.y, canvasHeight - cursorPosition.y);

                                // Y needs to be inverted for MapContent since origin is bottom-left for it
                                int endX = Math.max(userInterface.selectionStartPosition.x, cursorPosition.x);
                                int endY = Math.max(canvasHeight - userInterface.selectionStartPosition.y, canvasHeight - cursorPosition.y);

                                IMapArea selectionArea = map.mapContext.getRectangleOnScreen(startX, startY, endX, endY);
                                SelectAreaAction selection = new SelectAreaAction(selectionArea);
                                map.fireAction(selection);
                            }

                            userInterface.selectionStartPosition.x = 0;
                            userInterface.selectionStartPosition.y = 0;
                        }
                    }
                }

                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> System.out.println("handling right click");

                default -> {
                    // do nothing
                }
            }

            return;
        }


        static void consumeEvent(
            Application application,
            UserInterface userInterface,
            MapContent map,
            GLFWGOEventConverter eventConverter,
            InputEvent event) {

            switch (event) {

                case CursorEvent cursor -> {

                    if (userInterface.isLmbPressed == false) {
                        userInterface.selectionStartPosition.x = 0;
                        userInterface.selectionStartPosition.y = 0;
                    }

                    else {
                        // do nothing
                    }
                }

                case MouseEvent mouse -> {
                    // eventConverter.onMouseButton(application, mouse);
                    GameInput.handleMouseEvent(application, userInterface, map, mouse);
                }

                default -> {
                    // do nothing
                }
            }

            return;
        }
    }


    public static void handleInput(
        Application application,
        UserInterface userInterface,
        Camera camera,
        GLFWGOEventConverter eventConverter,
        MapContent map) {

        InputSystem input = InputSystem.getInstance();

        // gather input events
        GLFW.glfwPollEvents();

        // handle input queue
        while (input.queue.isEmpty() == false) {

            InputEvent event = input.queue.remove();

            if (ApplicationInput.consumeEvent(application, event) == true) {
                continue;
            }

            else if (UserInterfaceInput.consumeEvent(application, event) == true) {
                continue;
            }

            else if (CameraInput.consumeEvent(camera, event) == true) {
                continue;
            }

            else {
                GameInput.consumeEvent(application, userInterface, map, eventConverter, event);
                continue;
            }
        }

        return;
    }
}