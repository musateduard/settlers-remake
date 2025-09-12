package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JPanel;
import jsettlers.main.swing.JSettlersFrame;


public abstract class OriginalBackgroundBase extends JPanel {

    public Font menuFont;
    public final JSettlersFrame mainFrame;
    public final BufferedImage tempBuffer;
    public final double idealAspectRatio = (double) 800 / (double) 600;


    public OriginalBackgroundBase(JSettlersFrame mainFrame) {

        this.mainFrame = mainFrame;
        this.tempBuffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        // load font
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream fontStream = loader.getResourceAsStream("micross.ttf");

        assert fontStream != null;

        try {
            this.menuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        }

        catch (IOException | FontFormatException exception) {

            this.menuFont = new Font("Arial", Font.PLAIN, 11);

            System.out.printf("failed to open menu font: %s\n", loader.getName());
            exception.printStackTrace();
        }

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
