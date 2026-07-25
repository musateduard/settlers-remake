package org.example.mainwindow;

import java.nio.IntBuffer;
import org.lwjgl.opengl.GL33C;


public class Texture {

    public final int id;
    public final int width;
    public final int height;
    public final int offsetX;
    public final int offsetY;


    public Texture(int width, int height, int offsetX, int offsetY, IntBuffer pixelBuffer) {

        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.id = GL33C.glGenTextures();

        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, this.id);
        GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, GL33C.GL_RGBA, width, height, 0, GL33C.GL_RGBA, GL33C.GL_UNSIGNED_INT_8_8_8_8, pixelBuffer);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_WRAP_S, GL33C.GL_CLAMP_TO_EDGE);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_WRAP_T, GL33C.GL_CLAMP_TO_EDGE);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MIN_FILTER, GL33C.GL_LINEAR);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MAG_FILTER, GL33C.GL_LINEAR);

        return;
    }
}