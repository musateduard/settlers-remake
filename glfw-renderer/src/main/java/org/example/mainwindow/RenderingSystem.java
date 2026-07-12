package org.example.mainwindow;

import java.awt.Rectangle;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33C;
import org.example.gamemap.SettlersMap;

import static org.lwjgl.opengl.GL33C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL33C.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL33C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL33C.GL_FRAMEBUFFER;


public class RenderingSystem {

    public static void drawGameScene(long frameDuration, Application application, Camera camera, SettlersMap gameMap) {
        // todo: move renderer.renderGameScene to RenderingSystem
        application.renderer.renderGameScene(frameDuration, application.window, camera, gameMap);
        return;
    }


    public static void drawUI(Application application, UserInterface userInterface) {
        // todo: move userInterface.renderUIStack to RenderingSystem
        userInterface.renderUIStack(application);
        return;
    }


    public static void drawFrame(long frameDuration, Application application, UserInterface userInterface, Camera camera, SettlersMap gameMap) {

        // activate canvas framebuffer
        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, application.renderer.canvas.framebufferId);
        GL33C.glClearColor(1.00f, 1.00f, 1.00f, 1.00f);
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // draw game scene
        drawGameScene(frameDuration, application, camera, gameMap);

        // draw ui
        drawUI(application, userInterface);

        Rectangle viewport = application.renderer.viewport;

        // activate screen buffer
        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GL33C.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        GL33C.glClearColor(1.00f, 0.00f, 1.00f, 1.00f);  // magenta
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // draw canvas to screen with aspect ratio
        GL33C.glDisable(GL_DEPTH_TEST);
        GL33C.glUseProgram(application.renderer.screenShader.id);
        GL33C.glActiveTexture(GL_TEXTURE0);
        GL33C.glBindTexture(GL_TEXTURE_2D, application.renderer.canvas.textureId);
        GL33C.glBindVertexArray(application.renderer.viewportVAOId);
        GL33C.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        GL33C.glEnable(GL_DEPTH_TEST);

        // present frame
        GLFW.glfwSwapBuffers(application.window.handle);

        return;
    }
}