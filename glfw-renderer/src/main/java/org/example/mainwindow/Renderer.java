package org.example.mainwindow;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;
import org.example.gamemap.SettlersMap;
import org.lwjgl.system.MemoryUtil;


/**
 * this class handles all low level rendering functions related to opengl.
 */
public class Renderer implements ResizeListener {

    public final int frameBufferObjectId;
    public final int shaderProgramId;
    public final int modelMatrixAddress;
    public final int viewMatrixAddress;
    public final int projectionMatrixAddress;
    public final int colorUniformAddress;
    public final float[] floatBuffer;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    public final GLCapabilities glCapabilities;
    public final String glslVersion;


    public Renderer(Window window, EventManager eventManager) {

        if (window.windowId == MemoryUtil.NULL) {
            throw new RuntimeException("cannot initialize opengl context before creating glfw window");
        }

        // set projection matrix
        this.floatBuffer = new float[16];
        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.ortho(0, window.width, 0, window.height, -1, 1);

        // set view matrix
        this.viewMatrix = new Matrix4f();
        this.viewMatrix.scale(1);
        this.viewMatrix.translate(0, 0, 0);

        // init gl capabilities for current context
        this.glCapabilities = GL.createCapabilities();
        this.glslVersion = "#version 330";

        // create frame buffer object
        this.frameBufferObjectId = GL33C.glGenFramebuffers();

        int frameBufferStatus = GL33C.glCheckFramebufferStatus(GL33C.GL_FRAMEBUFFER);

        if (frameBufferStatus != GL33C.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("failed to create frame buffer object");
        }

        // bind frame buffer
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, this.frameBufferObjectId);

        // todo: declare and bind texture to buffer between these calls

        // create texture object
        int canvasTextureId = GL33C.glGenTextures();

        // todo: finish declaring canvas texture object

