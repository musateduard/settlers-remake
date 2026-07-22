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

        this.id = Compiler.compileShader(vertex, fragment);

        return;
    }


    public void activate() {
        GL33C.glUseProgram(this.id);
        return;
    }
}