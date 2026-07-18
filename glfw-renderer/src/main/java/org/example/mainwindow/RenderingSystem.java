package org.example.mainwindow;

import java.awt.Point;
import java.awt.Rectangle;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiMouseButton;
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


    public static void drawGameScene(long frameDuration, Application application, Camera camera, LWJGLDrawContext context, MapContent map) {

        // update projection matrix
        application.renderer.canvas.updateProjectionMatrix(application.renderer.canvas.width, application.renderer.canvas.height);

        // update view matrix
        camera.updateCameraPosition(frameDuration);
        application.renderer.canvas.updateViewMatrix(camera);

        // render game scene
        // RenderingSystem.renderMapTerrain(application, gameMap);
        map.mapContext.getScreen().setScreenCenter(-camera.offsetX, -camera.offsetY);
        map.drawContent(context, application.renderer.canvas.width, application.renderer.canvas.height);

        return;
    }


    static class UserInterfaceRender {

        static void drawForeground(Application application, UserInterface userInterface) {

            if (userInterface.isLmbPressed == false) {
                return;
            }

            Point currentPos = application.getCursorPosition();
            ImDrawList drawList = ImGui.getForegroundDrawList();
            int deltaX = currentPos.x - userInterface.selectionStartPosition.x;
            int deltaY = currentPos.y - userInterface.selectionStartPosition.y;

            if ((deltaX * deltaX + deltaY * deltaY) >= 50) {

                float startX = userInterface.selectionStartPosition.x;
                float startY = userInterface.selectionStartPosition.y;
                float endX = currentPos.x;
                float endY = currentPos.y;
                int outlineColor = ImGui.getColorU32(1.00f, 1.00f, 1.00f, 1.00f);

                drawList.addRect(startX, startY, endX, endY, outlineColor, 0.00f, 0, 1.00f);
            }

            return;
        }


        static void drawDebugMenu(Application application, UserInterface userInterface) {

            Point cursor = application.getCursorPosition();
            ImGuiIO input = ImGui.getIO();

            // draw debug info
            ImGui.begin("debug info");
            ImGui.text("window pos %d %d".formatted((int) ImGui.getWindowPos().x, (int) ImGui.getWindowPos().y));
            // ImGui.text("window width %d".formatted(screenWidth));
            // ImGui.text("window height %d".formatted(screenHeight));
            // ImGui.text("window cursor x %d".formatted(mouseX));
            // ImGui.text("window cursor y %d".formatted(mouseY));
            ImGui.text("LMB down %s".formatted(GLFW.glfwGetMouseButton(application.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS));
            ImGui.text("canvas cursor x %d".formatted(cursor.x));
            ImGui.text("canvas cursor y %d".formatted(cursor.y));
            ImGui.text("start selection x %d".formatted(userInterface.selectionStartPosition.x));
            ImGui.text("start selection y %d".formatted(userInterface.selectionStartPosition.y));
            // ImGui.text("input queue size %d", event_queue_size);  // not available in java
            ImGui.end();

            return;
        }


        static void drawUI(Application application, UserInterface userInterface) {

            userInterface.glfwBackend.newFrame();
            userInterface.openglBackend.newFrame();

            ImGuiIO input = ImGui.getIO();
            Point cursor = application.getCursorPosition();

            // input.addMousePosEvent needs to be called here
            // glfwBackend.newFrame also calls it and overwrites the correct coordinates from InputSystem
            input.setDisplaySize(800.00f, 600.00f);
            input.setDisplayFramebufferScale(1.00f, 1.00f);
            input.addMousePosEvent((float) cursor.x, (float) cursor.y);

            ImGui.newFrame();

            UserInterfaceRender.drawDebugMenu(application, userInterface);
            UserInterfaceRender.drawForeground(application, userInterface);

            ImGui.render();
            userInterface.openglBackend.renderDrawData(ImGui.getDrawData());

            return;
        }
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
        RenderingSystem.drawGameScene(frameDuration, application, camera, context, jsettlersMap);

        // draw ui
        UserInterfaceRender.drawUI(application, userInterface);

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