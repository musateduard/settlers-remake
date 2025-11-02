package org.example.maingamewindow;

import org.example.gamemap.SettlersMap;
import org.example.gamesimulation.SettlersGame;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;


interface MouseListener {
    void onMouseEvent(MouseEvent event);
}


interface CursorListener {
    void onCursorEvent(CursorEvent event);
}


interface KeyListener {
    void onKeyEvent(KeyEvent event);
}


record KeyEvent(
    long windowId,
    int key,
    int scanCode,
    int action,
    int modifier
) {}


record MouseEvent(
    long window,
    int button,
    int action,
    int mods
) {}


record CursorEvent(
    long window,
    double xpos,
    double ypos
) {}


class EventManager {

    public final ArrayList<MouseListener> mouseListenerList;
    public final ArrayList<CursorListener> cursorListenerList;
    public final ArrayList<KeyListener> keyListenerList;


    public EventManager() {

        this.mouseListenerList = new ArrayList<>();
        this.cursorListenerList = new ArrayList<>();
        this.keyListenerList = new ArrayList<>();

        return;
    }


    public void addMouseListener(MouseListener listener) {
        this.mouseListenerList.add(listener);
        return;
    }


    public void addCursorListener(CursorListener listener) {
        this.cursorListenerList.add(listener);
        return;
    }


    public void addKeyListener(KeyListener listener) {
        this.keyListenerList.add(listener);
        return;
    }


    public void emitMouseEvent(MouseEvent event) {

        for (MouseListener item : this.mouseListenerList) {
            item.onMouseEvent(event);
        }

        return;
    }


    public void emitCursorEvent(CursorEvent event) {

        for (CursorListener item : this.cursorListenerList) {
            item.onCursorEvent(event);
        }

        return;
    }


    public void emitKeyEvent(KeyEvent event) {

        for (KeyListener item : this.keyListenerList) {
            item.onKeyEvent(event);
        }

        return;
    }
}


class Window {

    public final int width;
    public final int height;
    public final long windowId;
    public final EventManager eventManager;


    public Window(EventManager eventManager) {

        this.width = 800;
        this.height = 600;
        this.eventManager = eventManager;

        // init glfw
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW.");
        }

        // set window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        // create window
        this.windowId = GLFW.glfwCreateWindow(this.width, this.height, "demo window", MemoryUtil.NULL, MemoryUtil.NULL);

        if (this.windowId == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window.");
        }

        // make context current
        GLFW.glfwMakeContextCurrent(this.windowId);

        // enable vsync
        GLFW.glfwSwapInterval(1);

        // register keyboard handler
        GLFW.glfwSetKeyCallback(

            this.windowId,

            (long windowId, int key, int scanCode, int action, int modifier) -> {

                KeyEvent event = new KeyEvent(windowId, key, scanCode, action, modifier);
                this.eventManager.emitKeyEvent(event);

                return;
            }
        );

        GLFW.glfwSetCursorPosCallback(

            this.windowId,

            (long window, double xpos, double ypos) -> {

                CursorEvent event = new CursorEvent(window, xpos, ypos);
                this.eventManager.emitCursorEvent(event);

                return;
            }
        );

        GLFW.glfwSetMouseButtonCallback(

            this.windowId,

            (long window, int button, int action, int mods) -> {

                MouseEvent event = new MouseEvent(window, button, action, mods);
                this.eventManager.emitMouseEvent(event);

                return;
            }
        );

        // make window visible
        GLFW.glfwShowWindow(this.windowId);

        return;
    }


    public boolean shouldClose() {
        boolean shouldClose = GLFW.glfwWindowShouldClose(this.windowId);
        return shouldClose;
    }


    public void cleanup() {
        GLFW.glfwDestroyWindow(this.windowId);
        GLFW.glfwTerminate();
        return;
    }


    public void swapBuffers() {
        GLFW.glfwSwapBuffers(this.windowId);
        return;
    }


    public void pollEvents() {
        GLFW.glfwPollEvents();
        return;
    }
}


/**
 * this class handles all low level rendering functions related to opengl.
 */
class Renderer {

    public final int shaderProgramId;
    public final int transformMatrixAddress;
    public final int modelMatrixAddress;
    public final int viewMatrixAddress;
    public final int projectionMatrixAddress;
    public final int colorUniformAddress;
    public final float[] floatBuffer;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    public final GLCapabilities capabilities;


