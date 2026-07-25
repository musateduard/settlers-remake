package org.example.mainwindow;

import jsettlers.common.CommonConstants;
import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.map.IDirectGridProvider;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.image.reader.DatFileReader;
import jsettlers.graphics.image.reader.ImageArrayProvider;
import jsettlers.graphics.image.reader.ImageMetadata;
import jsettlers.graphics.image.reader.translator.DatBitmapTranslator;
import jsettlers.graphics.map.draw.DrawConstants;
import jsettlers.graphics.map.draw.ETextureOrientation;
import jsettlers.graphics.map.draw.ImageProvider;
import org.example.shaders.LandscapeShader;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;


/**
 * OpenGL landscape resources: atlas layout, texture atlas, mesh handle, and draw lists.
 */
public class LandscapeLayer {

    static final int LANDSCAPE_SECTION = 0;

    /**
     * The base texture size.
     */
    static final int TEXTURE_SIZE = 1024;

    /**
     * Our base texture is divided into multiple squares that all hold a single texture. Continuous textures occupy 5*5 squares
     */
    static final int TEXTURE_GRID = 32;

    static final int BYTES_PER_FIELD = 4 * 6 * 3 * 2; // 4 bytes per float * 6 components(x,y,z,t,v,color) * 3 points per triangle * 2 triangles per field

