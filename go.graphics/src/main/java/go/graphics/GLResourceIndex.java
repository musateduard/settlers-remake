/*******************************************************************************
 * Copyright (c) 2016
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
 *******************************************************************************/
package go.graphics;


/**
 * This class represents an abstract resource handle.
 *
 * @author Michael Zangl
 */
public abstract class GLResourceIndex {

    protected GLDrawContext drawContext;
	protected int id;


	public GLResourceIndex(GLDrawContext drawContext, int id) {

        this.drawContext = drawContext;
		this.id = id;

        return;
	}


	/**
	 * Checks if this resource is valid.
	 *
	 * @return {@code true} if the resource is valid and can be used.
	 */
	public boolean isValid() {
		return this.drawContext.isValid();
	}


	@Override
	public String toString() {
        return String.format("%s[index=%d]", this.getClass().getSimpleName(), this.id);
	}
}