/*
 * Copyright (c) 2015 - 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package jsettlers.graphics.image.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;


public class IntArrayWriter implements ImageArrayProvider {

	private static final short TRANSPARENT = 0;
	private IntBuffer array;
	private int width;
	private int line;


	@Override
	public void startImage(int width, int height) throws IOException {

		if (width == 0 && height == 0) {
			this.array = ByteBuffer.allocateDirect(4).asIntBuffer();
		}

		this.width = width;
		this.array = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder()).asIntBuffer();

        return;
	}


	@Override
	public void writeLine(int[] data, int lineLength) throws IOException {

		int offset = this.line * this.width;

        for (int index = 0; index < lineLength; index++) {
            this.array.put(offset + index, data[index]);
		}

		for (int lineIndex = lineLength; lineIndex < this.width; lineIndex++) {
            this.array.put(offset + lineIndex, TRANSPARENT);
		}

        this.line++;
        return;
	}


	public IntBuffer getArray() {
		return this.array;
	}
}