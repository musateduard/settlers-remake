package org.example.mainwindow;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImDrawList;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Queue;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33C;
import org.example.shaders.ScreenShader;
import org.example.shaders.LandscapeShader;
import go.graphics.swing.opengl.LWJGLDrawContext;
import jsettlers.graphics.map.MapContent;
import jsettlers.graphics.map.draw.DrawConstants;
import jsettlers.common.CommonConstants;
import jsettlers.common.map.IDirectGridProvider;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.position.FloatRectangle;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.opengl.GL33C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL33C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL33C.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL33C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL33C.GL_FRAMEBUFFER;


public class RenderingSystem {

    static class LandscapeRenderer {

        /**
         * this function updates all terrain mesh triangles based on the event queue filled by the game thread.
         */
        public static void updateLandscapeMesh(
            LandscapeTexture landscape,
            Queue<LandscapeEvent> events,
            IGraphicsGrid mapGrid) {

            for (LandscapeEvent event : events) {

                if (event instanceof FogOfWarEnabledChanged fow) {
                    landscape.fowEnabled = landscape.hasDirectGridProvider && fow.enabled();
                    continue;
                }

                if (event instanceof BackgroundLineChanged line) {

                    int offsetX = line.offsetX();
                    int offsetY = line.offsetY();

                    if (offsetY == landscape.bufferHeight) {
                        continue;
                    }

                    int x2 = offsetX + line.length();
                    if (offsetX != 0) {
                        offsetX = offsetX - 1;
                    }

                    if (x2 < landscape.bufferWidth) {
                        x2 = x2 + 1;
                    }

                    if (x2 > landscape.bufferWidth) {
                        x2 = landscape.bufferWidth;
                    }

                    LandscapeTexture.uploadLineSpan(landscape, mapGrid, offsetY, offsetX, x2);

                    if (offsetY > 0) {
                        LandscapeTexture.uploadLineSpan(landscape, mapGrid, offsetY - 1, offsetX, x2);
                    }

                    if (offsetY < landscape.bufferHeight - 1) {
                        LandscapeTexture.uploadLineSpan(landscape, mapGrid, offsetY + 1, offsetX, x2);
                    }
                }
            }

            return;
        }


        /**
         * this function calculates the culling for all visible terrain lines on the screen.
         */
        private static void calculateVisibleLines(
            Application application,
            LandscapeTexture landscape,
            Camera camera) {

            float centerX = -camera.offsetX;
            float centerY = -camera.offsetY;
            float halfWidth = application.canvas.width / 2.00f;
            float halfHeight = application.canvas.height / 2.00f;
            float screenPadding = 50.00f;

            float screenMinX = centerX - halfWidth - screenPadding;
            float screenMinY = centerY - halfHeight - screenPadding;
            float screenMaxX = centerX + halfWidth + screenPadding;
            float screenMaxY = centerY + halfHeight + screenPadding;

            float scaleX = DrawConstants.DISTANCE_X;
            float scaleY = DrawConstants.DISTANCE_Y;
            float realMapHeight = landscape.mapHeight - 1;
            float heightDisplacementY = 2.00f;
            float maxMountainHeight = heightDisplacementY * Byte.MAX_VALUE;

            // Port of MapCoordinateConverter.getMapForScreen (inverse height matrix).
            int mapMinX = (int) (screenMinX / scaleX + screenMaxY * (-0.50f / scaleY));
            int mapMaxX = (int) (screenMaxX / scaleX + screenMinY * (-0.50f / scaleY) + maxMountainHeight / scaleY);
            int mapMinY = (int) (screenMaxY * (-1.00f / scaleY) + realMapHeight);
            int mapMaxY = (int) (screenMinY * (-1.00f / scaleY) + maxMountainHeight * (2.00f / scaleY) + realMapHeight);

            int mapWidth = mapMaxX - mapMinX;
            int mapHeight = mapMaxY - mapMinY;
            if (mapWidth < 0) {
                mapWidth = 0;
            }
            if (mapHeight < 0) {
                mapHeight = 0;
            }

            landscape.visibleLineCount = mapHeight;
            if (landscape.visibleLineCount > landscape.lineVertexOffsetList.capacity()) {

                landscape.lineVertexOffsetList = MemoryUtil.memRealloc(
                    landscape.lineVertexOffsetList,
                    landscape.visibleLineCount
                );

                landscape.lineVertexCount = MemoryUtil.memRealloc(
                    landscape.lineVertexCount,
                    landscape.visibleLineCount
                );
            }

            landscape.lineVertexOffsetList.clear();
            landscape.lineVertexCount.clear();

            for (int index = 0; index < landscape.visibleLineCount; index++) {

                int lineY = mapMinY + index;
                if (lineY < 0 || lineY > landscape.bufferHeight) {
                    landscape.lineVertexOffsetList.put(index, 0);
                    landscape.lineVertexCount.put(index, 0);
                    continue;
                }

                // Port of MapRectangle line stagger: startX shifts by line/2.
                int startX = mapMinX + (index / 2);
                if (startX < 0) {
                    startX = 0;
                }

                int endX = startX + mapWidth - 1;
                if (endX >= landscape.bufferWidth) {
                    endX = landscape.bufferWidth;
                }

                if (endX < startX) {
                    landscape.lineVertexOffsetList.put(index, 0);
                    landscape.lineVertexCount.put(index, 0);
                    continue;
                }

                landscape.lineVertexOffsetList.put(index, (landscape.bufferWidth * lineY + startX) * 2 * 3);
                landscape.lineVertexCount.put(index, (endX - startX) * 2 * 3);
            }

            return;
        }