    public Renderer(int screenWidth, int screenHeight) {

        this.floatBuffer = new float[16];
        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.ortho(0, screenWidth, 0, screenHeight, -1, 1);

        this.viewMatrix = new Matrix4f();
        this.viewMatrix.scale(1);
        this.viewMatrix.translate(0, 0, 0);

        // init gl capabilities for current context
        this.capabilities = GL.createCapabilities();

        // create shader program
        // String vertexPath = MainWindowGLFW.class.getResource("vertex_shader.vert").getPath();
        // String vertexShaderSource = Files.readString(new File(vertexPath).toPath(), StandardCharsets.UTF_8);
        // String vertexShaderSource = new String(this.getClass().getResourceAsStream("vertex_shader.vert").readAllBytes(), StandardCharsets.UTF_8);

        // String fragmentPath = MainWindowGLFW.class.getResource("fragment_shader.frag").getPath();
        // String fragmentShaderSource = Files.readString(new File(fragmentPath).toPath(), StandardCharsets.UTF_8);
        // String fragmentShaderSource = new String(this.getClass().getResourceAsStream("fragment_shader.frag").readAllBytes(), StandardCharsets.UTF_8);

        String vertexShaderSource = """
        #version 330 core
        
        layout (location = 0) in vec3 vertex_position;
        
        uniform mat4 transform_matrix;
        uniform mat4 projection_matrix;
        uniform mat4 view_matrix;
        uniform mat4 model_matrix;
        
        
        void main() {
        
            // todo: calculate gl_Position = projection_matrix * transform_matrix * model_matrix
        
            // gl_Position = transform_matrix * vec4(vertex_position, 1.0);
            gl_Position = projection_matrix * view_matrix * model_matrix * vec4(vertex_position, 1.0);
        }
        """;

        String fragmentShaderSource = """
        #version 330 core
        
        uniform vec4 uniform_color;
        layout (location = 0) out vec4 fragment_color;
        
        
        void main() {
            fragment_color = uniform_color;
        }
        """;

        int vertexShaderId = GL20C.glCreateShader(GL20C.GL_VERTEX_SHADER);
        GL20C.glShaderSource(vertexShaderId, vertexShaderSource);
        GL20C.glCompileShader(vertexShaderId);

        int vertexCompileStatus = GL30C.glGetShaderi(vertexShaderId, GL30C.GL_COMPILE_STATUS);
        if (vertexCompileStatus != GL30C.GL_TRUE) {
            String info = GL30C.glGetShaderInfoLog(vertexShaderId);
            throw new RuntimeException(info);
        }

        int fragmentShaderId = GL20C.glCreateShader(GL20C.GL_FRAGMENT_SHADER);
        GL20C.glShaderSource(fragmentShaderId, fragmentShaderSource);
        GL20C.glCompileShader(fragmentShaderId);

        int fragmentCompileStatus = GL30C.glGetShaderi(fragmentShaderId, GL30C.GL_COMPILE_STATUS);
        if (fragmentCompileStatus != GL30C.GL_TRUE) {
            String info = GL30C.glGetShaderInfoLog(fragmentShaderId);
            throw new RuntimeException(info);
        }

        this.shaderProgramId = GL20C.glCreateProgram();
        GL20C.glAttachShader(this.shaderProgramId, vertexShaderId);
        GL20C.glAttachShader(this.shaderProgramId, fragmentShaderId);

        GL20C.glLinkProgram(this.shaderProgramId);

        int linkStatus = GL30C.glGetProgrami(this.shaderProgramId, GL30C.GL_LINK_STATUS);
        if (linkStatus != GL30C.GL_TRUE) {
            String info = GL30C.glGetProgramInfoLog(this.shaderProgramId);
            throw new RuntimeException(info);
        }

        GL20C.glDetachShader(this.shaderProgramId, vertexShaderId);
        GL20C.glDetachShader(this.shaderProgramId, fragmentShaderId);
        GL20C.glDeleteShader(vertexShaderId);
        GL20C.glDeleteShader(fragmentShaderId);

        // get uniform addresses from shader
        this.modelMatrixAddress = GL30C.glGetUniformLocation(this.shaderProgramId, "model_matrix");
        this.viewMatrixAddress = GL30C.glGetUniformLocation(this.shaderProgramId, "view_matrix");
        this.projectionMatrixAddress = GL30C.glGetUniformLocation(this.shaderProgramId, "projection_matrix");
        this.transformMatrixAddress = GL30C.glGetUniformLocation(this.shaderProgramId, "transform_matrix");
        this.colorUniformAddress = GL30C.glGetUniformLocation(this.shaderProgramId, "uniform_color");

        // todo: add error checking for shader uniform addresses

        return;
    }


