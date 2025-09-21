package jsettlers.main.swing.originalmenu.components;

import java.awt.Point;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;


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

        Point cursor = new Point(event.getX(), event.getY());
        Component component = this.getComponentAt(cursor);

        // dispatch event to component
        if (component != this) {

            int eventType = MouseEvent.MOUSE_PRESSED;
            int offsetX = cursor.x - component.getX();
            int offsetY = cursor.y - component.getY();
            long when = event.getWhen();
            int modifiers = event.getModifiersEx();
            int count = event.getClickCount();
            boolean popup = event.isPopupTrigger();

            MouseEvent newEvent = new MouseEvent(component, eventType, when, modifiers, offsetX, offsetY, count, popup);
            component.dispatchEvent(newEvent);
        }

        else {

            if (this.clickOutside == true) {
                JLayeredPane internalPanel = (JLayeredPane) this.getParent();
                internalPanel.remove(this);
                internalPanel.repaint();
            }
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