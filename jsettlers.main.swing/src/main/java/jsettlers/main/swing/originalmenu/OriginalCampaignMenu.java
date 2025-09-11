package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

import jsettlers.common.images.EImageLinkType;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.image.SingleImage;
import jsettlers.main.swing.JSettlersFrame;


class CampaignBackground extends JPanel {

    public final BufferedImage menuImage;
    public final JSettlersFrame mainFrame;
    public final double idealAspectRatio = (double) 800 / (double) 600;


    public CampaignBackground(JSettlersFrame mainFrame) {

        ImageProvider imageProvider = ImageProvider.getInstance();
        OriginalImageLink imageLink = new OriginalImageLink(EImageLinkType.GUI, 2, 7);
        BufferedImage campaignImage = ((SingleImage) imageProvider.getImage(imageLink)).convertToBufferedImage();

        this.mainFrame = mainFrame;
        this.menuImage = campaignImage;

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
    public void paintComponent(Graphics graphics) {

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        super.paintComponent(graphics);

        // Graphics2D menuGraphics = this.menuImage.createGraphics();
        //
        // menuGraphics.translate(80, 20);
        // this.buttonsPanel.printAll(menuGraphics);
        //
        // menuGraphics.translate(-80, -20);
        // menuGraphics.setColor(new Color(255, 223, 0));
        // menuGraphics.setFont(this.menuFont.deriveFont(11f));
        // menuGraphics.drawString(String.format("Version %s", CommitInfo.COMMIT_HASH_SHORT), 34, 588);
        // menuGraphics.dispose();

        graphics.drawImage(this.menuImage, 0, 0, this.getWidth(), this.getHeight(), this);

        return;
    }
}


public class OriginalCampaignMenu extends JPanel {

    public final JSettlersFrame mainFrame;
    public final KeyEventDispatcher campaignMenuKeyListener;


    public OriginalCampaignMenu(JSettlersFrame mainFrame) {

        this.mainFrame = mainFrame;
        this.campaignMenuKeyListener = (event) -> {

            if (event.getID() == KeyEvent.KEY_PRESSED && event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.out.printf("escape key pressed\n");
                this.returnToMainMenu();
            }

            return true;
        };

        this.setOpaque(true);
        this.setBackground(Color.BLACK);
        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 600));

        CampaignBackground background = new CampaignBackground(this.mainFrame);

        this.add(background);

        return;
    }


    public void returnToMainMenu() {

        KeyboardFocusManager keyManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        keyManager.removeKeyEventDispatcher(this.campaignMenuKeyListener);
        this.mainFrame.showOriginalMainMenu();

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}