package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


/**
 * this class is a canvas on which all menu elements get painted on. it holds an internal buffer
 * of fixed size of 800 x 600 and all images and text get painted on this buffer. the buffer
 * then gets painted on the {@link JPanel} background and the panel is added to the actual menu component.
 * this panel is set to scale at a fixed aspect ratio of 4:3.
 */
public class MenuBackground extends JPanel {

    public final BufferedImage menuImage;
    public final BufferedImage tempBuffer;
    public final JPanel buttonsPanel;
    public final OriginalMenuButton[] buttonList;
    public final OriginalMenuEventListener eventListener;
    public final MenuText[] textList;
    public final double idealAspectRatio = (double) 800 / (double) 600;


    public MenuBackground(BufferedImage menuImage, OriginalMenuButton[] buttonList, MenuText[] textList) {

        this.menuImage = menuImage;
        this.buttonList = buttonList;
        this.textList = textList;
        this.tempBuffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        // add buttons panel
        this.buttonsPanel = new JPanel(null);

        this.buttonsPanel.setOpaque(false);
        this.buttonsPanel.setBounds(0, 0, 800, 600);

        for (OriginalMenuButton item : this.buttonList) {
            this.buttonsPanel.add(item);
        }

        // add event listener
        this.eventListener = new OriginalMenuEventListener(this, this.buttonList);

        this.addMouseListener(this.eventListener);
        this.addMouseMotionListener(this.eventListener);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, this.getWidth(), this.getHeight());

        // note: tempBuffer has a fixed size of 800 x 600
        // note: all images are painted on temp buffer which is then painted onto the main frame
        // note: don't paint onto the menuImage directly; it will cause artifacts from text antialiasing
        // note: text antialiasing artifacts make text look thicker on each resize

        Graphics2D tempContext = this.tempBuffer.createGraphics();

        tempContext.drawImage(this.menuImage, 0, 0, this.tempBuffer.getWidth(), this.tempBuffer.getHeight(), this);

        // paint buttons
        this.buttonsPanel.printAll(tempContext);

        // paint text
        for (MenuText item : this.textList) {

            tempContext.setFont(item.textFont);

            // draw first if text has shadow
            if (item.shadowX != 0 || item.shadowY != 0) {

                tempContext.setColor(Color.BLACK);

                // text has letter spacing
                if (item.letterSpacing != null) {

                    for (int index = 0; index < item.textString.length(); index += 1) {

                        String letter = String.format("%c", item.textString.charAt(index));
                        int letterX = item.offsetX + item.shadowX + item.letterSpacing[index];
                        int letterY = item.offsetY + item.shadowY;

                        tempContext.drawString(letter, letterX, letterY);
                    }
                }

                else {
                    tempContext.drawString(item.textString, item.offsetX + item.shadowX, item.offsetY + item.shadowY);
                }
            }

            // draw text
            tempContext.setColor(item.textColor);

            if (item.letterSpacing != null) {

                for (int index = 0; index < item.textString.length(); index += 1) {

                    String letter = String.format("%c", item.textString.charAt(index));
                    int letterX = item.offsetX + item.letterSpacing[index];
                    int letterY = item.offsetY;

                    tempContext.drawString(letter, letterX, letterY);
                }
            }

            else {
                tempContext.drawString(item.textString, item.offsetX, item.offsetY);
            }
        }

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