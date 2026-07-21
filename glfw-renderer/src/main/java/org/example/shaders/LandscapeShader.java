package org.example.shaders;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33C;


public class LandscapeShader {

    public final int id;
    public final int projectionMatrixUniform;
    public final int viewMatrixUniform;
    public final int heightUniform;
    public final int texHandleUniform;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    public final float[] heightMatrix;
    public final float[] buffer;


    public LandscapeShader(float width, float height, int mapHeight) {

        String vertex = """
        #version 330 core

        layout(location = 0) in vec3 vertex;
        layout(location = 1) in vec2 texcoord;
        layout(location = 2) in float color;

        uniform mat4 globalTransform;
        uniform mat4 projection;
        uniform mat4 height;

        out float frag_color;
        out vec2 frag_texcoord;

        void main() {
            vec4 transformed = height * vec4(vertex, 1.0);
            transformed.z = -0.1;
            gl_Position = projection * globalTransform * transformed;
            frag_color = color;
            frag_texcoord = texcoord;
        }
        """;

        String fragment = """
        #version 330 core

        in float frag_color;
        in vec2 frag_texcoord;

        out vec4 fragColor;

        uniform sampler2D texHandle;

        void main() {
            fragColor = texture(texHandle, frag_texcoord);
            fragColor.rgb *= frag_color;
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

        this.projectionMatrixUniform = GL33C.glGetUniformLocation(this.id, "projection");
        this.viewMatrixUniform = GL33C.glGetUniformLocation(this.id, "globalTransform");
        this.heightUniform = GL33C.glGetUniformLocation(this.id, "height");
        this.texHandleUniform = GL33C.glGetUniformLocation(this.id, "texHandle");

        // get uniform addresses for landscape shader
        if (this.projectionMatrixUniform == -1) {
            throw new RuntimeException("invalid projectionMatrixUniform value: %d".formatted(this.projectionMatrixUniform));
        }

        if (this.viewMatrixUniform == -1) {
            throw new RuntimeException("invalid viewMatrixUniform value: %d".formatted(this.viewMatrixUniform));
        }

        if (this.heightUniform == -1) {
            throw new RuntimeException("invalid heightUniform value: %d".formatted(this.heightUniform));
        }

        if (this.texHandleUniform == -1) {
            throw new RuntimeException("invalid texHandleUniform value: %d".formatted(this.texHandleUniform));
        }

        int openglError = GL33C.glGetError();
        if (openglError != GL33C.GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred: %d".formatted(openglError));
        }

        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.ortho(0.00f, width, 0.00f, height, -1.00f, 1.00f);
        this.viewMatrix = new Matrix4f();
        this.viewMatrix.scale(1.00f);
        this.viewMatrix.translate(0.00f, 0.00f, 0.00f);
        this.buffer = new float[16];

        final float scaleX = 16f; // DrawConstants.DISTANCE_X
        final float scaleY = 9f;  // DrawConstants.DISTANCE_Y
        final int realMapHeight = mapHeight - 1;

        this.heightMatrix = new float[] {
            scaleX, 0.00f, 0.00f, 0.00f,
            -0.50f * scaleX, -scaleY, 0.00f, 0.00f,
            0.00f, 2.00f, 1.00f, 0.00f,
            realMapHeight * scaleX * 0.50f, realMapHeight * scaleY, 0.00f, 1.00f
        };

        return;
    }


    public void activate() {
        GL33C.glUseProgram(this.id);
        return;
    }
}