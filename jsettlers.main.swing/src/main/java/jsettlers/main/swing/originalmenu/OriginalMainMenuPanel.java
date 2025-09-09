package jsettlers.main.swing.originalmenu;

import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.common.CommitInfo;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.io.IOException;
import java.io.File;


class MenuButtonProperties {

    public Font buttonFont;
    public BufferedImage buttonImage;
    public BufferedImage buttonImageHovered;
    public BufferedImage buttonImageClicked;


    public MenuButtonProperties(BufferedImage buttonImage, BufferedImage buttonImageHovered, BufferedImage buttonImageClicked, Font buttonFont) {

        this.buttonFont = buttonFont;
        this.buttonImage = buttonImage;
        this.buttonImageHovered = buttonImageHovered;
        this.buttonImageClicked = buttonImageClicked;

        return;
    }
}


class MenuButton extends JButton {

    public final int buttonWidth = 172;
    public final int buttonHeight = 32;
    public boolean hovered = false;
    public boolean pressed = false;
    public int offsetY;

    public Font textFont;
    public BufferedImage buttonImage;
    public BufferedImage buttonImageHovered;
    public BufferedImage buttonImageClicked;


    public MenuButton(MenuButtonProperties properties, String buttonText, int offsetY) {

        this.textFont = properties.buttonFont;
        this.buttonImage = properties.buttonImage;
        this.buttonImageHovered = properties.buttonImageHovered;
        this.buttonImageClicked = properties.buttonImageClicked;

        this.offsetY = offsetY;
        this.setText(buttonText);
        this.setBounds(0, this.offsetY, this.buttonWidth, this.buttonHeight);
        this.setBorderPainted(false);
        this.setContentAreaFilled(false);
        this.setOpaque(false);
        this.setForeground(new Color(0, 12, 64));
        this.setFont(this.textFont.deriveFont(Font.PLAIN, 11f));

        return;
    }


