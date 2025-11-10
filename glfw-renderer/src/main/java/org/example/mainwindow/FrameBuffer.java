package org.example.mainwindow;

import org.lwjgl.opengl.GL33C;
import static org.lwjgl.system.MemoryUtil.NULL;


public class FrameBuffer {

    public final int width;
    public final int height;
    public final int frameBufferId;
    public final int textureId;
    public final int depthStencilBufferId;
    public final int shaderId;
    public final int canvasVboId;
    public final int canvasVaoId;


    public FrameBuffer() {

        this.width = 800;
        this.height = 600;

        // create canvas frame buffer object
        this.frameBufferId = GL33C.glGenFramebuffers();

        // bind canvas frame buffer
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, this.frameBufferId);

        // create texture object for color buffer
        this.textureId = GL33C.glGenTextures();

        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, this.textureId);
        GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, GL33C.GL_RGB, this.width, this.height, 0, GL33C.GL_RGB, GL33C.GL_UNSIGNED_BYTE, NULL);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MIN_FILTER, GL33C.GL_LINEAR);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MAG_FILTER, GL33C.GL_LINEAR);
        GL33C.glFramebufferTexture2D(GL33C.GL_FRAMEBUFFER, GL33C.GL_COLOR_ATTACHMENT0, GL33C.GL_TEXTURE_2D, this.textureId, 0);

        // create depth stencil buffer
        this.depthStencilBufferId = GL33C.glGenRenderbuffers();

        GL33C.glBindRenderbuffer(GL33C.GL_RENDERBUFFER, this.depthStencilBufferId);
        GL33C.glRenderbufferStorage(GL33C.GL_RENDERBUFFER, GL33C.GL_DEPTH24_STENCIL8, this.width, this.height);
        GL33C.glFramebufferRenderbuffer(GL33C.GL_FRAMEBUFFER, GL33C.GL_DEPTH_STENCIL_ATTACHMENT, GL33C.GL_RENDERBUFFER, this.depthStencilBufferId);
        GL33C.glBindRenderbuffer(GL33C.GL_RENDERBUFFER, 0);

        // check frame buffer status
        int frameBufferResult = GL33C.glCheckFramebufferStatus(GL33C.GL_FRAMEBUFFER);

        if (frameBufferResult != GL33C.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("failed to create frame buffer object");
        }

        // unbind frame buffer
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0);

        int openglError = GL33C.glGetError();
        if (openglError != GL33C.GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        // create canvas shader program
        String canvasVertexShaderSource = """
        #version 330 core

        layout (location = 0) in vec2 vertex_coordinate;
        layout (location = 1) in vec2 texture_offset;

        out vec2 texture_coordinate;


        void main() {
            gl_Position = vec4(vertex_coordinate.x, vertex_coordinate.y, 0.0, 1.0);
            texture_coordinate = texture_offset;
        }
        """;

        String canvasFragmentShaderSource = """
        #version 330 core

        in vec2 texture_coordinate;
        uniform sampler2D screen_texture;

        out vec4 fragment_color;


        void main() {
            fragment_color = texture(screen_texture, texture_coordinate);
        }
        """;

        this.shaderId = this.createShaderProgram(canvasVertexShaderSource, canvasFragmentShaderSource);

        // create canvas vbo and vao
        float[] canvasVertexBuffer = {
            -1.00f, -1.00f, 0.00f, 0.00f, 0.00f,  // bottom left
            -1.00f,  1.00f, 0.00f, 0.00f, 1.00f,  // top left
            1.00f, -1.00f, 0.00f, 1.00f, 0.00f,  // bottom right
            1.00f,  1.00f, 0.00f, 1.00f, 1.00f,  // top right
        };

        // create and bind vbo
        this.canvasVboId = GL33C.glGenBuffers();
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, this.canvasVboId);

        // upload vbo to gpu
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, canvasVertexBuffer, GL33C.GL_STATIC_DRAW);

        // create and bind canvas vao
        this.canvasVaoId = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(this.canvasVaoId);

        // define position attribute of canvas buffer
        int sizeOfFloat = 4;
        GL33C.glVertexAttribPointer(0, 3, GL33C.GL_FLOAT, false, 5 * sizeOfFloat, 0);
        GL33C.glEnableVertexAttribArray(0);

        // define uv attribute of canvas buffer
        GL33C.glVertexAttribPointer(1, 2, GL33C.GL_FLOAT, false, 5 * sizeOfFloat, 3 * sizeOfFloat);
        GL33C.glEnableVertexAttribArray(1);

        // unbind canvas vao
        GL33C.glBindVertexArray(0);

        // unbind vbo
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0);

        openglError = GL33C.glGetError();
        if (openglError != GL33C.GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        return;
    }


    public int createShaderProgram(String vertexShaderSource, String fragmentShaderSource) {

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

        int shaderId = GL33C.glCreateProgram();
        GL33C.glAttachShader(shaderId, vertexShaderId);
        GL33C.glAttachShader(shaderId, fragmentShaderId);

        GL33C.glLinkProgram(shaderId);

        int linkStatus = GL33C.glGetProgrami(shaderId, GL33C.GL_LINK_STATUS);
        String linkInfo = GL33C.glGetProgramInfoLog(shaderId);

        if (linkStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(linkInfo);
        }

        GL33C.glDetachShader(shaderId, vertexShaderId);
        GL33C.glDetachShader(shaderId, fragmentShaderId);
        GL33C.glDeleteShader(vertexShaderId);
        GL33C.glDeleteShader(fragmentShaderId);

        return shaderId;
    }
}