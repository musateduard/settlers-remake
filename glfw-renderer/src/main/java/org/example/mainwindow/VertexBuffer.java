package org.example.mainwindow;

import org.lwjgl.opengl.GL33C;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_NO_ERROR;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;


/**
 * this class is currently only used for the screen vertex buffer.
 */
public class VertexBuffer {

    public final int vboId;
    public final int vaoId;


    public VertexBuffer() {

        // create viewport vbo and vao (full-screen quad in ndc)
        final int sizeof_float = 4;
        final float[] viewportVertexBuffer = {
            -1.00f, -1.00f, 0.00f, 0.00f, 0.00f,  // bottom left
            -1.00f,  1.00f, 0.00f, 0.00f, 1.00f,  // top left
            1.00f,  -1.00f, 0.00f, 1.00f, 0.00f,  // bottom right
            1.00f,   1.00f, 0.00f, 1.00f, 1.00f,  // top right
        };

        // create viewport vbo and vao
        this.vboId = GL33C.glGenBuffers();
        this.vaoId = GL33C.glGenVertexArrays();

        // bind viewport vbo
        GL33C.glBindBuffer(GL_ARRAY_BUFFER, this.vboId);

        // upload vbo to gpu
        GL33C.glBufferData(GL_ARRAY_BUFFER, viewportVertexBuffer, GL_STATIC_DRAW);

        // bind viewport vao
        GL33C.glBindVertexArray(this.vaoId);

        // define position attribute of viewport buffer
        GL33C.glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * sizeof_float, 0);
        GL33C.glEnableVertexAttribArray(0);

        // define uv attribute of viewport buffer
        GL33C.glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * sizeof_float, 3 * sizeof_float);
        GL33C.glEnableVertexAttribArray(1);

        // unbind viewport buffers
        GL33C.glBindVertexArray(0);
        GL33C.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int openglError = GL33C.glGetError();
        if (openglError != GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        return;
    }
}