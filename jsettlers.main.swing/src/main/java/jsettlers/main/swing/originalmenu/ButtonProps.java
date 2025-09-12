package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;


public record ButtonProps(
    BufferedImage buttonImage,
    BufferedImage buttonImageHovered,
    BufferedImage buttonImagePressed,
    Font buttonFont,
    int fontSize,
    boolean shadow,
    Color textColor
) {}