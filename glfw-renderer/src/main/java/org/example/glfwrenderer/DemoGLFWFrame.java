package org.example.glfwrenderer;

import go.graphics.swing.opengl.LWJGLDrawContext;
import go.graphics.swing.sound.SwingSoundPlayer;
import jsettlers.common.Color;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.images.EImageLinkType;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.position.FloatRectangle;
import jsettlers.common.resources.ResourceManager;
import jsettlers.graphics.image.SettlerImage;
import jsettlers.graphics.map.ETextDrawPosition;
import jsettlers.graphics.map.MapContent;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.sound.SoundManager;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.loading.EMapStartResources;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.DirectoryMapLister;
import jsettlers.logic.player.InitialGameState;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGameGLFW;
import jsettlers.main.swing.resources.SwingResourceProvider;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.client.task.packets.TaskPacket;
import jsettlers.network.synchronic.timer.ITaskExecutor;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.GLFW;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;


class MyTaskExecutor implements ITaskExecutor {

    public MyTaskExecutor() {
        return;
    }


    @Override
    public void executeTask(TaskPacket task) {
        System.out.printf("executing task object: %s\n", task);
        return;
    }
}


public class DemoGLFWFrame {

    public final int width;
    public final int height;
    public final long windowId;
    public final long startTime;
    public final float saturation;
    public final GLCapabilities capabilities;


    public DemoGLFWFrame() {

        this.width = 800;
        this.height = 600;
        this.startTime = System.currentTimeMillis();
        this.saturation = 0.40f;

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

        // make window visible
        GLFW.glfwShowWindow(this.windowId);

        // init gl capabilities for current context
        this.capabilities = GL.createCapabilities();

        return;
    }


    public void renderTestFrame() {

        // create shader program
        String vertexSource = """
        #version 330 core

        layout (location = 0) in vec3 vertex_position;
        uniform mat4 transform_matrix;

        void main() {
            gl_Position = transform_matrix * vec4(vertex_position, 1.0);
        }
        """;

        String fragmentSource = """
        #version 330 core

        uniform vec4 uniform_color;
        layout (location = 0) out vec4 fragment_color;

        void main() {
            fragment_color = uniform_color;
        }
        """;

        int vertexShaderId = GL20C.glCreateShader(GL20C.GL_VERTEX_SHADER);
        GL20C.glShaderSource(vertexShaderId, vertexSource);
        GL20C.glCompileShader(vertexShaderId);

        int fragmentShaderId = GL20C.glCreateShader(GL20C.GL_FRAGMENT_SHADER);
        GL20C.glShaderSource(fragmentShaderId, fragmentSource);
        GL20C.glCompileShader(fragmentShaderId);

        int shaderProgramId = GL20C.glCreateProgram();
        GL20C.glAttachShader(shaderProgramId, vertexShaderId);
        GL20C.glAttachShader(shaderProgramId, fragmentShaderId);

        GL20C.glLinkProgram(shaderProgramId);

        GL20C.glDetachShader(shaderProgramId, vertexShaderId);
        GL20C.glDetachShader(shaderProgramId, fragmentShaderId);
        GL20C.glDeleteShader(vertexShaderId);
        GL20C.glDeleteShader(fragmentShaderId);

        // get uniform values from shader
        int transformMatrixAddress = GL30C.glGetUniformLocation(shaderProgramId, "transform_matrix");
        int colorUniformAddress = GL30C.glGetUniformLocation(shaderProgramId, "uniform_color");

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

        while (GLFW.glfwWindowShouldClose(this.windowId) == false) {

            float currentTime = (System.currentTimeMillis() - this.startTime) / 1000.00f;

            float red = (float) (Math.sin(currentTime) * this.saturation + (1.00f - this.saturation));
            float green = (float) (Math.sin(currentTime + 2.00 * Math.PI / 3.00) * this.saturation + (1.00f - this.saturation));
            float blue = (float) (Math.sin(currentTime + 4.00 * Math.PI / 3.00) * this.saturation + (1.00f - this.saturation));

            // set clear color
            GL11C.glClearColor(red, green, blue, 1.00f);

            // clear screen
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

            // poll events
            GLFW.glfwPollEvents();

            /*
            draw using glBegin and glEnd

            // activate projection mode
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();

            // define screen size
            GL11.glOrtho(0, this.width, 0, this.height, -1, 1);

            // activate model view
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            // set vertex color
            GL11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);

            // begin vertex draw
            GL11.glBegin(GL11C.GL_QUADS);

            GL11.glVertex2f(50.0f, 50.0f);
            GL11.glVertex2f(150.0f, 50.0f);
            GL11.glVertex2f(150.0f, 150.0f);
            GL11.glVertex2f(50.0f, 150.0f);

            // end vertex draw
            GL11.glEnd();
            */

            /*
            draw using shader
            */
            // activate shader
            GL30C.glUseProgram(shaderProgramId);

            // set transform matrix uniform
            transformMatrix.get(transformMatrixArray);

            GL30C.glUniformMatrix4fv(transformMatrixAddress, false, transformMatrixArray);

            // set color uniform
            GL30C.glUniform4f(colorUniformAddress, 1.00f, 0.00f, 0.00f, 1.00f);

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
            GLFW.glfwSwapBuffers(this.windowId);
        }

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

        while (GLFW.glfwWindowShouldClose(this.windowId) == false) {

            // set clear color
            GL11C.glClearColor(0.90f, 0.90f, 0.90f, 1.00f);

            // clear screen
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

            // poll events
            GLFW.glfwPollEvents();

            // note: need to configure model, view, projection matrix before drawing sprite to screen

            context.updateViewMatrix(0, 0, 0, 1, 1, 1);
            context.updateProjectionMatrix(this.width, this.height);

            testImage.drawAt(context, 100, 100, 0, Color.CYAN, 1.00f);

            // swap buffers
            GLFW.glfwSwapBuffers(this.windowId);
        }

        return;
    }


