package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JFormattedTextField;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jsettlers.main.swing.originalmenu.components.Hoverable;
import jsettlers.main.swing.originalmenu.components.OriginalButton;
import jsettlers.main.swing.originalmenu.components.OriginalDropdown;
import jsettlers.main.swing.originalmenu.components.OriginalLabel;


/**
 * this class is a {@link JPanel} derived class that acts as a canvas on which all menu elements get painted.
 * it holds an internal {@link BufferedImage} of fixed size 800 x 600 and all images and text get painted
 * on this buffer. the buffer then gets painted on the {@link JPanel} background using {@link #paintComponent(Graphics)}
 * and the panel is added to the actual menu component. this panel is set to scale at a fixed aspect ratio of 4:3.
 */
public class MenuCanvas extends JPanel implements MouseListener, MouseMotionListener {

    // todo: create bold version of ms sans serif 13

    /*
    note:
    mouse events should be passed to the uppermost overlay
    the menus underneath should keep their previous state while overlay is visible
    */

    public final JLayeredPane internalPanel;
    public final BufferedImage menuImage;
    public final BufferedImage tempBuffer;
    public final OriginalButton[] buttonList;
    public final double idealAspectRatio = (double) 800 / (double) 600;
    public OriginalButton pressedButton;
    public Hoverable hoveredElement;


    public MenuCanvas(
        BufferedImage menuImage,
        OriginalButton[] buttonList,
        OriginalLabel[] labelList,
        JFormattedTextField[] inputFieldList,
        OriginalDropdown[] dropdownList) {

        this.menuImage = menuImage;
        this.buttonList = buttonList;
        this.tempBuffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        // add internal panel
        this.internalPanel = new JLayeredPane();

        this.internalPanel.setOpaque(false);
        this.internalPanel.setBounds(0, 0, 800, 600);

        if (buttonList != null) {
            for (OriginalButton button : this.buttonList) {
                this.internalPanel.add(button, JLayeredPane.DEFAULT_LAYER);
            }
        }

        if (inputFieldList != null) {
            for (JFormattedTextField input : inputFieldList) {
                internalPanel.add(input, JLayeredPane.DEFAULT_LAYER);
            }
        }

        if (labelList != null) {
            for (JLabel label : labelList) {
                internalPanel.add(label, JLayeredPane.DEFAULT_LAYER);
            }
        }

        if (dropdownList != null) {
            for (OriginalDropdown dropdown : dropdownList) {
                internalPanel.add(dropdown, JLayeredPane.DEFAULT_LAYER);
            }
        }

        // add event listeners
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        return;
    }


    /*
    note:

    internal panel should handle all swing component logic without having to handle events
    this should be done in the menu event listener class
    */
    public void openDialog() {
        System.out.printf("opening dialog window\n");
        return;
    }


    public void openDropDownList() {
        System.out.printf("opening dropdown list\n");
        return;
    }


    /**
     * this method takes the current position of the cursor and returns a {@link Point} with the equivalent
     * coordinates on an 800 x 600 screen. this is used for positioning the cursor inside the
     * inner buffer of menu panels in order to determine if a button is pressed or hovered.
     */
    public Point getScaledPosition(MouseEvent event) {

        int parentWidth = this.getWidth();
        int parentHeight = this.getHeight();

        int translatedX = (int) (((double) event.getX() / (double) parentWidth) * (double) 800);
        int translatedY = (int) (((double) event.getY() / (double) parentHeight) * (double) 600);

        Point position = new Point(translatedX, translatedY);

        return position;
    }


