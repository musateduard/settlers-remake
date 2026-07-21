package org.example.mainwindow;

import java.util.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL33C;
import org.example.events.ResizeEvent;
import org.example.shaders.ScreenShader;
import static org.lwjgl.opengl.GL33C.GL_NO_ERROR;


public class Application {

    public final Window window;
    public final Renderer renderer;
    public final Framebuffer canvas;
    public final Rectangle viewport;
    public final float idealAspectRatio = 800.00f / 600.00f;
    public float currentAspectRatio = 800.00f / 600.00f;
    public final Point cursorPosition = new Point(0, 0);
    public boolean isRunning;
    public final ScreenShader screenShader;
    public final VertexBuffer viewportBuffer;


    public Application() {

        GLFW.glfwSetErrorCallback(this::errorCallback);

        // init glfw
        if (GLFW.glfwInit() == false) {
            throw new RuntimeException("Failed to initialize GLFW.");
        }

        final int width = 800;
        final int height = 600;
        this.viewport = new Rectangle(0, 0, width, height);

        this.window = new Window();
        this.renderer = new Renderer(this.window);
        this.canvas = new Framebuffer();
        this.isRunning = true;
        this.screenShader = new ScreenShader();

        final float[] viewportVertices = {
            -1.00f, -1.00f, 0.00f, 0.00f, 0.00f,  // bottom left
            -1.00f,  1.00f, 0.00f, 0.00f, 1.00f,  // top left
            1.00f,  -1.00f, 0.00f, 1.00f, 0.00f,  // bottom right
            1.00f,   1.00f, 0.00f, 1.00f, 1.00f,  // top right
        };

        final int sizeof_float = 4;
        final int viewportStride = 5 * sizeof_float;

        final List<VertexAttribute> attributeList = List.of(
            new VertexAttribute(0, 3, GL33C.GL_FLOAT, false, viewportStride, 0),
            new VertexAttribute(1, 2, GL33C.GL_FLOAT, false, viewportStride, 3 * 4)
        );

        this.viewportBuffer = new VertexBuffer(viewportVertices, attributeList, GL33C.GL_STATIC_DRAW);

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

        this.viewport.width = (int) viewportWidth;
        this.viewport.height = (int) viewportHeight;
        this.viewport.x = (int) viewportX;
        this.viewport.y = (int) viewportY;

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
        GL33C.glDeleteFramebuffers(this.canvas.framebufferId);
        GL33C.glDeleteTextures(this.canvas.textureId);
        GL33C.glDeleteRenderbuffers(this.canvas.depthBufferId);

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