    public void renderGameFrame() throws Exception {

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
        JSettlersGameGLFW game = new JSettlersGameGLFW(selectedMap, initialGameState);

        game.networkConnector.getGameClock().setTaskExecutor(new MyTaskExecutor());

        MatchConstants.init(game.networkConnector.getGameClock(), randomSeed);
        MatchConstants.clock().setTaskExecutor(new MyTaskExecutor());

        JSettlersGameGLFW.GameRunner runner = (JSettlersGameGLFW.GameRunner) game.start();
        SwingSoundPlayer soundPlayer = new SwingSoundPlayer(SettingsManager.getInstance());

        // JSettlersFrame settlersFrame = new JSettlersFrame();
        // StartingGamePanel startPanel = new StartingGamePanel(settlersFrame);
        // runner.setListener(startPanel);

        // note: MapContent can only be instantiated after GameRunner.mainGrid is properly loaded
        while (runner.getMainGrid() == null) {
            Thread.sleep(100);
        }

        MapContent mapContent = new MapContent(runner, soundPlayer, ETextDrawPosition.DESKTOP);
        LWJGLDrawContext context = new LWJGLDrawContext(this.capabilities, false, 1.00f);

        SettlerImage testImage = (SettlerImage) ImageProvider.getInstance().getImage(new OriginalImageLink(EImageLinkType.SETTLER, 33, 11, 0));

        while (GLFW.glfwWindowShouldClose(this.windowId) == false) {

            float currentTime = (System.currentTimeMillis() - this.startTime) / 1000.00f;

            float red = (float) (Math.sin(currentTime) * this.saturation + (1.00f - this.saturation));
            float green = (float) (Math.sin(currentTime + 2.00 * Math.PI / 3.00) * this.saturation + (1.00f - this.saturation));
            float blue = (float) (Math.sin(currentTime + 4.00 * Math.PI / 3.00) * this.saturation + (1.00f - this.saturation));

            // set clear color
            // GL11C.glClearColor(0.80f, 0.80f, 0.80f, 1.00f);
            GL11C.glClearColor(red, green, blue, 1.00f);

            // clear screen
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

            // poll events
            GLFW.glfwPollEvents();

            /*
            note:

            drawContent doesn't work in current setup
            try rendering map elements separately one by one
             */

            // todo: render map terrain using mapContent

            // draw map terrain
            FloatRectangle screen = mapContent.mapContext.getScreen().getPosition().bigger(MapContent.SCREEN_PADDING);

            mapContent.drawContent(context, this.width, this.height);
            mapContent.drawMapTerrain(screen);

            // draw test sprite
            context.updateViewMatrix(0, 0, 0, 1, 1, 1);
            context.updateProjectionMatrix(this.width, this.height);

            testImage.drawAt(context, 100, 100, 0, Color.CYAN, 1.00f);

            // swap buffers
            GLFW.glfwSwapBuffers(this.windowId);
        }

        return;
    }


    public void start() throws Exception {

        // this.renderTestFrame();
        // this.renderBuildingFrame();
        this.renderGameFrame();

        System.out.printf("closing game\n");

        return;
    }


    public void cleanup() {

        GLFW.glfwDestroyWindow(this.windowId);
        GLFW.glfwTerminate();

        return;
    }
}