package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JButton;


public class OriginalMenuButton extends JButton implements MouseListener, MouseMotionListener {

    public final int buttonWidth;
    public final int buttonHeight;
    public final int offsetX;
    public final int offsetY;
    public boolean hovered;
    public boolean pressed;

    public final Font textFont;
    public final boolean textShadow;

    public final BufferedImage buttonImage;
    public final BufferedImage buttonImageHovered;
    public final BufferedImage buttonImagePressed;


    public OriginalMenuButton(String buttonText, int offsetX, int offsetY, ButtonProps props) {

        this.textFont = props.textFont();
        this.textShadow = props.shadow();

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
        this.setFont(this.textFont);

        this.addMouseMotionListener(this);
        this.addMouseListener(this);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        // todo: paint text with offset when button is pressed

        if (this.pressed) {
            graphics.drawImage(this.buttonImagePressed, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        else if (this.hovered) {
            graphics.drawImage(this.buttonImageHovered, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        else {
            graphics.drawImage(this.buttonImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        if (this.textShadow) {

            graphics.setFont(this.textFont);

            FontMetrics metrics = graphics.getFontMetrics();

            // get text offsets
            int textX = ((this.getWidth() - metrics.stringWidth(this.getText())) + 2 - 1) / 2;  // this ensures all divisions are rounded up
            int textY = ((this.getHeight() - metrics.getAscent() - (metrics.getDescent() / 2)) / 2) + metrics.getAscent();

            // correction "descent / 2" might not be accurate
            // correction might be just 1

            graphics.setColor(Color.BLACK);
            graphics.drawString(this.getText(), textX + 1, textY + 1);
        }

        super.paintComponent(graphics);

        return;
    }


    @Override
    public void mouseClicked(MouseEvent event) {
        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {
        System.out.printf("mouse pressed dfghjfdgjhdfgjg\n");
        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {
        return;
    }


    @Override
    public void mouseEntered(MouseEvent event) {
        System.out.printf("mouse entered button %s\n", this.getText());
        return;
    }


    @Override
    public void mouseExited(MouseEvent event) {
        System.out.printf("mouse exited button %s\n", this.getText());
        return;
    }


    @Override
    public void mouseDragged(MouseEvent event) {
        return;
    }


    @Override
    public void mouseMoved(MouseEvent event) {
        System.out.printf("mouse moved inside button %s\n", this.getText());
        return;
    }
}