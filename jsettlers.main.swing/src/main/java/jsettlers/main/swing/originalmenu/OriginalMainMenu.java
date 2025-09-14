package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.image.RescaleOp;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
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


/**
 * this panel is used as a container for the actual main menu background.
 * it is set to a black background and covers the entire frame regardless of size and aspect ratio.
 * it then contains an additional {@link MenuBackground} background panel set to a fixed aspect ratio of 4:3.<br>
 * the background panel then contains in image buffer that is set to a fixed resolution of 800 x 600.
 * this buffer is set to fill the entire background panel and contains the actual image of the main menu
 * as well as the buttons and any additional text painted onto the menu.
 */
public class OriginalMainMenu extends JPanel {

    public final JSettlersFrame mainFrame;

    // todo: make background class generic for all menus
    // note: menu class should be declarative with all necessary components for rendering respective menu


    public OriginalMainMenu(JSettlersFrame mainFrame) {

        // super();

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

        // declare all buttons with event listeners
        ButtonProps buttonProps = new ButtonProps(
            buttonImage.convertToBufferedImage(),
            buttonImageHovered,
            buttonImagePressed.convertToBufferedImage(),
            menuFont, 11, false,
            new Color(0, 12, 64)
        );

        OriginalMenuButton tutorialButton = new OriginalMenuButton(buttonProps, "Tutorial", 80, 20);
        OriginalMenuButton campaignButton = new OriginalMenuButton(buttonProps, "Campaign", 80, 60);
        OriginalMenuButton missionCDCampaignButton = new OriginalMenuButton(buttonProps, "Mission CD Campaign", 80, 100);
        OriginalMenuButton amazonCampaignButton = new OriginalMenuButton(buttonProps, "Amazon Campaign", 80, 140);
        OriginalMenuButton campaignDifficultyButton = new OriginalMenuButton(buttonProps, "Campaign: Normal", 80, 180);
        OriginalMenuButton singlePlayerScenarioButton = new OriginalMenuButton(buttonProps, "Single Player: Scenario", 80, 220);
        OriginalMenuButton multiplayerGameLanButton = new OriginalMenuButton(buttonProps, "Multi-player Game: LAN", 80, 260);
        OriginalMenuButton multiplayerGameInternetButton = new OriginalMenuButton(buttonProps, "Multi-player Game: Internet", 80, 300);
        OriginalMenuButton loadGameButton = new OriginalMenuButton(buttonProps, "Load Game", 80, 340);
        OriginalMenuButton onlineHelpButton = new OriginalMenuButton(buttonProps, "Online Help", 80, 400);
        OriginalMenuButton tipsTricksButton = new OriginalMenuButton(buttonProps, "Tips & Tricks", 80, 440);
        OriginalMenuButton creditsButton = new OriginalMenuButton(buttonProps, "Credits", 80, 480);
        OriginalMenuButton exitGameButton = new OriginalMenuButton(buttonProps, "Exit Game", 80, 540);

        tutorialButton.addActionListener(
            (event) -> {
                System.out.printf("tutorials button pressed\n");
                return;
            }
        );

        campaignButton.addActionListener(
            (event) -> {
                this.mainFrame.showOriginalCampaignMenu();
                return;
            }
        );

        exitGameButton.addActionListener(
            (event) -> {
                System.exit(0);
                return;
            }
        );

        OriginalMenuButton[] buttonList = {
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

        // todo: finish all event listeners

        // declare all text
        TextProps textProps = new TextProps(
            menuFont.deriveFont(Font.PLAIN, 11.00f),
            new Color(255, 223, 0), 0, 0
        );

        MenuText[] textList = {
            new MenuText(textProps, String.format("Version %s", CommitInfo.COMMIT_HASH_SHORT), 34, 588, null),
        };

        MenuBackground background = new MenuBackground(menuImage.convertToBufferedImage(), buttonList, textList);
        this.add(background);

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}