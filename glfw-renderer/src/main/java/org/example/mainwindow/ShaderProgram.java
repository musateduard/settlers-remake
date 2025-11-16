package org.example.mainwindow;

import org.lwjgl.opengl.GL33C;


public class ShaderProgram {

    public final int shaderId;
    public final String vertexShaderSource;
    public final String fragmentShaderSource;


    public ShaderProgram(String vertexSource, String fragmentSource) {

        this.vertexShaderSource = vertexSource;
        this.fragmentShaderSource = fragmentSource;

        int vertexShaderId = GL33C.glCreateShader(GL33C.GL_VERTEX_SHADER);
        GL33C.glShaderSource(vertexShaderId, this.vertexShaderSource);
        GL33C.glCompileShader(vertexShaderId);

        int vertexCompileStatus = GL33C.glGetShaderi(vertexShaderId, GL33C.GL_COMPILE_STATUS);
        String vertexCompileInfo = GL33C.glGetShaderInfoLog(vertexShaderId);

        if (vertexCompileStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(vertexCompileInfo);
        }

        int fragmentShaderId = GL33C.glCreateShader(GL33C.GL_FRAGMENT_SHADER);
        GL33C.glShaderSource(fragmentShaderId, this.fragmentShaderSource);
        GL33C.glCompileShader(fragmentShaderId);

        int fragmentCompileStatus = GL33C.glGetShaderi(fragmentShaderId, GL33C.GL_COMPILE_STATUS);
        String fragmentCompileInfo = GL33C.glGetShaderInfoLog(fragmentShaderId);

        if (fragmentCompileStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(fragmentCompileInfo);
        }

        this.shaderId = GL33C.glCreateProgram();

        GL33C.glAttachShader(this.shaderId, vertexShaderId);
        GL33C.glAttachShader(this.shaderId, fragmentShaderId);
        GL33C.glLinkProgram(this.shaderId);

        int linkStatus = GL33C.glGetProgrami(this.shaderId, GL33C.GL_LINK_STATUS);
        String linkInfo = GL33C.glGetProgramInfoLog(this.shaderId);

        if (linkStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(linkInfo);
        }

        GL33C.glDetachShader(this.shaderId, vertexShaderId);
        GL33C.glDetachShader(this.shaderId, fragmentShaderId);
        GL33C.glDeleteShader(vertexShaderId);
        GL33C.glDeleteShader(fragmentShaderId);

        return;
    }


    public void activateShader() {
        GL33C.glUseProgram(this.shaderId);
        return;
    }
}