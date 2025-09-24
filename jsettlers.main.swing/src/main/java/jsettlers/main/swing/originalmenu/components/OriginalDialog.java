package jsettlers.main.swing.originalmenu.components;

import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Objects;


public class OriginalDialog extends JPanel implements MouseListener {

    public final BufferedImage dialogImage;
    public boolean initialDisplay;


    public OriginalDialog(
        BufferedImage dialogImage,
        OriginalButton[] buttonList,
        OriginalToggleButton[] toggleButtonList,
        OriginalLabel[] labelList,
        OriginalDropdown[] dropdownList) {

        super();

        this.dialogImage = dialogImage;
        this.initialDisplay = true;

        this.setLayout(null);
        this.setBounds(100, 100, 600, 400);
        // this.setBackground(Color.CYAN);
        this.setOpaque(true);

        if (buttonList != null) {
            for (OriginalButton button : buttonList) {
                this.add(button);
            }
        }

        if (toggleButtonList != null) {
            for (OriginalToggleButton toggleButton : toggleButtonList) {

                if (Objects.equals(toggleButton.getText(), "Random")) {
                    toggleButton.setSelected(true);
                }

                this.add(toggleButton);
            }
        }

        if (labelList != null) {
            for (OriginalLabel label : labelList) {
                this.add(label);
            }
        }

        if (dropdownList != null) {
            for (OriginalDropdown dropdown : dropdownList) {
                this.add(dropdown);
            }
        }

        // load image
        // add elements to dialog window
        // add dropdown list popups to overlay on top of dialog layer

        // image index at file_2::menu_11

        this.addMouseListener(this);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        graphics.drawImage(this.dialogImage, 0, 0, this.getWidth(), this.getHeight(), this);

        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {
        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {
        return;
    }


    @Override
    public void mouseClicked(MouseEvent event) {
        return;
    }


    @Override
    public void mouseEntered(MouseEvent event) {
        return;
    }


    @Override
    public void mouseExited(MouseEvent event) {
        return;
    }
}