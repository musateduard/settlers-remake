package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.image.BufferedImage;


public record ButtonProps(
    BufferedImage buttonImage,
    BufferedImage buttonImageHovered,
    BufferedImage buttonImagePressed,
    Font textFont,
    Color textColor,
    boolean shadow
) {}