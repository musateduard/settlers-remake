package org.example.shaders;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33C;
import org.example.mainwindow.Camera;


public class CanvasShader {

    public final int id;
    public final int modelMatrixAddress;
    public final int viewMatrixAddress;
    public final int projectionMatrixAddress;
    public final int colorUniformAddress;
    public final float[] buffer;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;


    public CanvasShader(float width, float height) {

        this.id = CanvasShader.createShaderProgram();
        this.buffer = new float[16];
        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.ortho(0.00f, width, 0.00f, height, -1.00f, 1.00f);
        this.viewMatrix = new Matrix4f();
        this.viewMatrix.scale(1.00f);
        this.viewMatrix.translate(0.00f, 0.00f, 0.00f);

        // get uniform addresses from canvas-space shader
        this.modelMatrixAddress = GL33C.glGetUniformLocation(this.id, "model_matrix");
        this.viewMatrixAddress = GL33C.glGetUniformLocation(this.id, "view_matrix");
        this.projectionMatrixAddress = GL33C.glGetUniformLocation(this.id, "projection_matrix");
        this.colorUniformAddress = GL33C.glGetUniformLocation(this.id, "uniform_color");

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

        int error = GL33C.glGetError();
        if (error != GL33C.GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(error));
        }

        return;
    }


    public static int createShaderProgram() {

        String vertex = """
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

        String fragment = """
        #version 330 core

        uniform vec4 uniform_color;
        layout (location = 0) out vec4 fragment_color;


        void main() {
            fragment_color = uniform_color;
        }
        """;

        int vertexShaderId = GL33C.glCreateShader(GL33C.GL_VERTEX_SHADER);
        GL33C.glShaderSource(vertexShaderId, vertex);
        GL33C.glCompileShader(vertexShaderId);

        int vertexCompileStatus = GL33C.glGetShaderi(vertexShaderId, GL33C.GL_COMPILE_STATUS);
        String vertexCompileInfo = GL33C.glGetShaderInfoLog(vertexShaderId);

        if (vertexCompileStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(vertexCompileInfo);
        }

        int fragmentShaderId = GL33C.glCreateShader(GL33C.GL_FRAGMENT_SHADER);
        GL33C.glShaderSource(fragmentShaderId, fragment);
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


    public void activate() {
        GL33C.glUseProgram(this.id);
        return;
    }


    public void updateProjectionMatrix(int newWidth, int newHeight) {

        this.activate();

        this.projectionMatrix.identity();
        this.projectionMatrix.ortho(0, newWidth, 0, newHeight, -1, 1);
        this.projectionMatrix.get(this.buffer);

        GL33C.glUniformMatrix4fv(this.projectionMatrixAddress, false, this.buffer);

        // update viewport size and position
        GL33C.glViewport(0, 0, newWidth, newHeight);

        return;
    }


    public void updateViewMatrix(Camera cameraView) {

        this.activate();

        this.viewMatrix.identity();
        this.viewMatrix.translate(cameraView.offsetX, cameraView.offsetY, 0);
        this.viewMatrix.get(this.buffer);

        GL33C.glUniformMatrix4fv(this.viewMatrixAddress, false, this.buffer);

        return;
    }
}