package org.example.mainwindow;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33C;

import static org.lwjgl.system.MemoryUtil.NULL;


public class Framebuffer {

    public final int width;  // note: width and height are currently fixed
    public final int height;  // note: width and height are currently fixed
    public final int framebufferId;
    public final int textureId;
    public final int depthBufferId;
    public final ShaderProgram shader;
    public final int modelMatrixAddress;
    public final int viewMatrixAddress;
    public final int projectionMatrixAddress;
    public final int colorUniformAddress;
    public final float[] floatBuffer;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;


    public Framebuffer() {

        this.width = 800;
        this.height = 600;

        // set canvas-space matrixes (fixed at canvas resolution)
        this.floatBuffer = new float[16];
        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.ortho(0, this.width, 0, this.height, -1, 1);
        this.viewMatrix = new Matrix4f();
        this.viewMatrix.scale(1);
        this.viewMatrix.translate(0, 0, 0);

        // create canvas frame buffer object
        this.framebufferId = GL33C.glGenFramebuffers();

        // bind canvas frame buffer
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, this.framebufferId);

        // create texture object for color buffer
        this.textureId = GL33C.glGenTextures();

        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, this.textureId);
        GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, GL33C.GL_RGB, this.width, this.height, 0, GL33C.GL_RGB, GL33C.GL_UNSIGNED_BYTE, NULL);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MIN_FILTER, GL33C.GL_LINEAR);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MAG_FILTER, GL33C.GL_LINEAR);
        GL33C.glFramebufferTexture2D(GL33C.GL_FRAMEBUFFER, GL33C.GL_COLOR_ATTACHMENT0, GL33C.GL_TEXTURE_2D, this.textureId, 0);

        // create depth stencil buffer
        this.depthBufferId = GL33C.glGenRenderbuffers();

        GL33C.glBindRenderbuffer(GL33C.GL_RENDERBUFFER, this.depthBufferId);
        GL33C.glRenderbufferStorage(GL33C.GL_RENDERBUFFER, GL33C.GL_DEPTH24_STENCIL8, this.width, this.height);
        GL33C.glFramebufferRenderbuffer(GL33C.GL_FRAMEBUFFER, GL33C.GL_DEPTH_STENCIL_ATTACHMENT, GL33C.GL_RENDERBUFFER, this.depthBufferId);
        GL33C.glBindRenderbuffer(GL33C.GL_RENDERBUFFER, 0);

        // check frame buffer status
        int framebufferResult = GL33C.glCheckFramebufferStatus(GL33C.GL_FRAMEBUFFER);

        if (framebufferResult != GL33C.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("failed to create frame buffer object");
        }

        // unbind frame buffer
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0);

        int openglError = GL33C.glGetError();
        if (openglError != GL33C.GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        // create canvas-space shader (renders game scene in canvas coordinates)
        String canvasVertexSource = """
        #version 330 core

        layout (location = 0) in vec3 vertex_position;

        uniform mat4 transform_matrix;
        uniform mat4 projection_matrix;
        uniform mat4 view_matrix;
        uniform mat4 model_matrix;


        void main() {
            gl_Position = projection_matrix * view_matrix * model_matrix * vec4(vertex_position, 1.0);
        }
        """;

        String canvasFragmentSource = """
        #version 330 core

        uniform vec4 uniform_color;
        layout (location = 0) out vec4 fragment_color;


        void main() {
            fragment_color = uniform_color;
        }
        """;

        this.shader = new ShaderProgram(canvasVertexSource, canvasFragmentSource);

        // get uniform addresses from canvas-space shader
        this.modelMatrixAddress = GL33C.glGetUniformLocation(this.shader.id, "model_matrix");
        this.viewMatrixAddress = GL33C.glGetUniformLocation(this.shader.id, "view_matrix");
        this.projectionMatrixAddress = GL33C.glGetUniformLocation(this.shader.id, "projection_matrix");
        this.colorUniformAddress = GL33C.glGetUniformLocation(this.shader.id, "uniform_color");

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

        openglError = GL33C.glGetError();
        if (openglError != GL33C.GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        this.updateProjectionMatrix(this.width, this.height);

        return;
    }


    public void updateProjectionMatrix(int newWidth, int newHeight) {

        // update projection matrix on the canvas-space shader
        this.shader.activate();

        this.projectionMatrix.identity();
        this.projectionMatrix.ortho(0, newWidth, 0, newHeight, -1, 1);
        this.projectionMatrix.get(this.floatBuffer);

        GL33C.glUniformMatrix4fv(this.projectionMatrixAddress, false, this.floatBuffer);

        // update viewport size and position
        GL33C.glViewport(0, 0, newWidth, newHeight);

        return;
    }


    public void updateViewMatrix(Camera cameraView) {

        this.shader.activate();

        this.viewMatrix.identity();
        this.viewMatrix.translate(cameraView.offsetX, cameraView.offsetY, 0);
        this.viewMatrix.get(this.floatBuffer);

        GL33C.glUniformMatrix4fv(this.viewMatrixAddress, false, this.floatBuffer);

        return;
    }
}