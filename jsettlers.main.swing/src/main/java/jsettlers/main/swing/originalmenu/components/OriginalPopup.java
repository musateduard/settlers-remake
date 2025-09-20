package jsettlers.main.swing.originalmenu.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLayeredPane;
import javax.swing.JList;


public class OriginalPopup extends JList<String> implements MouseListener {

    public final OriginalDropdownList dropdown;


    public OriginalPopup(OriginalDropdownList parent) {

        super(parent.optionList);

        this.dropdown = parent;

        int offsetX = this.dropdown.getX();
        int offsetY = this.dropdown.getY() + this.dropdown.getHeight() + 8;
        int width = this.dropdown.getWidth();
        int height = this.getCellBounds(0, 0).height * this.dropdown.getModel().getSize();

        this.setOpaque(false);
        this.setFont(this.dropdown.getFont());
        this.setBackground(Color.BLACK);
        this.setForeground(this.dropdown.getForeground());
        this.setBounds(offsetX, offsetY, width, height);

        this.addMouseListener(this);

        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {

        // get list item at cursor
        Point cursor = event.getPoint();
        JLayeredPane internalPanel = (JLayeredPane) this.getParent().getParent();
        OriginalOverlay overlay = (OriginalOverlay) this.getParent();
        int index = this.locationToIndex(cursor);

        // set dropdown to selected item
        this.dropdown.setSelectedIndex(index);

        // close overlay
        internalPanel.remove(overlay);
        internalPanel.revalidate();
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