/*******************************************************************************
 * Copyright (c) 2015
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
package jsettlers.common.position;


/**
 * This is an int rectangle. It covers the area from (including) x1 to (unincluding) x2.
 *
 * @author michael
 */
public class FloatRectangle {

	private final float minX;
	private final float minY;
	private final float maxX;
	private final float maxY;


	public FloatRectangle(float minX, float minY, float maxX, float maxY) {

		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;

        return;
	}


	/**
	 * Gets the minimal x coordinate for a point that is inside the rect.
	 *
	 * @return The x coordinate.
	 */
	public float getMinX() {
		return this.minX;
	}


	/**
	 * Gets the minimal y coordinate for a point that is inside the rect.
	 *
	 * @return The y coordinate.
	 */
	public float getMinY() {
		return this.minY;
	}


	/**
	 * Gets the x coordinate for a point that is just outside of rect. Points with a x coordinate smaller than this value are inside the rect.
	 *
	 * @return The x coordinate.
	 */
	public float getMaxX() {
		return this.maxX;
	}


	/**
	 * Gets the x coordinate for a point that is just outside of rect. Points with a x coordinate smaller than this value are inside the rect.
	 *
	 * @return The x coordinate.
	 */
	public float getMaxY() {
		return this.maxY;
	}


	/**
	 * Gets the width of the rectangle. That states how many pixels are contained in it in x direction.
	 *
	 * @return The width
	 */
	public float getWidth() {
        float width = this.maxX - this.minX;
		return width;
	}


	/**
	 * Gets the height of the rectangle. That states how many pixels are contained in it in y direction.
	 *
	 * @return The height
	 */
	public float getHeight() {
        float height = this.maxY - this.minY;
		return height;
	}


	/**
	 * Gets the center of the rectangle in x direction.
	 *
	 * @return The center.
	 */
	public float getCenterX() {
        float centerX = (this.minX + this.maxX) / 2;
		return centerX;
	}


	/**
	 * Gets the center of the rectangle in y direction.
	 *
	 * @return The center.
	 */
	public float getCenterY() {
        float centerY = (this.minY + this.maxY) / 2;
		return centerY;
	}


	/**
	 * Checks whether a point is inside the rectangle.
	 *
	 * @param pointX The x coordinate to check
	 * @param pointY The y coordinate to check
     *
	 * @return If the point is inside.
	 */
	public boolean contains(float pointX, float pointY) {
		return pointX >= this.minX && pointX < this.maxX && pointY >= this.minY && pointY < this.maxY;
	}


	/**
	 * Shrinks the rectangle by the given amount. The center of the new rectangle is the same as the one of the old one, but its size is in each
	 * direction 2*border bigger.
	 *
	 * @param border the size to reduce the rectangle on each side. If it is negative, the rectangle is made smaller.
	 * @return The bigger rectangle.
	 */
	public FloatRectangle bigger(float border) {
        FloatRectangle newRectangle = new FloatRectangle(this.minX - border, this.minY - border, this.maxX + border, this.maxY + border);
		return newRectangle;
	}


	@Override
	public boolean equals(Object object) {

		if (object instanceof FloatRectangle) {
			return this.equals((FloatRectangle) object);
		}

        else {
			return false;
		}
	}


	private boolean equals(FloatRectangle rect) {
		return rect.minX == this.minX && rect.minY == this.minY && rect.maxX == this.maxX && rect.maxY == this.maxY;
	}


	@Override
	public String toString() {
        String string = "rect[minX=%f,minY=%f,maxX=%f,maxY=%f]".formatted(this.minX, this.minY, this.maxX, this.maxY);
		return string;
	}


	@Override
	public int hashCode() {

        int hash = Float.floatToIntBits(this.minX) * 104729 +
            Float.floatToIntBits(this.minY) * 4900099 +
            Float.floatToIntBits(this.maxX) * 135084239 +
            Float.floatToIntBits(this.maxY);

        return hash;
	}
}