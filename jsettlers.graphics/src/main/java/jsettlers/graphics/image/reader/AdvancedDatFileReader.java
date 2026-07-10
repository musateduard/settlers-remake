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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SettlerImage;
import jsettlers.graphics.image.reader.bytereader.ByteReader;
import jsettlers.graphics.image.reader.shadowmap.IdentityShadowMapping;
import jsettlers.graphics.image.reader.shadowmap.ShadowMapping;
import jsettlers.graphics.image.reader.translator.DatBitmapTranslator;
import jsettlers.graphics.image.reader.translator.GuiTranslator;
import jsettlers.graphics.image.reader.translator.LandscapeTranslator;
import jsettlers.graphics.image.reader.translator.SettlerTranslator;
import jsettlers.graphics.image.reader.translator.ShadowTranslator;
import jsettlers.graphics.image.reader.translator.TorsoTranslator;
import jsettlers.graphics.image.reader.versions.DefaultGfxFolderMapping.DefaultDatFileMapping;
import jsettlers.graphics.image.sequence.ArraySequence;
import jsettlers.graphics.image.sequence.Sequence;
import jsettlers.graphics.image.sequence.SequenceList;

import static jsettlers.graphics.image.reader.versions.GfxFolderMapping.DatFileMapping;


/**
 * This is an advanced dat file reader. It can read the file, but it only reads needed sequences.
 *
 * <p>The format of a dat file is (all numbers in little endian):
 *
 * <table>
 * <tr>
 * <td>Bytes 0x0000+20:</td>
 * <td>Always the same</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0030+4:</td>
 * <td>file size</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0034+4:</td>
 * <td>Text Section pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0038+4:</td>
 * <td>Landscape Sequence pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x003C+4:</td>
 * <td>Menu Section pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0040+4:</td>
 * <td>Sprite Section pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0044+4:</td>
 * <td>Color Sprite Section pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0048+4:</td>
 * <td>Shadow Section pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x004C+4:</td>
 * <td>Animation Section pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0050+4:</td>
 * <td>Palette Section pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0054+4:</td>
 * <td>text_section.magic_number 0x1904</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x0058+4:</td>
 * <td>text_section.header_size 0x000c</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x005C+2:</td>
 * <td>text_section.string_count</td>
 * </tr>
 * <tr>
 * <td>Bytes 0x005E+2:</td>
 * <td>text_section.language_count</td>
 * </tr>
 * <tr>
 * <td>e.g. Bytes 0x0066+2:</td>
 * <td>Image count of image sequences for one type</td>
 * </tr>
 * <tr>
 * <td>e.g. Bytes 0x0068+2:</td>
 * <td>Start position of fist image sequence list.</td>
 * </tr>
 * </table>
 *
 * @author michael
 */
public class AdvancedDatFileReader implements DatFileReader {

	/**
	 * Every dat file seems to have to start with this sequence.
	 */
    private static final byte[] FILE_START1 = {
        0x04, 0x13, 0x04, 0x00,  // magic number
        0x0c, 0x00, 0x00, 0x00,  // unknown_1
        0x00, 0x00, 0x00, 0x00,  // unknown_2
        0x54, 0x00, 0x00, 0x00,  // header_size
        0x20, 0x00, 0x00, 0x00,  // unknown_3
        0x40, 0x00, 0x00, 0x00,  // unknown_4
        0x00, 0x00, 0x00, 0x00,  // unknown_5
        0x10, 0x00, 0x00, 0x00,  // unknown_6
        0x00,
    };

    private static final byte[] FILE_START2 = {
                                 // missing read_channel_bits  (0xf800)
                    0x00, 0x00,  // missing green_channel_bits (0x07e0)
        0x1f, 0x00, 0x00, 0x00,  // blue_channel_bits  (0x001f)
        0x00, 0x00, 0x00, 0x00
    };