    public void clearScreen() {

        float saturation = 0.40f;
        long currentFrameTimeMs = System.currentTimeMillis();
        float currentTimeDeltaS = (currentFrameTimeMs - MainWindowGLFW.GAME_START_TIME_MS) / 1000.00f;

        float red = (float) (Math.sin(currentTimeDeltaS) * saturation + (1.00f - saturation));
        float green = (float) (Math.sin(currentTimeDeltaS + 2.00 * Math.PI / 3.00) * saturation + (1.00f - saturation));
        float blue = (float) (Math.sin(currentTimeDeltaS + 4.00 * Math.PI / 3.00) * saturation + (1.00f - saturation));

        // set clear color
        GL11C.glClearColor(red, green, blue, 1.00f);

        // clear screen
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

        return;
    }


    public void updateViewMatrix(Camera cameraView) {

        this.viewMatrix.identity();
        this.viewMatrix.translate(cameraView.offsetX, cameraView.offsetY, 0);
        this.viewMatrix.get(this.floatBuffer);

        return;
    }


    public void renderMapTerrain(SettlersMap gameMap) {

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

        // create and bind vao
        int vaoId = GL30C.glGenVertexArrays();
        GL30C.glBindVertexArray(vaoId);

        // create and bind vbo
        int vboId = GL30C.glGenBuffers();
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, vboId);

        // upload vertex buffer to gpu
        GL30C.glBufferData(GL30C.GL_ARRAY_BUFFER, mapVertexBuffer, GL30C.GL_STATIC_DRAW);

        // set vao properties for current vbo
        GL30C.glVertexAttribPointer(0, 3, GL30C.GL_FLOAT, false, 0, 0);

        // set start index for current vao
        GL30C.glEnableVertexAttribArray(0);

        // unbind vbo and vao
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, 0);
        GL30C.glBindVertexArray(0);

        // activate shader
        GL30C.glUseProgram(this.shaderProgramId);

        // upload projection matrix
        this.projectionMatrix.get(this.floatBuffer);
        GL30C.glUniformMatrix4fv(this.projectionMatrixAddress, false, this.floatBuffer);

        // upload view matrix
        this.viewMatrix.get(this.floatBuffer);
        GL30C.glUniformMatrix4fv(this.viewMatrixAddress, false, this.floatBuffer);

        // todo: add modelMatrix to gameMap object

        // upload model matrix
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.translate(50, 50, 0);
        modelMatrix.scale(100, 100, 1);

        modelMatrix.get(this.floatBuffer);
        GL30C.glUniformMatrix4fv(this.modelMatrixAddress, false, this.floatBuffer);

        // set color uniform
        GL30C.glUniform4f(this.colorUniformAddress, 0.00f, 0.50f, 0.50f, 1.00f);

        // bind vao and vbo
        GL30C.glBindVertexArray(vaoId);
        GL30C.glEnableVertexAttribArray(0);

        // draw buffer
        GL30C.glDrawArrays(GL11C.GL_TRIANGLE_FAN, 0, 4);

        // cleanup
        GL30C.glDisableVertexAttribArray(0);
        GL30C.glBindVertexArray(0);
        GL30C.glUseProgram(0);

        return;
    }


    public void renderStaticObjects() {
        return;
    }


    public void renderNonStaticObjects() {
        return;
    }


    public void renderTeamObjects() {
        return;
    }


    public void renderGui() {
        return;
    }


    /*
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
    */


    /*
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
            float currentTimeDelta = (currentTime - MainWindowGLFW.GAME_START_TIME_MS) / 1000.00f;

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
    */


    /*
    public void renderTestFrame() {

        // create vertex buffer
        float[] vertexList = {
            0.00f, 0.00f, 0.00f,  // bottom left
            1.00f, 0.00f, 0.00f,  // bottom right
            1.00f, 1.00f, 0.00f,  // top right
            0.00f, 1.00f, 0.00f,  // top left
        };

        // create and bind vao
        int vaoId = GL30C.glGenVertexArrays();
        GL30C.glBindVertexArray(vaoId);

        // create and bind vbo
        int vboId = GL30C.glGenBuffers();
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, vboId);

        // upload vertex buffer to gpu
        GL30C.glBufferData(GL30C.GL_ARRAY_BUFFER, vertexList, GL30C.GL_STATIC_DRAW);

        // set vao properties for current vbo
        GL30C.glVertexAttribPointer(0, 3, GL30C.GL_FLOAT, false, 0, 0);

        // set start index for current vao
        GL30C.glEnableVertexAttribArray(0);

        // unbind vbo and vao
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, 0);
        GL30C.glBindVertexArray(0);

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

            float currentTime = (System.currentTimeMillis() - MainWindowGLFW.GAME_START_TIME_MS) / 1000.00f;

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
            GL30C.glUseProgram(this.shaderProgramId);

            // set transform matrix uniform
            transformMatrix.get(transformMatrixArray);

            GL30C.glUniformMatrix4fv(this.transformMatrixAddress, false, transformMatrixArray);

            // set color uniform
            GL30C.glUniform4f(this.colorUniformAddress, 1.00f, 0.00f, 0.00f, 1.00f);

            // bind vao and vbo
            GL30C.glBindVertexArray(vaoId);
            GL30C.glEnableVertexAttribArray(0);

            // draw buffer
            GL30C.glDrawArrays(GL11C.GL_QUADS, 0, 4);

            // cleanup
            GL30C.glDisableVertexAttribArray(0);
            GL30C.glBindVertexArray(0);
            GL30C.glUseProgram(0);

            // swap buffers
            GLFW.glfwSwapBuffers(this.window.windowId);
        }

        return;
    }
    */
}


