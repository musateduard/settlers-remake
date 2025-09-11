/*******************************************************************************
 * Copyright (c) 2015 - 2017
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
package jsettlers.common.images;

import java.util.Locale;


/**
 * This is a virtual link to an image in a settler image .dat file.
 * <p>
 * Files indexes are always the ones of the gold edition of settlers 3.
 * Internal translation allows to migrate between gold and demo.
 *
 * @author Michael Zangl
 * @see EImageLinkType
 */
public final class OriginalImageLink extends ImageLink {

	private static final long serialVersionUID = -9042004381156308651L;
	private final EImageLinkType type;
	private final int file;
	private final int sequence;
	private final int image;
	private final int length;
	private final String humanName;
	private OriginalImageLink fallback;


	/**
	 * Creates a new image link description.
     * this constructor is used for {@link EImageLinkType#SETTLER} type images.
	 *
	 * @param type image type; possible values: SETTLER, GUI, LANDSCAPE
	 * @param file file index where image is located. all image files are located in the ./GFX folder and
     *             they are packed into .dat archives. the file index is the number that comes after the
     *             "siedler3_" prefix. ex.:  for "siedler3_03.7c003e01f.dat" the index would be 3.
	 * @param sequence image sequence index. all images inside each dat archive are laid out in sequence
     *                 starting at index 0. the name schema for images in the archive is: {type of image}{sequence index}.bmp
     *                 example of image sequence index: for an image in archive "siedler3_03.7c003e01f.dat" with
     *                 name "menu23.bmp" the sequence index would be 23.
	 * @param image The image in the sequence, for {@link EImageLinkType#SETTLER} images.
	 * @param length The number contained in the sequence that is linked,
     * @param name user friendly name of the image; used internally.
	 */
	public OriginalImageLink(EImageLinkType type, int file, int sequence, int image, int length, String name) {
		this.type = type;
		this.file = file;
		this.sequence = sequence;
		this.image = image;
		this.length = length;
		this.humanName = name;
	}

	public OriginalImageLink(EImageLinkType type, int file, int sequence, int image, int length) {
		this(type, file, sequence, image, length, null);
	}


	/**
	 * Creates a new image link description.
     * this constructor is used for {@link EImageLinkType#SETTLER} type images.
	 *
	 * @param type image type; possible values: SETTLER, GUI, LANDSCAPE
	 * @param file file index where image is located. all image files are located in the ./GFX folder and
     *             they are packed into .dat archives. the file index is the number that comes after the
     *             "siedler3_" prefix. ex.:  for "siedler3_03.7c003e01f.dat" the index would be 3.
     * @param sequence image sequence index. all images inside each dat archive are laid out in sequence
     *                 starting at index 0. the name schema for images in the archive is: {type of image}{sequence index}.bmp
     *                 example of image sequence index: for an image in archive "siedler3_03.7c003e01f.dat" with
     *                 name "menu23.bmp" the sequence index would be 23.
	 * @param image The image in the sequence, for {@link EImageLinkType#SETTLER} images.
	 */
	public OriginalImageLink(EImageLinkType type, int file, int sequence, int image) {
		this(type, file, sequence, image, 0);
	}


	/**
	 * Creates a new image link description for {@link EImageLinkType#GUI} images.
	 *
     * @param type image type; possible values: SETTLER, GUI, LANDSCAPE
     * @param file file index where image is located. all image files are located in the ./GFX folder and
     *             they are packed into .dat archives. the file index is the number that comes after the
     *             "siedler3_" prefix.<br>
     *             ex.:  for "siedler3_03.7c003e01f.dat" the index would be 3.
     * @param sequence image sequence index. all images inside each dat archive are laid out in sequence
     *                 starting at index 0. the name schema for images in the archive is: {type of image}{sequence index}.bmp<br>
     *                 example of image sequence index: for an image in archive "siedler3_03.7c003e01f.dat" with
     *                 name "menu23.bmp" the sequence index would be 23.
	 */
	public OriginalImageLink(EImageLinkType type, int file, int sequence) {
		this(type, file, sequence, 0);
	}


	/**
	 * Gets the type of the image.
	 *
	 * @return The image type
	 */
	public EImageLinkType getType() {
		return type;
	}

	/**
	 * Gets the file.
	 *
	 * @return The files number.
	 */
	public int getFile() {
		return file;
	}

	/**
	 * Gets the seuqence index inside the file.
	 * <p>
	 * For GUI and LANDSCAPE images, this defines the image.
	 *
	 * @return The index
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * Gets the image index inside the sequence.
	 *
	 * @return The image index
	 */
	public int getImage() {
		return image;
	}

	@Override
	public String toString() {
		return "image[type=" + type + ", file=" + file + ", sequence=" + sequence + ", image=" + image + "]";
	}

	/**
	 * Gets the length of this strip.
	 *
	 * @return The length as int
	 */
	public int getLength() {
		return length;
	}

	@Override
	public String getName() {
		return String.format(Locale.ENGLISH, "original_%d_%s_%d", file, type.toString(), sequence);
	}

	@Override
	public int getImageIndex() {
		return image;
	}

	public EImageLinkType type() {
		return type;
	}

	public String getHumanName() {
		return humanName;
	}

	public void setFallback(OriginalImageLink fallback) {
		this.fallback = fallback;
	}

	public OriginalImageLink getFallback() {
		return fallback;
	}
}