    // this is not the file header end; it spills into text section
	private static final byte[] FILE_HEADER_END = {
		0x04, 0x19, 0x00, 0x00,  // text_section.magic_number
        0x0c, 0x00, 0x00, 0x00,  // text_section.header_size
        0x00, 0x00, 0x00, 0x00   // text_section.string_count
	};

	private static final int SECTION_COUNT = 6;
	private static final int ID_SETTLERS         = 0x106;
	private static final int ID_TORSOS           = 0x3112;
	private static final int ID_LANDSCAPE        = 0x2412;
	private static final int ID_SHADOWS          = 0x5982;
	// fullscreen images
	private static final int ID_MENUS = 0x11306;

	private final DatBitmapTranslator<SettlerImage>  settlerTranslator;
	private final DatBitmapTranslator<SingleImage>     torsoTranslator;
	private final DatBitmapTranslator<SingleImage> landscapeTranslator;
	private final DatBitmapTranslator<SingleImage>    shadowTranslator;
	private final DatBitmapTranslator<SingleImage>       guiTranslator;

	private final DatFileMapping mapping;
	private final ShadowMapping shadowMapping;

	private       ByteReader reader = null;
	private final File       file;

	/**
	 * This is a list of file positions where the settler sequences start.
	 */
	private int[] settlerStarts;

	/**
	 * A list of loaded settler sequences.
	 */
	private Sequence<Image>[] settlerSequences = null;
	/**
	 * An array with the same length as settlers.
	 */
	private int[]             torsoStarts;
	/**
	 * An array with the same length as settlers.
	 */
	private int[]             shadowStarts;

	/**
	 * A list of loaded landscape images.
	 */
	private       SingleImage[]         landscapeImages   = null;
	private final Sequence<SingleImage> landscapeSequence = new LandscapeImageSequence();
	private       int[]                 landscapeStarts;

	private       SingleImage[]         guiImages   = null;
	private       int[]                 guiStarts;
	private final Sequence<SingleImage> guiSequence = new GuiImageSequence();

	private final SequenceList<Image> directSettlerList;

	private static final byte[] SEQUENCE_START = new byte[] {
        0x02, 0x14, 0x00, 0x00,  // SequenceHeader.magic_number
        0x08, 0x00, 0x00         // SequenceHeader.unknown_1
	};

	private final DatFileType type;
	private final String file_name;

    private static final Sequence<Image> NULL_SETTLER_SEQUENCE = new ArraySequence<>(new SettlerImage[0]);


	public AdvancedDatFileReader(File file, DatFileType type, String file_name) {
		this(file, type, new DefaultDatFileMapping(), new IdentityShadowMapping(), file_name);
	}


	public AdvancedDatFileReader(File file, DatFileType type, DatFileMapping mapping, ShadowMapping shadowMapping, String file_name) {

        this.file = file;
		this.type = type;
		this.mapping = mapping;
		this.shadowMapping = shadowMapping;
		this.file_name = file_name;

		this.directSettlerList = new DirectSettlerSequenceList();
		this.settlerTranslator = new SettlerTranslator(type);
		this.torsoTranslator = new TorsoTranslator();
		this.landscapeTranslator = new LandscapeTranslator(type);
		this.shadowTranslator = new ShadowTranslator();
		this.guiTranslator = new GuiTranslator(type);

        return;
	}


	public Hashes getSettlersHashes() {
		SequenceList<Image> settlers = getSettlers();

		return new Hashes(IntStream.range(0, settlers.size())
				.mapToObj(settlers::get)
				.map(sequence -> sequence.getImage(0, null))
				.filter(image -> image instanceof SingleImage)
				.map(image -> (SingleImage) image)
				.map(SingleImage::hash)
				.collect(Collectors.toList()));
	}

	public Hashes getGuiHashes() {
		Sequence<SingleImage> sequence = getGuis();

		return new Hashes(IntStream.range(0, sequence.length())
				.mapToObj(index -> sequence.getImage(index, null))
				.map(SingleImage::hash)
				.collect(Collectors.toList()));
	}


