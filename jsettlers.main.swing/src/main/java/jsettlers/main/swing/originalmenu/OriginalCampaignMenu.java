package jsettlers.main.swing.originalmenu;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.image.RescaleOp;
import java.awt.image.BufferedImage;
import java.awt.FontFormatException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.io.IOException;
import java.io.InputStream;
import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.common.images.EImageLinkType;
import jsettlers.main.swing.JSettlersFrame;


public class OriginalCampaignMenu extends JPanel {

    public final JSettlersFrame mainFrame;
    public final KeyEventDispatcher campaignMenuKeyListener;


    public OriginalCampaignMenu(JSettlersFrame mainFrame) {

        this.mainFrame = mainFrame;

        this.setOpaque(true);
        this.setBackground(Color.BLACK);
        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 600));

        // declare key listener
        this.campaignMenuKeyListener = (event) -> {

            if (event.getID() == KeyEvent.KEY_PRESSED && event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                this.returnToMainMenu();
            }

            return true;
        };

        // load all images
        ImageProvider imageProvider = ImageProvider.getInstance();

        SingleImage backgroundImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.GUI, 2, 7));
        SingleImage buttonImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 4, 0));
        SingleImage buttonImagePressed = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 4, 1));
        BufferedImage buttonImageHovered = new BufferedImage(buttonImagePressed.getWidth(), buttonImagePressed.getHeight(), BufferedImage.TYPE_INT_ARGB);

        assert backgroundImage instanceof NullImage == false;
        assert buttonImage instanceof NullImage == false;
        assert buttonImagePressed instanceof NullImage == false;

        RescaleOp brightness = new RescaleOp(1.10f, 0, null);
        brightness.filter(buttonImage.convertToBufferedImage(), buttonImageHovered);

        // load all fonts
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream fontStream = loader.getResourceAsStream("ms-sans-serif-1.ttf");
        InputStream fontBoldStream = loader.getResourceAsStream("ms-sans-serif-bold.ttf");

        assert fontStream != null;
        assert fontBoldStream != null;

        Font menuFont;
        Font menuFontBold;

        try {
            menuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            menuFontBold = Font.createFont(Font.TRUETYPE_FONT, fontBoldStream);
        }

        catch (IOException | FontFormatException exception) {

            menuFont = new Font("Arial", Font.PLAIN, 11);
            menuFontBold = new Font("Arial", Font.BOLD, 14);

            System.out.printf("failed to open menu font: %s\n", loader.getName());
            exception.printStackTrace();
        }

        // declare all buttons and event listeners
        ButtonProps buttonProps = new ButtonProps(
            buttonImage.convertToBufferedImage(),
            buttonImageHovered,
            buttonImagePressed.convertToBufferedImage(),
            menuFont.deriveFont(Font.PLAIN, 11.00f), new Color(248, 220, 0), true
        );

        OriginalMenuButton[] buttonList = {
            new OriginalMenuButton("Egyptians", 56, 164, buttonProps),
            new OriginalMenuButton("Romans", 240, 440, buttonProps),
            new OriginalMenuButton("Asians", 664, 200, buttonProps)
        };

        // todo: add button event listeners

        // declare all labels
        TitleTextOrange menuTitle = new TitleTextOrange("Choose a Race", 350, 42);
        LabelTextYellow descriptionEgyptians = new LabelTextYellow("Ramadamses (hard)", 125, 357);
        LabelTextYellow descriptionRomans = new LabelTextYellow("Septimus Marius (easy)", 342, 505);
        LabelTextYellow descriptionAsians = new LabelTextYellow("Tsu Tang (medium)", 602, 357);

        JLabel[] labelList = {
            menuTitle,
            descriptionEgyptians,
            descriptionRomans,
            descriptionAsians
        };

        MenuBackground background = new MenuBackground(backgroundImage.convertToBufferedImage(), buttonList, labelList, null);

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