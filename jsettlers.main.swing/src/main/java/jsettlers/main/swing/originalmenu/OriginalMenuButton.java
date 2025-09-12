package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JButton;


public class OriginalMenuButton extends JButton {

    public final int buttonWidth;
    public final int buttonHeight;
    public final int offsetX;
    public final int offsetY;
    public boolean hovered;
    public boolean pressed;

    public final Font textFont;
    public final int fontSize;
    public final boolean shadow;

    public final BufferedImage buttonImage;
    public final BufferedImage buttonImageHovered;
    public final BufferedImage buttonImagePressed;


    public OriginalMenuButton(ButtonProps props, String buttonText, int offsetX, int offsetY) {

        this.textFont = props.buttonFont();
        this.fontSize = props.fontSize();
        this.shadow = props.shadow();

        this.buttonImage = props.buttonImage();
        this.buttonImageHovered = props.buttonImageHovered();
        this.buttonImagePressed = props.buttonImagePressed();

        this.hovered = false;
        this.pressed = false;

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.buttonWidth = this.buttonImage.getWidth();
        this.buttonHeight = this.buttonImage.getHeight();
        this.setText(buttonText);
        this.setBounds(this.offsetX, this.offsetY, this.buttonWidth, this.buttonHeight);
        this.setBorderPainted(false);
        this.setContentAreaFilled(false);
        this.setOpaque(false);
        this.setForeground(props.textColor());
        this.setFont(this.textFont.deriveFont(Font.PLAIN, 11f));

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        if (this.pressed) {
            graphics.drawImage(this.buttonImagePressed, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        else if (this.hovered) {
            graphics.drawImage(this.buttonImageHovered, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        else {
            graphics.drawImage(this.buttonImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        if (this.shadow) {

            graphics.setFont(this.textFont.deriveFont((float) this.fontSize));

            FontMetrics metrics = graphics.getFontMetrics();

            // get text position
            int textX = ((this.getWidth() - metrics.stringWidth(this.getText())) + 2 - 1) / 2;  // this ensures all divisions are rounded up
            int textY = ((this.getHeight() - metrics.getAscent() - (metrics.getDescent() / 2)) / 2) + metrics.getAscent() - (metrics.getDescent() / 2);

            graphics.setColor(Color.BLACK);
            graphics.drawString(this.getText(), textX + 1, textY + 1);
        }

        super.paintComponent(graphics);

        return;
    }
}