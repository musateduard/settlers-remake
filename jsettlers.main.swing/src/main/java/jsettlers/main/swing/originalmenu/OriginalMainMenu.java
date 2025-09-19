package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.image.RescaleOp;
import java.awt.image.BufferedImage;
import java.awt.FontFormatException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JPanel;

import jsettlers.common.CommitInfo;
import jsettlers.common.images.EImageLinkType;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.image.NullImage;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.originalmenu.components.ButtonProps;
import jsettlers.main.swing.originalmenu.components.LabelTextYellow;
import jsettlers.main.swing.originalmenu.components.OriginalButton;


/**
 * this panel is used as a container for the actual main menu background.
 * it is set to a black background and covers the entire frame regardless of size and aspect ratio.
 * it then contains an additional {@link MenuCanvas} background panel set to a fixed aspect ratio of 4:3.<br>
 * the background panel then contains in image buffer that is set to a fixed resolution of 800 x 600.
 * this buffer is set to fill the entire background panel and contains the actual image of the main menu
 * as well as the buttons and any additional text painted onto the menu.
 */
public class OriginalMainMenu extends JPanel {

    public final JSettlersFrame mainFrame;


    public OriginalMainMenu(JSettlersFrame mainFrame) {

        this.mainFrame = mainFrame;

        this.setOpaque(true);
        this.setBackground(Color.BLACK);
        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 600));

        // load all images
        // note: EImageLinkType.SETTLER is also used for menu buttons not just settlers sprites
        // note: OriginalImageLink doesn't throw exception if index is out of bounds
        // note: you need to check if getImage() returned instanceof NullImage

        ImageProvider imageProvider = ImageProvider.getInstance();

        SingleImage menuImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.GUI, 61, 2));
        SingleImage buttonImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 61, 0, 0));
        SingleImage buttonImagePressed = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 61, 0, 1));
        BufferedImage buttonImageHovered = new BufferedImage(buttonImagePressed.getWidth(), buttonImagePressed.getHeight(), BufferedImage.TYPE_INT_ARGB);

        assert menuImage instanceof NullImage == false;
        assert buttonImage instanceof NullImage == false;
        assert buttonImagePressed instanceof NullImage == false;

        RescaleOp brightness = new RescaleOp(0.95f, 0, null);
        brightness.filter(buttonImage.convertToBufferedImage(), buttonImageHovered);

        // load all fonts
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream fontStream = loader.getResourceAsStream("ms-sans-serif-1.ttf");

        assert fontStream != null;

        Font menuFont;

        try {
            menuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        }

        catch (IOException | FontFormatException exception) {

            menuFont = new Font("Arial", Font.PLAIN, 11);

            System.out.printf("failed to open menu font: %s\n", loader.getName());
            exception.printStackTrace();
        }

        // declare all buttons and event listeners
        ButtonProps buttonProps = new ButtonProps(
            buttonImage.convertToBufferedImage(),
            buttonImageHovered,
            buttonImagePressed.convertToBufferedImage(),
            menuFont.deriveFont(Font.PLAIN, 11.00f), new Color(0, 12, 64), false
        );

        OriginalButton tutorialButton = new OriginalButton("Tutorial", 80, 20, buttonProps);
        OriginalButton campaignButton = new OriginalButton("Campaign", 80, 60, buttonProps);
        OriginalButton missionCDCampaignButton = new OriginalButton("Mission CD Campaign", 80, 100, buttonProps);
        OriginalButton amazonCampaignButton = new OriginalButton("Amazon Campaign", 80, 140, buttonProps);
        OriginalButton campaignDifficultyButton = new OriginalButton("Campaign: Normal", 80, 180, buttonProps);
        OriginalButton singlePlayerScenarioButton = new OriginalButton("Single Player: Scenario", 80, 220, buttonProps);
        OriginalButton multiplayerGameLanButton = new OriginalButton("Multi-player Game: LAN", 80, 260, buttonProps);
        OriginalButton multiplayerGameInternetButton = new OriginalButton("Multi-player Game: Internet", 80, 300, buttonProps);
        OriginalButton loadGameButton = new OriginalButton("Load Game", 80, 340, buttonProps);
        OriginalButton onlineHelpButton = new OriginalButton("Online Help", 80, 400, buttonProps);
        OriginalButton tipsTricksButton = new OriginalButton("Tips & Tricks", 80, 440, buttonProps);
        OriginalButton creditsButton = new OriginalButton("Credits", 80, 480, buttonProps);
        OriginalButton exitGameButton = new OriginalButton("Exit Game", 80, 540, buttonProps);

        // todo: finish all event listeners

        ActionListener menuListener = (event) -> {

            if (event.getSource() == tutorialButton) {
                System.out.printf("tutorials button pressed\n");
            }

            else if (event.getSource() == campaignButton) {
                this.mainFrame.showOriginalCampaignMenu();
            }

            else if (event.getSource() == missionCDCampaignButton) {
                System.out.printf("mission cd campaign pressed\n");
            }

            else if (event.getSource() == amazonCampaignButton) {
                System.out.printf("amazon campaign pressed\n");
            }

            else if (event.getSource() == campaignDifficultyButton) {
                System.out.printf("campaign difficulty toggled\n");
            }

            else if (event.getSource() == singlePlayerScenarioButton) {
                this.mainFrame.showOriginalScenarioMenu();
            }

            else if (event.getSource() == multiplayerGameLanButton) {
                System.out.printf("multiplayer lan pressed\n");
            }

            else if (event.getSource() == multiplayerGameInternetButton) {
                System.out.printf("multiplayer online pressed\n");
            }

            else if (event.getSource() == loadGameButton) {
                System.out.printf("load game pressed\n");
            }

            else if (event.getSource() == onlineHelpButton) {
                System.out.printf("online help pressed\n");
            }

            else if (event.getSource() == tipsTricksButton) {
                System.out.printf("tips and tricks pressed\n");
            }

            else if (event.getSource() == creditsButton) {
                System.out.printf("credits pressed\n");
            }

            else if (event.getSource() == exitGameButton) {
                System.exit(0);
            }

            else {
                System.out.printf("button not recognized\n");
            }

            return;
        };

        tutorialButton.addActionListener(menuListener);
        campaignButton.addActionListener(menuListener);
        missionCDCampaignButton.addActionListener(menuListener);
        amazonCampaignButton.addActionListener(menuListener);
        campaignDifficultyButton.addActionListener(menuListener);
        singlePlayerScenarioButton.addActionListener(menuListener);
        multiplayerGameLanButton.addActionListener(menuListener);
        multiplayerGameInternetButton.addActionListener(menuListener);
        loadGameButton.addActionListener(menuListener);
        onlineHelpButton.addActionListener(menuListener);
        tipsTricksButton.addActionListener(menuListener);
        creditsButton.addActionListener(menuListener);
        exitGameButton.addActionListener(menuListener);

        OriginalButton[] buttonList = {
            tutorialButton,
            campaignButton,
            missionCDCampaignButton,
            amazonCampaignButton,
            campaignDifficultyButton,
            singlePlayerScenarioButton,
            multiplayerGameLanButton,
            multiplayerGameInternetButton,
            loadGameButton,
            onlineHelpButton,
            tipsTricksButton,
            creditsButton,
            exitGameButton
        };

        // declare all labels
        LabelTextYellow version = new LabelTextYellow(String.format("Version %s", CommitInfo.COMMIT_HASH_SHORT), 34, 578);

        LabelTextYellow[] labelList = {
            version
        };

        // add background to menu
        MenuCanvas background = new MenuCanvas(menuImage.convertToBufferedImage(), buttonList, labelList, null, null);
        this.add(background);

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}