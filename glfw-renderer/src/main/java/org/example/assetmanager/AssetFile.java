package org.example.assetmanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Parsed Settlers 3 GFX DAT file: raw bytes plus section/image headers.
 * Does not decode pixel or text payloads.
 */
public final class AssetFile {

    private static final int SIZE_U8 = 1;
    private static final int SIZE_U16 = 2;
    private static final int SIZE_U32 = 4;

    private static final int MAGIC_FILE = 0x41304;
    private static final int MAGIC_TEXT = 0x1904;
    private static final int MAGIC_LANDSCAPE = 0x2412;
    private static final int MAGIC_MENU = 0x11306;
    private static final int MAGIC_SPRITE = 0x106;
    private static final int MAGIC_COLOR_SPRITE = 0x3112;
    private static final int MAGIC_SHADOW = 0x5982;
    private static final int MAGIC_SEQUENCE = 0x1402;
    private static final int MAGIC_SPRITE_IMAGE = 0x0c;

    private static final int CHANNEL_RED_565 = 0xf800;
    private static final int CHANNEL_GREEN_565 = 0x07e0;
    private static final int CHANNEL_RED_555 = 0x7c00;
    private static final int CHANNEL_GREEN_555 = 0x03e0;
    private static final int CHANNEL_BLUE = 0x001f;

    public static final int[] RED_BLUE_TABLE = {
        0,     8,  16,  25,  33,  41,  49,  58,  66,  74,  82,  90,  99, 107, 115, 123,
        132, 140, 148, 156, 165, 173, 181, 189, 197, 206, 214, 222, 230, 239, 247, 255
    };

    public static final int[] GREEN_TABLE = {
        0,     4,   8,  12,  16,  20,  24,  28,  32,  36,  40,  45,  49,  53,  57,  61,
        65,   69,  73,  77,  81,  85,  89,  93,  97, 101, 105, 109, 113, 117, 121, 125,
        130, 134, 138, 142, 146, 150, 154, 158, 162, 166, 170, 174, 178, 182, 186, 190,
        194, 198, 202, 206, 210, 215, 219, 223, 227, 231, 235, 239, 243, 247, 251, 255
    };

    public final int index;
    public final Path filePath;
    public final byte[] byteArray;
    public final FileHeader header;
    public final TextSectionHeader textSection;
    public final LandscapeSection landscapeSection;
    public final MenuSection menuSection;
    public final SpriteSection spriteSection;
    public final SpriteSection colorSpriteSection;
    public final SpriteSection shadowSection;
    public final PaletteSectionHeader paletteSection;


    public AssetFile(int fileIndex, Path filePath) throws IOException {

        // todo: add missing decode_image method

        this.index = fileIndex;
        this.filePath = filePath;
        this.byteArray = Files.readAllBytes(filePath);
        this.header = AssetFile.parseFileHeader(this.byteArray);

        this.textSection = AssetFile.parseTextSection(this.byteArray, this.header.textSectionOffset());
        this.landscapeSection = AssetFile.parseLandscapeSection(this.byteArray, this.header.landscapeSectionOffset());
        this.menuSection = AssetFile.parseMenuSection(this.byteArray, this.header.menuSectionOffset());
        this.spriteSection = AssetFile.parseSpriteSection(this.byteArray, this.header.spriteSectionOffset());
        this.colorSpriteSection = AssetFile.parseSpriteSection(this.byteArray, this.header.colorSpriteSectionOffset());
        this.shadowSection = AssetFile.parseSpriteSection(this.byteArray, this.header.shadowSectionOffset());
        this.paletteSection = AssetFile.parsePaletteSection(this.byteArray, this.header.paletteSectionOffset());

        return;
    }