        public static void drawLandscape(
            Application application,
            Camera camera,
            IGraphicsGrid mapGrid,
            LandscapeTexture landscape,
            LandscapeEventBus eventBus) {

            Queue<LandscapeEvent> events = eventBus.drainEventQueue();
            LandscapeShader shader = landscape.shader;

            LandscapeRenderer.updateLandscapeMesh(landscape, events, mapGrid);
            LandscapeRenderer.calculateVisibleLines(application, landscape, camera);

            // 1) Bind the landscape texture to texture units 0 and 1.
            // Legacy drawBackground always bound the same texture twice.
            GL33C.glActiveTexture(GL33C.GL_TEXTURE0);
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, landscape.atlas.id);
            GL33C.glActiveTexture(GL33C.GL_TEXTURE1);
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, landscape.atlas.id);

            // activate landscape shader
            shader.activate();

            // update mvp matrix
            GL33C.glUniform1i(shader.textureHandleUniform, 0);

            // update projection matrix
            shader.projectionMatrix.identity();
            shader.projectionMatrix.ortho(
                0.00f, (float) application.canvas.width,
                0.00f, (float) application.canvas.height,
                -1.00f, 1.00f
            );

            shader.projectionMatrix.get(shader.buffer);
            GL33C.glUniformMatrix4fv(shader.projectionMatrixUniform, false, shader.buffer);  // this needs to update only on screen resize

            // update view matrix
            shader.viewMatrix.identity();
            shader.viewMatrix.translate(
                camera.offsetX + (application.canvas.width / 2.00f),
                camera.offsetY + (application.canvas.height / 2.00f),
                0.00f
            );

            shader.viewMatrix.get(shader.buffer);
            GL33C.glUniformMatrix4fv(shader.viewMatrixUniform, false, shader.buffer);

            // update height matrix
            GL33C.glUniformMatrix4fv(shader.heightUniform, false, shader.heightMatrix);

            // 3) Bind geometry.
            GL33C.glBindVertexArray(landscape.mesh.vaoId);

            // 4) Draw all visible terrain lines in one multi-draw call.
            landscape.lineVertexOffsetList.limit(landscape.visibleLineCount).position(0);  // resize to line count
            landscape.lineVertexCount.limit(landscape.visibleLineCount).position(0);

            GL33C.glMultiDrawArrays(
                GL33C.GL_TRIANGLES,
                landscape.lineVertexOffsetList,
                landscape.lineVertexCount
            );

