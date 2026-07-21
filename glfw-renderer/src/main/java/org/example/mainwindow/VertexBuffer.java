package org.example.mainwindow;

import java.util.List;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL33C;
import static org.lwjgl.opengl.GL11C.GL_NO_ERROR;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;


/**
 * Owns a VBO + VAO backed by a CPU-side {@code float[]} vertex array.
 */
public class VertexBuffer {

    public final float[] vertexList;
    public final int vboId;
    public final int vaoId;


    public VertexBuffer(float[] vertexList, List<VertexAttribute> attributeList, int usage) {

        this.vertexList = vertexList;
        this.vboId = GL33C.glGenBuffers();
        this.vaoId = GL33C.glGenVertexArrays();

        GL33C.glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        GL33C.glBufferData(GL_ARRAY_BUFFER, this.vertexList, usage);

        GL33C.glBindVertexArray(this.vaoId);
        GL33C.glBindBuffer(GL_ARRAY_BUFFER, this.vboId);

        for (VertexAttribute attribute : attributeList) {

            GL33C.glVertexAttribPointer(
                attribute.location(),
                attribute.size(),
                attribute.type(),
                attribute.normalized(),
                attribute.stride(),
                attribute.pointer()
            );

            GL33C.glEnableVertexAttribArray(attribute.location());
            continue;
        }

        GL33C.glBindVertexArray(0);
        GL33C.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int openglError = GL33C.glGetError();
        if (openglError != GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        return;
    }


    public void updateVertexBuffer(long offset, ByteBuffer buffer) {

        GL33C.glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        GL33C.glBufferSubData(GL_ARRAY_BUFFER, offset, buffer);

        return;
    }
}