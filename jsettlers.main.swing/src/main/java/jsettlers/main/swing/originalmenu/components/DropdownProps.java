package jsettlers.main.swing.originalmenu.components;

import java.awt.Font;
import java.awt.Color;
import java.awt.image.BufferedImage;


public record DropdownProps(
    int width,
    int height,
    Font textFont,
    Color textColor,
    BufferedImage arrow,
    BufferedImage arrowHovered
) {}