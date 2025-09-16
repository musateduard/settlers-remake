package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JFormattedTextField;
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

    public final BufferedImage menuImage;
    public final BufferedImage tempBuffer;
    public final JPanel buttonsPanel;
    public final OriginalMenuButton[] buttonList;
    public final OriginalMenuEventListener eventListener;
    public final OriginalMenuText[] textList;
    public final double idealAspectRatio = (double) 800 / (double) 600;

    public ArrayList<JPanel> overlayList;


    public MenuBackground(
        BufferedImage menuImage,
        OriginalMenuButton[] buttonList,
        OriginalMenuText[] textList,
        JFormattedTextField[] inputFieldList) {

        this.menuImage = menuImage;
        this.buttonList = buttonList;
        this.textList = textList;
        this.tempBuffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        // add buttons panel
        this.buttonsPanel = new JPanel(null);

        this.buttonsPanel.setOpaque(false);
        this.buttonsPanel.setBounds(0, 0, 800, 600);

        if (buttonList != null) {
            for (OriginalMenuButton button : this.buttonList) {
                this.buttonsPanel.add(button);
            }
        }

        if (inputFieldList != null) {
            for (JFormattedTextField input : inputFieldList) {
                buttonsPanel.add(input);
            }
        }

        // add event listener
        this.eventListener = new OriginalMenuEventListener(this, this.buttonList, this.buttonsPanel);

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
        artifacts make text look thicker on each resize
        */

        Graphics2D tempContext = this.tempBuffer.createGraphics();

        tempContext.drawImage(this.menuImage, 0, 0, this.tempBuffer.getWidth(), this.tempBuffer.getHeight(), this);

        // paint buttons
        this.buttonsPanel.printAll(tempContext);

        // draw text based on font size and shadow
        for (OriginalMenuText item : this.textList) {

            tempContext.setFont(item.textFont);
            FontMetrics metrics = tempContext.getFontMetrics();

            // draw shadow first
            if (item.shadow == true) {

                tempContext.setColor(Color.BLACK);

                // draw text with proper letter spacing
                if (item.textFont.isBold()) {

                    int letterOffset = 0;
                    for (int index = 0; index < item.textString.length(); index += 1) {

                        char letter = item.textString.charAt(index);
                        int letterX = item.offsetX + 1 + letterOffset;
                        int letterY = item.offsetY + 1;

                        tempContext.drawString(String.valueOf(letter), letterX, letterY);

                        letterOffset += metrics.charWidth(letter) - (letter == ' ' ? 0 : 1);
                    }
                }

                // draw text normally
                else {
                    tempContext.drawString(item.textString, item.offsetX + 1, item.offsetY + 1);
                }
            }

            // draw text foreground
            tempContext.setColor(item.textColor);

            // draw text with letter spacing
            if (item.textFont.isBold()) {

                int letterOffset = 0;
                for (int index = 0; index < item.textString.length(); index += 1) {

                    char letter = item.textString.charAt(index);
                    int letterX = item.offsetX + letterOffset;
                    int letterY = item.offsetY;

                    tempContext.drawString(String.valueOf(letter), letterX, letterY);

                    letterOffset += metrics.charWidth(letter) - (letter == ' ' ? 0 : 1);
                }
            }

            // draw text normally
            else {
                tempContext.drawString(item.textString, item.offsetX, item.offsetY);
            }
        }

        // todo: draw all overlays if any

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