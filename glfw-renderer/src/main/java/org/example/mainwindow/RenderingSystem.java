package org.example.mainwindow;

import java.awt.Rectangle;

import go.graphics.swing.opengl.LWJGLDrawContext;
import jsettlers.graphics.map.MapContent;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33C;
import org.example.gamemap.SettlersMap;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL33C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL33C.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL33C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL33C.GL_FRAMEBUFFER;


public class RenderingSystem {

    public static void renderMapTerrain(Application application, SettlersMap gameMap) {

        // todo: move this method to RenderingSystem

        // get map terrain
        // calculate visible area
        // render visible terrain

        // note: this method should only scale its model matrix
        // note: projection and view matrixes are only modified by screen resize and camera move

        // create vertex buffer
        float[] mapVertexBuffer = {
            0.00f, 0.00f, 0.00f,  // bottom left
            1.00f, 0.00f, 0.00f,  // bottom right
            1.00f, 1.00f, 0.00f,  // top right
            0.00f, 1.00f, 0.00f,  // top left
        };

        // create vao vbo
        // note: vao vbo need to be created during construction and only referenced during rendering
        int vboId = application.renderer.createVBO(mapVertexBuffer, GL_ARRAY_BUFFER, GL_STATIC_DRAW);
        int vaoId = application.renderer.createVAO(vboId, GL_ARRAY_BUFFER, 0, 3, GL_FLOAT, false, 0, 0);

        // todo: add modelMatrix to gameMap object

        // upload model matrix
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.translate(50, 50, 0);
        modelMatrix.scale(100, 100, 1);

        modelMatrix.get(application.renderer.canvas.floatBuffer);
        GL33C.glUniformMatrix4fv(application.renderer.canvas.modelMatrixAddress, false, application.renderer.canvas.floatBuffer);

        // set color uniform
        GL33C.glUniform4f(application.renderer.canvas.colorUniformAddress, 0.00f, 1.00f, 1.00f, 1.00f);

        // bind vao and vbo
        GL33C.glBindVertexArray(vaoId);
        GL33C.glEnableVertexAttribArray(0);

        // draw buffer
        GL33C.glDrawArrays(GL33C.GL_TRIANGLE_FAN, 0, 4);

        // cleanup
        GL33C.glDisableVertexAttribArray(0);
        GL33C.glBindVertexArray(0);
        GL33C.glUseProgram(0);

        return;
    }


    public static void drawGameScene(long frameDuration, Application application, Camera camera, SettlersMap gameMap) {

        // update projection matrix
        application.renderer.canvas.updateProjectionMatrix(application.renderer.canvas.width, application.renderer.canvas.height);

        // update view matrix
        camera.updateCameraPosition(frameDuration);
        application.renderer.canvas.updateViewMatrix(camera);

        // render game scene
        RenderingSystem.renderMapTerrain(application, gameMap);

        return;
    }


    public static void drawUI(Application application, UserInterface userInterface) {

        // todo: move userInterface.renderUIStack to RenderingSystem
        userInterface.renderUIStack(application);

        return;
    }


    public static void drawFrame(
        long frameDuration,
        Application application,
        UserInterface userInterface,
        Camera camera,
        SettlersMap gameMap,
        LWJGLDrawContext context,
        MapContent jsettlersMap) {

        // activate canvas framebuffer
        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, application.renderer.canvas.framebufferId);
        GL33C.glClearColor(1.00f, 1.00f, 1.00f, 1.00f);
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        GL33C.glViewport(0, 0, 800, 600);

        // draw game scene
        // RenderingSystem.drawGameScene(frameDuration, application, camera, gameMap);
        jsettlersMap.drawContent(context, application.renderer.canvas.width, application.renderer.canvas.height);

        // draw ui
        // RenderingSystem.drawUI(application, userInterface);

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