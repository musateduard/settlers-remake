package org.example.mainwindow;

import java.util.Arrays;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.opengl.GLCapabilities;
import static org.lwjgl.opengl.GL33C.GL_NO_ERROR;
import static org.lwjgl.opengl.GL33C.GL_DONT_CARE;
import static org.lwjgl.opengl.GL33C.GL_CONTEXT_FLAGS;
import static org.lwjgl.system.MemoryUtil.NULL;


/**
 * this class handles all low level rendering functions related to opengl.
 */
public class Renderer {

    public final GLCapabilities capabilities;
    public final String glslVersion;
    public final String supportedVersion;
    public final String rendererName;
    public final String vendorName;


    public Renderer(Window window) {

        if (window.handle == NULL) {
            throw new RuntimeException("cannot initialize opengl context before creating glfw window");
        }

        // init gl capabilities for current context
        this.capabilities = GL.createCapabilities();
        this.glslVersion = "#version 330";
        this.supportedVersion = GL33C.glGetString(GL33C.GL_VERSION);
        this.rendererName = GL33C.glGetString(GL33C.GL_RENDERER);
        this.vendorName = GL33C.glGetString(GL33C.GL_VENDOR);

        // enable debug output
        int[] flags = new int[32];
        GL33C.glGetIntegerv(GL_CONTEXT_FLAGS, flags);

        if (Arrays.stream(flags).anyMatch(item -> item == KHRDebug.GL_CONTEXT_FLAG_DEBUG_BIT)) {

            System.out.printf("debug output enabled\n");

            GL33C.glEnable(KHRDebug.GL_DEBUG_OUTPUT);
            GL33C.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
            KHRDebug.glDebugMessageCallback(Renderer::debugCallback, NULL);
            KHRDebug.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, true);
        }

        int openglError = GL33C.glGetError();
        if (openglError != GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        return;
    }


    public static void debugCallback(int source, int type, int id, int severity, int length, long message, long userParam) {

        String sourceString;
        switch (source) {
            case KHRDebug.GL_DEBUG_SOURCE_API -> sourceString = "GL_DEBUG_SOURCE_API";
            case KHRDebug.GL_DEBUG_SOURCE_WINDOW_SYSTEM -> sourceString = "GL_DEBUG_SOURCE_WINDOW_SYSTEM";
            case KHRDebug.GL_DEBUG_SOURCE_SHADER_COMPILER -> sourceString = "GL_DEBUG_SOURCE_SHADER_COMPILER";
            case KHRDebug.GL_DEBUG_SOURCE_THIRD_PARTY -> sourceString = "GL_DEBUG_SOURCE_THIRD_PARTY";
            case KHRDebug.GL_DEBUG_SOURCE_APPLICATION -> sourceString = "GL_DEBUG_SOURCE_APPLICATION";
            case KHRDebug.GL_DEBUG_SOURCE_OTHER -> sourceString = "GL_DEBUG_SOURCE_OTHER";
            default -> sourceString = "";
        }

        String typeString;
        switch (type) {
            case KHRDebug.GL_DEBUG_TYPE_ERROR -> typeString = "GL_DEBUG_TYPE_ERROR";
            case KHRDebug.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> typeString = "GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR";
            case KHRDebug.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> typeString = "GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR";
            case KHRDebug.GL_DEBUG_TYPE_PORTABILITY -> typeString = "GL_DEBUG_TYPE_PORTABILITY";
            case KHRDebug.GL_DEBUG_TYPE_PERFORMANCE -> typeString = "GL_DEBUG_TYPE_PERFORMANCE";
            case KHRDebug.GL_DEBUG_TYPE_MARKER -> typeString = "GL_DEBUG_TYPE_MARKER";
            case KHRDebug.GL_DEBUG_TYPE_PUSH_GROUP -> typeString = "GL_DEBUG_TYPE_PUSH_GROUP";
            case KHRDebug.GL_DEBUG_TYPE_POP_GROUP -> typeString = "GL_DEBUG_TYPE_POP_GROUP";
            case KHRDebug.GL_DEBUG_TYPE_OTHER -> typeString = "GL_DEBUG_TYPE_OTHER";
            default -> typeString = "";
        }

        String severityString;
        switch (severity) {
            case KHRDebug.GL_DEBUG_SEVERITY_HIGH -> severityString = "GL_DEBUG_SEVERITY_HIGH";
            case KHRDebug.GL_DEBUG_SEVERITY_MEDIUM -> severityString = "GL_DEBUG_SEVERITY_MEDIUM";
            case KHRDebug.GL_DEBUG_SEVERITY_LOW -> severityString = "GL_DEBUG_SEVERITY_LOW";
            case KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION -> severityString = "GL_DEBUG_SEVERITY_NOTIFICATION";
            default -> severityString = "";
        }

        String messageString = MemoryUtil.memUTF8(message);

        throw new RuntimeException("%s %s %s %s".formatted(sourceString, typeString, severityString, messageString));
    }


