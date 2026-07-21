package org.example.shaders;

import org.lwjgl.opengl.GL33C;


public class ScreenShader {

    public final int id;


    public ScreenShader() {

        // create screen shader program (projects canvas texture to screen)
        String vertex = """
        #version 330 core

        layout (location = 0) in vec2 vertex_coordinate;
        layout (location = 1) in vec2 texture_offset;

        out vec2 texture_coordinate;


        void main() {
            gl_Position = vec4(vertex_coordinate.x, vertex_coordinate.y, 0.0, 1.0);
            texture_coordinate = texture_offset;
        }
        """;

        String fragment = """
        #version 330 core

        in vec2 texture_coordinate;
        uniform sampler2D screen_texture;

        out vec4 fragment_color;


        void main() {
            fragment_color = texture(screen_texture, texture_coordinate);
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

        this.id = GL33C.glCreateProgram();

        GL33C.glAttachShader(this.id, vertexShaderId);
        GL33C.glAttachShader(this.id, fragmentShaderId);
        GL33C.glLinkProgram(this.id);

        int linkStatus = GL33C.glGetProgrami(this.id, GL33C.GL_LINK_STATUS);
        String linkInfo = GL33C.glGetProgramInfoLog(this.id);

        if (linkStatus != GL33C.GL_TRUE) {
            throw new RuntimeException(linkInfo);
        }

        GL33C.glDetachShader(this.id, vertexShaderId);
        GL33C.glDetachShader(this.id, fragmentShaderId);
        GL33C.glDeleteShader(vertexShaderId);
        GL33C.glDeleteShader(fragmentShaderId);

        return;
    }


    public void activate() {
        GL33C.glUseProgram(this.id);
        return;
    }
}