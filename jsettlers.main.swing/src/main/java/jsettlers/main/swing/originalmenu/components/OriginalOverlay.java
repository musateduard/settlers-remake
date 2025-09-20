package jsettlers.main.swing.originalmenu.components;

import javax.swing.JPanel;
import javax.swing.JComponent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class OriginalOverlay extends JPanel implements MouseListener {

    public OriginalOverlay(JComponent overlayItem) {

        this.setLayout(null);
        this.setBounds(0, 0, 800, 600);
        this.setOpaque(false);
        this.add(overlayItem);

        this.addMouseListener(this);

        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {

        System.out.printf("mouse pressed on overlay\n");

        Component[] comp = this.getComponents();

        for (Component item : comp) {
            System.out.printf("%s\n", item);
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