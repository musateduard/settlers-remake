package org.example.mainwindow;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GLCapabilities;
import org.example.gamemap.SettlersMap;


/**
 * this class handles all low level rendering functions related to opengl.
 */
public class Renderer {

    public final int shaderProgramId;
    public final int transformMatrixAddress;
    public final int modelMatrixAddress;
    public final int viewMatrixAddress;
    public final int projectionMatrixAddress;
    public final int colorUniformAddress;
    public final float[] floatBuffer;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    // public final GLCapabilities capabilities;


    public Renderer(int screenWidth, int screenHeight) {

        this.floatBuffer = new float[16];
        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.ortho(0, screenWidth, 0, screenHeight, -1, 1);

        this.viewMatrix = new Matrix4f();
        this.viewMatrix.scale(1);
        this.viewMatrix.translate(0, 0, 0);

        // todo: add imgui initialization to renderer

        // init gl capabilities for current context
        // this.capabilities = GL.createCapabilities();

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
        float currentTimeDeltaS = (currentFrameTimeMs - MainLauncher.GAME_START_TIME_MS) / 1000.00f;

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