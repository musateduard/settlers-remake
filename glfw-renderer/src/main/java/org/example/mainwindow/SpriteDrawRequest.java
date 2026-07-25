package org.example.mainwindow;


/**
 * GPU-ready sprite draw data produced by {@code SpriteRenderer.resolveMapObject}.
 * Contains no map-object domain types — only what OpenGL needs.
 *
 * @param x      View-space X of the sprite quad's top-left after clipping.
 * @param y      View-space Y of the sprite quad's top-left after clipping.
 * @param width  Clipped quad width in view pixels.
 * @param height Clipped quad height in view pixels.
 * @param fow    Fog-of-war shade multiplier in [0, 1].
 */
public record SpriteDrawRequest(
    Texture texture,
    float x,
    float y,
    float z,
    float width,
    float height,
    float fow,
    float u0,
    float v0,
    float u1,
    float v1
) {}