            return;
        }
    }


    static class GameRenderer {

        /*
        public static void renderTestScene(Application application, SettlersMap gameMap) {

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

            // upload model matrix
            Matrix4f modelMatrix = new Matrix4f();
            modelMatrix.translate(50, 50, 0);
            modelMatrix.scale(100, 100, 1);
            modelMatrix.get(application.canvas.shader.buffer);

            GL33C.glUniformMatrix4fv(application.canvas.shader.modelMatrixAddress, false, application.canvas.shader.buffer);

            // set color uniform
            GL33C.glUniform4f(application.canvas.shader.colorUniformAddress, 0.00f, 1.00f, 1.00f, 1.00f);

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
        */


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


        public static void frameTeardownLegacy(LWJGLDrawContext context, MapContent map) {

            // long startTime = System.nanoTime();

            FloatRectangle screen = map.mapContext.getScreen().getPosition().bigger(MapContent.SCREEN_PADDING);
            // map.drawMapTerrain(screen);
            // long backgroundDuration = System.nanoTime() - startTime;

            // startTime = System.nanoTime();
            map.drawMapObjects(screen);

            if (map.scrollMarker != null) {
                map.drawGotoMarker();
            }

            if (map.moveToMarker != null) {
                map.drawMoveToMarker();
            }

            map.mapContext.end();
            // long foregroundDuration = System.nanoTime() - startTime;

            // startTime = System.nanoTime();
            context.clearDepthBuffer();
            context.updateViewMatrix(0.00f, 0.00f, 0.00f, 1.00f, 1.00f, 1.00f);

            map.drawSelectionHint(context);
            map.controls.drawAt(context);
            map.drawMessages(context);
            map.drawWinStateMsg(context);
            map.drawFramerateTimeAndHash(context);

            if (map.actionThreadIsSlow) {
                map.drawActionThreadSlow(context);
            }

            map.drawTooltip(context);
            // long uiTime = System.nanoTime() - startTime;

			if (CommonConstants.ENABLE_GRAPHICS_TIMES_DEBUG_OUTPUT) {
				// System.out.println("Background: " + backgroundDuration / 1000 + "µs, Foreground: " + foregroundDuration / 1000 + "µs, UI: " + uiTime / 1000 + "µs");
			}

            return;
        }


        public static void drawGameScene(
            long frameDuration,
            Application application,
            Camera camera,
            LandscapeTexture landscape,
            LandscapeEventBus eventBus,
            LWJGLDrawContext context,
            MapContent map) {

            // todo: use IGraphicsGrid as single source of truth for game state

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
            camera.updateCameraPosition(
                frameDuration,
                (float) application.canvas.width,
                (float) application.canvas.height,
                (float) application.viewport.width,
                (float) application.viewport.height
            );

            application.canvas.shader.updateViewMatrix(camera);  // view matrix should only change when camera moves

            // render game scene
            map.mapContext.getScreen().setScreenCenter(-camera.offsetX, -camera.offsetY);

            GameRenderer.frameSetupLegacy(application, context, map);

            LandscapeRenderer.drawLandscape(application, camera, map.map, landscape, eventBus);
            // draw static sprites
            // draw animated sprites
            // draw settlers units

            context.invalidateDrawState();  // this forces the LWJGLDrawContext managed variables to update their state
            GameRenderer.frameTeardownLegacy(context, map);

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
        LandscapeTexture landscape,
        LandscapeEventBus eventBus,
        LWJGLDrawContext context,
        MapContent settlersMap) {

        // activate canvas framebuffer
        RenderingSystem.activateCanvasBuffer(application.canvas);

        GameRenderer.drawGameScene(frameDuration, application, camera, landscape, eventBus, context, settlersMap);
        UserInterfaceRenderer.drawUI(application, userInterface);

        // activate screen buffer
        RenderingSystem.activateScreenBuffer(application.viewport);

        // draw canvas to screen with aspect ratio
        RenderingSystem.renderCanvasToScreen(application.screenShader, application.canvas, application.viewportBuffer);

        // present frame
        GLFW.glfwSwapBuffers(application.window.handle);

        return;
    }


    public static void activateScreenBuffer(Rectangle viewport) {

        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GL33C.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        GL33C.glClearColor(1.00f, 0.00f, 1.00f, 1.00f);  // magenta
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        return;
    }


    public static void activateCanvasBuffer(Framebuffer canvas) {

        GL33C.glBindFramebuffer(GL_FRAMEBUFFER, canvas.framebufferId);
        GL33C.glViewport(0, 0, canvas.width, canvas.height);
        GL33C.glClearColor(1.00f, 1.00f, 1.00f, 1.00f);
        GL33C.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        return;
    }


    public static void renderCanvasToScreen(ScreenShader shader, Framebuffer canvas, VertexBuffer viewport) {

        GL33C.glDisable(GL_DEPTH_TEST);
        GL33C.glUseProgram(shader.id);
        GL33C.glActiveTexture(GL_TEXTURE0);
        GL33C.glBindTexture(GL_TEXTURE_2D, canvas.textureId);
        GL33C.glBindVertexArray(viewport.vaoId);
        GL33C.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        GL33C.glEnable(GL_DEPTH_TEST);

        return;
    }
}