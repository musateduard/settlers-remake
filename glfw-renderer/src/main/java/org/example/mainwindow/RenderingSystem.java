package org.example.mainwindow;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33C;

import static org.lwjgl.opengl.GL33C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL33C.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL33C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL33C.GL_FRAMEBUFFER;


public class RenderingSystem {

    public static void drawGameScene(long frameDuration, Application application, Camera camera) {
        // todo: move renderer.renderGameScene to RenderingSystem
        application.renderer.renderGameScene(frameDuration, application.window, camera);
        return;
    }


    public static void drawUI(Application application, GuiRenderer userInterface) {
        // todo: move userInterface.renderUIStack to RenderingSystem
        userInterface.renderUIStack(application);
        return;
    }


    public static void drawFrame(long frameDuration, Application application, GuiRenderer userInterface, Camera camera) {

        // activate canvas framebuffer
        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, application.renderer.canvas.frameBufferId);
        GL33C.glClearColor(1.00f, 1.00f, 1.00f, 1.00f);
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // draw game scene
        drawGameScene(frameDuration, application, camera);

        // draw ui
        drawUI(application, userInterface);

        // activate screen buffer
        // todo: set canvas size on resize and don't calculate every frame
        int[] width = { 0 };
        int[] height = { 0 };
        GLFW.glfwGetWindowSize(application.window.handle, width, height);

        int screenWidth = width[0];
        int screenHeight = height[0];

        float idealAspectRatio = 800.00f / 600.00f;
        float currentAspectRatio = (float) screenWidth / (float) screenHeight;
        boolean isWideScreen = currentAspectRatio >= idealAspectRatio;

        int canvasWidth  = isWideScreen ? (int) (screenHeight * idealAspectRatio) : screenWidth;
        int canvasHeight = isWideScreen ? screenHeight                            : (int) (screenWidth / idealAspectRatio);
        int canvasX      = isWideScreen ? (screenWidth - canvasWidth) / 2         : 0;
        int canvasY      = isWideScreen ? 0                                       : (screenHeight - canvasHeight) / 2;

        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GL33C.glViewport(canvasX, canvasY, canvasWidth, canvasHeight);
        GL33C.glClearColor(1.00f, 0.00f, 1.00f, 1.00f);  // magenta
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // draw canvas to screen with aspect ratio
        GL33C.glDisable(GL_DEPTH_TEST);
        GL33C.glUseProgram(application.renderer.canvas.shaderProgram.shaderId);
        GL33C.glActiveTexture(GL_TEXTURE0);
        GL33C.glBindTexture(GL_TEXTURE_2D, application.renderer.canvas.textureId);
        GL33C.glBindVertexArray(application.renderer.canvas.canvasVaoId);
        GL33C.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        GL33C.glEnable(GL_DEPTH_TEST);

        // present frame
        GLFW.glfwSwapBuffers(application.window.handle);

        return;
    }
}