	/**
	 * Initializes the reader, reads the index.
	 */
	@SuppressWarnings("unchecked")
	public void initialize() {

		try {
			this.reader = new ByteReader(new RandomAccessFile(this.file, "r"));
			this.initFromReader(this.file, this.reader);
		}

        catch (IOException exception) {

			if (this.reader != null) {

				try {
                    this.reader.close();
				}

                catch (IOException ioException) {
                    /* do nothing */
                }

                this.reader = null;
			}

			System.out.println("Could not read dat file " + this.file + " due to: " + exception.getMessage());
		}

        this.initializeNullFile();

        this.landscapeImages = new SingleImage[this.landscapeStarts.length];
		this.guiImages = new SingleImage[this.guiStarts.length];
		this.settlerSequences = new Sequence[this.settlerStarts.length];

		int torsoDifference = this.settlerStarts.length - this.torsoStarts.length;

        if (torsoDifference > 0) {

			int[] oldTorsos = this.torsoStarts;
            this.torsoStarts = new int[this.settlerStarts.length];
			System.arraycopy(oldTorsos, 0, this.torsoStarts, torsoDifference, oldTorsos.length);

            for (int index = 0; index < torsoDifference; index++) {
                this.torsoStarts[index] = -1;
			}
		}

		int shadowDifference = this.settlerStarts.length - this.shadowStarts.length;
        int shadowIndex;

		if (shadowDifference > 0) {

			int[] oldShadows = this.shadowStarts;
            this.shadowStarts = new int[this.settlerStarts.length];

			if (shadowDifference == 8 || shadowDifference == 7) {

				// push shadows to end of settler images
				for (shadowIndex = 0; shadowIndex < shadowDifference; shadowIndex++) {
                    this.shadowStarts[shadowIndex] = -1;
				}

				for (; shadowIndex < this.settlerStarts.length; shadowIndex++) {
                    this.shadowStarts[shadowIndex] = oldShadows[shadowIndex - shadowDifference];
				}
			}

            else {

				// push shadows to beginning of settler images
				for (shadowIndex = 0; shadowIndex < oldShadows.length; shadowIndex++) {
					this.shadowStarts[shadowIndex] = oldShadows[shadowIndex];
				}

				for (; shadowIndex < this.settlerStarts.length; shadowIndex++) {
					this.shadowStarts[shadowIndex] = -1;
				}
			}

            // change shadows in file 13:
			if (shadowDifference == 26) {

				for (shadowIndex = 0; shadowIndex < 27; shadowIndex++) {
                    this.shadowStarts[shadowIndex] = this.shadowStarts[shadowIndex + 3];
				}

				for (shadowIndex = 27; shadowIndex < 36; shadowIndex++) {
                    this.shadowStarts[shadowIndex] = this.shadowStarts[shadowIndex + 2];
				}

				this.shadowStarts[28] = -1; // marketplace gets no shadow (has it already)
				this.shadowStarts[44] = this.shadowStarts[38]; // dock
				this.shadowStarts[45] = this.shadowStarts[39]; // harbour

				for (shadowIndex = 36; shadowIndex < 44; shadowIndex++) {
                    this.shadowStarts[shadowIndex] = -1; // rest has no shadow
				}

				for (shadowIndex = 46; shadowIndex < this.shadowStarts.length; shadowIndex++) {
                    this.shadowStarts[shadowIndex] = -1; // rest has no shadow
				}

			}

            // change shadows in file 36:
            else if (shadowDifference == 28) {
				this.shadowStarts[4] = this.shadowStarts[1]; // roman ferry
				this.shadowStarts[6] = -1; // roman ferry front has no extra shadow
				this.shadowStarts[2] = -1; // roman cargo ship front has no extra shadow
			}
		}

        else if (shadowDifference == 0) {

            // change shadows in file 11:
			if (this.settlerStarts.length == 239) {
				for (shadowIndex = 171; shadowIndex >= 13; shadowIndex--) {
                    this.shadowStarts[shadowIndex] = this.shadowStarts[shadowIndex - 13]; // several specialists
				}
			}
		}

        return;
	}


