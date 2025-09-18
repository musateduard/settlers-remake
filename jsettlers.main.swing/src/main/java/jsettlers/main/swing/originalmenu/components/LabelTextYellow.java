package jsettlers.main.swing.originalmenu.components;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontFormatException;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import java.io.IOException;
import java.io.InputStream;


public class LabelTextYellow extends JLabel {

    public LabelTextYellow(String labelText, int offsetX, int offsetY) {

        // load font
        Font labelFont;
        Color labelColor = new Color(255, 223, 0);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream fileStream = loader.getResourceAsStream("ms-sans-serif-1.ttf");

        assert fileStream != null;

        try {
            labelFont = Font.createFont(Font.TRUETYPE_FONT, fileStream);
        }

        catch (IOException | FontFormatException exception) {
            labelFont = new Font("Arial", Font.PLAIN, 11);

            System.out.printf("failed to open menu font: %s\n", loader.getName());
            exception.printStackTrace();
        }

        this.setText(labelText);
        this.setOpaque(false);
        this.setForeground(labelColor);
        this.setFont(labelFont.deriveFont(Font.PLAIN, 11.00f));
        this.setVerticalAlignment(SwingConstants.TOP);
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.setBounds(offsetX, offsetY, this.getPreferredSize().width, this.getPreferredSize().height + 2);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        super.paintComponent(graphics);

        return;
    }
}