    private static FileHeader parseFileHeader(byte[] bytes) {

        FileHeader header = new FileHeader(
            AssetFile.readU32(bytes, 0),
            AssetFile.readU32(bytes, 1 * SIZE_U32),
            AssetFile.readU32(bytes, 2 * SIZE_U32),
            AssetFile.readU32(bytes, 3 * SIZE_U32),
            AssetFile.readU32(bytes, 4 * SIZE_U32),
            AssetFile.readU32(bytes, 5 * SIZE_U32),
            AssetFile.readU32(bytes, 6 * SIZE_U32),
            AssetFile.readU32(bytes, 7 * SIZE_U32),
            AssetFile.readU32(bytes, 8 * SIZE_U32),
            AssetFile.readU32(bytes, 9 * SIZE_U32),
            AssetFile.readU32(bytes, 10 * SIZE_U32),
            AssetFile.readU32(bytes, 11 * SIZE_U32),
            AssetFile.readU32(bytes, 12 * SIZE_U32),
            AssetFile.readU32(bytes, 13 * SIZE_U32),
            AssetFile.readU32(bytes, 14 * SIZE_U32),
            AssetFile.readU32(bytes, 15 * SIZE_U32),
            AssetFile.readU32(bytes, 16 * SIZE_U32),
            AssetFile.readU32(bytes, 17 * SIZE_U32),
            AssetFile.readU32(bytes, 18 * SIZE_U32),
            AssetFile.readU32(bytes, 19 * SIZE_U32),
            AssetFile.readU32(bytes, 20 * SIZE_U32)
        );

        // note: should we use assert instead?
        if (header.magicNumber() != MAGIC_FILE) {
            throw new IllegalArgumentException("unexpected DAT magic: 0x" + Integer.toHexString(header.magicNumber()));
        }

        if (header.unknown1() != 0x0c || header.unknown2() != 0x00 || header.headerSize() != 0x54) {
            throw new IllegalArgumentException("unexpected DAT header layout");
        }

        if (header.unknown3() != 0x20 || header.unknown4() != 0x40 || header.unknown5() != 0x00 || header.unknown6() != 0x10) {
            throw new IllegalArgumentException("unexpected DAT header unknowns");
        }

        boolean rgb565 = header.redChannelBits() == CHANNEL_RED_565 && header.greenChannelBits() == CHANNEL_GREEN_565;
        boolean rgb555 = header.redChannelBits() == CHANNEL_RED_555 && header.greenChannelBits() == CHANNEL_GREEN_555;

        if ((rgb565 || rgb555) == false) {
            throw new IllegalArgumentException("unexpected DAT channel bit masks");
        }

        if (header.blueChannelBits() != CHANNEL_BLUE || header.alphaChannelBits() != 0x00) {
            throw new IllegalArgumentException("unexpected DAT blue/alpha channel bits");
        }

        if (header.fileSize() != bytes.length) {
            throw new IllegalArgumentException("DAT fileSize does not match byte length");
        }

        if (header.textSectionOffset() != 0x54) {
            throw new IllegalArgumentException("unexpected text section offset");
        }

        return header;
    }


    private static TextSectionHeader parseTextSection(byte[] bytes, int sectionOffset) {

        int magicNumber = AssetFile.readU32(bytes, sectionOffset);
        int headerSize = AssetFile.readU32(bytes, sectionOffset + SIZE_U32);
        short stringCount = (short) AssetFile.readU16(bytes, sectionOffset + 2 * SIZE_U32);
        short languageCount = (short) AssetFile.readU16(bytes, sectionOffset + 2 * SIZE_U32 + SIZE_U16);

        if (magicNumber != MAGIC_TEXT) {
            throw new IllegalArgumentException("unexpected text section magic: 0x" + Integer.toHexString(magicNumber));
        }

        return new TextSectionHeader(
            magicNumber,
            headerSize,
            stringCount,
            languageCount,
            Collections.emptyList(),
            sectionOffset
        );
    }


