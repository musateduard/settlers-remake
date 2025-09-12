package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.io.IOException;
import java.io.File;
import java.util.List;

import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.common.images.EImageLinkType;
import jsettlers.main.swing.JSettlersFrame;


class CampaignMenuButton extends JButton {

    public final int buttonWidth = 104;
    public final int buttonHeight = 44;
    public boolean hovered = false;
    public boolean pressed = false;
    public int offsetX;
    public int offsetY;

    public final Font textFont;
    public final BufferedImage buttonImage;
    public final BufferedImage buttonImageHovered;
    public final BufferedImage buttonImagePressed;


    public CampaignMenuButton(MenuButtonProperties properties, String buttonText, int offsetX, int offsetY) {

        this.textFont = properties.buttonFont;
        this.buttonImage = properties.buttonImage;
        this.buttonImageHovered = properties.buttonImageHovered;
        this.buttonImagePressed = properties.buttonImagePressed;

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.setText(buttonText);
        this.setBounds(this.offsetX, this.offsetY, this.buttonWidth, this.buttonHeight);
        this.setBorderPainted(false);
        this.setContentAreaFilled(false);
        this.setOpaque(false);
        this.setForeground(new Color(248, 220, 0));
        this.setFont(this.textFont.deriveFont(Font.PLAIN, 11f));

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        if (this.pressed) {
            graphics.drawImage(this.buttonImagePressed, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        else if (this.hovered) {
            graphics.drawImage(this.buttonImageHovered, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        else {
            graphics.drawImage(this.buttonImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        super.paintComponent(graphics);

        return;
    }
}


class CampaignBackground extends JPanel {

    public final BufferedImage menuImage;
    public final JSettlersFrame mainFrame;
    public final double idealAspectRatio = (double) 800 / (double) 600;

    public final JPanel buttonsPanel;
    public final CampaignMenuButton egyptians;
    public final CampaignMenuButton romans;
    public final CampaignMenuButton asians;

    public Font menuFont;
    public Font menuFontBold;
    public final BufferedImage buttonImage;
    public final BufferedImage buttonImagePressed;
    public final BufferedImage buttonImageHovered;


    public CampaignBackground(JSettlersFrame mainFrame) {

        this.mainFrame = mainFrame;

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

        // set hover effects
        RescaleOp brightness = new RescaleOp(0.95f, 0, null);
        brightness.filter(this.buttonImage, this.buttonImageHovered);

        // load font
        File fontPath = new File("D:\\ms-sans-serif-1.ttf");
        File fontPathBold = new File("D:\\ms-sans-serif-bold.ttf");

        try {
            this.menuFont = Font.createFont(Font.TRUETYPE_FONT, fontPath);
            this.menuFontBold = Font.createFont(Font.TRUETYPE_FONT, fontPathBold);
        }

        catch (IOException | FontFormatException exception) {

            this.menuFont = new Font("Arial", Font.PLAIN, 11);
            this.menuFontBold = new Font("Arial", Font.BOLD, 11);

            System.out.printf("failed to open campaign menu font\n");
            exception.printStackTrace();
        }

        // add buttons
        MenuButtonProperties buttonProperties = new MenuButtonProperties(this.buttonImage, this.buttonImageHovered, this.buttonImagePressed, this.menuFont);

        this.egyptians = new CampaignMenuButton(buttonProperties, "Egyptians", 56, 164);
        this.romans = new CampaignMenuButton(buttonProperties, "Romans", 240, 440);
        this.asians = new CampaignMenuButton(buttonProperties, "Asians", 664, 200);

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

        Graphics2D menuGraphics = this.menuImage.createGraphics();

        // paint buttons
        this.buttonsPanel.printAll(menuGraphics);

        // paint text
        menuGraphics.setColor(new Color(248, 220, 0));
        menuGraphics.setFont(this.menuFont.deriveFont(11f));

        menuGraphics.drawString("Ramadamses (hard)", 125, 367);
        menuGraphics.drawString("Septimus Marius (easy)", 342, 515);
        menuGraphics.drawString("Tsu Tang (medium)", 602, 367);

        try {
            this.menuFontBold = Font.createFont(Font.TRUETYPE_FONT, new File("D:\\ms-sans-serif-bold.ttf"));
        }

        catch (Exception exception) {
            exception.printStackTrace();
        }

        menuGraphics.setColor(new Color(248, 92, 24));

        // test font sizes
        /**
        for (int item = 0; item < 6; item += 1) {
            menuGraphics.setFont(this.menuFontBold.deriveFont(Font.BOLD, (float) (11 + item)));
            // menuGraphics.drawString("Choose a Race", 352, 80 + item * 20);
            menuGraphics.drawString("Game Setup", 352, 80 + item * 20);

            menuGraphics.setFont(this.menuFontBold.deriveFont((float) (11 + item)));
            // menuGraphics.drawString("Choose a Race", 500, 80 + item * 20);
            menuGraphics.drawString("Game Setup", 500, 80 + item * 20);
        }
        */

        String title = "Choose a Race";
        int[] letterXOffsets = {0, 10, 18, 27, 36, 44, 53, 57, 66, 70, 81, 90, 98};

        for (int index = 0; index < title.length(); index += 1) {
            menuGraphics.setFont(this.menuFontBold.deriveFont(14.00f));
            menuGraphics.drawString(String.format("%c", title.charAt(index)), 350 + letterXOffsets[index], 53);  // note: bold is not working
        }

        menuGraphics.dispose();

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