    static final int[][] TEXTURE_POSITIONS = {
        /* 0: big */ { 0, 0, 5 },
        /* 1: big */ { 5, 0, 5 },
        /* 2: big */ { 10, 0, 5 },
        /* 3: big */ { 15, 0, 5 },
        /* 4: big */ { 20, 0, 5 },
        /* 5: small */ { 30, 0, 1 },
        /* 6: small */ { 31, 0, 1 },
        /* 7: big */ { 25, 0, 5 },
        /* 8: small */ { 30, 1, 1 },
        /* 9: small */ { 31, 1, 1 },
        /* 10: big */ { 0, 5, 5 },
        /* 11: small, continuous */ { 0, 20, 2 },
        /* 12: small, continuous */ { 2, 20, 2 },
        /* 13: small, continuous */ { 4, 20, 2 },
        /* 14: small, continuous */ { 6, 20, 2 },
        /* 15: small, continuous */ { 8, 20, 2 },
        /* 16: small, continuous */ { 10, 20, 2 },
        /* 17: small, continuous */ { 12, 20, 2 },
        /* 18: big */ { 5, 5, 5 },
        /* 19: small */ { 31, 5, 1 },
        /* 20: small */ { 30, 6, 1 },
        /* 21: big */ { 10, 5, 5 },
        /* 22: small */ { 31, 6, 1 },
        /* 23: small */ { 30, 7, 1 },
        /* 24: big */ { 15, 5, 5 },
        /* 25: small */ { 31, 7, 1 },
        /* 26: small */ { 30, 8, 1 },
        /* 27: small */ { 31, 8, 1 },
        /* 28: small */ { 30, 9, 1 },
        /* 29: small */ { 31, 9, 1 },
        /* 30: small */ { 30, 10, 1 },
        /* 31: big */ { 20, 5, 5 },
        /* 32: small */ { 31, 10, 1 },
        /* 33: small */ { 30, 11, 1 },
        /* 34: small */ { 31, 11, 1 },
        /* 35: big */ { 25, 5, 5 },
        /* 36: big */ { 0, 10, 5 },
        /* 37: small */ { 30, 13, 1 },
        /* 38: small */ { 31, 13, 1 },
        /* 39: small */ { 30, 14, 1 },
        /* 40: small */ { 31, 14, 1 },
        /* 41: small */ { 0, 15, 1 },
        /* 42: small */ { 1, 15, 1 },
        /* 43: small */ { 2, 15, 1 },
        /* 44: small */ { 3, 15, 1 },
        /* 45: small */ { 4, 15, 1 },
        /* 46: small */ { 5, 15, 1 },
        /* 47: small */ { 6, 15, 1 },
        /* 48: small */ { 7, 15, 1 },
        /* 49: small */ { 8, 15, 1 },
        /* 50: small */ { 9, 15, 1 },
        /* 51: small */ { 10, 15, 1 },
        /* 52: small */ { 11, 15, 1 },
        /* 53: small */ { 12, 15, 1 },
        /* 54: small */ { 13, 15, 1 },
        /* 55: small */ { 14, 15, 1 },
        /* 56: small */ { 15, 15, 1 },
        /* 57: small */ { 16, 15, 1 },
        /* 58: small */ { 17, 15, 1 },
        /* 59: small */ { 18, 15, 1 },
        /* 60: small */ { 19, 15, 1 },
        /* 61: small */ { 20, 15, 1 },
        /* 62: small */ { 21, 15, 1 },
        /* 63: small */ { 22, 15, 1 },
        /* 64: small */ { 23, 15, 1 },
        /* 65: small */ { 24, 15, 1 },
        /* 66: small */ { 25, 15, 1 },
        /* 67: small */ { 26, 15, 1 },
        /* 68: small */ { 27, 15, 1 },
        /* 69: small */ { 28, 15, 1 },
        /* 70: small */ { 29, 15, 1 },
        /* 71: small */ { 30, 15, 1 },
        /* 72: small */ { 31, 15, 1 },
        // ------------------------------------
        /* 73: small */ { 0, 16, 1 },
        /* 74: small */ { 1, 16, 1 },
        /* 75: small */ { 2, 16, 1 },
        /* 76: small */ { 3, 16, 1 },
        /* 77: small */ { 4, 16, 1 },
        /* 78: small */ { 5, 16, 1 },
        /* 79: small */ { 6, 16, 1 },
        /* 80: small */ { 7, 16, 1 },
        /* 81: small */ { 8, 16, 1 },
        /* 82: small */ { 9, 16, 1 },
        /* 83: small */ { 10, 16, 1 },
        /* 84: small */ { 11, 16, 1 },
        /* 85: small */ { 12, 16, 1 },
        /* 86: small */ { 13, 16, 1 },
        /* 87: small */ { 14, 16, 1 },
        /* 88: small */ { 15, 16, 1 },
        /* 89: small */ { 16, 16, 1 },
        /* 90: small */ { 17, 16, 1 },
        /* 91: small */ { 18, 16, 1 },
        /* 92: small */ { 19, 16, 1 },
        /* 93: small */ { 20, 16, 1 },
        /* 94: small */ { 21, 16, 1 },
        /* 95: small */ { 22, 16, 1 },
        /* 96: small */ { 23, 16, 1 },
        /* 97: small */ { 24, 16, 1 },
        /* 98: small */ { 30, 16, 1 },
        /* 99: small */ { 31, 12, 1 },
        /* 100: small */ { 25, 12, 1 },
        /* 101: small */ { 26, 16, 1 },
        /* 102: small */ { 27, 16, 1 },
        /* 103: small */ { 28, 16, 1 },
        /* 104: small */ { 29, 16, 1 },
        /* 105: small */ { 30, 16, 1 },
        /* 106: small */ { 31, 16, 1 },
        // ------------------------------------
        /* 107: small */ { 0, 17, 1 },
        /* 108: small */ { 1, 17, 1 },
        /* 109: small */ { 2, 17, 1 },
        /* 110: small */ { 3, 17, 1 },
        /* 111: small */ { 4, 17, 1 },
        /* 112: small */ { 5, 17, 1 },
        /* 113: small */ { 6, 17, 1 },
        /* 114: small */ { 7, 17, 1 },
        /* 115: small */ { 8, 17, 1 },
        /* 116: small */ { 9, 17, 1 },
        /* 117: small */ { 10, 17, 1 },
        /* 118: small */ { 11, 17, 1 },
        /* 119: small */ { 12, 17, 1 },
        /* 120: small */ { 13, 17, 1 },
        /* 121: small */ { 14, 17, 1 },
        /* 122: small */ { 15, 17, 1 },
        /* 123: small */ { 16, 17, 1 },
        /* 124: small */ { 17, 17, 1 },
        /* 125: small */ { 18, 17, 1 },
        /* 126: small */ { 19, 17, 1 },
        /* 127: small */ { 20, 17, 1 },
        /* 128: small */ { 21, 17, 1 },
        /* 129: small */ { 22, 17, 1 },
        /* 130: small */ { 23, 17, 1 },
        /* 131: small */ { 24, 17, 1 },
        /* 132: small */ { 25, 17, 1 },
        /* 133: small */ { 26, 17, 1 },
        /* 134: small */ { 27, 17, 1 },
        /* 135: small */ { 28, 17, 1 },
        /* 136: small */ { 29, 17, 1 },
        /* 137: small */ { 30, 17, 1 },
        /* 138: small */ { 31, 17, 1 },
        // ------------------------------------
        /* 139: small */ { 0, 18, 1 },
        /* 140: small */ { 1, 18, 1 },
        /* 141: small */ { 2, 18, 1 },
        /* 142: small */ { 3, 18, 1 },
        /* 143: small */ { 4, 18, 1 },
        /* 144: small */ { 5, 18, 1 },
        /* 145: small */ { 6, 18, 1 },
        /* 146: small */ { 7, 18, 1 },
        /* 147: small */ { 8, 18, 1 },
        /* 148: small */ { 9, 18, 1 },
        /* 149: small */ { 10, 18, 1 },
        /* 150: small */ { 11, 18, 1 },
        /* 151: small */ { 12, 18, 1 },
        /* 152: small */ { 13, 18, 1 },
        /* 153: small */ { 14, 18, 1 },
        /* 154: small */ { 15, 18, 1 },
        /* 155: small */ { 16, 18, 1 },
        /* 156: small */ { 17, 18, 1 },
        /* 157: small */ { 18, 18, 1 },
        /* 158: small */ { 19, 18, 1 },
        /* 159: small */ { 20, 18, 1 },
        /* 160: small */ { 21, 18, 1 },
        /* 161: small */ { 22, 18, 1 },
        /* 162: small */ { 23, 18, 1 },
        /* 163: small */ { 24, 18, 1 },
        /* 164: small */ { 25, 18, 1 },
        /* 165: small */ { 26, 18, 1 },
        /* 166: small */ { 27, 18, 1 },
        /* 167: small */ { 28, 18, 1 },
        /* 168: small */ { 29, 18, 1 },
        /* 169: small */ { 30, 18, 1 },
        /* 170: small */ { 31, 18, 1 },
        // ------------------------------------
        /* 171: small */ { 0, 19, 1 },
        /* 172: small */ { 1, 19, 1 },
        /* 173: small */ { 2, 19, 1 },
        /* 174: small */ { 3, 19, 1 },
        /* 175: small */ { 4, 19, 1 },
        /* 176: big (odd shape?) */ { 5, 10, 5 },
        /* 177: small */ { 6, 19, 1 },
        /* 178: small */ { 7, 19, 1 },
        /* 179: small */ { 8, 19, 1 },
        /* 180: small */ { 9, 19, 1 },
        /* 181: small */ { 10, 19, 1 },
        /* 182: small */ { 11, 19, 1 },
        /* 183: small */ { 12, 19, 1 },
        /* 184: small */ { 13, 19, 1 },
        /* 185: small */ { 14, 19, 1 },
        /* 186: small */ { 15, 19, 1 },
        /* 187: small */ { 16, 19, 1 },
        /* 188: small */ { 17, 19, 1 },
        /* 189: small */ { 18, 19, 1 },
        /* 190: small */ { 19, 19, 1 },
        /* 191: small */ { 20, 19, 1 },
        /* 192: small */ { 21, 19, 1 },
        /* 193: small */ { 22, 19, 1 },
        /* 194: small */ { 23, 19, 1 },
        /* 195: small */ { 24, 19, 1 },
        /* 196: small */ { 25, 19, 1 },
        /* 197: small */ { 26, 19, 1 },
        /* 198: small */ { 27, 19, 1 },
        /* 199: small */ { 28, 19, 1 },
        /* 200: small */ { 29, 19, 1 },
        /* 201: small */ { 30, 19, 1 },
        /* 202: small */ { 31, 19, 1 },
        // ------------------------------------
        /* 203: small */ { 0, 22, 1 },
        /* 204: small */ { 1, 22, 1 },
        /* 205: small */ { 2, 22, 1 },
        /* 206: small */ { 3, 22, 1 },
        /* 207: small */ { 4, 22, 1 },
        /* 208: small */ { 5, 22, 1 },
        /* 209: small */ { 6, 22, 1 },
        /* 210: small */ { 7, 22, 1 },
        /* 211: small */ { 8, 22, 1 },
        /* 212: small */ { 9, 22, 1 },
        /* 213: small */ { 10, 22, 1 },
        /* 214: small */ { 11, 22, 1 },
        /* 215: small */ { 12, 22, 1 },
        /* 216: small */ { 13, 22, 1 },
        /* 217: big */ { 14, 20, 5 },
        /* 218: small */ { 1, 23, 1 },
        /* 219: small */ { 2, 23, 1 },
        /* 220: small */ { 3, 23, 1 },
        /* 221: small */ { 4, 23, 1 },
        /* 222: small */ { 5, 23, 1 },
        /* 223: small */ { 6, 23, 1 },
        /* 224: small */ { 7, 23, 1 },
        /* 225: small */ { 8, 23, 1 },
        /* 226: small */ { 9, 23, 1 },
        /* 227: small */ { 10, 23, 1 },
        /* 228: small */ { 11, 23, 1 },
        /* 229: small */ { 12, 23, 1 },
        /* 230: big */ { 19, 20, 5 },
        /* 231: small */ { 13, 23, 1 },
        /* 232: small */ { 0, 24, 1 },
        /* 233: small */ { 1, 24, 1 },
        /* 234: small */ { 2, 24, 1 },
    };