    /**
     * this method runs each time the menu canvas element gets redrawn. this happens on mouse move,
     * mouse press, mouse release and window resize.
     *
     * @param graphics the {@code Graphics} object the provides the context to draw on.
     */
    @Override
    public void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, this.getWidth(), this.getHeight());

        /*
        note:

        tempBuffer has a fixed size of 800 x 600
        all images are painted on temp buffer which is then painted onto the main frame
        don't paint onto the menuImage directly; it will cause artifacts from text antialiasing
        artifacts make text look thicker on each repaint
        */

        Graphics2D tempContext = this.tempBuffer.createGraphics();

        tempContext.drawImage(this.menuImage, 0, 0, this.tempBuffer.getWidth(), this.tempBuffer.getHeight(), this);

        // paint internal panel
        this.internalPanel.printAll(tempContext);

        tempContext.dispose();

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        graphics.drawImage(this.tempBuffer, 0, 0, this.getWidth(), this.getHeight(), this);

        return;
    }


    @Override
    public Dimension getPreferredSize() {

        // size needs to be based on parent size while also keeping aspect ratio

        Dimension parentSize = this.getParent().getSize();
        double currentAspectRatio = (double) parentSize.width / (double) parentSize.height;

        // height becomes deciding
        if (currentAspectRatio >= this.idealAspectRatio) {

            int newViewportWidth = (int) (this.idealAspectRatio * parentSize.height);
            int newViewportHeight = parentSize.height;

            Dimension newSize = new Dimension(newViewportWidth, newViewportHeight);

            return newSize;
        }

        // width becomes deciding
        else {

            int newViewportWidth = parentSize.width;
            int newViewportHeight = (int) (parentSize.width / this.idealAspectRatio);

            Dimension newSize = new Dimension(newViewportWidth, newViewportHeight);

            return newSize;
        }
    }


    public Component findNestedComponent(Point initialCursor) {

        Point cursor = initialCursor;
        Component component = this.internalPanel.getComponentAt(cursor);
        Component currentComponent = null;

        while (true) {

            currentComponent = component.getComponentAt(cursor);

            // nested component found
            if (currentComponent != component && currentComponent != null) {
                cursor = new Point(cursor.x - currentComponent.getX(), cursor.y - currentComponent.getY());
                component = currentComponent;
                continue;
            }

            // no nested component found at cursor
            else {
                break;
            }
        }

        return component;
    }


    @Override
    public void mouseMoved(MouseEvent event) {

        Point cursor = this.getScaledPosition(event);
        Component component = this.findNestedComponent(cursor);

        if (component instanceof Hoverable) {

            /*
            note:

            normally we use dispatchEvent and let child handle the event but that is too much overhead for this case
            button states also need to be managed at the parent level not the child
            */

            if (component != this.hoveredElement && this.hoveredElement != null) {
                this.hoveredElement.setHovered(false);
            }

            ((Hoverable) component).setHovered(true);
            this.hoveredElement = (Hoverable) component;
        }

        else {

            if (this.hoveredElement != null) {
                this.hoveredElement.setHovered(false);
            }

            if (this.pressedButton != null) {
                this.pressedButton.pressed = false;
            }

            this.hoveredElement = null;
            this.pressedButton = null;
        }

        this.repaint();
        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {

        Point cursor = this.getScaledPosition(event);
        Component component = this.findNestedComponent(cursor);

        // keep state of currently pressed button
        if (component instanceof OriginalButton) {

            if (component != this.pressedButton && this.pressedButton != null) {
                this.pressedButton.pressed = false;
            }

            ((OriginalButton) component).pressed = true;
            this.pressedButton = (OriginalButton) component;
        }

        else {
            this.pressedButton = null;
        }

        // dispatch event to component
        int eventType = MouseEvent.MOUSE_PRESSED;
        int offsetX = cursor.x - component.getX();
        int offsetY = cursor.y - component.getY();
        long when = event.getWhen();
        int modifiers = event.getModifiersEx();
        int count = event.getClickCount();
        boolean popup = event.isPopupTrigger();

        MouseEvent newEvent = new MouseEvent(component, eventType, when, modifiers, offsetX, offsetY, count, popup);
        component.dispatchEvent(newEvent);

        this.repaint();
        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {

        Point cursor = this.getScaledPosition(event);
        Component component = this.findNestedComponent(cursor);

        // no button was pressed prior to release
        if (this.pressedButton == null) {

            if (component instanceof Hoverable) {
                ((Hoverable) component).setHovered(true);
                this.hoveredElement = (Hoverable) component;
            }
        }

        // button pressed prior to release
        else {

            // same button pressed and released
            if (component == this.pressedButton) {
                this.pressedButton.setHovered(true);
            }

            // button pressed but released somewhere else
            else {

                // mark previous button as not hovered
                this.pressedButton.setHovered(false);

                // mark new button as hovered
                if (component instanceof Hoverable) {
                    ((Hoverable) component).setHovered(true);
                    this.hoveredElement = (Hoverable) component;
                }

                // mark no element as hovered
                else {
                    this.hoveredElement.setHovered(false);
                    this.hoveredElement = null;
                }
            }

            // note: this pattern will not work if other components need to process mouseReleased

            // dispatch event to component
            int eventType = MouseEvent.MOUSE_RELEASED;
            int offsetX = cursor.x - component.getX();
            int offsetY = cursor.y - component.getY();
            long when = event.getWhen();
            int modifiers = event.getModifiersEx();
            int count = event.getClickCount();
            boolean popup = event.isPopupTrigger();

            MouseEvent newEvent = new MouseEvent(component, eventType, when, modifiers, offsetX, offsetY, count, popup);
            component.dispatchEvent(newEvent);

            this.pressedButton.pressed = false;
            this.pressedButton = null;
        }

        this.repaint();
        return;
    }


    @Override
    public void mouseDragged(MouseEvent event) {
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