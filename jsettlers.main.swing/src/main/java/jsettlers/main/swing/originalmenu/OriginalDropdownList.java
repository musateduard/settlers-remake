package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;


public class OriginalDropdownList extends JComboBox<String> implements Hoverable {

    private boolean hovered;
    public final BufferedImage arrow;
    public final BufferedImage arrowHovered;


    public OriginalDropdownList(String[] itemList, int offsetX, int offsetY, int width, int height, Font textFont, Color textColor, BufferedImage arrow, BufferedImage arrowHovered) {

        // create new overlay panel
        // create list panel with items
        // add list to overlay
        // add overlay to background panel

        this.hovered = false;
        this.arrow = arrow;
        this.arrowHovered = arrowHovered;

        this.setModel(new DefaultComboBoxModel<>(itemList));
        this.setBounds(offsetX, offsetY, width, height);
        this.setBackground(Color.CYAN);
        this.setOpaque(false);
        this.setBorder(null);
        this.setSelectedItem(itemList[0]);
        this.setFont(textFont);
        this.setForeground(textColor);

        return;
    }


    public void setHovered(boolean hoveredValue) {
        this.hovered = hoveredValue;
        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        // don't paint border and other elements
        // do paint dropdown arrow image
        // do paint currently selected option

        // draw selected option
        if (this.getSelectedItem() != null) {
            graphics.drawString(this.getSelectedItem().toString(), 1, 14);
        }

        // draw arrow
        if (this.hovered) {
            System.out.printf("painting hovered arrow\n");
            graphics.drawImage(this.arrowHovered, this.getWidth() - 11, (this.getHeight() / 2) + 1, this);
        }

        else {
            System.out.printf("painting arrow\n");
            graphics.drawImage(this.arrow, this.getWidth() - 11, (this.getHeight() / 2) + 1, this);
        }

        return;
    }
}