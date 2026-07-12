package org.example.mainwindow;

import org.joml.Matrix4f;
import java.awt.Rectangle;
import org.lwjgl.opengl.GL33C;

import static org.lwjgl.system.MemoryUtil.NULL;


public class FrameBuffer {

    public final int width;
    public final int height;
    public final int frameBufferId;
    public final int textureId;
    public final int depthStencilBufferId;
    public final ShaderProgram shaderProgram;
    public final int canvasVboId;
    public final int canvasVaoId;
    public final Matrix4f projectionMatrix;
    public final Rectangle viewport;


    public FrameBuffer() {

        this.width = 800;
        this.height = 600;
        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.ortho(0, this.width, 0, this.height, -1, 1);
        this.viewport = new Rectangle(0, 0, this.width, this.height);

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
        String canvasVertexSource = """
        #version 330 core

        layout (location = 0) in vec2 vertex_coordinate;
        layout (location = 1) in vec2 texture_offset;

        out vec2 texture_coordinate;


        void main() {
            gl_Position = vec4(vertex_coordinate.x, vertex_coordinate.y, 0.0, 1.0);
            texture_coordinate = texture_offset;
        }
        """;

        String canvasFragmentSource = """
        #version 330 core

        in vec2 texture_coordinate;
        uniform sampler2D screen_texture;

        out vec4 fragment_color;


        void main() {
            fragment_color = texture(screen_texture, texture_coordinate);
        }
        """;

        this.shaderProgram = new ShaderProgram(canvasVertexSource, canvasFragmentSource);

        // create canvas vbo and vao
        float[] canvasVertexBuffer = {
            -1.00f, -1.00f, 0.00f, 0.00f, 0.00f,  // bottom left
            -1.00f,  1.00f, 0.00f, 0.00f, 1.00f,  // top left
            1.00f,  -1.00f, 0.00f, 1.00f, 0.00f,  // bottom right
            1.00f,   1.00f, 0.00f, 1.00f, 1.00f,  // top right
        };

        // create and bind canvas vbo
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
}