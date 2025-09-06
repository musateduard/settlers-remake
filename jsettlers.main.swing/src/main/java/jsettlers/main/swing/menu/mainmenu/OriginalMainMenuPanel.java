package jsettlers.main.swing.menu.mainmenu;

import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class OriginalMainMenuPanel extends JPanel {

    static class MainMenuButton extends JButton {

        public final File buttonPath = new File("D:\\sprite0_0.bmp");


        public MainMenuButton(String buttonText, int offsetY) {

            this.setText(buttonText);
            this.setBounds(0, offsetY, 172, 32);
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
                graphics.drawImage(ImageIO.read(this.buttonPath), 0, 0, this.getWidth(), this.getHeight(), this);
            }

            catch (IOException exception) {
                System.out.printf("failed to open file: %s\n", this.buttonPath);
                exception.printStackTrace();
            }

            super.paintComponent(graphics);

            return;
        }
    }


    static class BackgroundPanel extends JPanel {

        public final double idealAspectRatio = (double) 800 / (double) 600;
        public final File imagePath = new File("D:\\menu2.bmp");
        public BufferedImage imageBuffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        public final JPanel buttonsPanel = new JPanel(null);
        public final JButton tutorialButton = new MainMenuButton("Tutorial", 0);
        public final JButton campaignButton = new MainMenuButton("Campaign", 40);
        public final JButton missionCDCampaignButton = new MainMenuButton("Mission CD Campaign", 80);
        public final JButton amazonCampaignButton = new MainMenuButton("Amazon Campaign", 120);
        public final JButton campaignDifficultyButton = new MainMenuButton("Campaign: Normal", 160);
        public final JButton singlePlayerScenarioButton = new MainMenuButton("Single Player: Scenario", 200);
        public final JButton multiplayerGameLanButton = new MainMenuButton("Multi-player Game: LAN", 240);
        public final JButton multiplayerGameInternetButton = new MainMenuButton("Multi-player Game: Internet", 280);
        public final JButton loadGameButton = new MainMenuButton("Load Game", 320);
        public final JButton onlineHelpButton = new MainMenuButton("Online Help", 380);
        public final JButton tipsTricksButton = new MainMenuButton("Tips & Tricks", 420);
        public final JButton creditsButton = new MainMenuButton("Credits", 460);
        public final JButton exitGameButton = new MainMenuButton("Exit Game", 520);


        public BackgroundPanel() {

            // set background image
            try {
                this.imageBuffer = ImageIO.read(this.imagePath);
            }

            catch (IOException exception) {
                System.out.printf("failed to open file: %s\n", this.imagePath);
                exception.printStackTrace();
            }

            // assign buttons panel
            this.buttonsPanel.setOpaque(false);
            this.buttonsPanel.setSize(new Dimension(172, 552));
            this.buttonsPanel.setBackground(Color.CYAN);

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
            this.buttonsPanel.add(this.exitGameButton);

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

            imageGraphics.dispose();

            graphics.drawImage(this.imageBuffer, 0, 0, this.getWidth(), this.getHeight(), this);

            return;
        }
    }


    public final JPanel backgroundPanel = new BackgroundPanel();


    public OriginalMainMenuPanel() {

        System.out.println("constructing original main menu panel");

        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.setOpaque(true);

        this.add(this.backgroundPanel);

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}