package go.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class ImageData {

	private ShortBuffer data16;
	private IntBuffer data32;
	private final int width;
	private final int height;


	public ImageData(int imageWidth, int imageHeight) {

        this(
            ByteBuffer.allocateDirect(imageWidth * imageHeight * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer(),
            imageWidth,
            imageHeight
        );

        return;
	}


	private ImageData(IntBuffer data, int imageWidth, int imageHeight) {

        this.data32 = data;
		this.width = imageWidth;
		this.height = imageHeight;

        return;
	}


	public ShortBuffer getReadData16() {
        this.convertTo16();
		return this.data16;
	}


	public IntBuffer getReadData32() {
        this.convertTo32();
		return this.data32;
	}


	public IntBuffer getWriteData32() {
        this.convertTo32();
		return this.data32;
	}


	private void convertTo16() {

        this.data16 = ByteBuffer.allocateDirect(this.width * this.height * 2).order(ByteOrder.nativeOrder()).asShortBuffer();

		while (this.data32.hasRemaining()) {

			int color = this.data32.get();
			int c1 = ((color >> 24));
			int c2 = ((color >> 16));
			int c3 = ((color >> 8));
			int c4 = (color & 0xFF);

            this.data16.put((short) (this.cnv8to4(c1) << 12 | this.cnv8to4(c2) << 8 | this.cnv8to4(c3) << 4 | this.cnv8to4(c4)));
		}

		this.data32.rewind();
		this.data16.rewind();

        return;
	}


	protected final int cnv8to4(int c8bit) {
		int unsigned = c8bit & 0xFF;
		return (int) (unsigned / 255f * 15f);
	}


	private void convertTo32() {

        if (this.data32 != null) {
            return;
        }

        this.data32 = ByteBuffer.allocateDirect(this.width * this.height * 4).order(ByteOrder.nativeOrder()).asIntBuffer();

		while (this.data16.hasRemaining()) {
            this.data32.put(Color.convertTo32Bit(this.data16.get()));
		}

		this.data32.rewind();
		this.data16 = null;

        return;
	}


	public int getWidth() {
		return this.width;
	}


	public int getHeight() {
		return this.height;
	}


	public ImageData convert(int newWidth, int newHeight) {

		if (this.width == newWidth && this.height == newHeight) {
			return this;
		}

		ImageData newImage = new ImageData(newWidth, newHeight);
		IntBuffer newData = newImage.getWriteData32();
		IntBuffer oldData = this.getReadData32();

		for (int newY = 0; newY < newHeight; newY++) {

			for (int newX = 0; newX < newWidth; newX++) {

				int offsetX = (int) (newX * this.width / (float) newWidth);
				int offsetY = (int) (newY * this.height / (float) newHeight);

				newData.put(oldData.get(offsetX + offsetY * this.width));
			}
		}

		newData.rewind();
		return newImage;
	}


	public static ImageData of(IntBuffer data, int width, int height) {
		return new ImageData(data, width, height);
	}
}