	private void initFromReader(File file, ByteReader reader) throws IOException {

		int[] sectionOffsetList = this.readSectionOffsetList(file.length(), reader);

		for (int index = 0; index < AdvancedDatFileReader.SECTION_COUNT; index++) {

            try {
				this.readSequencesAt(reader, sectionOffsetList[index]);
			}

            catch (IOException exception) {
				System.err.println("Error while loading sequence" + ": " + exception.getMessage());
				exception.printStackTrace();
			}
		}

        return;
	}


	private int[] readSectionOffsetList(long fileLength, ByteReader reader) throws IOException {

		reader.assumeToRead(AdvancedDatFileReader.FILE_START1);
		reader.assumeToRead(this.type.getFileStartMagic());
		reader.assumeToRead(AdvancedDatFileReader.FILE_START2);
		int fileSize = reader.read32();

		if (fileSize != fileLength) {
			throw new IOException("The length stored in the dat file is not the file length.");
		}

		// ignore unknown bytes.
		reader.read32();

		// read settler image pointer
		int[] sectionOffsetList = new int[AdvancedDatFileReader.SECTION_COUNT];
		for (int index = 0; index < AdvancedDatFileReader.SECTION_COUNT; index++) {
			sectionOffsetList[index] = reader.read32();
		}

		// ignore unknown bytes.
		reader.read32();
		reader.assumeToRead(AdvancedDatFileReader.FILE_HEADER_END);

        return sectionOffsetList;
	}


    /**
     * reads all sequence starts at a given position.
     *
     * <p>Does not align torsos and shadows.
     *
     * @param reader The reader to read from.
     * @param sequenceIndexStart The position to start at.
     *
     * @throws IOException if a read error occurred.
     */
    private void readSequencesAt(ByteReader reader, int sequenceIndexStart) throws IOException {

        // read data index 0
        reader.skipTo(sequenceIndexStart);

        int sectionType = reader.read32();
        int headerSize = reader.read16();
        int pointerCount = reader.read16();

        if (headerSize != pointerCount * 4 + 8) {
            throw new IOException("Sequence index block length (" + pointerCount + ") and " + "headerSize (" + headerSize + ") are not consistent.");
        }

        int[] sequenceOffsetList = new int[pointerCount];
        for (int index = 0; index < pointerCount; index++) {
            sequenceOffsetList[index] = reader.read32();
        }

        if (sectionType == AdvancedDatFileReader.ID_SETTLERS) {
            this.settlerStarts = sequenceOffsetList;
        }

        else if (sectionType == AdvancedDatFileReader.ID_TORSOS) {
            this.torsoStarts = sequenceOffsetList;
        }

        else if (sectionType == AdvancedDatFileReader.ID_LANDSCAPE) {
            this.landscapeStarts = sequenceOffsetList;
        }

        else if (sectionType == AdvancedDatFileReader.ID_SHADOWS) {
            this.shadowStarts = sequenceOffsetList;
        }

        else if (sectionType == AdvancedDatFileReader.ID_MENUS) {
            this.guiStarts = sequenceOffsetList;
        }

        return;
    }


	private void initializeNullFile() {
		if (settlerStarts == null) {
			settlerStarts = new int[0];
		}
		if (torsoStarts == null) {
			torsoStarts = new int[0];
		}
		if (shadowStarts == null) {
			shadowStarts = new int[0];
		}
		if (landscapeStarts == null) {
			landscapeStarts = new int[0];
		}
		if (guiStarts == null) {
			guiStarts = new int[0];
		}
	}

	private void initializeIfNeeded() {
		if (settlerSequences == null) {
			initialize();
		}
	}


	@Override
	public SequenceList<Image> getSettlers() {
		return this.directSettlerList;
	}


