package org.example.mainwindow;

import org.lwjgl.opengl.GL33C;
import org.example.shaders.CanvasShader;
import static org.lwjgl.system.MemoryUtil.NULL;


public class Framebuffer {

    public final int width;  // note: width and height are currently fixed
    public final int height;  // note: width and height are currently fixed
    public final int framebufferId;
    public final int textureId;
    public final int depthBufferId;
    public final CanvasShader shader;


    public Framebuffer() {

        this.width = 800;
        this.height = 600;

        // create canvas frame buffer object
        this.framebufferId = GL33C.glGenFramebuffers();

        // bind canvas frame buffer
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, this.framebufferId);

        // create texture object for color buffer
        this.textureId = GL33C.glGenTextures();

        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, this.textureId);
        GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, GL33C.GL_RGB, this.width, this.height, 0, GL33C.GL_RGB, GL33C.GL_UNSIGNED_BYTE, NULL);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MIN_FILTER, GL33C.GL_LINEAR);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MAG_FILTER, GL33C.GL_LINEAR);

        GL33C.glFramebufferTexture2D(GL33C.GL_FRAMEBUFFER, GL33C.GL_COLOR_ATTACHMENT0, GL33C.GL_TEXTURE_2D, this.textureId, 0);

        // create depth stencil buffer
        this.depthBufferId = GL33C.glGenRenderbuffers();

        GL33C.glBindRenderbuffer(GL33C.GL_RENDERBUFFER, this.depthBufferId);
        GL33C.glRenderbufferStorage(GL33C.GL_RENDERBUFFER, GL33C.GL_DEPTH24_STENCIL8, this.width, this.height);
        GL33C.glFramebufferRenderbuffer(GL33C.GL_FRAMEBUFFER, GL33C.GL_DEPTH_STENCIL_ATTACHMENT, GL33C.GL_RENDERBUFFER, this.depthBufferId);
        GL33C.glBindRenderbuffer(GL33C.GL_RENDERBUFFER, 0);

        // check frame buffer status
        int framebufferResult = GL33C.glCheckFramebufferStatus(GL33C.GL_FRAMEBUFFER);

        if (framebufferResult != GL33C.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("failed to create frame buffer object");
        }

        // unbind frame buffer
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0);

        int openglError = GL33C.glGetError();
        if (openglError != GL33C.GL_NO_ERROR) {
            throw new RuntimeException("opengl error occurred %d".formatted(openglError));
        }

        this.shader = new CanvasShader((float) this.width, (float) this.height);
        this.shader.updateProjectionMatrix(this.width, this.height);

        return;
    }
}