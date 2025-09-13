package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import javax.swing.JPanel;
import java.util.List;

import jsettlers.common.CommitInfo;
import jsettlers.common.images.EImageLinkType;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.image.NullImage;
import jsettlers.main.swing.JSettlersFrame;


class MainBackground extends OriginalBackgroundBase {

    public final BufferedImage menuImage;
    public final BufferedImage buttonImage;
    public final BufferedImage buttonImageHovered;
    public final BufferedImage buttonImageClicked;

    public final JPanel buttonsPanel;
    public final OriginalMenuButton tutorialButton;
    public final OriginalMenuButton campaignButton;
    public final OriginalMenuButton missionCDCampaignButton;
    public final OriginalMenuButton amazonCampaignButton;
    public final OriginalMenuButton campaignDifficultyButton;
    public final OriginalMenuButton singlePlayerScenarioButton;
    public final OriginalMenuButton multiplayerGameLanButton;
    public final OriginalMenuButton multiplayerGameInternetButton;
    public final OriginalMenuButton loadGameButton;
    public final OriginalMenuButton onlineHelpButton;
    public final OriginalMenuButton tipsTricksButton;
    public final OriginalMenuButton creditsButton;
    public final OriginalMenuButton exitGameButton;
    public final OriginalMenuEventListener eventListener;