    /*
    public int createVBO(float[] vertexBuffer, int bindingTarget, int usageType) {

        int vboId = GL33C.glGenBuffers();  // create vbo

        GL33C.glBindBuffer(bindingTarget, vboId);  // bind vbo
        GL33C.glBufferData(bindingTarget, vertexBuffer, usageType);  // upload vertex buffer to vbo
        GL33C.glBindBuffer(bindingTarget, 0);  // unbind vbo

        int openglError = GL33C.glGetError();
        if (openglError != GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        return vboId;
    }


    public int createVAO(
        int vboId, int vboBindTarget,
        int attributeIndex, int attributeSize,
        int attributeDataType, boolean normalized,
        int attributeStride, int pointerOffset) {

        // note: this function currently works only for vertex buffers with 1 attribute per vertex
        // note: it also only works for vbos that are fully initialized
        // note: this function will fail if the bound vbo is not already uploaded to the gpu

        // bind vbo
        GL33C.glBindBuffer(vboBindTarget, vboId);

        // create and bind vao
        int vaoId = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(vaoId);

        // set vao properties for current vbo
        GL33C.glVertexAttribPointer(attributeIndex, attributeSize, attributeDataType, normalized, attributeStride, pointerOffset);

        // set start index for current vao
        GL33C.glEnableVertexAttribArray(0);

        // unbind vao
        GL33C.glBindVertexArray(0);

        // unbind vbo
        GL33C.glBindBuffer(vboBindTarget, 0);

        int openglError = GL33C.glGetError();
        if (openglError != GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        return vaoId;
    }


    public void drawBuilding() {

        // todo: move this method to RenderingSystem

        ResourceManager.setProvider(new SwingResourceProvider());

        try {
            SettingsManager.setup();
        }

        catch (Exception exception) {
            System.out.println("error occurred");
        }

        SettingsManager settings = SettingsManager.getInstance();

        settings.setSettlersFolder(new File("C:\\games\\Settlers 3 Ultimate"));

        ImageProvider.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\GFX"), "745006780412758287");
        SoundManager.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\SND"));

        // note: Image is an abstract class; you need to use SettlerImage or SingleImage

        // LWJGLDrawContext context = new LWJGLDrawContext(this.glCapabilities, false, 1.00f);
        SettlerImage testImage = (SettlerImage) ImageProvider.getInstance().getImage(new OriginalImageLink(EImageLinkType.SETTLER, 33, 11, 0));

        // context.updateViewMatrix(0, 0, 0, 1, 1, 1);
        // context.updateProjectionMatrix(800, 600);

        // note: you need a LWJGLDrawContext context in order to draw sprites using SettlerImage

        // testImage.drawAt(context, 100, 100, 0, Color.CYAN, 1.00f);

        return;
    }


    public void renderBuildingFrame() throws Exception {

        ResourceManager.setProvider(new SwingResourceProvider());
        SettingsManager.setup();

        SettingsManager settings = SettingsManager.getInstance();

        settings.setSettlersFolder(new File("C:\\games\\Settlers 3 Ultimate"));

        ImageProvider.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\GFX"), "745006780412758287");
        SoundManager.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\SND"));

        // note: Image is an abstract class; you need to use SettlerImage or SingleImage

        LWJGLDrawContext context = new LWJGLDrawContext(this.capabilities, false, 1.00f);
        SettlerImage testImage = (SettlerImage) ImageProvider.getInstance().getImage(new OriginalImageLink(EImageLinkType.SETTLER, 33, 11, 0));

        while (GLFW.glfwWindowShouldClose(this.window.windowId) == false) {

            // set clear color
            GL11C.glClearColor(0.90f, 0.90f, 0.90f, 1.00f);

            // clear screen
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

            // poll events
            GLFW.glfwPollEvents();

            // note: need to configure model, view, projection matrix before drawing sprite to screen

            context.updateViewMatrix(0, 0, 0, 1, 1, 1);
            context.updateProjectionMatrix(this.window.screenWidth, this.window.screenHeight);

            testImage.drawAt(context, 100, 100, 0, Color.CYAN, 1.00f);

            // swap buffers
            GLFW.glfwSwapBuffers(this.window.windowId);
        }

        return;
    }


    public void renderGameFrame() throws Exception {

        // old project game initialization
        ResourceManager.setProvider(new SwingResourceProvider());
        SettingsManager.setup();

        SettingsManager settings = SettingsManager.getInstance();

        settings.setSettlersFolder(new File("C:\\games\\Settlers 3 Ultimate"));

        ImageProvider.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\GFX"), "745006780412758287");
        SoundManager.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\SND"));

        byte playerId = 0;
        long randomSeed = System.currentTimeMillis();

        PlayerSetting[] playerSettings = {
            new PlayerSetting(true, EPlayerType.AI_HARD, ECivilisation.ROMAN, (byte) 0),
            new PlayerSetting(true, EPlayerType.AI_HARD, ECivilisation.ASIAN, (byte) 1)
        };

        File file = new File("C:\\games\\Settlers 3 Ultimate\\Map\\User\\384-2-Brueckenkopf.map");
        MapLoader selectedMap = MapLoader.getLoaderForListedMap(new DirectoryMapLister.ListedMapFile(file));

        InitialGameState initialGameState = new InitialGameState(playerId, playerSettings, randomSeed, EMapStartResources.MEDIUM_GOODS);
        JSettlersGameGLFW offlineGame = new JSettlersGameGLFW(selectedMap, initialGameState);
        TaskExecutorGLFW taskExecutor = new TaskExecutorGLFW();

        offlineGame.networkConnector.getGameClock().setTaskExecutor(taskExecutor);

        MatchConstants.init(offlineGame.networkConnector.getGameClock(), randomSeed);
        MatchConstants.clock().setTaskExecutor(taskExecutor);

        JSettlersGameGLFW.GameRunner runner = (JSettlersGameGLFW.GameRunner) offlineGame.start();
        SwingSoundPlayer soundPlayer = new SwingSoundPlayer(SettingsManager.getInstance());

        // note: MapContent can only be instantiated after GameRunner.mainGrid is properly loaded
        while (runner.getMainGrid() == null) {
            Thread.sleep(100);
        }

        // todo: mapContent should be member of JSettlersGame
        // todo: mapContent needs reference to lwjgl context from the beginning
        // todo: mapContent doesn't need reference to soundPlayer or gui position

        MapContent mapContent = new MapContent(runner, soundPlayer, ETextDrawPosition.DESKTOP);
        LWJGLDrawContext context = new LWJGLDrawContext(this.capabilities, false, 1.00f);

        SettlerImage testImage = (SettlerImage) ImageProvider.getInstance().getImage(new OriginalImageLink(EImageLinkType.SETTLER, 33, 11, 0));

        float saturation = 0.40f;

        while (GLFW.glfwWindowShouldClose(this.window.windowId) == false) {

            long currentTimeNs = System.nanoTime();
            long currentTime = System.currentTimeMillis();
            float currentTimeDelta = (currentTime - MainLauncher.GAME_START_TIME_MS) / 1000.00f;

            float red = (float) (Math.sin(currentTimeDelta) * saturation + (1.00f - saturation));
            float green = (float) (Math.sin(currentTimeDelta + 2.00 * Math.PI / 3.00) * saturation + (1.00f - saturation));
            float blue = (float) (Math.sin(currentTimeDelta + 4.00 * Math.PI / 3.00) * saturation + (1.00f - saturation));

            // set clear color
            // GL11C.glClearColor(0.80f, 0.80f, 0.80f, 1.00f);
            GL11C.glClearColor(red, green, blue, 1.00f);

            // clear screen
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

            // poll events
            GLFW.glfwPollEvents();

            // this.updateCameraPosition(currentTimeNs - this.startFrameTimeNs);

            // note:
            //
            // drawContent doesn't work in current setup
            // try rendering map elements separately one by one

            // todo: render map terrain using mapContent

            // draw map terrain
            // FloatRectangle visibleMapSection = mapContent.mapContext.getScreen().getPosition().bigger(MapContent.SCREEN_PADDING);
            // FloatRectangle visibleMapSection = new FloatRectangle(-this.camera.offsetX + 20, -this.camera.offsetY + 520, -this.camera.offsetX + 290, -this.camera.offsetY + 580);

            // mapContent.drawContent(context, this.width, this.height);
            // context.updateViewMatrix(this.camera.offsetX, this.camera.offsetY, 0, 1, 1, 1);  // note: view matrix is set by mapContext during begin()
            mapContent.mapContext.begin(context);
            // mapContent.drawMapTerrain(visibleMapSection);

            // draw test sprite
            context.updateViewMatrix(0, 0, 0, 1, 1, 1);
            // context.updateProjectionMatrix(this.screenWidth, this.screenHeight);

            testImage.drawAt(context, 100, 100, 0, Color.CYAN, 1.00f);

            // swap buffers
            GLFW.glfwSwapBuffers(this.window.windowId);

            // this.startFrameTimeNs = System.nanoTime();
        }

        return;
    }


    public void renderTestFrame() {

        // create vertex buffer
        float[] vertexList = {
            0.00f, 0.00f, 0.00f,  // bottom left
            1.00f, 0.00f, 0.00f,  // bottom right
            1.00f, 1.00f, 0.00f,  // top right
            0.00f, 1.00f, 0.00f,  // top left
        };

        // create and bind vao
        int vaoId = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(vaoId);

        // create and bind vbo
        int vboId = GL33C.glGenBuffers();
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, vboId);

        // upload vertex buffer to gpu
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, vertexList, GL33C.GL_STATIC_DRAW);

        // set vao properties for current vbo
        GL33C.glVertexAttribPointer(0, 3, GL33C.GL_FLOAT, false, 0, 0);

        // set start index for current vao
        GL33C.glEnableVertexAttribArray(0);

        // unbind vbo and vao
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0);
        GL33C.glBindVertexArray(0);

        // allocate transform matrix buffers outside of render loop
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.translate(50, 50, 0);
        modelMatrix.scale(100, 100, 1);

        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.ortho(0, 800, 0, 600, -1, 1);

        Matrix4f transformMatrix = new Matrix4f(projectionMatrix).mul(modelMatrix);
        FloatBuffer transformMatrixArray = BufferUtils.createFloatBuffer(16);

        float saturation = 0.40f;

        while (GLFW.glfwWindowShouldClose(this.window.windowId) == false) {

            float currentTime = (System.currentTimeMillis() - MainLauncher.GAME_START_TIME_MS) / 1000.00f;

            float red = (float) (Math.sin(currentTime) * saturation + (1.00f - saturation));
            float green = (float) (Math.sin(currentTime + 2.00 * Math.PI / 3.00) * saturation + (1.00f - saturation));
            float blue = (float) (Math.sin(currentTime + 4.00 * Math.PI / 3.00) * saturation + (1.00f - saturation));

            // set clear color
            GL11C.glClearColor(red, green, blue, 1.00f);

            // clear screen
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

            // poll events
            GLFW.glfwPollEvents();

            // draw using glBegin and glEnd
            //
            // // activate projection mode
            // GL11.glMatrixMode(GL11.GL_PROJECTION);
            // GL11.glLoadIdentity();
            //
            // // define screen size
            // GL11.glOrtho(0, this.width, 0, this.height, -1, 1);
            //
            // // activate model view
            // GL11.glMatrixMode(GL11.GL_MODELVIEW);
            // GL11.glLoadIdentity();
            //
            // // set vertex color
            // GL11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
            //
            // // begin vertex draw
            // GL11.glBegin(GL11C.GL_QUADS);
            //
            // GL11.glVertex2f(50.0f, 50.0f);
            // GL11.glVertex2f(150.0f, 50.0f);
            // GL11.glVertex2f(150.0f, 150.0f);
            // GL11.glVertex2f(50.0f, 150.0f);
            //
            // // end vertex draw
            // GL11.glEnd();

            // draw using shader
            // activate shader
            GL33C.glUseProgram(this.shaderProgramId);

            // set transform matrix uniform
            transformMatrix.get(transformMatrixArray);

            GL33C.glUniformMatrix4fv(this.transformMatrixAddress, false, transformMatrixArray);

            // set color uniform
            GL33C.glUniform4f(this.colorUniformAddress, 1.00f, 0.00f, 0.00f, 1.00f);

            // bind vao and vbo
            GL33C.glBindVertexArray(vaoId);
            GL33C.glEnableVertexAttribArray(0);

            // draw buffer
            GL33C.glDrawArrays(GL11C.GL_QUADS, 0, 4);

            // cleanup
            GL33C.glDisableVertexAttribArray(0);
            GL33C.glBindVertexArray(0);
            GL33C.glUseProgram(0);

            // swap buffers
            GLFW.glfwSwapBuffers(this.window.windowId);
        }

        return;
    }
    */
}