        // unbind frame buffer
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0);

        // create shader program
        // String vertexPath = Renderer.class.getResource("vertex_shader.vert").getPath();
        // String vertexShaderSource = Files.readString(new File(vertexPath).toPath(), StandardCharsets.UTF_8);
        // String vertexShaderSource = new String(this.getClass().getResourceAsStream("vertex_shader.vert").readAllBytes(), StandardCharsets.UTF_8);

        // String fragmentPath = Renderer.class.getResource("fragment_shader.frag").getPath();
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

        int vertexShaderId = GL33C.glCreateShader(GL33C.GL_VERTEX_SHADER);
        GL33C.glShaderSource(vertexShaderId, vertexShaderSource);
        GL33C.glCompileShader(vertexShaderId);

        int vertexCompileStatus = GL33C.glGetShaderi(vertexShaderId, GL33C.GL_COMPILE_STATUS);
        String vertexCompileInfo = GL33C.glGetShaderInfoLog(vertexShaderId);

        if (vertexCompileStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(vertexCompileInfo);
        }

        int fragmentShaderId = GL33C.glCreateShader(GL33C.GL_FRAGMENT_SHADER);
        GL33C.glShaderSource(fragmentShaderId, fragmentShaderSource);
        GL33C.glCompileShader(fragmentShaderId);

        int fragmentCompileStatus = GL33C.glGetShaderi(fragmentShaderId, GL33C.GL_COMPILE_STATUS);
        String fragmentCompileInfo = GL33C.glGetShaderInfoLog(fragmentShaderId);

        if (fragmentCompileStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(fragmentCompileInfo);
        }

        this.shaderProgramId = GL33C.glCreateProgram();
        GL33C.glAttachShader(this.shaderProgramId, vertexShaderId);
        GL33C.glAttachShader(this.shaderProgramId, fragmentShaderId);

        GL33C.glLinkProgram(this.shaderProgramId);

        int linkStatus = GL33C.glGetProgrami(this.shaderProgramId, GL33C.GL_LINK_STATUS);
        String linkInfo = GL33C.glGetProgramInfoLog(this.shaderProgramId);

        if (linkStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(linkInfo);
        }

        GL33C.glDetachShader(this.shaderProgramId, vertexShaderId);
        GL33C.glDetachShader(this.shaderProgramId, fragmentShaderId);
        GL33C.glDeleteShader(vertexShaderId);
        GL33C.glDeleteShader(fragmentShaderId);

        // get uniform addresses from shader
        this.modelMatrixAddress = GL33C.glGetUniformLocation(this.shaderProgramId, "model_matrix");
        this.viewMatrixAddress = GL33C.glGetUniformLocation(this.shaderProgramId, "view_matrix");
        this.projectionMatrixAddress = GL33C.glGetUniformLocation(this.shaderProgramId, "projection_matrix");
        this.colorUniformAddress = GL33C.glGetUniformLocation(this.shaderProgramId, "uniform_color");

        if (this.modelMatrixAddress == -1) {
            throw new RuntimeException("invalid modelMatrixAddress value: %d".formatted(this.modelMatrixAddress));
        }

        if (this.viewMatrixAddress == -1) {
            throw new RuntimeException("invalid viewMatrixAddress value: %d".formatted(this.viewMatrixAddress));
        }

        if (this.projectionMatrixAddress == -1) {
            throw new RuntimeException("invalid projectionMatrixAddress value: %d".formatted(this.projectionMatrixAddress));
        }

        if (this.colorUniformAddress == -1) {
            throw new RuntimeException("invalid colorUniformAddress value: %d".formatted(this.colorUniformAddress));
        }

        // register event listener
        eventManager.addResizeListener(this);

        return;
    }


    @Override
    public void onResizeEvent(ResizeEvent event) {

        /*
        note:

        on resize projection matrix width and height should be set to
        correspond to the internal frame buffer object size, not the screen size
        */

        this.updateProjectionMatrix(event.width(), event.height());

        return;
    }


    public void clearScreen() {

        float saturation = 0.40f;
        long currentFrameTimeMs = System.currentTimeMillis();
        float currentTimeDeltaS = (currentFrameTimeMs - MainLauncher.GAME_START_TIME_MS) / 1000.00f;

        float red = (float) (Math.sin(currentTimeDeltaS) * saturation + (1.00f - saturation));
        float green = (float) (Math.sin(currentTimeDeltaS + 2.00 * Math.PI / 3.00) * saturation + (1.00f - saturation));
        float blue = (float) (Math.sin(currentTimeDeltaS + 4.00 * Math.PI / 3.00) * saturation + (1.00f - saturation));

        // set clear color
        // GL33C.glClearColor(red, green, blue, 1.00f);
        GL33C.glClearColor(1.00f, 1.00f, 1.00f, 1.00f);

        // clear screen
        GL33C.glClear(GL33C.GL_COLOR_BUFFER_BIT | GL33C.GL_DEPTH_BUFFER_BIT);

        return;
    }


    public void updateProjectionMatrix(int width, int height) {

        GL33C.glUseProgram(this.shaderProgramId);
        GL33C.glViewport(0, 0, width, height);

        this.projectionMatrix.identity();
        this.projectionMatrix.ortho(0, width, 0, height, -1, 1);
        this.projectionMatrix.get(this.floatBuffer);

        GL33C.glUniformMatrix4fv(this.projectionMatrixAddress, false, this.floatBuffer);

        return;
    }


    public void updateViewMatrix(Camera cameraView) {

        GL33C.glUseProgram(this.shaderProgramId);

        this.viewMatrix.identity();
        this.viewMatrix.translate(cameraView.offsetX, cameraView.offsetY, 0);
        this.viewMatrix.get(this.floatBuffer);

        GL33C.glUniformMatrix4fv(this.viewMatrixAddress, false, this.floatBuffer);

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
        int vaoId = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(vaoId);

        // create and bind vbo
        int vboId = GL33C.glGenBuffers();
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, vboId);

        // upload vertex buffer to gpu
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, mapVertexBuffer, GL33C.GL_STATIC_DRAW);

        // set vao properties for current vbo
        GL33C.glVertexAttribPointer(0, 3, GL33C.GL_FLOAT, false, 0, 0);

        // set start index for current vao
        GL33C.glEnableVertexAttribArray(0);

        // unbind vbo and vao
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0);
        GL33C.glBindVertexArray(0);

        // activate shader
        GL33C.glUseProgram(this.shaderProgramId);

        // upload projection matrix
        // this.projectionMatrix.get(this.floatBuffer);
        // GL33C.glUniformMatrix4fv(this.projectionMatrixAddress, false, this.floatBuffer);

        // upload view matrix
        // this.viewMatrix.get(this.floatBuffer);
        // GL33C.glUniformMatrix4fv(this.viewMatrixAddress, false, this.floatBuffer);

        // todo: add modelMatrix to gameMap object

        // upload model matrix
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.translate(50, 50, 0);
        modelMatrix.scale(100, 100, 1);

        modelMatrix.get(this.floatBuffer);
        GL33C.glUniformMatrix4fv(this.modelMatrixAddress, false, this.floatBuffer);

        // set color uniform
        GL33C.glUniform4f(this.colorUniformAddress, 0.00f, 1.00f, 1.00f, 1.00f);

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


    public void renderStaticObjects() {
        return;
    }


    public void renderNonStaticObjects() {
        return;
    }


    public void renderTeamObjects() {
        return;
    }


    public void renderGameScene(long frameTimeDeltaNs, Window window, Camera camera, SettlersMap gameMap) {

        // update projection matrix
        this.updateProjectionMatrix(window.width, window.height);

        // update view matrix
        camera.updateCameraPosition(frameTimeDeltaNs);
        this.updateViewMatrix(camera);

        // render game scene
        this.renderMapTerrain(gameMap);
        this.renderStaticObjects();
        this.renderNonStaticObjects();
        this.renderTeamObjects();

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