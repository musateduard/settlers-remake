package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.RescaleOp;
import java.awt.image.BufferedImage;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import java.util.List;
import javax.swing.JPanel;
import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.common.images.EImageLinkType;
import jsettlers.main.swing.JSettlersFrame;


class CampaignBackground extends OriginalBackgroundBase {

    public final JPanel buttonsPanel;
    public final OriginalMenuButton egyptians;
    public final OriginalMenuButton romans;
    public final OriginalMenuButton asians;

    public final BufferedImage menuImage;
    public final BufferedImage buttonImage;
    public final BufferedImage buttonImagePressed;
    public final BufferedImage buttonImageHovered;


    public CampaignBackground(JSettlersFrame mainFrame) {

        super(mainFrame);

        // load images
        ImageProvider imageProvider = ImageProvider.getInstance();

        SingleImage campaignImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.GUI, 2, 7));
        SingleImage buttonImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 4, 0));
        SingleImage buttonImagePressed = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 4, 0));

        for (SingleImage item : List.of(campaignImage, buttonImage, buttonImagePressed)) {

            if (item instanceof NullImage) {
                System.out.printf("image not found %s\n", item);
                new Exception().printStackTrace();
            }
        }

        this.menuImage = campaignImage.convertToBufferedImage();
        this.buttonImage = buttonImage.convertToBufferedImage();
        this.buttonImagePressed = buttonImagePressed.convertToBufferedImage();
        this.buttonImageHovered = new BufferedImage(104, 44, BufferedImage.TYPE_INT_ARGB);

        // create hovered version of menu button
        RescaleOp brightness = new RescaleOp(0.95f, 0, null);
        brightness.filter(this.buttonImage, this.buttonImageHovered);

        // add buttons
        ButtonProps buttonProps = new ButtonProps(
            this.buttonImage,
            this.buttonImageHovered,
            this.buttonImagePressed,
            this.menuFont, 11, true,
            new Color(248, 220, 0)
        );

        this.egyptians = new OriginalMenuButton(buttonProps, "Egyptians", 56, 164);
        this.romans = new OriginalMenuButton(buttonProps, "Romans", 240, 440);
        this.asians = new OriginalMenuButton(buttonProps, "Asians", 664, 200);

        // add button event listeners

        // add buttons panel
        this.buttonsPanel = new JPanel(null);

        this.buttonsPanel.setOpaque(false);
        this.buttonsPanel.setBounds(0, 0, 800, 600);

        this.buttonsPanel.add(this.egyptians);
        this.buttonsPanel.add(this.romans);
        this.buttonsPanel.add(this.asians);

        // add menu event listener

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        // todo: add hover effects

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
        tempContext.setColor(new Color(248, 220, 0));
        tempContext.setFont(this.menuFont.deriveFont(11f));

        tempContext.drawString("Ramadamses (hard)", 125, 367);
        tempContext.drawString("Septimus Marius (easy)", 342, 515);
        tempContext.drawString("Tsu Tang (medium)", 602, 367);

        // draw title
        // note: only use antialiasing for larger text
        // note: antialiasing on smaller fonts make them unreadable but is necessary for larger fonts
        tempContext.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);  // HRGB looks good

        String title = "Choose a Race";
        int[] letterXOffsets = {0, 10, 18, 27, 36, 44, 53, 57, 66, 70, 81, 90, 98};

        // note: this loop is necessary for proper letter spacing

        // draw title shadow
        tempContext.setColor(Color.BLACK);

        for (int index = 0; index < title.length(); index += 1) {
            tempContext.setFont(this.menuFont.deriveFont(Font.BOLD, 14.00f));
            tempContext.drawString(String.format("%c", title.charAt(index)), 351 + letterXOffsets[index], 54);
        }

        // draw title text
        tempContext.setColor(new Color(248, 92, 24));

        for (int index = 0; index < title.length(); index += 1) {
            tempContext.setFont(this.menuFont.deriveFont(Font.BOLD, 14.00f));
            tempContext.drawString(String.format("%c", title.charAt(index)), 350 + letterXOffsets[index], 53);
        }

        tempContext.dispose();

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        graphics.drawImage(this.tempBuffer, 0, 0, this.getWidth(), this.getHeight(), this);

        return;
    }
}


public class OriginalCampaignMenu extends OriginalMenuBase {

    public final KeyEventDispatcher campaignMenuKeyListener;


    public OriginalCampaignMenu(JSettlersFrame mainFrame) {

        super(mainFrame);

        this.campaignMenuKeyListener = (event) -> {

            if (event.getID() == KeyEvent.KEY_PRESSED && event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                this.returnToMainMenu();
            }

            return true;
        };

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
}