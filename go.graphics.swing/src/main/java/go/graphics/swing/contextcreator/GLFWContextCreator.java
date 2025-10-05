/*******************************************************************************
 * Copyright (c) 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package go.graphics.swing.contextcreator;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWCursorEnterCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

import go.graphics.UIPoint;
import go.graphics.event.command.EModifier;
import go.graphics.event.interpreter.AbstractEventConverter;
import go.graphics.swing.ContextContainer;


public class GLFWContextCreator extends AsyncContextCreator {

    private int glfw_width;
    private int glfw_height;
    protected long glfwWindowId;
    private long glfw_resize_time;
	private final GLFWEventConverter eventConverter;

    private static final HashMap<Integer, String> keys = new HashMap<>();
    private static final HashMap<Integer, EModifier> mods = new HashMap<>();

    static {
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_LEFT, "LEFT");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_RIGHT, "RIGHT");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_UP, "UP");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_DOWN, "DOWN");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_PAUSE, "PAUSE");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F1, "F1");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F2, "F2");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F3, "F3");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F4, "F4");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F5, "F5");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F6, "F6");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F7, "F7");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F8, "F8");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F9, "F9");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F10, "F10");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F11, "F11");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_F12, "F12");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_DELETE, "DELETE");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_ESCAPE, "ESCAPE");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_BACKSPACE, "BACK_SPACE");
        GLFWContextCreator.keys.put(GLFW.GLFW_KEY_SPACE, " ");

        GLFWContextCreator.mods.put(GLFW.GLFW_KEY_LEFT_SHIFT, EModifier.SHIFT);
        GLFWContextCreator.mods.put(GLFW.GLFW_KEY_RIGHT_SHIFT, EModifier.SHIFT);
        GLFWContextCreator.mods.put(GLFW.GLFW_KEY_LEFT_ALT, EModifier.ALT);
        GLFWContextCreator.mods.put(GLFW.GLFW_KEY_RIGHT_ALT, EModifier.ALT);
        GLFWContextCreator.mods.put(GLFW.GLFW_KEY_LEFT_CONTROL, EModifier.CTRL);
        GLFWContextCreator.mods.put(GLFW.GLFW_KEY_RIGHT_CONTROL, EModifier.CTRL);
    }


	public GLFWContextCreator(ContextContainer container, boolean debug) {

		super(container, debug);

		this.eventConverter = new GLFWEventConverter();
        this.glfw_resize_time = -1;

        return;
	}


	public void async_init() {

		if (this.debug) {
            GLFWErrorCallback ec = GLFWErrorCallback.createPrint(System.err);
			GLFW.glfwSetErrorCallback(ec);
		}

		if (GLFW.glfwInit() == false) {
            throw new Error("glfwInit() failed!");
        }

        this.configureWindow();

		synchronized (this.windowLock) {

            this.glfwWindowId = GLFW.glfwCreateWindow(this.width, this.height, "lwjgl-offscreen", 0, 0);
            this.setupContext();

            try {
                this.parentContainer.resizeContext(this.width, this.height);
			}

            catch (ContextException exception) {
				exception.printStackTrace();
			}
		}

        this.eventConverter.registerCallbacks();
        return;
	}


	protected void configureWindow() {

		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, this.debug ? GLFW.GLFW_TRUE : GLFW.GLFW_DONT_CARE);
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 1);

        return;
	}


	protected void setupContext() {

		GLFW.glfwMakeContextCurrent(this.glfwWindowId);
		GLFW.glfwSwapInterval(1);
        this.parentContainer.wrapNewGLContext();

        return;
	}


	public void async_set_size(int width, int height) {
		GLFW.glfwSetWindowSize(this.glfwWindowId, width, height);
        return;
	}


	public void async_refresh() {

		if (this.glfw_resize_time != -1) {

			if (this.glfw_resize_time + 10 <= System.currentTimeMillis()) {

				Window wnd = SwingUtilities.windowForComponent(this.canvas);
				int dw = wnd.getWidth() - this.canvas.getWidth();
				int dh = wnd.getHeight() - this.canvas.getHeight();

				wnd.setSize(this.glfw_width + dw, this.glfw_height + dh);

                this.glfw_resize_time = -1;

				this.resolutionChanged = true;
				this.async_resized = true;
				this.newWidth = this.glfw_width + dw;
				this.newHeight = this.glfw_height + dh;
			}
		}

		GLFW.glfwPollEvents();
        return;
	}


	public void async_swap_buffers() {
		GLFW.glfwSwapBuffers(this.glfwWindowId);
        return;
	}


	public void async_stop() {
		GLFW.glfwTerminate();
        return;
	}


	private class GLFWEventConverter extends AbstractEventConverter {

		private UIPoint last_point = new UIPoint(0, 0);

		private final EnumSet<EModifier> activeMods = EnumSet.noneOf(EModifier.class);

		private final GLFWKeyCallbackI key_callback = (window, key, scancode, action, modsUNUSUED) -> {

			String name = GLFW.glfwGetKeyName(key, scancode);

			if (name == null) {
				name = keys.get(key);
			}

			EModifier mod = mods.get(key);

			if (mod != null) {

				synchronized (activeMods) {

					if (action == GLFW.GLFW_PRESS) {
						activeMods.add(mod);
					}

                    else {
						activeMods.remove(mod);
					}
				}
			}

			if (action == GLFW.GLFW_PRESS) {
				startKeyEvent(name);
			}

            else if (action == GLFW.GLFW_RELEASE) {
				endKeyEvent(name);
			}
		};

		private double pressTime = -1;

		private final GLFWMouseButtonCallbackI mouse_callback = (window, button, action, mods) -> {

			UIPoint point = last_point;

			if (action == GLFW.GLFW_PRESS) {

				switch (button) {

					case GLFW.GLFW_MOUSE_BUTTON_1:
                    startDraw(point);
                    break;

					case GLFW.GLFW_MOUSE_BUTTON_2:
                    pressTime = GLFW.glfwGetTime();

					case GLFW.GLFW_MOUSE_BUTTON_3:
                    startPan(point);
                    break;
				}
			}

            else {

				switch (button) {

					case GLFW.GLFW_MOUSE_BUTTON_1:
                    endDraw(point);
                    break;

					case GLFW.GLFW_MOUSE_BUTTON_2:
                    pressTime = -1;

					case GLFW.GLFW_MOUSE_BUTTON_3:
                    endPan(point);
                    break;
				}
			}
		};

		private final GLFWCursorEnterCallbackI cursorenter_callback = (window, entered) -> {

			if (entered) {
				startHover(last_point);
			}

            else {
				endHover(last_point);
			}
		};

		private final GLFWCursorPosCallbackI cursorpos_callback = (window, xpos, ypos) -> {

			last_point = new UIPoint(xpos, height - ypos);
			updateHoverPosition(last_point);

			if (pressTime + 0.1 < GLFW.glfwGetTime()) {
				updatePanPosition(last_point);
				updateDrawPosition(last_point);
			}
		};

		private final GLFWScrollCallbackI scroll_callback = (window, xoffset, yoffset) -> {

			float factor = (float) Math.exp(yoffset / 20.0);

			startZoom();
			endZoomEvent(factor, last_point);
		};

		private final GLFWWindowSizeCallbackI size_callback = new GLFWWindowSizeCallbackI() {

			@Override
			public void invoke(long window, int width, int height) {

				if (GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == 0) {
                    return;
                }

				synchronized (windowLock) {

					if (GLFWContextCreator.this.width == width && GLFWContextCreator.this.height == height) {
						glfw_resize_time = -1;
						return;
					}

					glfw_resize_time = System.currentTimeMillis();
					glfw_width = width;
					glfw_height = height;
				}
			}
		};

		private final GLFWWindowCloseCallbackI close_callback = (window) -> {
			Window wnd = SwingUtilities.windowForComponent(canvas);
			wnd.dispatchEvent(new WindowEvent(wnd, WindowEvent.WINDOW_CLOSING));
		};

		private GLFWEventConverter() {

			super(parentContainer);

			addReplaceRule(new EventReplacementRule(ReplacableEvent.DRAW, Replacement.COMMAND_SELECT, 5, 10));
			addReplaceRule(new EventReplacementRule(ReplacableEvent.PAN, Replacement.COMMAND_ACTION, 5, 10));

            return;
		}


		@Override
		protected Set<EModifier> getCurrentModifiers() {

			synchronized (activeMods) {
				return activeMods.clone();
			}
		}


		private void registerCallbacks() {

			GLFW.glfwSetKeyCallback(glfwWindowId, key_callback);
			GLFW.glfwSetMouseButtonCallback(glfwWindowId, mouse_callback);
			GLFW.glfwSetScrollCallback(glfwWindowId, scroll_callback);
			GLFW.glfwSetCursorEnterCallback(glfwWindowId, cursorenter_callback);
			GLFW.glfwSetCursorPosCallback(glfwWindowId, cursorpos_callback);
			GLFW.glfwSetWindowSizeCallback(glfwWindowId, size_callback);
			GLFW.glfwSetWindowCloseCallback(glfwWindowId, close_callback);

            return;
		}
	}
}