package org.example.mainwindow;

import jsettlers.common.CommonConstants;
import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.map.IDirectGridProvider;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.map.shapes.MapRectangle;
import jsettlers.graphics.map.draw.DrawConstants;
import jsettlers.graphics.map.draw.ETextureOrientation;
import org.lwjgl.opengl.GL33C;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


/**
 * Builds and patches the landscape mesh on the render thread from {@link IGraphicsGrid} data.
 */
public class LandscapeMeshUpdater {

    private final LandscapeTexture landscape;
    private final ByteBuffer vertexBuffer;
    private final DirtyRegionBufferCache vertexCache;
    private boolean fowEnabled;
    private final boolean hasDGP;


    private record TextureIntersections(
        ELandscapeType type1,
        ELandscapeType type1alt,
        ELandscapeType type2,
        int baseIndex) {

        public TextureIntersections(ELandscapeType type1, ELandscapeType type2, int baseIndex) {
            this(type1, type1, type2, baseIndex);
        }
    }


    private final TextureIntersections[] borderTextures = new TextureIntersections[] {
        new TextureIntersections(ELandscapeType.SAND, ELandscapeType.WATER1, 37),

        // TODO find use for 41
        // TODO find use for 45
        // TODO find use for 49-51 textures

        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.SAND, ELandscapeType.RIVER1, 52),
        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.SAND, ELandscapeType.RIVER2, 56),
        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.SAND, ELandscapeType.RIVER3, 60),
        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.SAND, ELandscapeType.RIVER4, 64),

        new TextureIntersections(ELandscapeType.SAND, ELandscapeType.RIVER1, 68),
        new TextureIntersections(ELandscapeType.SAND, ELandscapeType.RIVER2, 72),
        new TextureIntersections(ELandscapeType.SAND, ELandscapeType.RIVER3, 76),
        new TextureIntersections(ELandscapeType.SAND, ELandscapeType.RIVER4, 80),

        new TextureIntersections(ELandscapeType.WATER1, ELandscapeType.WATER2, 84),
        new TextureIntersections(ELandscapeType.WATER2, ELandscapeType.WATER3, 88),
        new TextureIntersections(ELandscapeType.WATER3, ELandscapeType.WATER4, 92),
        new TextureIntersections(ELandscapeType.WATER4, ELandscapeType.WATER5, 96),
        new TextureIntersections(ELandscapeType.WATER5, ELandscapeType.WATER6, 100),
        new TextureIntersections(ELandscapeType.WATER6, ELandscapeType.WATER7, 104),
        new TextureIntersections(ELandscapeType.WATER7, ELandscapeType.WATER8, 108),

        new TextureIntersections(ELandscapeType.SAND, ELandscapeType.GRASS, 112),

        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.MOUNTAINBORDEROUTER, 116),
        new TextureIntersections(ELandscapeType.MOUNTAINBORDEROUTER, ELandscapeType.MOUNTAINBORDER, 120),
        new TextureIntersections(ELandscapeType.MOUNTAIN, ELandscapeType.MOUNTAINBORDER, 124),

        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.DESERTBORDEROUTER, 128),
        new TextureIntersections(ELandscapeType.DESERTBORDEROUTER, ELandscapeType.DESERTBORDER, 132),
        new TextureIntersections(ELandscapeType.DESERT, ELandscapeType.DESERTBORDER, 136),

        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.MUDBORDEROUTER, 140),
        new TextureIntersections(ELandscapeType.MUDBORDEROUTER, ELandscapeType.MUDBORDER, 144),
        new TextureIntersections(ELandscapeType.MUD, ELandscapeType.MUDBORDER, 148),

        // TODO find use for 152

        new TextureIntersections(ELandscapeType.MOUNTAIN, ELandscapeType.SNOWBORDEROUTER, 156),
        new TextureIntersections(ELandscapeType.SNOWBORDEROUTER, ELandscapeType.SNOWBORDER, 160),
        new TextureIntersections(ELandscapeType.SNOW, ELandscapeType.SNOWBORDER, 164),

        // some original maps have this
        new TextureIntersections(ELandscapeType.MOUNTAIN, ELandscapeType.SNOW, 156),

        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.EARTH, 168),
        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.FLATTENED, 172),

        // TODO find use for 176 landscape
        // TODO find use for 177 border
        // 181 is a duplicate of 172

        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.ROAD, 185),

        // 189 is another duplicate of 172
        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.DRY_GRASS, 193),
        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.DRY_EARTH, 197),

        new TextureIntersections(ELandscapeType.GRASS, ELandscapeType.MOORBORDEROUTER, 201),
        new TextureIntersections(ELandscapeType.MOORBORDEROUTER, ELandscapeType.MOORBORDER, 205),
        new TextureIntersections(ELandscapeType.MOOR, ELandscapeType.MOORBORDER, 209),

        // TODO find use for 213
        new TextureIntersections(ELandscapeType.DESERT, ELandscapeType.SHARP_FLATTENED_DESERT, 218),
        new TextureIntersections(ELandscapeType.DESERT, ELandscapeType.FLATTENED_DESERT, 222),
        // TODO find use for 226
        new TextureIntersections(ELandscapeType.MOUNTAIN, ELandscapeType.GRAVEL, 231),
    };


    public LandscapeMeshUpdater(LandscapeTexture landscape, IGraphicsGrid map) {
        this.landscape = landscape;
        this.hasDGP = map instanceof IDirectGridProvider;
        this.fowEnabled = this.hasDGP && ((IDirectGridProvider) map).isFoWEnabled();

        this.vertexBuffer = ByteBuffer
            .allocateDirect(LandscapeTexture.BYTES_PER_FIELD * landscape.bufferHeight * landscape.bufferWidth)
            .order(ByteOrder.nativeOrder());
        this.vertexCache = new DirtyRegionBufferCache(
            () -> this.vertexBuffer,
            LandscapeTexture.BYTES_PER_FIELD,
            () -> this.landscape.mesh,
            landscape.bufferWidth
        );
        return;
    }


    public void applyFogOfWarEvents(List<LandscapeEventBus.LandscapeEvent> events) {
        for (LandscapeEventBus.LandscapeEvent event : events) {
            if (event instanceof LandscapeEventBus.FogOfWarEnabledChanged fow) {
                this.fowEnabled = this.hasDGP && fow.enabled();
            }
        }
        return;
    }


    public void applyBackgroundLineEvents(List<LandscapeEventBus.LandscapeEvent> events, IGraphicsGrid map) {
        for (LandscapeEventBus.LandscapeEvent event : events) {
            if (event instanceof LandscapeEventBus.BackgroundLineChanged line) {
                this.applyBackgroundLineChanged(map, line.x(), line.y(), line.length());
            }
        }
        return;
    }


    public void applyEvents(List<LandscapeEventBus.LandscapeEvent> events, IGraphicsGrid map) {
        this.applyFogOfWarEvents(events);
        this.applyBackgroundLineEvents(events, map);
        return;
    }


    public void generateTerrainMesh(IGraphicsGrid map) {
        int vertexCount = this.landscape.bufferWidth * this.landscape.bufferHeight * 3 * 2;
        this.landscape.atlas = LandscapeTexture.generateLandscapeAtlas(true);

        final int stride = 6 * 4;
        float[] meshVertices = new float[vertexCount * 6];
        List<VertexAttribute> attributes = List.of(
            new VertexAttribute(0, 3, GL33C.GL_FLOAT, false, stride, 0),
            new VertexAttribute(1, 2, GL33C.GL_FLOAT, false, stride, 3 * 4),
            new VertexAttribute(2, 1, GL33C.GL_FLOAT, false, stride, 5 * 4)
        );
        this.landscape.mesh = new VertexBuffer(meshVertices, attributes, GL33C.GL_DYNAMIC_DRAW);

        if (this.hasDGP) {
            this.fowEnabled = ((IDirectGridProvider) map).isFoWEnabled();
        }

        ByteBuffer bufferLine = ByteBuffer
            .allocateDirect(LandscapeTexture.BYTES_PER_FIELD * this.landscape.bufferWidth)
            .order(ByteOrder.nativeOrder());

        for (int offsetY = 0; offsetY < this.landscape.bufferHeight; offsetY += 1) {
            for (int offsetX = 0; offsetX < this.landscape.bufferWidth; offsetX += 1) {
                this.addTrianglesToGeometry(map, bufferLine, offsetX, offsetY);
            }
            bufferLine.rewind();
            this.landscape.mesh.updateVertexBuffer(
                (long) LandscapeTexture.BYTES_PER_FIELD * this.landscape.bufferWidth * offsetY,
                bufferLine
            );
        }
        return;
    }


    public void uploadVisibleDirtyRegions(MapRectangle screen) {
        int height = screen.getHeight();
        int width = screen.getWidth();
        int miny = screen.getMinY();
        int minx = screen.getMinX();
        int maxy = miny + height;

        if (maxy > this.landscape.bufferHeight) {
            maxy = this.landscape.bufferHeight;
        }
        if (miny < 0) {
            miny = 0;
        }
        int lineStart = minx - (miny / 2);

        for (int y = miny; y < maxy; y++) {
            int lineStartX = lineStart + (y / 2);

            int linewidth = (width + lineStartX);
            if (linewidth >= this.landscape.bufferWidth) {
                linewidth = this.landscape.bufferWidth;
            }

            int linex = lineStartX;
            if (linex < 0) {
                linex = 0;
            }

            this.vertexCache.clearCacheRegion(y, linex, linewidth);
        }
        return;
    }


    private void applyBackgroundLineChanged(IGraphicsGrid map, int x, int y, int length) {
        if (y == this.landscape.bufferHeight) {
            return;
        }

        int x2 = x + length;
        if (x != 0) {
            x = x - 1;
        }
        if (x2 < this.landscape.bufferWidth) {
            x2 = x2 + 1;
        }
        if (x2 > this.landscape.bufferWidth) {
            x2 = this.landscape.bufferWidth;
        }

        this.updateLine(map, y, x, x2);
        if (y > 0) {
            this.updateLine(map, y - 1, x, x2);
        }
        if (y < this.landscape.bufferHeight - 1) {
            this.updateLine(map, y + 1, x, x2);
        }
        return;
    }


    private void updateLine(IGraphicsGrid map, int y, int x1, int x2) {
        this.vertexBuffer.order(ByteOrder.nativeOrder());
        this.vertexBuffer.position((y * this.landscape.bufferWidth + x1) * LandscapeTexture.BYTES_PER_FIELD);
        for (int i = x1; i != x2; i++) {
            this.addTrianglesToGeometry(map, this.vertexBuffer, i, y);
        }
        this.vertexCache.markLine(y, x1, x2 - x1);
        return;
    }


    private void addTrianglesToGeometry(IGraphicsGrid map, ByteBuffer buffer, int x, int y) {
        this.addTriangleToGeometry(map, buffer, x, y, true, x * 37 + y * 17);
        this.addTriangleToGeometry(map, buffer, x, y, false, x);
        return;
    }


    private void addTriangleToGeometry(
        IGraphicsGrid map,
        ByteBuffer buffer,
        int x1,
        int y,
        boolean up,
        int useSecondParameter) {

        int y1 = y + (up ? 1 : 0);
        int x2 = x1 + (up ? 0 : 1);
        int y2 = y + (up ? 0 : 1);
        int x3 = x1 + 1;
        int y3 = y + (up ? 1 : 0);

        ELandscapeType leftLandscape = map.getVisibleLandscapeTypeAt(x1, y1);
        ELandscapeType aLandscape = map.getVisibleLandscapeTypeAt(x2, y2);
        ELandscapeType rightLandscape = map.getVisibleLandscapeTypeAt(x3, y3);

        float[] texturePos;
        int textureIndex;
        int orientationIndex = up ? 0 : 1;

        if (aLandscape == leftLandscape && aLandscape == rightLandscape) {
            textureIndex = aLandscape.getImageNumber();
            texturePos = ETextureOrientation.CONTINUOS[orientationIndex];
        } else {
            textureIndex = leftLandscape.getImageNumber();

            for (TextureIntersections intersect : this.borderTextures) {
                int type1count = 0;
                int type1acount = 0;
                int type2count = 0;

                if (leftLandscape == intersect.type1) {
                    type1count++;
                } else if (leftLandscape == intersect.type1alt) {
                    type1acount++;
                }

                if (aLandscape == intersect.type1) {
                    type1count++;
                } else if (aLandscape == intersect.type1alt) {
                    type1acount++;
                }

                if (rightLandscape == intersect.type1) {
                    type1count++;
                } else if (rightLandscape == intersect.type1alt) {
                    type1acount++;
                }

                if (leftLandscape == intersect.type2) {
                    type2count++;
                }
                if (aLandscape == intersect.type2) {
                    type2count++;
                }
                if (rightLandscape == intersect.type2) {
                    type2count++;
                }

                if (type1count + type1acount + type2count != 3 || type1acount == 2 || type2count == 0) {
                    continue;
                }

                textureIndex = intersect.baseIndex;
                textureIndex += (type2count == 2) ? 2 : 0;
                textureIndex += useSecondParameter & 1;
                break;
            }

            if (leftLandscape == rightLandscape) {
                texturePos = ETextureOrientation.ORIENTATION[orientationIndex];
            } else if (leftLandscape == aLandscape) {
                texturePos = ETextureOrientation.LEFT[orientationIndex];
            } else {
                texturePos = ETextureOrientation.RIGHT[orientationIndex];
            }
        }

        int[] positions = LandscapeTexture.TEXTURE_POSITIONS[textureIndex];
        int addDx = 0;
        int addDy = 0;

        if (positions[2] >= 2) {
            addDx = x1 * DrawConstants.DISTANCE_X - y * DrawConstants.DISTANCE_X / 2;
            addDy = y * DrawConstants.TEXTUREUNIT_Y;
            addDx = realModulo(addDx, (positions[2] - 1) * LandscapeTexture.TEXTURE_GRID);
            addDy = realModulo(addDy, (positions[2] - 1) * LandscapeTexture.TEXTURE_GRID);
        }

        addDx += positions[0] * LandscapeTexture.TEXTURE_GRID;
        addDy += positions[1] * LandscapeTexture.TEXTURE_GRID;

        {
            float u = (texturePos[0] + addDx) / (float) LandscapeTexture.TEXTURE_SIZE;
            float v = (texturePos[1] + addDy) / (float) LandscapeTexture.TEXTURE_SIZE;
            this.addPointToGeometry(map, buffer, up ? x2 : x1, up ? y2 : y1, u, v);
        }
        {
            float u = (texturePos[2] + addDx) / (float) LandscapeTexture.TEXTURE_SIZE;
            float v = (texturePos[3] + addDy) / (float) LandscapeTexture.TEXTURE_SIZE;
            this.addPointToGeometry(map, buffer, up ? x1 : x2, up ? y1 : y2, u, v);
        }
        {
            float u = (texturePos[4] + addDx) / (float) LandscapeTexture.TEXTURE_SIZE;
            float v = (texturePos[5] + addDy) / (float) LandscapeTexture.TEXTURE_SIZE;
            this.addPointToGeometry(map, buffer, x3, y3, u, v);
        }
        return;
    }


    private void addPointToGeometry(IGraphicsGrid map, ByteBuffer buffer, int x, int y, float u, float v) {
        int height = getHeight(map, x, y);
        byte visibleStatus = map.getVisibleStatus(x, y);

        float color = 0;
        if (x > 0 && x < this.landscape.mapWidth - 2
            && y > 0 && y < this.landscape.mapHeight - 2
            && (visibleStatus > 0 || !this.fowEnabled)) {

            int dHeight = getHeight(map, x, y - 1) - height;
            color = 0.875f + dHeight * .125f;

            if (color < 0.4f) {
                color = 0.4f;
            }

            if (this.fowEnabled) {
                color *= visibleStatus / (float) CommonConstants.FOG_OF_WAR_VISIBLE;
            }
        }

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(height);
        buffer.putFloat(u);
        buffer.putFloat(v);
        buffer.putFloat(color);
        return;
    }


    private static int getHeight(IGraphicsGrid map, int x, int y) {
        if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()) {
            return map.getVisibleHeightAt(x, y);
        }
        return 0;
    }


    private static int realModulo(int number, int modulo) {
        if (number >= 0) {
            return number % modulo;
        }
        return number % modulo + modulo;
    }
}