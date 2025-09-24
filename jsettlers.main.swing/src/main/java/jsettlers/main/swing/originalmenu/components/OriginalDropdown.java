package jsettlers.main.swing.originalmenu.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JLayeredPane;
import javax.swing.JComboBox;


public class OriginalDropdown extends JComboBox<String> implements MouseListener, Hoverable {

    private boolean hovered;
    public final BufferedImage arrow;
    public final BufferedImage arrowHovered;
    public final String[] optionList;


    public OriginalDropdown(String[] itemList, int offsetX, int offsetY, int defaultIndex, DropdownProps props) {

        super(itemList);

        this.hovered = false;
        this.arrow = props.arrow();
        this.arrowHovered = props.arrowHovered();
        this.optionList = itemList;

        this.setBounds(offsetX, offsetY, props.width(), props.height());
        this.setBackground(Color.CYAN);
        this.setOpaque(false);
        this.setBorder(null);
        this.setSelectedItem(itemList[0]);
        this.setFont(props.textFont());
        this.setForeground(props.textColor());

        this.addMouseListener(this);

        if (defaultIndex != 0) {
            this.setSelectedIndex(defaultIndex);
            this.revalidate();
            this.repaint();
        }

        return;
    }


    public void setHovered(boolean hoveredValue) {
        this.hovered = hoveredValue;
        return;
    }


    public boolean getHovered() {
        return this.hovered;
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

        // note: dropdown can either be on menu component or dialog component that sits on an overlay

        Component internalPanel = this.getParent();

        while (true) {

            // note: this can lead to infinite loops if no JLayeredPane is found

            if (internalPanel instanceof JLayeredPane) {
                break;
            }

            else {
                internalPanel = internalPanel.getParent();
                continue;
            }
        }

        OriginalPopup list = new OriginalPopup(this);
        OriginalOverlay overlay = new OriginalOverlay(true);

        overlay.add(list);

        ((JLayeredPane) internalPanel).add(overlay, JLayeredPane.POPUP_LAYER);
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