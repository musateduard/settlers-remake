package go.graphics;


public class UnifiedDrawHandle extends GLResourceIndex {

	public final BufferHandle vertices;
	public TextureHandle texture;
	public TextureHandle texture2;
	public int offset;
	public final int vertexCount;

    private float[] trans;
    private float[] colors;
    private int cache_index = 0;

    private int cache_start_bias = 0;
    private int frame_drawcalls = 0;
    private long frameIndex = -1;
    private boolean forceNoCache = false;

    public static final int CACHE_START_AT_BIAS = 100;
    public static final int MAX_CACHE_ENTRIES = 100;
    private static final int MIN_BIAS = -1000;
    private static final int MAX_BIAS = 1000;


	public UnifiedDrawHandle(
        GLDrawContext context,
        int id, int offset, int vertexCount,
        TextureHandle texture,
        TextureHandle texture2,
        BufferHandle vertices) {

		super(context, id);

        this.vertexCount = vertexCount;
		this.vertices = vertices;
		this.texture = texture;
		this.texture2 = texture2;
		this.offset = offset;

        return;
	}


	public void forceNoCache() {
		this.forceNoCache = true;
        return;
	}


	private void enableCaching() {

        if (this.forceNoCache) {
            return;
        }

		this.trans = new float[UnifiedDrawHandle.MAX_CACHE_ENTRIES * 4];
		this.colors = new float[UnifiedDrawHandle.MAX_CACHE_ENTRIES * 4];

		this.drawContext.add(this);

        return;
	}


	private void disableCaching() {

		this.trans = null;
		this.colors = null;
		this.drawContext.remove(this);

        return;
	}


	private boolean nextFrame() {

		this.frameIndex = this.drawContext.frameIndex;
		boolean modified = false;

		if (this.trans == null && this.frame_drawcalls >= UnifiedDrawHandle.MAX_CACHE_ENTRIES) {

			this.cache_start_bias++;

			if (this.cache_start_bias == UnifiedDrawHandle.CACHE_START_AT_BIAS) {
				this.enableCaching();
				modified = true;
			}
		}

        else if (this.trans != null && this.frame_drawcalls < UnifiedDrawHandle.MAX_CACHE_ENTRIES) {

			this.cache_start_bias--;

			if (this.cache_start_bias == -UnifiedDrawHandle.CACHE_START_AT_BIAS) {
				this.disableCaching();
				modified = true;
			}
		}

		if (this.cache_start_bias < UnifiedDrawHandle.MIN_BIAS) {
			this.cache_start_bias = UnifiedDrawHandle.MIN_BIAS;
		}

		if (this.cache_start_bias > UnifiedDrawHandle.MAX_BIAS) {
			this.cache_start_bias = UnifiedDrawHandle.MAX_BIAS;
		}

        this.frame_drawcalls = 0;
		return modified;
	}


	public boolean flush() {

		boolean mod = (this.frameIndex != this.drawContext.frameIndex) && this.nextFrame();

        if (this.cache_index == 0) {
            return mod;
        }

		this.drawContext.drawUnifiedArray(this, EPrimitiveType.Quad, 4, this.trans, this.colors, this.cache_index);
		this.cache_index = 0;

		return mod;
	}


	public void drawProgress(int primitive, float x, float y, float z, float sx, float sy, AbstractColor progressRange, float intensity) {

        this.drawContext.drawUnified(
            this, primitive,
            this.vertexCount,
            EUnifiedMode.PROGRESS,
            x, y, z, sx, sy, progressRange, intensity
        );

        return;
	}


	public void drawSimple(int primitive, float x, float y, float z, float sx, float sy, AbstractColor color, float intensity) {

        this.drawContext.drawUnified(
            this, primitive,
            this.vertexCount,
            this.texture != null ? EUnifiedMode.TEXTURE : EUnifiedMode.COLOR_ONLY,
            x, y, z, sx, sy, color, intensity
        );

        return;
	}


	public void drawComplexQuad(int mode, float x, float y, float z, float sx, float sy, AbstractColor color, float intensity) {

		if (this.frameIndex != this.drawContext.frameIndex) {
            this.nextFrame();
        }

		if (this.trans != null && sx == 1 && sy == 1) {

			if (this.cache_index == UnifiedDrawHandle.MAX_CACHE_ENTRIES) {
                this.flush();
            }

			this.trans[this.cache_index * 4] = x;
			this.trans[this.cache_index * 4 + 1] = y;
			this.trans[this.cache_index * 4 + 2] = z;
			this.trans[this.cache_index * 4 + 3] = (mode * 10) + intensity + 1;

			this.colors[this.cache_index * 4] = color != null ? color.red : 1;
			this.colors[this.cache_index * 4 + 1] = color != null ? color.green : 1;
			this.colors[this.cache_index * 4 + 2] = color != null ? color.blue : 1;
			this.colors[this.cache_index * 4 + 3] = color != null ? color.alpha : 1;

            this.cache_index++;
		}

        else {
            this.drawContext.drawUnified(this, EPrimitiveType.Quad, 4, mode, x, y, z, sx, sy, color, intensity);
		}

        this.frame_drawcalls++;
        return;
	}


	public int getVertexArrayId() {
		return this.id;
	}
}