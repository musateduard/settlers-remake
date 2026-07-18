package org.example.mainwindow;

import java.awt.Point;
import java.awt.Rectangle;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImDrawList;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33C;
import go.graphics.GLDrawContext;
import go.graphics.BackgroundDrawHandle;
import go.graphics.IllegalBufferException;
import go.graphics.swing.opengl.LWJGLDrawContext;
import jsettlers.common.map.IDirectGridProvider;
import jsettlers.common.map.shapes.MapRectangle;
import jsettlers.common.position.FloatRectangle;
import jsettlers.graphics.map.draw.Background;
import jsettlers.graphics.map.MapContent;
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

    static class GameRenderer {

        public static void renderTestScene(Application application, SettlersMap gameMap) {

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

            modelMatrix.get(application.canvas.floatBuffer);
            GL33C.glUniformMatrix4fv(application.canvas.modelMatrixAddress, false, application.canvas.floatBuffer);

            // set color uniform
            GL33C.glUniform4f(application.canvas.colorUniformAddress, 0.00f, 1.00f, 1.00f, 1.00f);

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


        public static void frameSetupLegacy(Application application, LWJGLDrawContext context, MapContent map) {

            map.framerate.nextFrame();
            map.gameSpeedCalculator.tick();
            map.objectDrawer.setVisibleGrid(((IDirectGridProvider) map.map).getVisibleStatusArray());
            map.resizeTo(application.canvas.width, application.canvas.height);

            map.adaptScreenSize();  // adaptScreenSize fires a ScreenChangeAction on screen resize
            map.objectDrawer.nextFrame();
            map.mapContext.begin(context);

            return;
        }


        public static void renderLandscapeData(BackgroundDrawHandle drawHandle, LWJGLDrawContext context) {

            // 1) Bind the landscape texture to texture units 0 and 1.
            //    Legacy drawBackground always bound the same texture twice.
            int textureId = 0;
            if (drawHandle.texture != null) {
                textureId = drawHandle.texture.getTextureId();
            }

            GL33C.glActiveTexture(GL33C.GL_TEXTURE0);
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, textureId);
            GL33C.glActiveTexture(GL33C.GL_TEXTURE1);
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, textureId);

            // 2) Activate the background shader created by LWJGLDrawContext's constructor.
            GL33C.glUseProgram(context.getBackgroundShaderId());

            // 3) Bind geometry.
            // Normal path: VAO already has attrib layout from createBackgroundDrawCall.
            // Fallback path: no VAO, so set attrib pointers every draw.
            int vaoId = drawHandle.getVertexArrayId();
            if (vaoId != -1) {
                GL33C.glBindVertexArray(vaoId);
            }

            else {
                GL33C.glEnableVertexAttribArray(0);
                GL33C.glEnableVertexAttribArray(1);
                GL33C.glEnableVertexAttribArray(2);
                GL33C.glDisableVertexAttribArray(3);

                int vboId = 0;
                if (drawHandle.vertices != null) {
                    vboId = drawHandle.vertices.getBufferId();
                }

                GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, vboId);
                // Vertex layout: x, y, height, u, v, color  (6 floats)
                GL33C.glVertexAttribPointer(0, 3, GL33C.GL_FLOAT, false, 6 * 4, 0);
                GL33C.glVertexAttribPointer(1, 2, GL33C.GL_FLOAT, false, 6 * 4, 3 * 4);
                GL33C.glVertexAttribPointer(2, 1, GL33C.GL_FLOAT, false, 6 * 4, 5 * 4);
            }

            // 4) Convert interleaved [first, count, first, count, ...] into the two
            // arrays glMultiDrawArrays expects.
            int lineCount = drawHandle.visibleLineCount;
            int[] lineVertexOffsetList = new int[lineCount];
            int[] lineVertexCount = new int[lineCount];

            for (int index = 0; index < lineCount; index++) {
                lineVertexOffsetList[index] = drawHandle.visibleLineObjectList[index * 2];
                lineVertexCount[index] = drawHandle.visibleLineObjectList[index * 2 + 1];
            }

            // 5) Draw all visible terrain lines in one multi-draw call.
            GL33C.glMultiDrawArrays(GL33C.GL_TRIANGLES, lineVertexOffsetList, lineVertexCount);
            context.invalidateDrawState();
            return;
        }


        public static void drawLandscape(Application application, LWJGLDrawContext context, MapContent map) {

            FloatRectangle screen = map.mapContext.getScreen().getPosition().bigger(MapContent.SCREEN_PADDING);
            // map.background.drawMapContent(map.mapContext, screen);
            Background background = map.background;
            GLDrawContext glContext = map.mapContext.getGl();

            // todo: migrate Background.generateGeometry and Background.updateGeometry to RenderingSystem

            try {
                if (background.backgroundHandle == null || background.backgroundHandle.isValid() == false) {
                    background.generateGeometry(map.mapContext);
                    glContext.setHeightMatrix(map.mapContext.getConverter().getMatrixWithHeight());
                }
            }

            catch (IllegalBufferException exception) {
                exception.printStackTrace();
            }

            MapRectangle visibleMapSection = map.mapContext.getConverter().getMapForScreen(screen);
            background.updateGeometry(map.mapContext, visibleMapSection);
            background.backgroundHandle.texture = Background.getTextureData(glContext);
            background.backgroundHandle.visibleLineCount = visibleMapSection.getLines();
            background.backgroundHandle.visibleLineObjectList = new int[background.backgroundHandle.visibleLineCount * 2];

            for (int index = 0; index < background.backgroundHandle.visibleLineCount; index++) {

                int startX = visibleMapSection.getLineStartX(index);
                if (startX < 0) {
                    startX = 0;
                }

                int endX = visibleMapSection.getLineEndX(index);
                if (endX >= background.bufferWidth) {
                    endX = background.bufferWidth;
                }

                int lineY = visibleMapSection.getLineY(index);
                if (lineY < 0 || lineY > background.bufferHeight) {
                    continue;
                }

                background.backgroundHandle.visibleLineObjectList[index * 2] = (background.bufferWidth * lineY + startX) * 2 * 3;
                background.backgroundHandle.visibleLineObjectList[index * 2 + 1] = (endX - startX) * 2 * 3;
                continue;
            }

            // glContext.drawBackground(background.backgroundHandle);
            GameRenderer.renderLandscapeData(background.backgroundHandle, context);
            return;
        }


        public static void frameTeardownLegacy(Application application, LWJGLDrawContext context, MapContent map) {
            map.drawContent(context, application.canvas.width, application.canvas.height);
            return;
        }


        public static void drawGameScene(long frameDuration, Application application, Camera camera, LWJGLDrawContext context, MapContent map) {

            /*
            drawGameScene needs to follow the following structure
            - frame setup? (do we even need frame setup)
            - set camera position
            - draw terrain
            - draw static sprites
            - draw animated sprites
            - draw settlers
            - frame teardown? (legacy)
            */

            // update view matrix
            camera.updateCameraPosition(frameDuration);
            application.canvas.updateViewMatrix(camera);  // view matrix should only change when camera moves

            // render game scene
            map.mapContext.getScreen().setScreenCenter(-camera.offsetX, -camera.offsetY);

            GameRenderer.frameSetupLegacy(application, context, map);

            GameRenderer.drawLandscape(application, context, map);
            // draw static sprites
            // draw animated sprites
            // draw settlers units

            GameRenderer.frameTeardownLegacy(application, context, map);

            return;
        }
    }


    static class UserInterfaceRenderer {

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

            UserInterfaceRenderer.drawDebugMenu(application, userInterface);
            UserInterfaceRenderer.drawForeground(application, userInterface);

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
        // SettlersMap gameMap,
        LWJGLDrawContext context,
        MapContent jsettlersMap) {

        // activate canvas framebuffer
        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, application.canvas.framebufferId);
        GL33C.glClearColor(1.00f, 1.00f, 1.00f, 1.00f);
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        GL33C.glViewport(0, 0, 800, 600);

        GameRenderer.drawGameScene(frameDuration, application, camera, context, jsettlersMap);
        UserInterfaceRenderer.drawUI(application, userInterface);

        Rectangle viewport = application.viewport;

        // activate screen buffer
        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GL33C.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        GL33C.glClearColor(1.00f, 0.00f, 1.00f, 1.00f);  // magenta
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // draw canvas to screen with aspect ratio
        GL33C.glDisable(GL_DEPTH_TEST);
        GL33C.glUseProgram(application.renderer.screenShader.id);
        GL33C.glActiveTexture(GL_TEXTURE0);
        GL33C.glBindTexture(GL_TEXTURE_2D, application.canvas.textureId);
        GL33C.glBindVertexArray(application.renderer.viewportVAOId);
        GL33C.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        GL33C.glEnable(GL_DEPTH_TEST);

        // present frame
        GLFW.glfwSwapBuffers(application.window.handle);

        return;
    }
}