    private static LandscapeSection parseLandscapeSection(byte[] bytes, int sectionOffset) {

        int magicNumber = AssetFile.readU32(bytes, sectionOffset);
        short headerSize = (short) AssetFile.readU16(bytes, sectionOffset + 2 * SIZE_U16);
        short imageCount = (short) AssetFile.readU16(bytes, sectionOffset + 3 * SIZE_U16);

        if (imageCount == 0) {
            return new LandscapeSection(
                new SectionHeader(magicNumber, headerSize, imageCount, new int[0], sectionOffset),
                List.of()
            );
        }

        int[] offsetList = new int[imageCount];
        List<LandscapeImageHeader> imageList = new ArrayList<>(imageCount);

        for (int imageIndex = 0; imageIndex < imageCount; imageIndex++) {
            int imageOffset = AssetFile.readU32(bytes, sectionOffset + (2 + imageIndex) * SIZE_U32);
            offsetList[imageIndex] = imageOffset;
            imageList.add(AssetFile.parseLandscapeImageHeader(bytes, imageOffset));
        }

        if (magicNumber != MAGIC_LANDSCAPE) {
            throw new IllegalArgumentException("unexpected landscape section magic: 0x" + Integer.toHexString(magicNumber));
        }
        if (headerSize < 0x8) {
            throw new IllegalArgumentException("landscape section header size too small");
        }

        return new LandscapeSection(
            new SectionHeader(magicNumber, headerSize, imageCount, offsetList, sectionOffset),
            List.copyOf(imageList)
        );
    }


    private static LandscapeImageHeader parseLandscapeImageHeader(byte[] bytes, int imageOffset) {

        short width = (short) AssetFile.readU16(bytes, imageOffset);
        short height = (short) AssetFile.readU16(bytes, imageOffset + SIZE_U16);
        byte typeSize = bytes[imageOffset + 2 * SIZE_U16];
        byte textureType = bytes[imageOffset + 2 * SIZE_U16 + SIZE_U8];

        int afterHeader = imageOffset + 2 * SIZE_U16 + 2 * SIZE_U8;
        int dataStartOffset = (afterHeader % 2 == 1) ? afterHeader + 1 : afterHeader;

        if (imageOffset >= bytes.length || dataStartOffset >= bytes.length) {
            throw new IllegalArgumentException("landscape image offsets out of bounds");
        }

        return new LandscapeImageHeader(width, height, typeSize, textureType, imageOffset, dataStartOffset);
    }


    private static MenuSection parseMenuSection(byte[] bytes, int sectionOffset) {

        int magicNumber = AssetFile.readU32(bytes, sectionOffset);
        short headerSize = (short) AssetFile.readU16(bytes, sectionOffset + 2 * SIZE_U16);
        short imageCount = (short) AssetFile.readU16(bytes, sectionOffset + 3 * SIZE_U16);

        if (imageCount == 0) {
            return new MenuSection(
                new SectionHeader(magicNumber, headerSize, imageCount, new int[0], sectionOffset),
                List.of()
            );
        }

        int[] offsetList = new int[imageCount];
        List<MenuImageHeader> imageList = new ArrayList<>(imageCount);

        for (int imageIndex = 0; imageIndex < imageCount; imageIndex++) {
            int imageOffset = AssetFile.readU32(bytes, sectionOffset + (2 + imageIndex) * SIZE_U32);
            offsetList[imageIndex] = imageOffset;
            imageList.add(AssetFile.parseMenuImageHeader(bytes, imageOffset));
        }

        if (magicNumber != MAGIC_MENU) {
            throw new IllegalArgumentException("unexpected menu section magic: 0x" + Integer.toHexString(magicNumber));
        }
        if (headerSize < 0x8) {
            throw new IllegalArgumentException("menu section header size too small");
        }

        return new MenuSection(
            new SectionHeader(magicNumber, headerSize, imageCount, offsetList, sectionOffset),
            List.copyOf(imageList)
        );
    }


    private static MenuImageHeader parseMenuImageHeader(byte[] bytes, int imageOffset) {

        int padding = imageOffset % 2 == 0 ? 2 : 1;

        MenuImageHeader header = new MenuImageHeader(
            (short) AssetFile.readU16(bytes, imageOffset),
            (short) AssetFile.readU16(bytes, imageOffset + 1 * SIZE_U16),
            (short) AssetFile.readU16(bytes, imageOffset + 2 * SIZE_U16),
            (short) AssetFile.readU16(bytes, imageOffset + 3 * SIZE_U16),
            imageOffset,
            imageOffset + 4 * SIZE_U16 + padding
        );

        if (header.width() <= 0 || header.height() <= 0) {
            throw new IllegalArgumentException("menu image size is null");
        }
        if (header.headerStartOffset() >= bytes.length || header.dataStartOffset() >= bytes.length) {
            throw new IllegalArgumentException("menu image offsets out of bounds");
        }

        return header;
    }


