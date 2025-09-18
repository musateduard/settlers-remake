package jsettlers.main.swing.originalmenu.components;

import jsettlers.main.swing.originalmenu.MenuBackground;

import java.awt.Container;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;


public class OriginalDropdownList extends JComboBox<String> implements MouseListener, MouseMotionListener, Hoverable {

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

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

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
            graphics.drawImage(this.arrowHovered, this.getWidth() - 11, (this.getHeight() / 2) + 1, this);
        }

        else {
            graphics.drawImage(this.arrow, this.getWidth() - 11, (this.getHeight() / 2) + 1, this);
        }

        return;
    }


    @Override
    public void mouseMoved(MouseEvent event) {
        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {

        System.out.printf("event received\n");

        // create new jpanel
        // add it to parent component
        // repaint

        Container parent = this.getParent();

        JPanel overlay = new JPanel(null);

        overlay.setBounds(0, 0, 800, 600);
        overlay.setBackground(Color.BLUE);
        overlay.setOpaque(true);

        JPanel list = new JPanel();

        list.setBounds(100, 100, 100, 100);
        list.setBackground(Color.YELLOW);
        list.setOpaque(true);

        overlay.add(list);
        parent.add(overlay, 1);

        parent.repaint();
        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {
        return;
    }


    @Override
    public void mouseClicked(MouseEvent event) {
        return;
    }


    @Override
    public void mouseEntered(MouseEvent event) {
        return;
    }


    @Override
    public void mouseExited(MouseEvent event) {
        return;
    }


    @Override
    public void mouseDragged(MouseEvent event) {
        return;
    }
}