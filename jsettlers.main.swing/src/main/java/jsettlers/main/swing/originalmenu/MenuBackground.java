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
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.ArrayList;


/**
 * this class is a {@link JPanel} derived class that acts as a canvas on which all menu elements get painted.
 * it holds an internal {@link BufferedImage} of fixed size 800 x 600 and all images and text get painted
 * on this buffer. the buffer then gets painted on the {@link JPanel} background using {@link #paintComponent(Graphics)}
 * and the panel is added to the actual menu component. this panel is set to scale at a fixed aspect ratio of 4:3.
 */
public class MenuBackground extends JPanel implements MouseListener, MouseMotionListener {

    // todo: use JLayeredPane to to draw dialogs and dropdown lists
    // todo: create bold version of ms sans serif 13
    // todo: draw all overlays if any

    /*
    todo: add active overlay member that holds current active overlay

    note:
    mouse events should be passed to the upper most overlay
    the menus underneath should keep their previous state while overlay is visible

    note:
    button list always refers to the current menu's button list
    this needs to be adjusted to the current layer's button list
    we need to be able to retrieve the current layer button list
    */

    /*
    note:
    overlays should be handled inside the internal panel not in paintComponent
    ideally all elements should be handled inside the internal panel but due to limitations
    in font letter spacing, labels are currently drawn inside paintComponent using context.drawString
    */

    public final JPanel internalPanel;
    public final BufferedImage menuImage;
    public final BufferedImage tempBuffer;
    public final OriginalButton[] buttonList;
    public final double idealAspectRatio = (double) 800 / (double) 600;
    public OriginalButton pressedButton;
    public OriginalButton hoveredButton;
    public OriginalDropdownList hoveredDropdown;

    public ArrayList<JPanel> overlayList;


    public MenuBackground(
        BufferedImage menuImage,
        OriginalButton[] buttonList,
        JLabel[] labelList,
        JFormattedTextField[] inputFieldList,
        OriginalDropdownList[] dropdownList) {

        this.menuImage = menuImage;
        this.buttonList = buttonList;
        this.tempBuffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        // add internal panel
        this.internalPanel = new JPanel(null);

        this.internalPanel.setOpaque(false);
        this.internalPanel.setBounds(0, 0, 800, 600);

        if (buttonList != null) {
            for (OriginalButton button : this.buttonList) {
                this.internalPanel.add(button);
            }
        }

        if (inputFieldList != null) {
            for (JFormattedTextField input : inputFieldList) {
                internalPanel.add(input);
            }
        }

        if (labelList != null) {
            for (JLabel label : labelList) {
                internalPanel.add(label);
            }
        }

        if (dropdownList != null) {
            for (OriginalDropdownList dropdown : dropdownList) {
                internalPanel.add(dropdown);
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
     * @param graphics the <code>Graphics</code> object the provides the context to draw on.
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


    @Override
    public void mouseMoved(MouseEvent event) {

        Point cursor = this.getScaledPosition(event);
        Component component = this.internalPanel.getComponentAt(cursor);

        if (component == this.internalPanel) {

            // todo: add common interface hor hoverable and pressable elements

            // mark no button as hovered or pressed
            if (this.hoveredButton != null) {
                this.hoveredButton.hovered = false;
            }

            if (this.pressedButton != null) {
                this.pressedButton.pressed = false;
            }

            if (this.hoveredDropdown != null) {
                this.hoveredDropdown.hovered = false;
            }

            this.hoveredButton = null;
            this.pressedButton = null;

            this.hoveredDropdown = null;
        }

        else if (component instanceof JLabel) {
            // do nothing
        }

        else if (component instanceof OriginalButton) {

            /*
            note:

            normally we use dispatchEvent and let child handle the event
            that is too much overhead for this case
            */

            if (component != this.hoveredButton && this.hoveredButton != null) {
                this.hoveredButton.hovered = false;
            }

            ((OriginalButton) component).hovered = true;
            this.hoveredButton = (OriginalButton) component;
        }

        else if (component instanceof OriginalDropdownList) {

            if (component != this.hoveredDropdown && this.hoveredDropdown != null) {
                this.hoveredDropdown.hovered = false;
            }

            ((OriginalDropdownList) component).hovered = true;
            this.hoveredDropdown = (OriginalDropdownList) component;
        }

        else {
            System.out.printf("other element hovered\n");
        }

        this.repaint();
        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {

        Point cursor = this.getScaledPosition(event);
        Component component = this.internalPanel.getComponentAt(cursor);
        boolean anyButtonPressed = false;

        if (component == this.internalPanel) {
            // do nothing
        }

        else if (component instanceof JLabel) {
            // do nothing
        }

        else if (component instanceof OriginalButton) {

            if (component != this.pressedButton && this.pressedButton != null) {
                this.pressedButton.pressed = false;
            }

            ((OriginalButton) component).pressed = true;
            this.pressedButton = (OriginalButton) component;
            anyButtonPressed = true;
        }

        else {
            System.out.printf("other element pressed\n");
        }

        if (anyButtonPressed == false) {
            this.pressedButton = null;
        }

        this.repaint();
        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {

        Point cursor = this.getScaledPosition(event);
        Component component = this.internalPanel.getComponentAt(cursor);

        // no button was pressed prior to release
        if (this.pressedButton == null) {

            if (component instanceof OriginalButton) {
                ((OriginalButton) component).hovered = true;
                this.hoveredButton = (OriginalButton) component;
            }
        }

        // a button was pressed prior to release
        else {

            // same button pressed and released
            if (this.pressedButton == component) {
                this.pressedButton.hovered = true;
                this.pressedButton.doClick();
            }

            // button pressed but released somewhere else
            else {

                // mark previous button as not hovered
                this.pressedButton.hovered = false;

                // mark new button as hovered
                if (component instanceof OriginalButton) {
                    ((OriginalButton) component).hovered = true;
                    this.hoveredButton = (OriginalButton) component;
                }
            }

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