	private class DirectSettlerSequenceList implements SequenceList<Image> {

		@Override
		public Sequence<Image> get(int index) {

			initializeIfNeeded();

			if (settlerSequences[index] == null) {

				settlerSequences[index] = AdvancedDatFileReader.NULL_SETTLER_SEQUENCE;

                try {
					loadSettlers(index, file_name);
				}

                catch (Exception exception) {
					exception.printStackTrace();
				}
			}

			return settlerSequences[index];
		}

		@Override
		public int size() {
			initializeIfNeeded();
			return settlerSequences.length;
		}
	}


	@Override
	public synchronized <T extends Image> long readImageHeader(
        DatBitmapTranslator<T> translator,
        ImageMetadata metadata,
        long offset) throws IOException {

		initializeIfNeeded();
		this.reader.skipTo(offset);
		DatBitmapReader.readImageHeader(this.reader, translator, metadata);

        return this.reader.getReadBytes();
	}


	@Override
	public synchronized <T extends Image> void readCompressedData(
        DatBitmapTranslator<T> translator,
        ImageMetadata metadata,
        ImageArrayProvider array,
        long offset) throws IOException {

		this.initializeIfNeeded();
        this.reader.skipTo(offset);
		DatBitmapReader.readCompressedData(this.reader, translator, metadata.width, metadata.height, array);

        return;
	}


	private synchronized void loadSettlers(int goldIndex, String name) throws IOException {
		initializeIfNeeded();

		int realSettlerIndex = mapping.mapSettlersSequence(goldIndex);
		int realShadowIndex = mapping.mapSettlersSequence(shadowMapping.getShadowIndex(goldIndex));

		int position = settlerStarts[realSettlerIndex];
		long[] framePositions = readSequenceHeader(position);

		SettlerImage[] images = new SettlerImage[framePositions.length];
		for (int i = 0; i < framePositions.length; i++) {
			images[i] = DatBitmapReader.getImage(settlerTranslator, this, framePositions[i], name + "-S" + goldIndex + ":" + i);
		}

		int torsoPosition = torsoStarts[realSettlerIndex];
		if (torsoPosition >= 0) {
			long[] torsoPositions = readSequenceHeader(torsoPosition);
			for (int i = 0; i < torsoPositions.length && i < framePositions.length; i++) {
				SingleImage torso = DatBitmapReader.getImage(torsoTranslator, this, torsoPositions[i], name + "-T" + goldIndex + ":" + i);
				images[i].setTorso(torso);
			}
		}

		int shadowPosition = realShadowIndex != -1 ? shadowStarts[realShadowIndex] : -1;
		if (shadowPosition >= 0) {
			long[] shadowPositions = readSequenceHeader(shadowPosition);
			for (int i = 0; i < shadowPositions.length
				&& i < framePositions.length; i++) {
				SingleImage shadow = DatBitmapReader.getImage(shadowTranslator, this, shadowPositions[i], name + "-SH" + goldIndex + ":" + i);
				images[i].setShadow(shadow);
			}
		}

		settlerSequences[goldIndex] = new ArraySequence<>(images);
	}

	private long[] readSequenceHeader(int position) throws IOException {
		reader.skipTo(position);

		reader.assumeToRead(SEQUENCE_START);
		int frameCount = reader.read8();

		long[] framePositions = new long[frameCount];
		for (int i = 0; i < frameCount; i++) {
			framePositions[i] = reader.read32() + position;
		}
		return framePositions;
	}

	@Override
	public Sequence<SingleImage> getLandscapes() {
		return landscapeSequence;
	}

	@Override
	public Sequence<SingleImage> getGuis() {
		return guiSequence;
	}