    public MainBackground(JSettlersFrame mainFrame) {

        super(mainFrame);

        // note: EImageLinkType.SETTLER is also used for menu buttons not just settlers sprites
        // note: OriginalImageLink doesn't throw exception if index is out of bounds
        // note: you need to check if getImage() returned instanceof NullImage

        ImageProvider imageProvider = ImageProvider.getInstance();

        SingleImage backgroundImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.GUI, 61, 2));
        SingleImage buttonImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 61, 0, 0));
        SingleImage buttonImageClicked = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 61, 0, 1));

        for (SingleImage item : List.of(backgroundImage, buttonImage, buttonImageClicked)) {

            if (item instanceof NullImage) {
                System.out.printf("image not found %s\n", item);
                new Exception().printStackTrace();
            }
        }

        // load all images
        this.menuImage = backgroundImage.convertToBufferedImage();
        this.buttonImage = buttonImage.convertToBufferedImage();
        this.buttonImageClicked = buttonImageClicked.convertToBufferedImage();
        this.buttonImageHovered = new BufferedImage(172, 32, BufferedImage.TYPE_INT_ARGB);

        // create hovered version of menu button
        RescaleOp brightness = new RescaleOp(0.95f, 0, null);
        brightness.filter(this.buttonImage, this.buttonImageHovered);

        // set buttons
        ButtonProps buttonProps = new ButtonProps(
            this.buttonImage,
            this.buttonImageHovered,
            this.buttonImageClicked,
            this.menuFont, 11, false,
            new Color(0, 12, 64)
        );

        this.tutorialButton = new OriginalMenuButton(buttonProps, "Tutorial", 80, 20);
        this.campaignButton = new OriginalMenuButton(buttonProps, "Campaign", 80, 60);
        this.missionCDCampaignButton = new OriginalMenuButton(buttonProps, "Mission CD Campaign", 80, 100);
        this.amazonCampaignButton = new OriginalMenuButton(buttonProps, "Amazon Campaign", 80, 140);
        this.campaignDifficultyButton = new OriginalMenuButton(buttonProps, "Campaign: Normal", 80, 180);
        this.singlePlayerScenarioButton = new OriginalMenuButton(buttonProps, "Single Player: Scenario", 80, 220);
        this.multiplayerGameLanButton = new OriginalMenuButton(buttonProps, "Multi-player Game: LAN", 80, 260);
        this.multiplayerGameInternetButton = new OriginalMenuButton(buttonProps, "Multi-player Game: Internet", 80, 300);
        this.loadGameButton = new OriginalMenuButton(buttonProps, "Load Game", 80, 340);
        this.onlineHelpButton = new OriginalMenuButton(buttonProps, "Online Help", 80, 400);
        this.tipsTricksButton = new OriginalMenuButton(buttonProps, "Tips & Tricks", 80, 440);
        this.creditsButton = new OriginalMenuButton(buttonProps, "Credits", 80, 480);
        this.exitGameButton = new OriginalMenuButton(buttonProps, "Exit Game", 80, 540);

        // add button event listeners
        this.tutorialButton.addActionListener(
            (event) -> {
                System.out.printf("tutorials button pressed\n");
                return;
            }
        );

        this.campaignButton.addActionListener(
            (event) -> {
                this.mainFrame.showOriginalCampaignMenu();
                return;
            }
        );

        this.exitGameButton.addActionListener(
            (event) -> {
                System.exit(0);
                return;
            }
        );

        // add buttons to panel
        this.buttonsPanel = new JPanel(null);

        this.buttonsPanel.setOpaque(false);
        this.buttonsPanel.setBounds(0, 0, 800, 600);

        this.buttonsPanel.add(this.tutorialButton);
        this.buttonsPanel.add(this.campaignButton);
        this.buttonsPanel.add(this.missionCDCampaignButton);
        this.buttonsPanel.add(this.amazonCampaignButton);
        this.buttonsPanel.add(this.campaignDifficultyButton);
        this.buttonsPanel.add(this.singlePlayerScenarioButton);
        this.buttonsPanel.add(this.multiplayerGameLanButton);
        this.buttonsPanel.add(this.multiplayerGameInternetButton);
        this.buttonsPanel.add(this.loadGameButton);
        this.buttonsPanel.add(this.onlineHelpButton);
        this.buttonsPanel.add(this.tipsTricksButton);
        this.buttonsPanel.add(this.creditsButton);
        this.buttonsPanel.add(this.exitGameButton);

        // add menu event listener
        OriginalMenuButton[] buttonList =  {
            this.tutorialButton,
            this.campaignButton,
            this.missionCDCampaignButton,
            this.amazonCampaignButton,
            this.campaignDifficultyButton,
            this.singlePlayerScenarioButton,
            this.multiplayerGameLanButton,
            this.multiplayerGameInternetButton,
            this.loadGameButton,
            this.onlineHelpButton,
            this.tipsTricksButton,
            this.creditsButton,
            this.exitGameButton
        };

        this.eventListener = new OriginalMenuEventListener(this, buttonList);

        this.addMouseListener(this.eventListener);
        this.addMouseMotionListener(this.eventListener);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, this.getWidth(), this.getHeight());

        Graphics2D tempContext = this.tempBuffer.createGraphics();

        tempContext.drawImage(this.menuImage, 0, 0, this.tempBuffer.getWidth(), this.tempBuffer.getHeight(), this);

        this.buttonsPanel.printAll(tempContext);

        tempContext.setColor(new Color(255, 223, 0));
        tempContext.setFont(this.menuFont.deriveFont(11f));
        tempContext.drawString(String.format("Version %s", CommitInfo.COMMIT_HASH_SHORT), 34, 588);
        tempContext.dispose();

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        graphics.drawImage(this.tempBuffer, 0, 0, this.getWidth(), this.getHeight(), this);

        return;
    }
}


/**
 * this panel is used as a container for the actual main menu background.
 * it is set to a black background and covers the entire frame regardless of size and aspect ratio.
 * it then contains an additional background panel set to a fixed aspect ratio of 4:3.<br>
 * the background panel then contains in image buffered that is set to a fixed resolution of 800 x 600.
 * this buffer is set to fill the entire background panel and contains the actual image of the main menu
 * as well as the buttons and any additional text painted onto the menu.
 */
public class OriginalMainMenu extends OriginalMenuBase {

    // todo: make background class generic for all menus
    // note: menu class should be declarative with all necessary components for rendering respective menu

    public OriginalMainMenu(JSettlersFrame mainFrame) {

        super(mainFrame);

        MainBackground background = new MainBackground(this.mainFrame);
        this.add(background);

        return;
    }
}