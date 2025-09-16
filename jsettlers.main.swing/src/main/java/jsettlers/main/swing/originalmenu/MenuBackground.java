package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.ArrayList;


/**
 * this class is a canvas on which all menu elements get painted. it holds an internal {@link BufferedImage}
 * of fixed size of 800 x 600 and all images and text get painted on this buffer. the buffer
 * then gets painted on the {@link JPanel} background and the panel is added to the actual menu component.
 * this panel is set to scale at a fixed aspect ratio of 4:3.
 */
public class MenuBackground extends JPanel {

    // todo: use JLayeredPane to to draw dialogs and dropdown lists
    // todo: create bold version of ms sans serif 13
    // todo: draw all overlays if any

    /*
    note:
    overlays should be handled inside the internal panel not in paintComponent
    ideally all elements should be handled inside the internal panel but due to limitations
    in font letter spacing, labels are currently drawn inside paintComponent using context.drawString
    */

    public final BufferedImage menuImage;
    public final BufferedImage tempBuffer;
    public final JPanel internalPanel;
    public final OriginalMenuButton[] buttonList;
    public final OriginalMenuEventListener eventListener;
    public final double idealAspectRatio = (double) 800 / (double) 600;

    public ArrayList<JPanel> overlayList;


    public MenuBackground(
        BufferedImage menuImage,
        OriginalMenuButton[] buttonList,
        JLabel[] labelList,
        JFormattedTextField[] inputFieldList) {

        this.menuImage = menuImage;
        this.buttonList = buttonList;
        this.tempBuffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        // add internal panel
        this.internalPanel = new JPanel(null);

        this.internalPanel.setOpaque(false);
        this.internalPanel.setBounds(0, 0, 800, 600);

        if (buttonList != null) {
            for (OriginalMenuButton button : this.buttonList) {
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

        // add event listener
        this.eventListener = new OriginalMenuEventListener(this, this.buttonList, this.internalPanel);

        this.addMouseListener(this.eventListener);
        this.addMouseMotionListener(this.eventListener);

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
}