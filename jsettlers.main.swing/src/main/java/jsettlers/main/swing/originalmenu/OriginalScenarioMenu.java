package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.FontFormatException;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.List;

import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.map.MapContent;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.common.images.EImageLinkType;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.main.swing.originalmenu.components.ButtonProps;
import jsettlers.main.swing.originalmenu.components.DropdownProps;
import jsettlers.main.swing.originalmenu.components.LabelTextYellow;
import jsettlers.main.swing.originalmenu.components.OriginalButton;
import jsettlers.main.swing.originalmenu.components.OriginalDropdown;
import jsettlers.main.swing.originalmenu.components.OriginalDialog;
import jsettlers.main.swing.originalmenu.components.OriginalOverlay;
import jsettlers.main.swing.originalmenu.components.TitleTextOrange;
import jsettlers.main.swing.JSettlersFrame;


public class OriginalScenarioMenu extends JPanel {

    public final JSettlersFrame mainFrame;
    public final MenuCanvas menuCanvas;
    public OriginalDropdown currentGamePreset;
    public MapContent gameMap;


    public OriginalScenarioMenu(JSettlersFrame mainFrame) {

        /*
        note:

        this class should hold a MapContent instance whose properties are set by the various menu settings
        after map settings are set the class should call mainFrame.setContent()
        the start game function should be implemented similar to startGameListener in JoinGamePanel.setSinglePlayerMap()
        */

        // todo: add all input fields
        // todo: add player rows
        // todo: add event listener to cancel button
        // todo: add initial map dialog
        // todo: implement start game method using MapContent

        List<MapLoader> mapList = MapList.getDefaultList().getFreshMaps().getItems();

        System.out.printf("total maps %d\n", mapList.size());

        for (MapLoader item : mapList) {
            System.out.printf("%s\n", item.getMapName());
        }

        this.mainFrame = mainFrame;
        // this.gameMap = new MapContent();

        this.setOpaque(true);
        this.setBackground(Color.BLACK);
        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 600));

        // text colors
        Color regularColor = new Color(255, 223, 0);
        Color titleColor = new Color(248, 92, 24);

        // load all images
        ImageProvider imageProvider = ImageProvider.getInstance();

        SingleImage menuImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.GUI, 2, 13));
        SingleImage buttonImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 1, 0));
        SingleImage buttonImagePressed = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 1, 1));
        BufferedImage buttonImageHovered = new BufferedImage(buttonImage.getWidth(), buttonImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        SingleImage buttonImage120 = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 3, 0));
        SingleImage buttonImage120Pressed = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 3, 1));
        BufferedImage buttonImage120Hovered = new BufferedImage(buttonImage120.getWidth(), buttonImage120.getHeight(), BufferedImage.TYPE_INT_ARGB);

        SingleImage mapsButtonImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 21, 0));
        SingleImage mapsButtonImagePressed = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 21, 1));
        BufferedImage mapsButtonImageHovered = new BufferedImage(59, 22, BufferedImage.TYPE_INT_ARGB);
        SingleImage mapsDialogImage = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.GUI, 2, 12));

        SingleImage upArrow = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 6, 0));
        BufferedImage upArrowHovered = new BufferedImage(upArrow.getWidth(), upArrow.getHeight(), BufferedImage.TYPE_INT_ARGB);

        SingleImage downArrow = (SingleImage) imageProvider.getImage(new OriginalImageLink(EImageLinkType.SETTLER, 2, 5, 0));
        BufferedImage downArrowHovered = new BufferedImage(downArrow.getWidth(), downArrow.getHeight(), BufferedImage.TYPE_INT_ARGB);

        assert menuImage instanceof NullImage == false;
        assert buttonImage instanceof NullImage == false;
        assert buttonImagePressed instanceof NullImage == false;
        assert buttonImage120 instanceof NullImage == false;
        assert buttonImage120Pressed instanceof NullImage == false;
        assert mapsButtonImage instanceof NullImage == false;
        assert mapsButtonImagePressed instanceof NullImage == false;
        assert mapsDialogImage instanceof NullImage == false;
        assert upArrow instanceof NullImage == false;
        assert downArrow instanceof NullImage == false;

        RescaleOp brightness = new RescaleOp(1.10f, 0, null);

        brightness.filter(buttonImage.convertToBufferedImage(), buttonImageHovered);
        brightness.filter(mapsButtonImage.convertToBufferedImage(), mapsButtonImageHovered);
        brightness.filter(upArrow.convertToBufferedImage(), upArrowHovered);
        brightness.filter(downArrow.convertToBufferedImage(), downArrowHovered);
        brightness.filter(buttonImage120.convertToBufferedImage(), buttonImage120Hovered);

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

        ButtonProps upArrowProps = new ButtonProps(
            upArrow.convertToBufferedImage(),
            upArrowHovered,
            upArrow.convertToBufferedImage(),
            null, null, false
        );

        ButtonProps downArrowProps = new ButtonProps(
            downArrow.convertToBufferedImage(),
            downArrowHovered,
            downArrow.convertToBufferedImage(),
            null, null, false
        );

        /*
        note:

        arrows should be part of dropdown lists or number inputs not standalone buttons
        only real buttons on this menu are maps, cancel and ok

        todo: add event listeners
        todo: add dropdown lists
        todo: add number inputs
        todo: add text inputs
        */

        OriginalButton cancelButton = new OriginalButton("Cancel", 560, 556, buttonProps);
        OriginalButton okButton = new OriginalButton("OK", 680, 556, buttonProps);
        OriginalButton mapsButton = new OriginalButton(null, 321, 170, mapsButtonProps);

        // OriginalButton goodsArrow = new OriginalButton(null, 369, 246, downArrowProps);
        // OriginalButton gameTypeArrow = new OriginalButton(null, 369, 342, downArrowProps);
        // OriginalButton freeForAllArrow = new OriginalButton(null, 369, 374, downArrowProps);
        OriginalButton nrOfTeamsUpArrow = new OriginalButton(null, 301, 457, upArrowProps);
        OriginalButton nrOfTeamsDownArrow = new OriginalButton(null, 301, 466, downArrowProps);
        OriginalButton playersPerTeamUpArrow = new OriginalButton(null, 301, 489, upArrowProps);
        OriginalButton playersPerTeamDownArrow = new OriginalButton(null, 301, 498, downArrowProps);

        Font dialogFont = menuFont;
        ActionListener buttonListener = (event) -> {

            if (event.getSource() == cancelButton) {
                this.returnToMainMenu();
            }

            else if (event.getSource() == okButton) {
                System.out.printf("ok button pressed\n");
            }

            else if (event.getSource() == mapsButton) {

                ButtonProps buttonWideProps = new ButtonProps(
                    buttonImage120.convertToBufferedImage(),
                    buttonImage120Hovered,
                    buttonImage120Pressed.convertToBufferedImage(),
                    dialogFont.deriveFont(Font.PLAIN, 11.00f), regularColor, true
                );

                // todo: make category buttons togglable

                // create maps dialog component
                OriginalButton dialogCancel = new OriginalButton("Cancel", 360, 356, buttonProps);
                OriginalButton dialogOk = new OriginalButton("OK", 480, 356, buttonProps);
                OriginalButton dialogRandom = new OriginalButton("Random", 20, 20, buttonWideProps);
                OriginalButton dialogSinglePlayer = new OriginalButton("Single Player Map", 20, 44, buttonWideProps);
                OriginalButton dialogMultiPlayer = new OriginalButton("Multi-player Map", 20, 68, buttonWideProps);
                OriginalButton dialogUser = new OriginalButton("User", 20, 92, buttonWideProps);

                OriginalButton[] dialogButtonList = {
                    dialogCancel,
                    dialogOk,
                    dialogRandom,
                    dialogSinglePlayer,
                    dialogMultiPlayer,
                    dialogUser
                };

                // todo: add dialog labels
                // todo: add dialog dropdowns
                // todo: make buttons togglable

                OriginalDialog mapsDialog = new OriginalDialog(mapsDialogImage.convertToBufferedImage(), dialogButtonList, null);

                this.showMapsDialog(mapsDialog);
            }

            else {
                System.out.printf("button missing action listener %s\n", event.getSource());
            }

            return;
        };

        cancelButton.addActionListener(buttonListener);
        okButton.addActionListener(buttonListener);
        mapsButton.addActionListener(buttonListener);

        OriginalButton[] buttonList = {
            cancelButton,
            okButton,
            mapsButton,
            // goodsArrow,
            // gameTypeArrow,
            // freeForAllArrow,
            nrOfTeamsUpArrow,
            nrOfTeamsDownArrow,
            playersPerTeamUpArrow,
            playersPerTeamDownArrow
        };

        // declare all labels
        TitleTextOrange gameSetup = new TitleTextOrange("Game Setup", 371, 38);
        TitleTextOrange mapSettings = new TitleTextOrange("Map settings", 22, 142);
        TitleTextOrange commonTeamSetups = new TitleTextOrange("Common Team Setups", 22, 302);
        TitleTextOrange teamSettingsGeneral = new TitleTextOrange("Team Settings: General", 22, 426);
        TitleTextOrange teamSettingsSpecific = new TitleTextOrange("Team Settings: Specific", 574, 142);

        LabelTextYellow nameOfGame = new LabelTextYellow("Name of the Game", 254, 86);
        LabelTextYellow map = new LabelTextYellow("Map:", 22, 174);
        LabelTextYellow mapType = new LabelTextYellow("Map Type", 22, 206);
        LabelTextYellow goodsLabel = new LabelTextYellow("Goods:", 22, 238);
        LabelTextYellow gameType = new LabelTextYellow("Game Type:", 22, 334);
        LabelTextYellow nrOfTeams = new LabelTextYellow("Number of Teams:", 22, 458);
        LabelTextYellow playersPerTeam = new LabelTextYellow("Players per Team:", 22, 490);
        LabelTextYellow players = new LabelTextYellow("Players", 629, 169);
        LabelTextYellow computers = new LabelTextYellow("Computers", 697, 169);

        JLabel[] labelList = {
            gameSetup,
            mapSettings,
            commonTeamSetups,
            teamSettingsGeneral,
            teamSettingsSpecific,
            nameOfGame,
            map,
            mapType,
            goodsLabel,
            gameType,
            nrOfTeams,
            playersPerTeam,
            players,
            computers
        };

        // todo: finish game name input field

        // declare all input fields and their event listeners
        JFormattedTextField test1 = new JFormattedTextField("player's game");

        test1.setBounds(380, 86, 228, 13);
        test1.setOpaque(false);
        test1.setBackground(new Color(0, 0, 0, 0));
        test1.setForeground(regularColor);
        test1.setBorder(null);
        test1.setFont(menuFont.deriveFont(Font.PLAIN, 11.00f));

        JFormattedTextField[] inputFieldList = {
            test1
        };

        // declare all dropdowns
        DropdownProps dropdownProps = new DropdownProps(
            231, 22,
            menuFont.deriveFont(Font.PLAIN, 11.00f), regularColor,
            downArrow.convertToBufferedImage(), downArrowHovered
        );

        String[] goodsList = {"Default", "Low", "Medium", "High"};
        String[] gameTypeList = {"Map Defaults", "No Teams", "Teams"};  // default value: No Teams
        String[] gamePresetList = {"League", "Free for All", "Free Alliances", "Play Alone"};  // dynamically allocated based on game type

        OriginalDropdown goodsDropdown = new OriginalDropdown(goodsList, 149, 234, 0, dropdownProps);
        OriginalDropdown gameTypeDropdown = new OriginalDropdown(gameTypeList, 149, 327, 1, dropdownProps);
        OriginalDropdown gamePresetDropdown = new OriginalDropdown(gamePresetList, 149, 359, 1, dropdownProps);

        this.currentGamePreset = gamePresetDropdown;

        ActionListener dropdownListener = (event) -> {

            OriginalDropdown element = (OriginalDropdown) event.getSource();
            String selectedItem = (String) element.getSelectedItem();

            if (element == goodsDropdown) {
                System.out.printf("goods selected\n");
            }

            else if (element == gameTypeDropdown) {
                this.updateGamePresetDropdown(selectedItem);
            }

            else if (element == gamePresetDropdown) {
                System.out.printf("game preset selected\n");
            }

            else {
                System.out.printf("dropdown element missing action listener %s\n", event.getSource());
            }

            return;
        };

        goodsDropdown.addActionListener(dropdownListener);
        gameTypeDropdown.addActionListener(dropdownListener);
        gamePresetDropdown.addActionListener(dropdownListener);

        OriginalDropdown[] dropdownList = {
            goodsDropdown,
            gameTypeDropdown,
            gamePresetDropdown
        };

        // add background to menu
        this.menuCanvas = new MenuCanvas(menuImage.convertToBufferedImage(), buttonList, labelList, inputFieldList, dropdownList);
        this.add(this.menuCanvas);

        return;
    }


    public void updateGamePresetDropdown(String selectedItem) {

        // create new preset dropdown
        String[] newPresetList;
        int newDefaultIndex;

        if (Objects.equals(selectedItem, "Map Defaults")) {
            newPresetList = new String[] {"No map defaults"};
            newDefaultIndex = 0;
        }

        else if (Objects.equals(selectedItem, "No Teams")) {
            newPresetList = new String[] {"League", "Free for All", "Free Alliances", "Play Alone"};
            newDefaultIndex = 1;
        }

        else {
            newPresetList = new String[] {"2 vs. 2", "3 vs. 3", "4 vs. 4", "2 vs. 2 vs. 2", "other"};
            newDefaultIndex = 0;
        }

        DropdownProps props = new DropdownProps(
            this.currentGamePreset.getWidth(), this.currentGamePreset.getHeight(),
            this.currentGamePreset.getFont(), this.currentGamePreset.getForeground(),
            this.currentGamePreset.arrow, this.currentGamePreset.arrowHovered
        );

        OriginalDropdown newPresetDropdown = new OriginalDropdown(
            newPresetList, this.currentGamePreset.getX(), this.currentGamePreset.getY(),
            newDefaultIndex, props
        );

        // remove current preset dropdown
        this.menuCanvas.internalPanel.remove(this.currentGamePreset);
        this.currentGamePreset = newPresetDropdown;

        // add new preset dropdown to internal panel
        this.menuCanvas.internalPanel.add(this.currentGamePreset);
        this.menuCanvas.internalPanel.revalidate();
        this.menuCanvas.internalPanel.repaint();

        return;
    }


    public void returnToMainMenu() {
        this.mainFrame.showOriginalMainMenu();
        return;
    }


    public void showMapsDialog(OriginalDialog dialogElement) {

        System.out.printf("maps button pressed\n");

        // add dialog to overlay
        OriginalOverlay overlay = new OriginalOverlay(false);

        overlay.add(dialogElement);

        // add overlay to internal panel
        this.menuCanvas.internalPanel.add(overlay, JLayeredPane.PALETTE_LAYER);
        this.menuCanvas.internalPanel.revalidate();
        this.menuCanvas.internalPanel.repaint();

        // repaint
        // when user clicks ok set selected map
        // close dialog and remove overlay

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}