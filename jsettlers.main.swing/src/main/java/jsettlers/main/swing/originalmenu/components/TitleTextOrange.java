package jsettlers.main.swing.originalmenu.components;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.FontFormatException;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import java.io.IOException;
import java.io.InputStream;


public class TitleTextOrange extends JLabel {

    public TitleTextOrange(String text, int offsetX, int offsetY) {

        // load font
        Font titleFont;
        Color titleColor = new Color(248, 92, 24);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream fileStream = loader.getResourceAsStream("ms-sans-serif-bold.ttf");

        assert fileStream != null;

        try {
            titleFont = Font.createFont(Font.TRUETYPE_FONT, fileStream);
        }

        catch (IOException | FontFormatException exception) {
            titleFont = new Font("Arial", Font.BOLD, 14);

            System.out.printf("failed to open menu font: %s\n", loader.getName());
            exception.printStackTrace();
        }

        this.setText(text);
        this.setOpaque(false);
        this.setForeground(titleColor);
        this.setFont(titleFont.deriveFont(Font.BOLD, 14.00f));
        this.setVerticalAlignment(SwingConstants.TOP);
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.setBounds(offsetX, offsetY, this.getPreferredSize().width, this.getPreferredSize().height + 2);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        FontMetrics metrics = graphics.getFontMetrics();

        // paint shadow
        graphics.setColor(Color.BLACK);

        // adjust letter offsets
        int letterOffset = 0;
        for (int index = 0; index < this.getText().length(); index += 1) {

            char letter = this.getText().charAt(index);
            int letterX = letterOffset + 1;
            int letterY = metrics.getAscent() + 1;

            graphics.drawString(String.valueOf(letter), letterX, letterY);

            letterOffset += metrics.charWidth(letter) - ((letter == ' ') || (letter == 'R') ? 0 : 1);
        }

        // paint foreground
        graphics.setColor(new Color(248, 92, 24));

        // adjust letter offsets
        letterOffset = 0;
        for (int index = 0; index < this.getText().length(); index += 1) {

            char letter = this.getText().charAt(index);
            int letterX = letterOffset;
            int letterY = metrics.getAscent();

            graphics.drawString(String.valueOf(letter), letterX, letterY);

            letterOffset += metrics.charWidth(letter) - ((letter == ' ') || (letter == 'R') ? 0 : 1);
        }

        return;
    }
}