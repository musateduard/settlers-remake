package jsettlers.main.swing.originalmenu.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import javax.swing.SwingConstants;
import javax.swing.JLabel;


public class OriginalLabel extends JLabel {

    public OriginalLabel(String labelText, int offsetX, int offsetY, LabelProps props) {

        this.setText(labelText);
        this.setOpaque(false);
        this.setForeground(props.textColor());
        this.setFont(props.textFont());
        this.setVerticalAlignment(SwingConstants.TOP);
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.setBounds(offsetX, offsetY, this.getPreferredSize().width, this.getPreferredSize().height + 2);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        if (this.getFont().getSize() == 14) {

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
            graphics.setColor(this.getForeground());

            // adjust letter offsets
            letterOffset = 0;
            for (int index = 0; index < this.getText().length(); index += 1) {

                char letter = this.getText().charAt(index);
                int letterX = letterOffset;
                int letterY = metrics.getAscent();

                graphics.drawString(String.valueOf(letter), letterX, letterY);

                letterOffset += metrics.charWidth(letter) - ((letter == ' ') || (letter == 'R') ? 0 : 1);
            }
        }

        else {
            super.paintComponent(graphics);
        }

        return;
    }
}