/**
 * this class handles all camera related functions like panning and zooming.
 */
class Camera implements MouseListener, CursorListener, KeyListener {

    public float offsetX;
    public float offsetY;
    public float prevCursorX;
    public float prevCursorY;
    public boolean lmbPressed;
    public boolean rmbPressed;
    public boolean mmbPressed;
    public boolean keyUpPressed;
    public boolean keyDownPressed;
    public boolean keyLeftPressed;
    public boolean keyRightPressed;


    public Camera(EventManager eventManager) {

        this.offsetX = 0;
        this.offsetY = 0;
        this.prevCursorX = 0;
        this.prevCursorY = 0;
        this.lmbPressed = false;
        this.rmbPressed = false;
        this.mmbPressed = false;
        this.keyUpPressed = false;
        this.keyDownPressed = false;
        this.keyLeftPressed = false;
        this.keyRightPressed = false;

        eventManager.addMouseListener(this);
        eventManager.addCursorListener(this);
        eventManager.addKeyListener(this);

        return;
    }


    @Override
    public void onMouseEvent(MouseEvent event) {

        if (event.action() == GLFW.GLFW_PRESS) {

            switch (event.button()) {

                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> this.rmbPressed = true;
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> this.lmbPressed = true;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> this.mmbPressed = true;

                default -> {
                    // do nothing
                }
            }
        }

        else {

            switch (event.button()) {

                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> this.rmbPressed = false;
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> this.lmbPressed = false;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> this.mmbPressed = false;

                default -> {
                    // do nothing
                }
            }
        }

        return;
    }


    @Override
    public void onCursorEvent(CursorEvent event) {

        /*
        note:

        glfw screen coordinates are calculated from top-left to bottom right
        opengl coordinates are bottom-left to top-right
        deltaY needs to be calculated inverted (i.e. previous - current) so that moves up are positive and down are negative
        */

        float deltaX = (float) event.xpos() - this.prevCursorX;
        float deltaY = this.prevCursorY - (float) event.ypos();  // deltaY needs to be inverted so that moves up are positive

        if (this.rmbPressed) {

            // note: when panning using rmb we apply delta negatively to simulate moving the camera

            this.offsetX -= deltaX;
            this.offsetY -= deltaY;
        }

        else if (this.mmbPressed) {

            // note: when panning using mmb we apply delta positively to simulate dragging the map

            this.offsetX += deltaX;
            this.offsetY += deltaY;
        }

        this.prevCursorX = (float) event.xpos();
        this.prevCursorY = (float) event.ypos();

        return;
    }


    @Override
    public void onKeyEvent(KeyEvent event) {

        if (event.action() == GLFW.GLFW_PRESS) {

            switch (event.key()) {

                case GLFW.GLFW_KEY_UP -> this.keyUpPressed = true;
                case GLFW.GLFW_KEY_DOWN -> this.keyDownPressed = true;
                case GLFW.GLFW_KEY_LEFT -> this.keyLeftPressed = true;
                case GLFW.GLFW_KEY_RIGHT -> this.keyRightPressed = true;

                default -> {
                    // do nothing
                }
            }
        }

        else if (event.action() == GLFW.GLFW_RELEASE) {

            switch (event.key()) {

                case GLFW.GLFW_KEY_UP -> this.keyUpPressed = false;
                case GLFW.GLFW_KEY_DOWN -> this.keyDownPressed = false;
                case GLFW.GLFW_KEY_LEFT -> this.keyLeftPressed = false;
                case GLFW.GLFW_KEY_RIGHT -> this.keyRightPressed = false;

                default -> {
                    // do nothing
                }
            }
        }

        else {
            // do nothing
        }

        return;
    }