    private static SpriteSection parseSpriteSection(byte[] bytes, int sectionOffset) {

        int sectionMagicNumber = AssetFile.readU32(bytes, sectionOffset);
        short sectionHeaderSize = (short) AssetFile.readU16(bytes, sectionOffset + 2 * SIZE_U16);
        short sequenceCount = (short) AssetFile.readU16(bytes, sectionOffset + 3 * SIZE_U16);

        if (sequenceCount == 0) {
            return new SpriteSection(
                new SectionHeader(sectionMagicNumber, sectionHeaderSize, sequenceCount, new int[0], sectionOffset),
                List.of()
            );
        }

        int[] sequenceOffsetList = new int[sequenceCount];
        List<SequenceHeader> sequenceList = new ArrayList<>(sequenceCount);

        for (int sequenceIndex = 0; sequenceIndex < sequenceCount; sequenceIndex++) {
            int currentArrayOffset = sectionOffset + (2 + sequenceIndex) * SIZE_U32;
            int sequenceOffset = AssetFile.readU32(bytes, currentArrayOffset);
            sequenceOffsetList[sequenceIndex] = sequenceOffset;
            sequenceList.add(AssetFile.parseSpriteSequence(bytes, sequenceOffset));
        }

        boolean knownMagic =
            sectionMagicNumber == MAGIC_SPRITE
                || sectionMagicNumber == MAGIC_COLOR_SPRITE
                || sectionMagicNumber == MAGIC_SHADOW;

        if (knownMagic == false) {
            throw new IllegalArgumentException("unexpected sprite-like section magic: 0x" + Integer.toHexString(sectionMagicNumber));
        }
        if (sectionHeaderSize < 0x8) {
            throw new IllegalArgumentException("sprite-like section header size too small");
        }

        return new SpriteSection(
            new SectionHeader(sectionMagicNumber, sectionHeaderSize, sequenceCount, sequenceOffsetList, sectionOffset),
            List.copyOf(sequenceList)
        );
    }


    private static SequenceHeader parseSpriteSequence(byte[] bytes, int sequenceOffset) {

        int sequenceMagicNumber = AssetFile.readU32(bytes, sequenceOffset);
        int unknown1 = AssetFile.readU16(bytes, sequenceOffset + SIZE_U32);
        byte unknown2 = bytes[sequenceOffset + SIZE_U32 + SIZE_U16];
        byte spriteCount = bytes[sequenceOffset + SIZE_U32 + SIZE_U16 + SIZE_U8];

        int count = spriteCount & 0xFF;
        List<SpriteHeader> spriteList = new ArrayList<>(count);

        for (int spriteIndex = 0; spriteIndex < count; spriteIndex++) {
            int currentArrayOffset = sequenceOffset + (2 + spriteIndex) * SIZE_U32;
            int spriteAbsoluteOffset = sequenceOffset + AssetFile.readU32(bytes, currentArrayOffset);
            spriteList.add(AssetFile.parseSpriteHeader(bytes, spriteAbsoluteOffset));
        }

        if (sequenceMagicNumber != MAGIC_SEQUENCE) {
            throw new IllegalArgumentException("unexpected sequence magic: 0x" + Integer.toHexString(sequenceMagicNumber));
        }
        if (sequenceOffset == 0) {
            throw new IllegalArgumentException("sequence offset is null");
        }

        return new SequenceHeader(
            sequenceMagicNumber,
            unknown1,
            unknown2,
            spriteCount,
            List.copyOf(spriteList),
            sequenceOffset
        );
    }


