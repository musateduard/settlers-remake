package org.example.shaders;

import org.lwjgl.opengl.GL33C;


public class Compiler {

    static int compileShader(String vertexSource, String fragmentSource) {

        int vertexShaderId = GL33C.glCreateShader(GL33C.GL_VERTEX_SHADER);
        GL33C.glShaderSource(vertexShaderId, vertexSource);
        GL33C.glCompileShader(vertexShaderId);

        int vertexCompileStatus = GL33C.glGetShaderi(vertexShaderId, GL33C.GL_COMPILE_STATUS);
        String vertexCompileInfo = GL33C.glGetShaderInfoLog(vertexShaderId);

        if (vertexCompileStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(vertexCompileInfo);
        }

        int fragmentShaderId = GL33C.glCreateShader(GL33C.GL_FRAGMENT_SHADER);
        GL33C.glShaderSource(fragmentShaderId, fragmentSource);
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