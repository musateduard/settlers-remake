package jsettlers.main.swing.originalmenu.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;


public class OriginalDialog extends JPanel implements MouseListener {

    public final BufferedImage dialogImage;


    public OriginalDialog(
        BufferedImage dialogImage,
        OriginalButton[] buttonList,
        LabelTextYellow[] labelList,
        OriginalDropdown[] dropdownList) {

        super();

        this.dialogImage = dialogImage;

        this.setLayout(null);
        this.setBounds(100, 100, 600, 400);
        // this.setBackground(Color.CYAN);
        this.setOpaque(true);

        if (buttonList != null) {

            for (OriginalButton button : buttonList) {
                this.add(button);
            }
        }

        if (labelList != null) {

            for (LabelTextYellow label : labelList) {
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
        System.out.printf("dialog received mouse press\n");
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