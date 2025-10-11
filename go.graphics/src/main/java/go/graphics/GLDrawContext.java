package go.graphics;

import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import go.graphics.text.EFontSize;
import go.graphics.text.TextDrawer;
import go.graphics.text.AbstractTextDrawer;


public abstract class GLDrawContext {

    protected List<ManagedHandle> managedHandles = new ArrayList<>();
    protected int maxUniformBlockSize;
    protected int maxTextureSize;
    protected AbstractTextDrawer<GLDrawContext> textDrawer;
    private final TextDrawer[] sizedTextDrawers = new TextDrawer[EFontSize.values().length];
    private boolean valid = true;
    private final List<UnifiedDrawHandle> caches = new ArrayList<>();
    protected long frameIndex = 0;

    /**
     * Returns a texture id which is positive or 0. It returns a negative number on error.
     *
     * @param image The data as array. It needs to have a length of width * height and each element is a color with:
     *              4 bits red, 4 bits green, 4 bits blue and 4 bits alpha.
     * @return The id of the generated texture.
     */
    public abstract TextureHandle generateTexture(ImageData image, String name);
    protected abstract void drawMulti(MultiDrawHandle call);
    protected abstract void drawUnifiedArray(UnifiedDrawHandle call, int primitive, int vertexCount, float[] trans, float[] colors, int array_len);
    protected abstract void drawUnified(UnifiedDrawHandle call, int primitive, int vertices, int mode, float x, float y, float z, float sx, float sy, AbstractColor color, float intensity);
    public abstract void drawBackground(BackgroundDrawHandle call);
    public abstract void setHeightMatrix(float[] matrix);
    public abstract void updateViewMatrix(float offsetX, float offsetY, float offsetZ, float scaleX, float scaleY, float scaleZ);
    public abstract void setShadowDepthOffset(float depth);

    /**
     * Updates a part of a texture image.
     *
     * @param textureIndex The texture to use.
     * @param left
     * @param bottom
     * @param image
     * @throws IllegalBufferException
     */
    public abstract void updateTexture(TextureHandle textureIndex, int left, int bottom, ImageData image) throws IllegalBufferException;
    public abstract TextureHandle resizeTexture(TextureHandle textureIndex, ImageData image);
    public abstract void updateBufferAt(BufferHandle handle, int pos, ByteBuffer data) throws IllegalBufferException;
    public abstract BackgroundDrawHandle createBackgroundDrawCall(int vertices, TextureHandle texture);

    /**
     * @param vertices Maximum number of vertices
     * @param name     The label that the OpenGL handles get (nullable)
     * @param texture  It determines whether this handle is textured or only single colored
     * @param texture2
     * @param data     If data is not equal null this will be a readonly buffer filled with data
     * @return A handle to draw via the unified shader
     */
    public abstract UnifiedDrawHandle createUnifiedDrawCall(int vertices, String name, TextureHandle texture, TextureHandle texture2, float[] data);
    protected abstract MultiDrawHandle createMultiDrawCall(String name, ManagedHandle source);
    public abstract void clearDepthBuffer();
    public abstract void updateProjectionMatrix(int screenWidth, int screenHeight);


	public GLDrawContext() {
		ManagedHandle.instance_count = 0;
        return;
	}


	/**
	 * Gets a text drawer for the given text size.
	 *
	 * @param size The size for the drawer.
	 * @return An instance of a drawer for that size.
	 */
	public TextDrawer getTextDrawer(EFontSize size) {

		if (this.sizedTextDrawers[size.ordinal()] == null) {
			this.sizedTextDrawers[size.ordinal()] = this.textDrawer.derive(size);
		}

		return this.sizedTextDrawers[size.ordinal()];
	}


	public static float[] createQuadGeometry(float lx, float ly, float hx, float hy, float lu, float lv, float hu, float hv) {

        float[] matrix = new float[] {
            hx, ly, hu, lv,  // bottom right
            hx, hy, hu, hv,  // top right
            lx, hy, lu, hv,  // top left
            lx, ly, lu, lv,  // bottom left
        };

		return matrix;
	}


	private void addNewHandle() {

		int quad_count = this.getMaxManagedQuads();
		int texture_size = this.getMaxManagedTextureSize();

		TextureHandle tex = this.generateTexture(new ImageData(texture_size, texture_size), "managed" + ManagedHandle.instance_count);
		TextureHandle tex2 = this.generateTexture(new ImageData(texture_size, texture_size), "managed" + ManagedHandle.instance_count + "-2");
		UnifiedDrawHandle parent = this.createUnifiedDrawCall(quad_count * 4, "managed" + ManagedHandle.instance_count, tex, tex2, null);
		this.managedHandles.add(new ManagedHandle(parent, quad_count, texture_size));

        return;
	}


	public ManagedUnifiedDrawHandle createManagedUnifiedDrawCall(ImageData texture, float offsetX, float offsetY, int width, int height) {

		int textureWidth = texture.getWidth();
		int textureHeight = texture.getHeight();

		for (ManagedHandle handle : this.managedHandles) {

			int position;
			if (handle.quad_index != handle.quad_count && (position = handle.findTextureHole(textureWidth, textureHeight)) != -1) {

				UIPoint corner;
				if ((corner = handle.addTexture(texture, position)) == null) {
                    continue;
                }

				float lu = (float) corner.getX();
				float lv = (float) corner.getY();
				float hu = lu + textureWidth / (float) handle.texture_size;
				float hv = lv + textureHeight / (float) handle.texture_size;

				float[] data = GLDrawContext.createQuadGeometry(offsetX, -offsetY, offsetX + width, -offsetY - height, lu, lv, hu, hv);

				handle.addQuad(data);

				return new ManagedUnifiedDrawHandle(handle, lu, lv, hu, hv);
			}
		}

        this.addNewHandle();
		return this.createManagedUnifiedDrawCall(texture, offsetX, offsetY, width, height);
	}


	public void invalidate() {
        this.valid = false;
        return;
	}


	public boolean isValid() {
		return this.valid;
	}


	protected void add(UnifiedDrawHandle cache) {
        this.caches.add(cache);
        return;
	}


	protected void remove(UnifiedDrawHandle cache) {
		this.caches.remove(cache);
        return;
	}


	public void finishFrame() {

		for (int index = 0; index != caches.size(); index++) {
			if (this.caches.get(index).flush()) index--;
		}

		for (ManagedHandle mh : this.managedHandles) {
			if (mh.multiCache != null) {
                mh.multiCache.flush();
            }
		}

        return;
	}


	public long getFrameIndex() {
		return this.frameIndex;
	}


	public void startFrame() {
		this.frameIndex++;
        return;
	}


	protected int getMaxManagedQuads() {

		int maxManagedHandleQuads = this.maxUniformBlockSize / (4 * 4 * 4);  // size of one quad

		if (maxManagedHandleQuads >= ManagedHandle.MAX_QUADS) {
			return ManagedHandle.MAX_QUADS;
		}

		return maxManagedHandleQuads;
	}


	protected int getMaxManagedTextureSize() {

		int maxManagedHandleTextureSize = this.maxTextureSize;

		if (maxManagedHandleTextureSize >= ManagedHandle.MAX_TEXTURE_SIZE) {
			return ManagedHandle.MAX_TEXTURE_SIZE;
		}

		return maxManagedHandleTextureSize;
	}


	protected String getManagedHandleDefine() {
		return "#define MAX_GEOMETRY_DATA_QUAD_COUNT %d".formatted(this.getMaxManagedQuads());
	}
}