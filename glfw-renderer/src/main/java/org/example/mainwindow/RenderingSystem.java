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
import org.example.shaders.SpriteShader;
import go.graphics.swing.opengl.LWJGLDrawContext;
import jsettlers.graphics.map.MapContent;
import jsettlers.graphics.map.draw.DrawConstants;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.common.CommonConstants;
import jsettlers.common.images.EImageLinkType;
import jsettlers.common.map.IDirectGridProvider;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.mapobject.IMapObject;
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
            LandscapeLayer landscape,
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

                    landscape.uploadLineSpan(mapGrid, offsetY, offsetX, x2);

                    if (offsetY > 0) {
                        landscape.uploadLineSpan(mapGrid, offsetY - 1, offsetX, x2);
                    }

                    if (offsetY < landscape.bufferHeight - 1) {
                        landscape.uploadLineSpan(mapGrid, offsetY + 1, offsetX, x2);
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
            LandscapeLayer landscape,
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
            AssetManager assetManager,
            Camera camera,
            IGraphicsGrid mapGrid,
            LandscapeLayer landscape,
            LandscapeEventBus eventBus) {

            Queue<LandscapeEvent> events = eventBus.drainEventQueue();
            LandscapeShader shader = landscape.shader;

            LandscapeRenderer.updateLandscapeMesh(landscape, events, mapGrid);
            LandscapeRenderer.calculateVisibleLines(application, landscape, camera);

            // 1) Bind the landscape texture to texture units 0 and 1.
            // Legacy drawBackground always bound the same texture twice.
            GL33C.glActiveTexture(GL33C.GL_TEXTURE0);
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, assetManager.landscapeAtlas.id);
            GL33C.glActiveTexture(GL33C.GL_TEXTURE1);
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, assetManager.landscapeAtlas.id);

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


    static class AnimationSystem {

        public static void resolveSpriteFrames(DrawRequestArena requestArena) {

            requestArena.clearRequestCount();

            // todo: finish implementing resolveSpriteFrames

            return;
        }
    }


    static class SpriteRenderer {

        public static void drawSprites(
            Application application,
            Camera camera,
            SpriteLayer sprites,
            AssetManager assets,
            IGraphicsGrid grid) {

            int screenPadding = 50;
            int tallSpriteOverdraw = 50;
            float heightYDisplacement = 2.00f;

            float centerX = -camera.offsetX;
            float centerY = -camera.offsetY;
            float halfWidth = application.canvas.width / 2.00f;
            float halfHeight = application.canvas.height / 2.00f;

            float screenMinX = centerX - halfWidth - screenPadding;
            float screenMinY = centerY - halfHeight - screenPadding;
            float screenMaxX = centerX + halfWidth + screenPadding;
            float screenMaxY = centerY + halfHeight + screenPadding;

            float scaleX = DrawConstants.DISTANCE_X;
            float scaleY = DrawConstants.DISTANCE_Y;
            int mapHeight = grid.getHeight();
            int mapWidth = grid.getWidth();
            float realMapHeight = mapHeight - 1;
            float maxMountainHeight = heightYDisplacement * Byte.MAX_VALUE;

            int mapMinX = (int) (screenMinX / scaleX + screenMaxY * (-0.50f / scaleY));
            int mapMaxX = (int) (screenMaxX / scaleX + screenMinY * (-0.50f / scaleY) + maxMountainHeight / scaleY);
            int mapMinY = (int) (screenMaxY * (-1.00f / scaleY) + realMapHeight);
            int mapMaxY = (int) (screenMinY * (-1.00f / scaleY) + maxMountainHeight * (2.00f / scaleY) + realMapHeight);

            int visibleWidth = mapMaxX - mapMinX;
            int visibleHeight = mapMaxY - mapMinY;
            if (visibleWidth < 0) {
                visibleWidth = 0;
            }
            if (visibleHeight < 0) {
                visibleHeight = 0;
            }

            float zPerY = 1.00f / (mapHeight * 100.00f);

            SpriteShader shader = sprites.shader;
            shader.activate();

            shader.projectionMatrix.identity();
            shader.projectionMatrix.ortho(
                0.00f, (float) application.canvas.width,
                0.00f, (float) application.canvas.height,
                -1.00f, 1.00f
            );
            shader.projectionMatrix.get(shader.buffer);
            GL33C.glUniformMatrix4fv(shader.projectionMatrixUniform, false, shader.buffer);

            shader.viewMatrix.identity();
            shader.viewMatrix.translate(
                camera.offsetX + (application.canvas.width / 2.00f),
                camera.offsetY + (application.canvas.height / 2.00f),
                0.00f
            );
            shader.viewMatrix.get(shader.buffer);
            GL33C.glUniformMatrix4fv(shader.viewMatrixUniform, false, shader.buffer);

            GL33C.glUniform1i(shader.textureUniform, 0);
            GL33C.glActiveTexture(GL_TEXTURE0);

            GL33C.glEnable(GL33C.GL_BLEND);
            GL33C.glBlendFunc(GL33C.GL_SRC_ALPHA, GL33C.GL_ONE_MINUS_SRC_ALPHA);
            GL33C.glDisable(GL_DEPTH_TEST);

            GL33C.glBindVertexArray(sprites.quad.vaoId);

            int lineCount = visibleHeight + tallSpriteOverdraw;
            for (int line = 0; line < lineCount; line++) {
                int tileY = mapMinY + line;
                if (tileY < 0 || tileY >= mapHeight) {
                    continue;
                }

                int startX = mapMinX + (line / 2);
                int endX = startX + visibleWidth;
                if (startX < 0) {
                    startX = 0;
                }
                if (endX > mapWidth) {
                    endX = mapWidth;
                }

                for (int tileX = startX; tileX < endX; tileX++) {
                    byte fogStatus = grid.getVisibleStatus(tileX, tileY);
                    if (fogStatus == 0) {
                        continue;
                    }

                    IMapObject object = grid.getVisibleMapObjectsAt(tileX, tileY);
                    while (object != null) {

                        SpriteDrawRequest request = resolveMapObject(
                            assets,
                            grid,
                            tileX,
                            tileY,
                            object,
                            fogStatus,
                            screenMinX,
                            screenMinY,
                            screenMaxX,
                            screenMaxY,
                            zPerY
                        );

                        if (request != null) {
                            drawSingleSprite(sprites, request);
                        }

                        object = object.getNextObject();
                        continue;
                    }
                }
            }

            GL33C.glBindVertexArray(0);
            GL33C.glBindTexture(GL_TEXTURE_2D, 0);
            GL33C.glDisable(GL33C.GL_BLEND);
            GL33C.glEnable(GL_DEPTH_TEST);
            return;
        }


        static SpriteDrawRequest resolveMapObject(
            AssetManager assets,
            IGraphicsGrid grid,
            int tileX,
            int tileY,
            IMapObject object,
            byte fogStatus,
            float screenMinX,
            float screenMinY,
            float screenMaxX,
            float screenMaxY,
            float zPerY) {

            int objectsFile = 1;
            int animalsFile = 6;
            int stoneSequence = 31;
            int decorationSequence = 27;
            int fishSequence = 7;
            int[] treeSequences = { 1, 2, 4, 7, 8, 16, 17 };

            int file;
            int sequence;
            int frame;

            switch (object.getObjectType()) {

                case STONE: {

                    file = objectsFile;
                    sequence = stoneSequence;
                    int availableStones = (int) object.getStateProgress();
                    int seqLength = ImageProvider.getInstance().getSettlerSequence(file, sequence).length();
                    frame = seqLength - availableStones - 1;

                    if (frame < 0) {
                        frame = 0;
                    }

                    if (frame >= seqLength) {
                        frame = Math.max(0, seqLength - 1);
                    }

                    break;
                }

                case CUT_OFF_STONE: {
                    file = objectsFile;
                    sequence = stoneSequence;
                    int seqLength = ImageProvider.getInstance().getSettlerSequence(file, sequence).length();
                    frame = Math.max(0, seqLength - 1);
                    break;
                }

                case PLANT_DECORATION: {
                    file = objectsFile;
                    sequence = decorationSequence;
                    frame = (tileX * 13 + tileY * 233) % 8;
                    break;
                }

                case DESERT_DECORATION: {
                    file = objectsFile;
                    sequence = decorationSequence;
                    frame = (tileX * 13 + tileY * 233) % 5 + 10;
                    break;
                }

                case SWAMP_DECORATION: {
                    file = objectsFile;
                    sequence = decorationSequence;
                    frame = (tileX * 13 + tileY * 233) % 6 + 27;
                    break;
                }

                case TREE_ADULT: {
                    file = objectsFile;
                    int treeTypes = 7;
                    int treeType = (tileX + tileX / 5 + tileY / 3 + tileY + tileY / 7) % treeTypes;
                    sequence = treeSequences[treeType];
                    frame = 0;
                    break;
                }

                case FISH_DECORATION: {
                    file = animalsFile;
                    sequence = fishSequence;
                    frame = 0;
                    break;
                }

                default: {
                    return null;
                }
            }

            AssetLocator locator = new AssetLocator(
                file,
                EImageLinkType.SETTLER.ordinal(),
                sequence,
                Math.max(0, frame)
            );

            Texture texture = assets.getOrCreateTexture(locator);
            if (texture == null) {
                return null;
            }

            float fow = fogStatus / (float) CommonConstants.FOG_OF_WAR_VISIBLE;

            int height = grid.getVisibleHeightAt(tileX, tileY) & 0xFF;
            int mapHeight = grid.getHeight();
            float realMapHeight = mapHeight - 1;
            float scaleX = DrawConstants.DISTANCE_X;
            float scaleY = DrawConstants.DISTANCE_Y;
            float heightYDisplacement = 2.00f;

            // Port of MapCoordinateConverter.getViewX / getViewY with DrawConstants tile spacing.
            float viewX = tileX * scaleX + tileY * (-0.50f * scaleX) + realMapHeight * scaleX * 0.50f;
            float viewY = tileY * (-scaleY) + height * heightYDisplacement + realMapHeight * scaleY;
            float z = tileY * zPerY;

            // Legacy quad: left=offsetX, top=-offsetY, right=offsetX+w, bottom=-offsetY-h
            // todo: use Sprite instead of Texture; textures don't have offsetX
            float offsetX = texture.offsetX;
            float offsetY = texture.offsetY;
            float fullWidth = texture.width;
            float fullHeight = texture.height;

            float left = viewX + offsetX;
            float top = viewY - offsetY;
            float right = left + fullWidth;
            float bottom = top - fullHeight;

            float clipLeft = Math.max(left, screenMinX);
            float clipRight = Math.min(right, screenMaxX);
            float clipBottom = Math.max(bottom, screenMinY);
            float clipTop = Math.min(top, screenMaxY);

            if (clipLeft >= clipRight || clipBottom >= clipTop) {
                return null;
            }

            float u0 = (clipLeft - left) / fullWidth;
            float u1 = (clipRight - left) / fullWidth;
            // Texture is uploaded flipped (GL v=0 = image bottom); map clip from top into GL V.
            float v0 = 1.00f - (top - clipTop) / fullHeight;
            float v1 = 1.00f - (top - clipBottom) / fullHeight;

            return new SpriteDrawRequest(
                texture,
                clipLeft,
                clipTop,
                z,
                clipRight - clipLeft,
                clipTop - clipBottom,
                fow,
                u0,
                v0,
                u1,
                v1
            );
        }


        static void drawSingleSprite(SpriteLayer sprites, SpriteDrawRequest request) {
            GL33C.glBindTexture(GL_TEXTURE_2D, request.texture().id);

            SpriteShader shader = sprites.shader;
            shader.modelMatrix.identity();
            shader.modelMatrix.translate(request.x(), request.y(), request.z());
            shader.modelMatrix.scale(request.width(), request.height(), 1.00f);
            shader.modelMatrix.get(shader.buffer);
            GL33C.glUniformMatrix4fv(shader.modelMatrixUniform, false, shader.buffer);

            GL33C.glUniform1f(shader.fowUniform, request.fow());
            GL33C.glUniform4f(shader.uvRectUniform, request.u0(), request.v0(), request.u1(), request.v1());

            GL33C.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
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
            // map.drawMapObjects(screen);

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
            AssetManager assetManager,
            Camera camera,
            LandscapeLayer landscape,
            SpriteLayer sprites,
            DrawRequestArena requestArena,
            AssetManager assets,
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

            LandscapeRenderer.drawLandscape(application, assetManager, camera, map.map, landscape, eventBus);
            AnimationSystem.resolveSpriteFrames(requestArena);
            SpriteRenderer.drawSprites(application, camera, sprites, assets, map.map);

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
        LandscapeLayer landscape,
        SpriteLayer sprites,
        DrawRequestArena requestArena,
        AssetManager assets,
        LandscapeEventBus eventBus,
        LWJGLDrawContext context,
        MapContent settlersMap) {

        // activate canvas framebuffer
        RenderingSystem.activateCanvasBuffer(application.canvas);

        GameRenderer.drawGameScene(
            frameDuration,
            application,
            assets,
            camera,
            landscape,
            sprites,
            requestArena,
            assets,
            eventBus,
            context,
            settlersMap
        );

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