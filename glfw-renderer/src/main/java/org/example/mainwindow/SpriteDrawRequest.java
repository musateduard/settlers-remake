package org.example.mainwindow;


/**
 * GPU-ready sprite draw data written by {@link DrawRequestArena#addDrawRequest} into pooled slots.
 * Contains no map-object domain types — only what OpenGL needs.
 * Mutable so arena slots can be overwritten in place each frame.
 */
public class SpriteDrawRequest {

    public Texture texture;
    public float x;
    public float y;
    public float width;
    public float height;
    public float fow;
    public float tintR;
    public float tintG;
    public float tintB;
    public float u0;
    public float v0;
    public float u1;
    public float v1;


    public void set(
        Texture texture,
        float x, float y, float width, float height,
        float fow, float tintR, float tintG, float tintB,
        float u0, float v0, float u1, float v1) {

        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fow = fow;
        this.tintR = tintR;
        this.tintG = tintG;
        this.tintB = tintB;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;

        return;
    }
}