    public final int bufferWidth; // in map tiles
    public final int bufferHeight; // in map tiles
    final int mapWidth;
    final int mapHeight;
    public final LandscapeShader shader;
    public VertexBuffer mesh;
    public int visibleLineCount;
    public IntBuffer lineVertexOffsetList = MemoryUtil.memAllocInt(0);  // Vertex start indices for glMultiDrawArrays
    public IntBuffer lineVertexCount = MemoryUtil.memAllocInt(0);  // Vertex counts for glMultiDrawArrays
    public final boolean hasDirectGridProvider;
    public boolean fowEnabled;
    public final ByteBuffer buffer;  // Reusable upload scratch for one map line of fields


    private record TextureIntersections(
        ELandscapeType type1,
        ELandscapeType type1alt,
        ELandscapeType type2,
        int baseIndex) {

        TextureIntersections(ELandscapeType type1, ELandscapeType type2, int baseIndex) {
            this(type1, type1, type2, baseIndex);
        }
    }

    private static final TextureIntersections[] BORDER_TEXTURES = {
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


    public LandscapeLayer(Framebuffer canvas, IGraphicsGrid mapGrid) {

        this.mapWidth = mapGrid.getWidth();
        this.mapHeight = mapGrid.getHeight();
        this.bufferWidth = this.mapWidth - 1;
        this.bufferHeight = this.mapHeight - 1;
        this.shader = new LandscapeShader((float) canvas.width, (float) canvas.height, this.mapHeight);
        this.hasDirectGridProvider = mapGrid instanceof IDirectGridProvider;
        this.fowEnabled = this.hasDirectGridProvider && ((IDirectGridProvider) mapGrid).isFoWEnabled();

        this.buffer = ByteBuffer
            .allocateDirect(BYTES_PER_FIELD * this.bufferWidth)
            .order(ByteOrder.nativeOrder());

        this.mesh = LandscapeLayer.generateTerrainMesh(
            this.buffer,
            this.bufferWidth,
            this.bufferHeight,
            this.mapWidth,
            this.mapHeight,
            this.fowEnabled,
            mapGrid
        );

        return;
    }


    /**
     * Builds the full terrain mesh from the current grid and returns a GPU vertex buffer.
     */
    public static VertexBuffer generateTerrainMesh(
        ByteBuffer bufferLine,
        int bufferWidth,
        int bufferHeight,
        int mapWidth,
        int mapHeight,
        boolean fowEnabled,
        IGraphicsGrid map) {

        int vertexCount = bufferWidth * bufferHeight * 3 * 2;
        final int stride = 6 * 4;
        float[] meshVertices = new float[vertexCount * 6];
        List<VertexAttribute> attributes = List.of(
            new VertexAttribute(0, 3, GL33C.GL_FLOAT, false, stride, 0),
            new VertexAttribute(1, 2, GL33C.GL_FLOAT, false, stride, 3 * 4),
            new VertexAttribute(2, 1, GL33C.GL_FLOAT, false, stride, 5 * 4)
        );
        VertexBuffer mesh = new VertexBuffer(meshVertices, attributes, GL33C.GL_DYNAMIC_DRAW);

        for (int offsetY = 0; offsetY < bufferHeight; offsetY += 1) {

            bufferLine.clear();
            for (int offsetX = 0; offsetX < bufferWidth; offsetX += 1) {
                LandscapeLayer.addTrianglesToGeometry(
                    map, bufferLine, offsetX, offsetY, mapWidth, mapHeight, fowEnabled
                );
            }

            bufferLine.flip();
            mesh.updateVertexBuffer((long) BYTES_PER_FIELD * bufferWidth * offsetY, bufferLine);
        }

        return mesh;
    }


    /**
     * Writes fields [{@code x1}, {@code x2}) on map line {@code y} into {@link #buffer}
     * and uploads that span to the GPU mesh immediately.
     */
    public void uploadLineSpan(IGraphicsGrid map, int y, int x1, int x2) {

        ByteBuffer buffer = this.buffer;
        buffer.clear();

        for (int i = x1; i != x2; i++) {
            LandscapeLayer.addTrianglesToGeometry(
                map, buffer, i, y, this.mapWidth, this.mapHeight, this.fowEnabled
            );
        }

        buffer.flip();
        this.mesh.updateVertexBuffer(((long) y * this.bufferWidth + x1) * BYTES_PER_FIELD, buffer);

        return;
    }


    public static void addTrianglesToGeometry(
        IGraphicsGrid map,
        ByteBuffer buffer,
        int x,
        int y,
        int mapWidth,
        int mapHeight,
        boolean fowEnabled) {

        LandscapeLayer.addTriangleToGeometry(
            map, buffer, x, y, true, x * 37 + y * 17, mapWidth, mapHeight, fowEnabled
        );

        LandscapeLayer.addTriangleToGeometry(
            map, buffer, x, y, false, x, mapWidth, mapHeight, fowEnabled
        );

        return;
    }


    public static void addTriangleToGeometry(
        IGraphicsGrid map,
        ByteBuffer buffer,
        int x1,
        int y,
        boolean up,
        int useSecondParameter,
        int mapWidth,
        int mapHeight,
        boolean fowEnabled) {

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

            for (TextureIntersections intersect : BORDER_TEXTURES) {
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

        int[] positions = TEXTURE_POSITIONS[textureIndex];
        int addDx = 0;
        int addDy = 0;

        if (positions[2] >= 2) {
            addDx = x1 * DrawConstants.DISTANCE_X - y * DrawConstants.DISTANCE_X / 2;
            addDy = y * DrawConstants.TEXTUREUNIT_Y;
            addDx = realModulo(addDx, (positions[2] - 1) * TEXTURE_GRID);
            addDy = realModulo(addDy, (positions[2] - 1) * TEXTURE_GRID);
        }

        addDx += positions[0] * TEXTURE_GRID;
        addDy += positions[1] * TEXTURE_GRID;

        {
            float u = (texturePos[0] + addDx) / (float) TEXTURE_SIZE;
            float v = (texturePos[1] + addDy) / (float) TEXTURE_SIZE;
            LandscapeLayer.addPointToGeometry(
                map, buffer, up ? x2 : x1, up ? y2 : y1, u, v, mapWidth, mapHeight, fowEnabled
            );
        }
        {
            float u = (texturePos[2] + addDx) / (float) TEXTURE_SIZE;
            float v = (texturePos[3] + addDy) / (float) TEXTURE_SIZE;
            LandscapeLayer.addPointToGeometry(
                map, buffer, up ? x1 : x2, up ? y1 : y2, u, v, mapWidth, mapHeight, fowEnabled
            );
        }
        {
            float u = (texturePos[4] + addDx) / (float) TEXTURE_SIZE;
            float v = (texturePos[5] + addDy) / (float) TEXTURE_SIZE;
            LandscapeLayer.addPointToGeometry(
                map, buffer, x3, y3, u, v, mapWidth, mapHeight, fowEnabled
            );
        }
        return;
    }


    public static void addPointToGeometry(
        IGraphicsGrid map,
        ByteBuffer buffer,
        int x,
        int y,
        float u,
        float v,
        int mapWidth,
        int mapHeight,
        boolean fowEnabled) {

        int height = getHeight(map, x, y);
        byte visibleStatus = map.getVisibleStatus(x, y);

        float color = 0;
        if (x > 0 && x < mapWidth - 2
            && y > 0 && y < mapHeight - 2
            && (visibleStatus > 0 || !fowEnabled)) {

            int dHeight = getHeight(map, x, y - 1) - height;
            color = 0.875f + dHeight * .125f;

            if (color < 0.4f) {
                color = 0.4f;
            }

            if (fowEnabled) {
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


    public static int getHeight(IGraphicsGrid map, int x, int y) {
        if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()) {
            return map.getVisibleHeightAt(x, y);
        }
        return 0;
    }


    public static int realModulo(int number, int modulo) {
        if (number >= 0) {
            return number % modulo;
        }
        return number % modulo + modulo;
    }


    private static class ImageWriter implements ImageArrayProvider {
        int arrayOffset;
        int cellSize;
        int maxOffset;
        int[] data;

        // nothing to do. We assume images are a rectangle and have the right size.
        @Override
        public void startImage(int width, int height) {
            return;
        }

        @Override
        public void writeLine(int[] data, int length) {
            if (arrayOffset < maxOffset) {
                for (int i = 0; i < cellSize; i++) {
                    this.data[arrayOffset + i] = data[i % length];
                }
                arrayOffset += TEXTURE_SIZE;
            }
        }
    }

    /**
     * Generates the texture data.
     *
     * @param data The texture data buffer.
     *
     * @throws IOException If the necessary file reader is missing
     */
    public static void decodeImage(int[] data) throws IOException {

        DatFileReader assetFile = ImageProvider.getInstance().getFileReader(LandscapeLayer.LANDSCAPE_SECTION);

        if (assetFile == null) {
            throw new IOException("Could not get a file reader for the file.");
        }

        ImageWriter imageWriter = new ImageWriter();
        imageWriter.data = data;

        ImageMetadata meta = new ImageMetadata();

        DatBitmapTranslator<SingleImage> translator = assetFile.getLandscapeTranslator();

        for (int index = 0; index < TEXTURE_POSITIONS.length; index++) {

            int[] position = TEXTURE_POSITIONS[index];
            int x = position[0] * TEXTURE_GRID;
            int y = position[1] * TEXTURE_GRID;
            int start = y * TEXTURE_SIZE + x;
            int cellSize = position[2] * TEXTURE_GRID;
            int end = (y + cellSize) * TEXTURE_SIZE + x;

            imageWriter.arrayOffset = start;
            imageWriter.cellSize = cellSize;
            imageWriter.maxOffset = end;

            long dataPos = assetFile.readImageHeader(translator, meta, assetFile.getOffsetForLandscape(index));
            assetFile.readCompressedData(translator, meta, imageWriter, dataPos);

            // freaky stuff
            int arrayOffset = imageWriter.arrayOffset;
            int l = arrayOffset - start;
            while (arrayOffset < end) {
                System.arraycopy(data, arrayOffset - l, data, arrayOffset, cellSize);
                arrayOffset += TEXTURE_SIZE;
            }
        }
    }
}