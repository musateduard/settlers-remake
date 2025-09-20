package jsettlers.main.swing.originalmenu.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLayeredPane;
import javax.swing.JComboBox;


public class OriginalDropdownList extends JComboBox<String> implements MouseListener, Hoverable {

    private boolean hovered;
    public final BufferedImage arrow;
    public final BufferedImage arrowHovered;
    public final String[] optionList;


    public OriginalDropdownList(String[] itemList, int offsetX, int offsetY, DropdownProps props) {

        this.hovered = false;
        this.arrow = props.arrow();
        this.arrowHovered = props.arrowHovered();
        this.optionList = itemList;

        this.setModel(new DefaultComboBoxModel<>(this.optionList));
        this.setBounds(offsetX, offsetY, props.width(), props.height());
        this.setBackground(Color.CYAN);
        this.setOpaque(false);
        this.setBorder(null);
        this.setSelectedItem(itemList[0]);
        this.setFont(props.textFont());
        this.setForeground(props.textColor());

        this.addMouseListener(this);

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
    public void mousePressed(MouseEvent event) {

        JLayeredPane internalPanel = (JLayeredPane) this.getParent();
        OriginalPopup list = new OriginalPopup(this);
        OriginalOverlay overlay = new OriginalOverlay();

        overlay.add(list);

        internalPanel.add(overlay, JLayeredPane.PALETTE_LAYER);
        internalPanel.repaint();

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
}