	/**
	 * This landscape image list loads the landscape images.
	 *
	 * @author michael
	 */
	private class LandscapeImageSequence implements Sequence<SingleImage> {
		/**
		 * Forces a get of the image.
		 */
		@Override
		public SingleImage getImage(int index, Supplier<String> custom_name) {
			initializeIfNeeded();
			if (landscapeImages[index] == null) {
				String str_custom_name = custom_name != null ? custom_name.get() : null;
				if(str_custom_name == null) str_custom_name = "";
				loadLandscapeImage(index, str_custom_name+file_name+"-L"+index);
			}
			return landscapeImages[index];
		}

		@Override
		public int length() {
			initializeIfNeeded();
			return landscapeImages.length;
		}

		@Override
		public SingleImage getImageSafe(int index, Supplier<String> custom_name) {
			initializeIfNeeded();
			if (index < 0 || index >= length()) {
				return NullImage.getInstance();
			} else {
				if (landscapeImages[index] == null) {
					String str_custom_name = custom_name != null ? custom_name.get() : null;
					if(str_custom_name == null) str_custom_name = "";
					loadLandscapeImage(index, str_custom_name+file_name+"-L"+index);
				}
				return landscapeImages[index];
			}
		}
	}

	@Override
	public long getOffsetForLandscape(int index) {
		initializeIfNeeded();

		return landscapeStarts[index];
	}

	private void loadLandscapeImage(int index, String name) {
		initializeIfNeeded();

		try {
			SingleImage image = DatBitmapReader.getImage(landscapeTranslator, this, landscapeStarts[index], name);
			landscapeImages[index] = image;
		} catch (IOException e) {
			landscapeImages[index] = NullImage.getForLandscape();
		}
	}

	/**
	 * This landscape image list loads the landscape images.
	 *
	 * @author michael
	 */
	private class GuiImageSequence implements Sequence<SingleImage> {
		/**
		 * Forces a get of the image.
		 */
		@Override
		public SingleImage getImage(int index, Supplier<String> custom_name) {
			initializeIfNeeded();
			if (guiImages[index] == null) {
				String str_custom_name = custom_name != null ? custom_name.get() : null;
				if(str_custom_name == null) str_custom_name = "";
				loadGuiImage(index, str_custom_name+file_name+"-G"+index);
			}
			return guiImages[index];
		}

		@Override
		public int length() {
			initializeIfNeeded();
			return guiImages.length;
		}

		@Override
		public SingleImage getImageSafe(int index, Supplier<String> custom_name) {
			initializeIfNeeded();
			if (index < 0 || index >= length()) {
				return NullImage.getInstance();
			} else {
				if (guiImages[index] == null) {
					String str_custom_name = custom_name != null ? custom_name.get() : null;
					if(str_custom_name == null) str_custom_name = "";
					loadGuiImage(index, str_custom_name+file_name+"-G"+index);
				}
				return guiImages[index];
			}
		}
	}

	private void loadGuiImage(int goldIndex, String name) {
		initializeIfNeeded();
		try {
			int theseGraphicsFilesIndex = mapping.mapGuiImage(goldIndex);
			SingleImage image = DatBitmapReader.getImage(guiTranslator, this, guiStarts[theseGraphicsFilesIndex], name);
			guiImages[goldIndex] = image;
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			guiImages[goldIndex] = NullImage.getForGui();
		}
	}

	public long[] getSettlerPointers(int seqIndex) throws IOException {
		initializeIfNeeded();
		return readSequenceHeader(settlerStarts[seqIndex]);
	}

	public long[] getTorsoPointers(int seqIndex) throws IOException {
		initializeIfNeeded();
		int position = torsoStarts[seqIndex];
		if (position >= 0) {
			return readSequenceHeader(position);
		} else {
			return null;
		}
	}

	public DatBitmapTranslator<SettlerImage> getSettlerTranslator() {
		return settlerTranslator;
	}

	public DatBitmapTranslator<SingleImage> getTorsoTranslator() {
		return torsoTranslator;
	}

	@Override
	public DatBitmapTranslator<SingleImage> getLandscapeTranslator() {
		return landscapeTranslator;
	}
}