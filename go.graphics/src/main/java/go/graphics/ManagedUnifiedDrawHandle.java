package go.graphics;


public class ManagedUnifiedDrawHandle extends UnifiedDrawHandle {

	public final float texX;
	public final float texY;
	public final float texWidth;
	public final float texHeight;
	private final ManagedHandle parent;


	protected ManagedUnifiedDrawHandle(ManagedHandle parent, float texX, float texY, float texWidth, float texHeight) {

		super(parent.bufferHolder.drawContext, parent.bufferHolder.id, 4*parent.quad_index++, 4, parent.bufferHolder.texture, parent.bufferHolder.texture2, parent.bufferHolder.vertices);

        this.texX = texX;
		this.texY = texY;
		this.parent = parent;
		this.texWidth = texWidth;
		this.texHeight = texHeight;

        return;
	}


	@Override
	public void drawComplexQuad(int mode, float modelX, float modelY, float modelZ, float scaleX, float scaleY, AbstractColor color, float intensity) {

		if (this.parent.multiCache != null) {
			this.parent.multiCache.schedule(this, mode, modelX, modelY, modelZ, scaleX, scaleY, color, intensity);
		}

        else {
			super.drawComplexQuad(mode, modelX, modelY, modelZ, scaleX, scaleY, color, intensity);
		}

        return;
	}
}