package org.example.mainwindow;

import java.awt.Point;
import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL33C;
import org.example.events.ResizeEvent;

import static org.lwjgl.opengl.GL33C.GL_NO_ERROR;


public class Application {

    public Window window;
    public Renderer renderer;
    public final float idealAspectRatio = 800.00f / 600.00f;
    public float currentAspectRatio = 800.00f / 600.00f;
    public Point cursorPosition = new Point(0, 0);
    public boolean isRunning;


    public Application() {

        GLFW.glfwSetErrorCallback(this::errorCallback);

        // init glfw
        if (GLFW.glfwInit() == false) {
            throw new RuntimeException("Failed to initialize GLFW.");
        }

        this.window = new Window();
        this.renderer = new Renderer(this.window);
        this.isRunning = true;

        return;
    }


    private void errorCallback(int errorCode, long errorMessage) {
        throw new RuntimeException(GLFWErrorCallback.getDescription(errorMessage));
    }


    public void resizeWindow(ResizeEvent event) {

        this.window.width = event.width();
        this.window.height = event.height();
        this.currentAspectRatio = (float) event.width() / (float) event.height();
        boolean isWideScreen = this.currentAspectRatio >= this.idealAspectRatio;

        float viewportWidth = isWideScreen ? (float) this.window.height * this.idealAspectRatio : (float) this.window.width;
        float viewportHeight = isWideScreen ? (float) this.window.height : (float) this.window.width / this.idealAspectRatio;
        float viewportX = isWideScreen ? ((float) this.window.width - viewportWidth) / 2 : 0;
        float viewportY = isWideScreen ? 0 : ((float) this.window.height - viewportHeight) / 2;

        this.renderer.viewport.width = (int) viewportWidth;
        this.renderer.viewport.height = (int) viewportHeight;
        this.renderer.viewport.x = (int) viewportX;
        this.renderer.viewport.y = (int) viewportY;

        return;
    }


    /**
     * this function returns the cursor position on the canvas based on window size.
     *
     * @return {@code Point}
     */
    public Point getCursorPosition() {

        // note: this function heap allocates during rendering pipeline

        DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(this.window.handle, mouseX, mouseY);

        boolean isWideScreen = this.currentAspectRatio >= this.idealAspectRatio;

        float canvasWidth = isWideScreen ? (float) this.window.height * this.idealAspectRatio : (float) this.window.width;
        float canvasHeight = isWideScreen ? (float) this.window.height : (float) this.window.width / this.idealAspectRatio;
        float canvasX = isWideScreen ? ((float) this.window.width - canvasWidth) / 2 : 0;
        float canvasY = isWideScreen ? 0 : ((float) this.window.height - canvasHeight) / 2;
        float canvasCursorScale = isWideScreen ? (this.window.height / 600.00f) : (this.window.width / 800.00f);

        float canvasCursorX = ((float) mouseX.get() - canvasX) / canvasCursorScale;
        float canvasCursorY = ((float) mouseY.get() - canvasY) / canvasCursorScale;

        this.cursorPosition.x = (int) canvasCursorX;
        this.cursorPosition.y = (int) canvasCursorY;

        return this.cursorPosition;
    }


    public boolean shouldClose() {
        final boolean result = GLFW.glfwWindowShouldClose(this.window.handle);
        return result;
    }


    public void cleanup() {

        // renderer cleanup
        // todo: delete viewport vbo and vao
        GL33C.glDeleteFramebuffers(this.renderer.canvas.framebufferId);
        GL33C.glDeleteTextures(this.renderer.canvas.textureId);
        GL33C.glDeleteRenderbuffers(this.renderer.canvas.depthBufferId);

        // window cleanup
        Callbacks.glfwFreeCallbacks(this.window.handle);
        GLFW.glfwDestroyWindow(this.window.handle);
        GLFW.glfwTerminate();

        int openglError = GL33C.glGetError();
        if (openglError != GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        return;
    }
}