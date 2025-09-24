package jsettlers.main.swing.originalmenu.components;

import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class OriginalOverlay extends JPanel implements MouseListener {

    public final boolean clickOutside;


    public OriginalOverlay(boolean clickOutside) {

        this.clickOutside = clickOutside;

        this.setLayout(null);
        this.setBounds(0, 0, 800, 600);
        this.setOpaque(false);

        this.addMouseListener(this);

        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {

        if (this.clickOutside == true) {
            JLayeredPane internalPanel = (JLayeredPane) this.getParent();
            internalPanel.remove(this);
            internalPanel.repaint();
        }

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