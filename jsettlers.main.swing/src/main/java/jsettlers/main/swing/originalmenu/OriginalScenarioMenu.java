package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JPanel;

import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.common.images.EImageLinkType;
import jsettlers.main.swing.JSettlersFrame;


public class OriginalScenarioMenu extends JPanel {

    public final JSettlersFrame mainFrame;


    public OriginalScenarioMenu(JSettlersFrame mainFrame) {

        // todo: add all input fields
        // todo: add player rows
        // todo: add event listener to cancel button
        // todo: add initial map dialog

        this.mainFrame = mainFrame;

        this.setOpaque(true);
        this.setBackground(Color.BLACK);
        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 600));

        Color regularColor = new Color(255, 223, 0);
        Color titleColor = new Color(248, 92, 24);

        // load all images
        ImageProvider imageProvider = ImageProvider.getInstance();

        SingleImage menuImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.GUI, 2, 13));
        SingleImage buttonImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 1, 0));
        SingleImage buttonImagePressed = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 1, 1));
        BufferedImage buttonImageHovered = new BufferedImage(buttonImagePressed.getWidth(), buttonImagePressed.getHeight(), BufferedImage.TYPE_INT_ARGB);

        SingleImage mapsButtonImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 21, 0));
        SingleImage mapsButtonImagePressed = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 21, 1));
        BufferedImage mapsButtonImageHovered = new BufferedImage(59, 22, BufferedImage.TYPE_INT_ARGB);
        SingleImage upArrow = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 6, 0));
        SingleImage downArrow = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 5, 0));

        // todo: create hovered and pressed versions of arrow

        assert menuImage instanceof NullImage == false;
        assert buttonImage instanceof NullImage == false;
        assert buttonImagePressed instanceof NullImage == false;
        assert mapsButtonImage instanceof NullImage == false;
        assert mapsButtonImagePressed instanceof NullImage == false;
        assert upArrow instanceof NullImage == false;
        assert downArrow instanceof NullImage == false;

        RescaleOp brightness = new RescaleOp(1.10f, 0, null);

        brightness.filter(buttonImage.convertToBufferedImage(), buttonImageHovered);
        brightness.filter(mapsButtonImage.convertToBufferedImage(), mapsButtonImageHovered);

        // load all fonts
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        /*
        note:

        the original ms sans serif is a bitmap font but the Font class doesn't handle bitmap fonts
        the ttf fonts loaded here are vector fonts that trace the original bitmap pixels
        the original ms sans serif font has 2 sizes that are primarily used in settlers
        size 11 is used for regular text and size 13 bold is used for titles
        these fake bitmap fonts don't scale well to other sizes in java
        ms-sans-serif-1.ttf is based on size 11 looks best at size 11.00
        ms-sans-serif-bold.ttf is based on size 13 but scales better at size 14.00
        */

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
            menuFont.deriveFont(Font.PLAIN, 11.00f), regularColor, true
        );

        ButtonProps mapsButtonProps = new ButtonProps(
            mapsButtonImage.convertToBufferedImage(),
            mapsButtonImageHovered,
            mapsButtonImagePressed.convertToBufferedImage(),
            null, null, false
        );

        ButtonProps arrowUpProps = new ButtonProps(
            upArrow.convertToBufferedImage(),
            upArrow.convertToBufferedImage(),
            upArrow.convertToBufferedImage(),
            null, null, false
        );

        ButtonProps arrowDownProps = new ButtonProps(
            downArrow.convertToBufferedImage(),
            downArrow.convertToBufferedImage(),
            downArrow.convertToBufferedImage(),
            null, null, false
        );

        // todo: add event listeners

        OriginalMenuButton cancelButton = new OriginalMenuButton("Cancel", 560, 556, buttonProps);
        OriginalMenuButton okButton = new OriginalMenuButton("OK", 680, 556, buttonProps);
        OriginalMenuButton mapsButton = new OriginalMenuButton(null, 321, 170, mapsButtonProps);
        OriginalMenuButton goodsArrow = new OriginalMenuButton(null, 369, 246, arrowDownProps);
        OriginalMenuButton gameTypeArrow = new OriginalMenuButton(null, 369, 342, arrowDownProps);
        OriginalMenuButton freeForAllArrow = new OriginalMenuButton(null, 369, 374, arrowDownProps);
        OriginalMenuButton nrOfTeamsUpArrow = new OriginalMenuButton(null, 301, 457, arrowUpProps);
        OriginalMenuButton nrOfTeamsDownArrow = new OriginalMenuButton(null, 301, 466, arrowDownProps);
        OriginalMenuButton playersPerTeamUpArrow = new OriginalMenuButton(null, 301, 489, arrowUpProps);
        OriginalMenuButton playersPerTeamDownArrow = new OriginalMenuButton(null, 301, 498, arrowDownProps);

        OriginalMenuButton[] buttonList = {
            cancelButton,
            okButton,
            mapsButton,
            goodsArrow,
            gameTypeArrow,
            freeForAllArrow,
            nrOfTeamsUpArrow,
            nrOfTeamsDownArrow,
            playersPerTeamUpArrow,
            playersPerTeamDownArrow
        };

        // declare all text
        TextProps titleProps = new TextProps(menuFontBold.deriveFont(Font.BOLD, 14.00f), titleColor, true);

        OriginalMenuText gameSetup = new OriginalMenuText("Game Setup", 371, 49, titleProps);
        OriginalMenuText mapSettings = new OriginalMenuText("Map settings", 22, 153, titleProps);
        OriginalMenuText commonTeamSetups = new OriginalMenuText("Common Team Setups", 22, 313, titleProps);
        OriginalMenuText teamSettingsGeneral = new OriginalMenuText("Team Settings: General", 22, 437, titleProps);
        OriginalMenuText teamSettingsSpecific = new OriginalMenuText("Team Settings: Specific", 574, 153, titleProps);

        TextProps regularTextProps = new TextProps(menuFont.deriveFont(Font.PLAIN, 11.00f), regularColor, false);

        OriginalMenuText nameOfGame = new OriginalMenuText("Name of the Game", 254, 96, regularTextProps);
        OriginalMenuText map = new OriginalMenuText("Map:", 22, 184, regularTextProps);
        OriginalMenuText mapType = new OriginalMenuText("Map Type", 22, 216, regularTextProps);
        OriginalMenuText goods = new OriginalMenuText("Goods:", 22, 248, regularTextProps);
        OriginalMenuText gameType = new OriginalMenuText("Game Type:", 22, 344, regularTextProps);
        OriginalMenuText nrOfTeams = new OriginalMenuText("Number of Teams:", 22, 468, regularTextProps);
        OriginalMenuText playersPerTeam = new OriginalMenuText("Players per Team:", 22, 500, regularTextProps);
        OriginalMenuText players = new OriginalMenuText("Players", 629, 179, regularTextProps);
        OriginalMenuText computers = new OriginalMenuText("Computers", 697, 179, regularTextProps);

        OriginalMenuText[] labelList = {
            gameSetup,
            mapSettings,
            commonTeamSetups,
            teamSettingsGeneral,
            teamSettingsSpecific,
            nameOfGame,
            map,
            mapType,
            goods,
            gameType,
            nrOfTeams,
            playersPerTeam,
            players,
            computers
        };

        // add background to menu
        MenuBackground background = new MenuBackground(menuImage.convertToBufferedImage(), buttonList, labelList);
        this.add(background);

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}