    private static SpriteHeader parseSpriteHeader(byte[] bytes, int spriteOffset) {

        // some images have odd addresses; padding sits between header and pixel data
        int padding = spriteOffset % 2 == 0 ? 2 : 1;

        SpriteHeader header = new SpriteHeader(
            AssetFile.readU32(bytes, spriteOffset),
            (short) AssetFile.readU16(bytes, spriteOffset + 2 * SIZE_U16),
            (short) AssetFile.readU16(bytes, spriteOffset + 3 * SIZE_U16),
            (short) AssetFile.readU16(bytes, spriteOffset + 4 * SIZE_U16),
            (short) AssetFile.readU16(bytes, spriteOffset + 5 * SIZE_U16),
            spriteOffset,
            spriteOffset + 6 * SIZE_U16 + padding
        );

        if (header.magicNumber() != MAGIC_SPRITE_IMAGE) {
            throw new IllegalArgumentException("unexpected sprite image magic: 0x" + Integer.toHexString(header.magicNumber()));
        }
        if (header.headerStartOffset() == 0 || header.headerStartOffset() >= bytes.length) {
            throw new IllegalArgumentException("sprite header offset out of bounds");
        }

        return header;
    }


    private static PaletteSectionHeader parsePaletteSection(byte[] bytes, int sectionOffset) {

        int magicNumber = AssetFile.readU32(bytes, sectionOffset);
        short headerSize = (short) AssetFile.readU16(bytes, sectionOffset + SIZE_U32);
        short offsetCount = (short) AssetFile.readU16(bytes, sectionOffset + SIZE_U32 + SIZE_U16);
        int paletteSize = AssetFile.readU32(bytes, sectionOffset + 2 * SIZE_U32);

        if (offsetCount == 0) {
            return new PaletteSectionHeader(
                magicNumber,
                headerSize,
                offsetCount,
                paletteSize,
                new int[0],
                sectionOffset
            );
        }

        int[] paletteOffsetList = new int[offsetCount];
        for (int offsetIndex = 0; offsetIndex < offsetCount; offsetIndex++) {
            // offsets begin after magic + headerSize + offsetCount + paletteSize
            paletteOffsetList[offsetIndex] = AssetFile.readU32(bytes, sectionOffset + (3 + offsetIndex) * SIZE_U32);
        }

        return new PaletteSectionHeader(
            magicNumber,
            headerSize,
            offsetCount,
            paletteSize,
            paletteOffsetList,
            sectionOffset
        );
    }


    private static int readU32(byte[] byteArray, int baseAddress) {

        if (byteArray.length == 0) {
            throw new IllegalArgumentException("raw bytes list is empty");
        }
        if (baseAddress < 0 || baseAddress >= byteArray.length) {
            throw new IllegalArgumentException("base address is outside of file address space");
        }

        int result = 0;
        int arraySize = byteArray.length;

        if (baseAddress + 0 < arraySize) {
            result |= byteArray[baseAddress + 0] & 0xFF;
        }
        if (baseAddress + 1 < arraySize) {
            result |= (byteArray[baseAddress + 1] & 0xFF) << 8;
        }
        if (baseAddress + 2 < arraySize) {
            result |= (byteArray[baseAddress + 2] & 0xFF) << 16;
        }
        if (baseAddress + 3 < arraySize) {
            result |= (byteArray[baseAddress + 3] & 0xFF) << 24;
        }

        return result;
    }


    private static int readU16(byte[] byteArray, int baseAddress) {

        if (byteArray.length == 0) {
            throw new IllegalArgumentException("raw bytes list is empty");
        }
        if (baseAddress < 0 || baseAddress >= byteArray.length) {
            throw new IllegalArgumentException("base address is outside of file address space");
        }

        int result = 0;
        int arraySize = byteArray.length;

        if (baseAddress + 0 < arraySize) {
            result |= byteArray[baseAddress + 0] & 0xFF;
        }
        if (baseAddress + 1 < arraySize) {
            result |= (byteArray[baseAddress + 1] & 0xFF) << 8;
        }

        return result;
    }
}