    public void paintComponent(Graphics graphics) {

        if (this.pressed) {
            graphics.drawImage(this.buttonImageClicked, 0, 0, this.getWidth(), this.getHeight(), this);
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


class MenuEventListener implements MouseListener, MouseMotionListener {

    public final MainBackground component;
    public final MenuButton[] buttonList;
    public MenuButton pressedButton;


    public MenuEventListener(MainBackground menuPanel, MenuButton[] buttonList) {
        this.component = menuPanel;
        this.buttonList = buttonList;
        return;
    }


    public Point getScaledPosition(MouseEvent event) {

        int parentWidth = this.component.getWidth();
        int parentHeight = this.component.getHeight();

        int translatedX = (int) (((double) event.getX() / (double) parentWidth) * (double) 800);
        int translatedY = (int) (((double) event.getY() / (double) parentHeight) * (double) 600);

        Point position = new Point(translatedX, translatedY);

        return position;
    }


    public Rectangle getButtonCoordinates(MenuButton button) {

        Rectangle bounds = button.getBounds();

        bounds.x += 80;
        bounds.y += 20;

        return bounds;
    }


    public boolean isCursorOutsideMenuArea(Point click) {
        boolean outsideMenu = (click.x < 80 || click.x >= (172 + 80) || click.y < 20 || click.y >= (20 + 552));
        return outsideMenu;
    }


    @Override
    public void mouseMoved(MouseEvent event) {

        Point cursor = this.getScaledPosition(event);

        // set hovered status
        for (MenuButton item : this.buttonList) {

            Rectangle buttonBounds = this.getButtonCoordinates(item);

            if (this.isCursorOutsideMenuArea(cursor)) {
                item.hovered = false;
                item.pressed = false;
            }

            else {
                item.hovered = buttonBounds.contains(cursor);
            }
        }

        this.component.repaint();

        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {

        // System.out.printf("pressed %d %d\n", event.getX(), event.getY());

        Point cursor = this.getScaledPosition(event);
        boolean anyButtonPressed = false;

        if (this.isCursorOutsideMenuArea(cursor)) {
            this.pressedButton = null;
        }

        else {

            for (MenuButton item : this.buttonList) {

                Rectangle buttonBounds = this.getButtonCoordinates(item);

                if (buttonBounds.contains(cursor)) {

                    item.pressed = true;
                    this.pressedButton = item;
                    anyButtonPressed = true;

                    break;
                }

                continue;
            }

            if (anyButtonPressed == false) {
                this.pressedButton = null;
            }
        }

        this.component.repaint();

        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {

        // System.out.printf("released %d %d\n", event.getX(), event.getY());

        Point cursor = this.getScaledPosition(event);

        // cursor released outside menu area
        if (this.isCursorOutsideMenuArea(cursor)) {

            this.pressedButton = null;

            for (MenuButton item : this.buttonList) {
                item.hovered = false;
                item.pressed = false;
            }
        }

        // cursor released inside menu area
        else {

            // no button was pressed prior to release
            if (this.pressedButton == null) {

                // System.out.printf("button released inside menu area but no button pressed\n");

                // check if any button is hovered
                for (MenuButton item : this.buttonList) {

                    Rectangle buttonBounds = this.getButtonCoordinates(item);

                    item.pressed = false;
                    item.hovered = buttonBounds.contains(cursor);
                }
            }

            // button pressed prior to released
            else {

                // System.out.printf("button released inside menu area with pressed button\n");
                Rectangle pressedButtonBounds = this.getButtonCoordinates(this.pressedButton);

                // cursor released on same pressed button
                if (pressedButtonBounds.contains(cursor)) {

                    this.pressedButton.hovered = true;
                    this.pressedButton.pressed = false;
                    this.pressedButton.doClick();  // note: what will doClick do?
                }

                // cursor released on other button
                else {

                    for (MenuButton item : this.buttonList) {

                        Rectangle currentButtonBounds = this.getButtonCoordinates(item);

                        item.pressed = false;
                        item.hovered = currentButtonBounds.contains(cursor);
                    }
                }

                this.pressedButton = null;
            }
        }

        this.component.repaint();

        return;
    }


    @Override
    public void mouseClicked(MouseEvent event) {
        return;
    }


    @Override
    public void mouseEntered(MouseEvent event) {
        // System.out.printf("entered %d %d\n", event.getX(), event.getY());
        return;
    }


    @Override
    public void mouseExited(MouseEvent event) {
        // System.out.printf("exited %d %d\n", event.getX(), event.getY());
        return;
    }


    @Override
    public void mouseDragged(MouseEvent event) {
        return;
    }
}


class MainBackground extends JPanel {

    public final double idealAspectRatio = (double) 800 / (double) 600;
    public BufferedImage menuImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
    public BufferedImage buttonImage = new BufferedImage(172, 32, BufferedImage.TYPE_INT_ARGB);
    public BufferedImage buttonImageHovered = new BufferedImage(172, 32, BufferedImage.TYPE_INT_ARGB);
    public BufferedImage buttonImageClicked = new BufferedImage(172, 32, BufferedImage.TYPE_INT_ARGB);
    public Font menuFont = new Font("Arial", Font.PLAIN, 11);

    public final MenuEventListener eventListener;
    public final MenuButton[] buttonList;

    public final JSettlersFrame mainFrame;
    public final JPanel buttonsPanel;
    public final MenuButton tutorialButton;
    public final MenuButton campaignButton;
    public final MenuButton missionCDCampaignButton;
    public final MenuButton amazonCampaignButton;
    public final MenuButton campaignDifficultyButton;
    public final MenuButton singlePlayerScenarioButton;
    public final MenuButton multiplayerGameLanButton;
    public final MenuButton multiplayerGameInternetButton;
    public final MenuButton loadGameButton;
    public final MenuButton onlineHelpButton;
    public final MenuButton tipsTricksButton;
    public final MenuButton creditsButton;
    public final MenuButton exitGameButton;


    public MainBackground(JSettlersFrame mainFrame) {

        this.mainFrame = mainFrame;

        File imagePath = new File("D:\\menu2.bmp");
        File fontPath = new File("D:\\ms-sans-serif-1.ttf");
        File buttonImagePath = new File("D:\\sprite0_0.bmp");
        File buttonImageClickedPath = new File("D:\\sprite0_1.bmp");

        // load all images
        try {
            this.menuImage = ImageIO.read(imagePath);
            this.buttonImage = ImageIO.read(buttonImagePath);

            RescaleOp brightness = new RescaleOp(0.95f, 0, null);
            brightness.filter(this.buttonImage, this.buttonImageHovered);

            this.buttonImageClicked = ImageIO.read(buttonImageClickedPath);
        }

        catch (IOException exception) {
            System.out.printf("failed to open background image: %s\n", imagePath);
            exception.printStackTrace();
        }

        // load all fonts
        try {
            this.menuFont = Font.createFont(Font.TRUETYPE_FONT, fontPath);
        }

        catch (IOException | FontFormatException exception) {
            System.out.printf("failed to open menu font: %s\n", fontPath);
            exception.printStackTrace();
        }

        // set buttons
        MenuButtonProperties buttonProperties = new MenuButtonProperties(this.buttonImage, this.buttonImageHovered, this.buttonImageClicked, this.menuFont);

        this.tutorialButton = new MenuButton(buttonProperties, "Tutorial", 0);
        this.campaignButton = new MenuButton(buttonProperties, "Campaign", 40);
        this.missionCDCampaignButton = new MenuButton(buttonProperties, "Mission CD Campaign", 80);
        this.amazonCampaignButton = new MenuButton(buttonProperties, "Amazon Campaign", 120);
        this.campaignDifficultyButton = new MenuButton(buttonProperties, "Campaign: Normal", 160);
        this.singlePlayerScenarioButton = new MenuButton(buttonProperties, "Single Player: Scenario", 200);
        this.multiplayerGameLanButton = new MenuButton(buttonProperties, "Multi-player Game: LAN", 240);
        this.multiplayerGameInternetButton = new MenuButton(buttonProperties, "Multi-player Game: Internet", 280);
        this.loadGameButton = new MenuButton(buttonProperties, "Load Game", 320);
        this.onlineHelpButton = new MenuButton(buttonProperties, "Online Help", 380);
        this.tipsTricksButton = new MenuButton(buttonProperties, "Tips & Tricks", 420);
        this.creditsButton = new MenuButton(buttonProperties, "Credits", 460);
        this.exitGameButton = new MenuButton(buttonProperties, "Exit Game", 520);

        // assign buttons panel
        this.buttonsPanel = new JPanel(null);

        this.buttonsPanel.setSize(new Dimension(172, 552));
        this.buttonsPanel.setOpaque(false);

        // add button event listeners
        this.tutorialButton.addActionListener(
            event -> {
                System.out.printf("tutorials button pressed\n");
                return;
            }
        );

        this.campaignButton.addActionListener(
            event -> {
                this.mainFrame.showOriginalCampaignMenu();
                return;
            }
        );

        this.exitGameButton.addActionListener(
            event -> {
                System.out.printf("exit button pressed\n");
                System.exit(0);
                return;
            }
        );

        // add buttons to panel
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
        this.buttonList = new MenuButton[] {
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

        this.eventListener = new MenuEventListener(this, this.buttonList);

        this.addMouseListener(this.eventListener);
        this.addMouseMotionListener(this.eventListener);

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

        menuGraphics.translate(80, 20);
        this.buttonsPanel.printAll(menuGraphics);

        menuGraphics.translate(-80, -20);
        menuGraphics.setColor(new Color(255, 223, 0));
        menuGraphics.setFont(this.menuFont.deriveFont(11f));
        menuGraphics.drawString(String.format("Version %s", CommitInfo.COMMIT_HASH_SHORT), 34, 588);
        menuGraphics.dispose();

        graphics.drawImage(this.menuImage, 0, 0, this.getWidth(), this.getHeight(), this);

        return;
    }
}


/**
 * this panel is used as a container for the actual main menu background.
 * it is set to a black background and covers the entire frame window regardless os size or aspect ratio.
 * it then contains an additional background panel set to a fixed aspect ratio of 4:3.
 * the background panel then contains a buffered image that is set to a fixed resolution of 800 x 600.
 * this buffered image is set to fill the entire background panel and contains the actual background image of the main menu
 * as well as the buttons and any additional text painted onto the main menu.
 */
public class OriginalMainMenuPanel extends JPanel {

    public final JSettlersFrame mainFrame;


    public OriginalMainMenuPanel(JSettlersFrame mainFrame) {

        this.mainFrame = mainFrame;

        this.setOpaque(true);
        this.setBackground(Color.BLACK);
        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 600));

        MainBackground background = new MainBackground(this.mainFrame);

        this.add(background);

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}