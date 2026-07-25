package org.example.mainwindow;

import java.util.List;
import org.lwjgl.opengl.GL33C;
import org.example.shaders.SpriteShader;


/**
 * OpenGL sprite resources: shader and unit quad mesh.
 */
public class SpriteLayer {

    public final SpriteShader shader;
    public final VertexBuffer quad;


    public SpriteLayer(Framebuffer canvas) {

        this.shader = new SpriteShader((float) canvas.width, (float) canvas.height);

        // Local unit quad: (0,0) top-left → (1,-1) bottom-right; UVs in [0,1].
        float[] vertices = {
            0.00f,  0.00f, 0.00f, 0.00f,
            0.00f, -1.00f, 0.00f, 1.00f,
            1.00f,  0.00f, 1.00f, 0.00f,
            1.00f, -1.00f, 1.00f, 1.00f,
        };

        int stride = 4 * Float.BYTES;
        List<VertexAttribute> attributes = List.of(
            new VertexAttribute(0, 2, GL33C.GL_FLOAT, false, stride, 0),
            new VertexAttribute(1, 2, GL33C.GL_FLOAT, false, stride, 2 * Float.BYTES)
        );

        this.quad = new VertexBuffer(vertices, attributes, GL33C.GL_STATIC_DRAW);

        return;
    }
}