package jsettlers.main.swing.menu.mainmenu;

import jsettlers.common.CommitInfo;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;


class MenuButton extends JButton {

    public final File buttonPath = new File("D:\\sprite0_0.bmp");
    public final File buttonPressedPath = new File("D:\\sprite0_0_pressed.bmp");
    public final File buttonHoveredPath = new File("D:\\sprite0_0_hovered.bmp");
    public boolean hovered = false;
    public boolean pressed = false;
    public int offsetY;


    public MenuButton(String buttonText, int offsetY) {

        this.offsetY = offsetY;
        this.setText(buttonText);
        this.setBounds(0, this.offsetY, 172, 32);
        this.setBorderPainted(false);
        this.setContentAreaFilled(false);
        this.setOpaque(false);
        this.setForeground(new Color(0, 12, 64));

        try {

            Font msSans = Font.createFont(Font.TRUETYPE_FONT, new File("D:\\ms-sans-serif-1.ttf"));
            this.setFont(msSans.deriveFont(11f));
        }

        catch (Exception exception) {
            System.out.printf("failed to open font: %s\n", "D:\\ms-sans-serif-1.ttf");
            exception.printStackTrace();
        }

        return;
    }


    public void paintComponent(Graphics graphics) {

        try {

            if (this.hovered) {
                graphics.drawImage(ImageIO.read(this.buttonHoveredPath), 0, 0, this.getWidth(), this.getHeight(), this);
            }

            else if (this.pressed) {
                graphics.drawImage(ImageIO.read(this.buttonPressedPath), 0, 0, this.getWidth(), this.getHeight(), this);
            }

            else {
                graphics.drawImage(ImageIO.read(this.buttonPath), 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }

        catch (IOException exception) {
            System.out.printf("failed to open file: %s\n", this.buttonPath);
            exception.printStackTrace();
        }

        super.paintComponent(graphics);

        return;
    }
}


class MenuListener implements MouseListener {

    public BackgroundPanel component;
    public ArrayList<MenuButton> buttonList;


    public MenuListener(BackgroundPanel menuPanel, ArrayList<MenuButton> buttonList) {
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


    @Override
    public void mouseClicked(MouseEvent event) {

        Point clickPoint = this.getScaledPosition(event);

        System.out.printf("\n");
        System.out.printf("exit button %d %d %d %d\n", this.component.exitGameButton.getX(), this.component.exitGameButton.getY(), this.component.exitGameButton.getWidth(), this.component.exitGameButton.getHeight());
        System.out.printf("parent size %d %d\n", this.component.getWidth(), this.component.getHeight());
        System.out.printf("clicked %d %d\n", clickPoint.x, clickPoint.y);

        if (clickPoint.x < 80 || clickPoint.x >= (172 + 80) || clickPoint.y < 20 || clickPoint.y >= (20 + 552)) {
            System.out.printf("point outside of menu area\n");
            return;
        }

        /*
        for item in button list
            get button
            calculate absolute position of button based offset from menu panel and main panel
            if click inside button
                do action
            else
                continue

        return
        */

        System.out.printf("clicked inside menu area\n");
        for (MenuButton item : this.buttonList) {

            Rectangle bounds = this.getButtonCoordinates(item);

            if (bounds.contains(clickPoint)) {
                // run button action
                System.out.printf("clicked inside button: %d %d %d %d\n", bounds.x, bounds.y, bounds.width, bounds.height);
                item.doClick();
            }

            continue;
        }

        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {
        // System.out.printf("pressed %d %d\n", event.getX(), event.getY());
        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {
        // System.out.printf("released %d %d\n", event.getX(), event.getY());
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
}


class BackgroundPanel extends JPanel {

    public final double idealAspectRatio = (double) 800 / (double) 600;
    public final File imagePath = new File("D:\\menu2.bmp");
    public BufferedImage imageBuffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
    public Font msSans;

    public final JPanel buttonsPanel = new JPanel(null);
    public final MenuButton tutorialButton = new MenuButton("Tutorial", 0);
    public final MenuButton campaignButton = new MenuButton("Campaign", 40);
    public final MenuButton missionCDCampaignButton = new MenuButton("Mission CD Campaign", 80);
    public final MenuButton amazonCampaignButton = new MenuButton("Amazon Campaign", 120);
    public final MenuButton campaignDifficultyButton = new MenuButton("Campaign: Normal", 160);
    public final MenuButton singlePlayerScenarioButton = new MenuButton("Single Player: Scenario", 200);
    public final MenuButton multiplayerGameLanButton = new MenuButton("Multi-player Game: LAN", 240);
    public final MenuButton multiplayerGameInternetButton = new MenuButton("Multi-player Game: Internet", 280);
    public final MenuButton loadGameButton = new MenuButton("Load Game", 320);
    public final MenuButton onlineHelpButton = new MenuButton("Online Help", 380);
    public final MenuButton tipsTricksButton = new MenuButton("Tips & Tricks", 420);
    public final MenuButton creditsButton = new MenuButton("Credits", 460);
    public final MenuButton exitGameButton = new MenuButton("Exit Game", 520);


    public BackgroundPanel() {

        // set background image
        try {
            this.imageBuffer = ImageIO.read(this.imagePath);
            this.msSans = Font.createFont(Font.TRUETYPE_FONT, new File("D:\\ms-sans-serif-1.ttf"));
        }

        catch (IOException exception) {
            System.out.printf("failed to open file: %s\n", this.imagePath);
            exception.printStackTrace();
        }

        catch (FontFormatException exception) {
            System.out.printf("failed to open font: %s\n", "D:\\ms-sans-serif-1.ttf");
            exception.printStackTrace();
        }

        // assign buttons panel
        this.buttonsPanel.setSize(new Dimension(172, 552));
        this.buttonsPanel.setOpaque(false);

        // add buttons
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

        this.exitGameButton.addActionListener(
            event -> {
                System.out.printf("exit button pressed\n");
                System.exit(0);
                return;
            }
        );

        this.buttonsPanel.add(this.exitGameButton);

        // add event listener
        ArrayList<MenuButton> buttonList = new ArrayList<>(
            List.of(
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
            )
        );

        this.addMouseListener(new MenuListener(this, buttonList));

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

        Graphics2D imageGraphics = this.imageBuffer.createGraphics();

        imageGraphics.translate(80, 20);
        this.buttonsPanel.printAll(imageGraphics);

        imageGraphics.translate(-80, -20);
        imageGraphics.setColor(new Color(255, 223, 0));
        imageGraphics.setFont(this.msSans.deriveFont(11f));
        imageGraphics.drawString(String.format("Version %s", CommitInfo.COMMIT_HASH_SHORT), 34, 588);

        /*
        for (item in buttonList) {
            imageGraphics.translate(item.offsetX, item.offsetY);
            item.printAll(imageGraphics);
        }
        */

        imageGraphics.dispose();

        graphics.drawImage(this.imageBuffer, 0, 0, this.getWidth(), this.getHeight(), this);

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

    public OriginalMainMenuPanel() {

        System.out.println("constructing original main menu panel");

        this.setMinimumSize(new Dimension(800, 600));
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.BLACK);
        this.setOpaque(true);

        BackgroundPanel background = new BackgroundPanel();

        this.add(background);

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}