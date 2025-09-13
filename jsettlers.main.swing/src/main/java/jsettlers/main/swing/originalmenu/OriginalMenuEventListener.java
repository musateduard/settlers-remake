package jsettlers.main.swing.originalmenu;

import javax.swing.JPanel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public class OriginalMenuEventListener implements MouseListener, MouseMotionListener {

    public final JPanel parentComponent;
    public final OriginalMenuButton[] buttonList;
    public OriginalMenuButton pressedButton;


    public OriginalMenuEventListener(JPanel menuPanel, OriginalMenuButton[] buttonList) {
        this.parentComponent = menuPanel;
        this.buttonList = buttonList;
        return;
    }


    /**
     * this method takes the current position of the cursor and returns a Point with the equivalent
     * coordinates on an 800 x 600 screen. this is used for positioning the cursor inside the
     * inner buffer of menu panels in order to determine if a button is pressed or hovered.
     */
    public Point getScaledPosition(MouseEvent event) {

        int parentWidth = this.parentComponent.getWidth();
        int parentHeight = this.parentComponent.getHeight();

        int translatedX = (int) (((double) event.getX() / (double) parentWidth) * (double) 800);
        int translatedY = (int) (((double) event.getY() / (double) parentHeight) * (double) 600);

        Point position = new Point(translatedX, translatedY);

        return position;
    }


    @Override
    public void mouseMoved(MouseEvent event) {

        // when mouse moves check each button to see if mouse is hovering

        Point cursor = this.getScaledPosition(event);

        // set hovered status
        for (OriginalMenuButton item : this.buttonList) {

            Rectangle buttonBounds = item.getBounds();

            if (buttonBounds.contains(cursor) == false) {
                item.hovered = false;
                item.pressed = false;
            }

            else {
                item.hovered = buttonBounds.contains(cursor);
            }
        }

        this.parentComponent.repaint();
        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {

        // when mouse is pressed check each button if it received the click

        Point cursor = this.getScaledPosition(event);
        boolean anyButtonPressed = false;

        for (OriginalMenuButton item : this.buttonList) {

            if (item.getBounds().contains(cursor)) {

                item.pressed = true;
                this.pressedButton = item;
                anyButtonPressed = true;

                break;
            }

            continue;
        }

        if (anyButtonPressed == false) {
            this.pressedButton = null;
        }

        this.parentComponent.repaint();
        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {

        // when cursor is released check if pressedButton is null and if cursor is in bounds then do action

        Point cursor = this.getScaledPosition(event);

        // no button was pressed prior to release
        if (this.pressedButton == null) {

            // check if any button is hovered
            for (OriginalMenuButton item : this.buttonList) {

                Rectangle buttonBounds = item.getBounds();
                item.pressed = false;
                item.hovered = buttonBounds.contains(cursor);
            }
        }

        else {

            // button pressed and released
            if (this.pressedButton.getBounds().contains(cursor)) {

                this.pressedButton.hovered = true;
                this.pressedButton.pressed = false;
                this.pressedButton.doClick();
            }

            // button pressed but released somewhere else
            else {

                // check if any button is hovered
                for (OriginalMenuButton item : this.buttonList) {

                    Rectangle itemBounds = item.getBounds();
                    item.pressed = false;
                    item.hovered = itemBounds.contains(cursor);
                }
            }

            this.pressedButton = null;
        }

        this.parentComponent.repaint();
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