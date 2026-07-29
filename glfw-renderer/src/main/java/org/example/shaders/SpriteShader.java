package org.example.shaders;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33C;


/**
 * Textured sprite quad with player tint and fog-of-war multiply.
 * Unit geometry: (0,0) top-left to (1,-1) bottom-right in local space.
 * Use tint (1,1,1) for normal sprites; player color for ColorSprite masks.
 */
public class SpriteShader {

    public final int id;
    public final int projectionMatrixUniform;
    public final int viewMatrixUniform;
    public final int modelMatrixUniform;
    public final int textureUniform;
    public final int fowUniform;
    public final int tintUniform;
    public final int uvRectUniform;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    public final Matrix4f modelMatrix;
    public final float[] buffer;


    public SpriteShader(float canvasWidth, float canvasHeight) {

        String vertex = """
        #version 330 core

        layout(location = 0) in vec2 vertex;
        layout(location = 1) in vec2 texcoord;

        uniform mat4 projection;
        uniform mat4 view;
        uniform mat4 model;
        uniform vec4 uvRect;

        out vec2 frag_texcoord;

        void main() {
            frag_texcoord = mix(uvRect.xy, uvRect.zw, texcoord);
            gl_Position = projection * view * model * vec4(vertex, 0.0, 1.0);
        }
        """;

        String fragment = """
        #version 330 core

        in vec2 frag_texcoord;

        out vec4 fragColor;

        uniform sampler2D texHandle;
        uniform float fow;
        uniform vec3 tint;

        void main() {
            vec4 color = texture(texHandle, frag_texcoord);
            if (color.a < 0.01) {
                discard;
            }
            fragColor = vec4(color.rgb * tint * fow, color.a);
        }
        """;

        this.id = Compiler.compileShader(vertex, fragment);
        this.projectionMatrixUniform = GL33C.glGetUniformLocation(this.id, "projection");
        this.viewMatrixUniform = GL33C.glGetUniformLocation(this.id, "view");
        this.modelMatrixUniform = GL33C.glGetUniformLocation(this.id, "model");
        this.textureUniform = GL33C.glGetUniformLocation(this.id, "texHandle");
        this.fowUniform = GL33C.glGetUniformLocation(this.id, "fow");
        this.tintUniform = GL33C.glGetUniformLocation(this.id, "tint");
        this.uvRectUniform = GL33C.glGetUniformLocation(this.id, "uvRect");

        if (this.projectionMatrixUniform == -1 ||
            this.viewMatrixUniform == -1 ||
            this.modelMatrixUniform == -1 ||
            this.textureUniform == -1 ||
            this.fowUniform == -1 ||
            this.tintUniform == -1 ||
            this.uvRectUniform == -1) {
            throw new RuntimeException("SpriteShader: missing uniform location");
        }

        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.ortho(0.00f, canvasWidth, 0.00f, canvasHeight, -1.00f, 1.00f);
        this.viewMatrix = new Matrix4f();
        this.modelMatrix = new Matrix4f();
        this.buffer = new float[16];
    }


    public void activate() {
        GL33C.glUseProgram(this.id);
    }
}