    public void updateCameraPosition(long frameTimeDeltaNs) {

        // todo: add separate camera class

        // update based on key press
        // update based on mouse movement while pressed
        // how do i detect a key was pressed/released within a single frame?

        float vectorX = 0.00f;
        float vectorY = 0.00f;

        if (this.keyUpPressed) {
            vectorY -= 1.00f;
        }

        if (this.keyDownPressed) {
            vectorY += 1.00f;
        }

        if (this.keyLeftPressed) {
            vectorX += 1.00f;
        }

        if (this.keyRightPressed) {
            vectorX -= 1.00f;
        }

        if (vectorX != 0 || vectorY != 0) {

            final float FRAME_TIME_DELTA_S = frameTimeDeltaNs / 1_000_000_000.00f;
            final float CAMERA_SPEED_UPS = 400.00f;  // units per second

            float vectorMagnitude = (float) Math.sqrt(vectorX * vectorX + vectorY * vectorY);

            float normalX = vectorX / vectorMagnitude;
            float normalY = vectorY / vectorMagnitude;

            float distance = CAMERA_SPEED_UPS * FRAME_TIME_DELTA_S;

            float deltaX = normalX * distance;
            float deltaY = normalY * distance;

            this.offsetX += deltaX;
            this.offsetY += deltaY;
        }

        return;
    }
}


public class MainWindowGLFW {

    public static final long GAME_START_TIME_MS = System.currentTimeMillis();

    public long startFrameTimeNs;
    public SettlersMap gameMap;

    public final Window window;
    public final Renderer renderer;
    public final Camera camera;
    public final EventManager eventManager;


    public MainWindowGLFW() throws Exception {

        this.eventManager = new EventManager();
        this.window = new Window(this.eventManager);
        this.renderer = new Renderer(this.window.width, this.window.height);
        this.camera = new Camera(this.eventManager);

        return;
    }


    public void renderGame(SettlersMap gameMap) {

        this.startFrameTimeNs = System.nanoTime();

        while (this.window.shouldClose() == false) {

            /*
            - calculate frame time
            - get all input
            - calculate camera position
            - calculate game state
            - render game based on last_state and current_state

            note:

            when using separate game thread simulation you need a state buffer
            this means all state objects are being calculated inside the game thread
            and the render thread keeps a copy of all game objects for rendering
            when the game thread finishes running a new state it swaps its state buffer
            with the renderer's buffer

            this way the render thread always has a state ready to render and
            the game thread doesn't need to lock the state list

            todo: implement this method using just the map terrain first, then add other types of objects to simulation/rendering
            */

            // calculate frame time
            long endFrameTimeNs = System.nanoTime();
            long frameTimeDeltaNs = endFrameTimeNs - this.startFrameTimeNs;
            this.startFrameTimeNs = endFrameTimeNs;

            // poll events
            this.window.pollEvents();

            // update camera position
            this.camera.updateCameraPosition(frameTimeDeltaNs);
            this.renderer.updateViewMatrix(this.camera);

            // clear screen
            this.renderer.clearScreen();

            // render game state
            this.renderer.renderMapTerrain(gameMap);
            this.renderer.renderStaticObjects();
            this.renderer.renderNonStaticObjects();
            this.renderer.renderTeamObjects();
            this.renderer.renderGui();

            // swap buffers
            this.window.swapBuffers();

            continue;
        }

        return;
    }


    public void startGame() throws Exception {

        // create map instance
        SettlersMap gameMap = new SettlersMap();

        // start game thread
        SettlersGame gameSimulation = new SettlersGame(gameMap);
        Thread gameThread = new Thread(gameSimulation, "GameSimulationThread");

        // gameThread.start();

        /*
        note:

        instead of a game thread we should schedule a lockstep task to run ever n seconds
        the game simulation doesn't do anything outside the lockstep task
        */

        // start rendering
        // this.renderBuildingFrame();
        // this.renderGameFrame();
        // this.renderTestFrame();
        this.renderGame(gameMap);

        gameMap.gameOver = true;
        gameSimulation.running = false;
        gameThread.join();

        System.out.printf("closing game\n");

        this.window.cleanup();

        return;
    }
}