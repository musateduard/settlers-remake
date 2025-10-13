package jsettlers.main.swing.originalmenu.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.JList;

import jsettlers.logic.map.loading.MapLoader;


public class OriginalDialog extends JPanel implements MouseListener {

    public boolean initialDisplay;
    public final BufferedImage randomMapImage;
    public final BufferedImage mapPreviewImage;
    public OriginalToggleButton randomMapButton;
    public final ArrayList<Component> randomMapComponentList;
    public final ArrayList<MapLoader> singleMapList;
    public final ArrayList<MapLoader> multiMapList;
    public final ArrayList<MapLoader> userMapList;


    public OriginalDialog(
        BufferedImage randomMapImage,
        BufferedImage mapPreviewImage,
        OriginalButton[] buttonList,
        OriginalToggleButton[] toggleButtonList,
        OriginalLabel[] labelList,
        OriginalDropdown[] dropdownList,
        ArrayList<MapLoader> singleMapList,
        ArrayList<MapLoader> multiMapList,
        ArrayList<MapLoader> userMapList) {

        // note: map list size 118 x 195

        super();

        this.randomMapImage = randomMapImage;
        this.mapPreviewImage = mapPreviewImage;
        this.initialDisplay = true;
        this.randomMapComponentList = new ArrayList<>();
        this.singleMapList = singleMapList;
        this.multiMapList = multiMapList;
        this.userMapList = userMapList;

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
                    this.randomMapButton = toggleButton;
                }

                this.add(toggleButton);
            }
        }

        if (labelList != null) {
            for (OriginalLabel label : labelList) {
                this.add(label);
                this.randomMapComponentList.add(label);
            }
        }

        if (dropdownList != null) {
            for (OriginalDropdown dropdown : dropdownList) {
                this.add(dropdown);
                this.randomMapComponentList.add(dropdown);
            }
        }

        // load image
        // add elements to dialog window
        // add dropdown list popups to overlay on top of dialog layer

        // image index at file_2::menu_11

        String[] items = {
            "test item", "test item", "test item", "test item", "test item", "test item", "test item",
            "test item", "test item", "test item", "test item", "test item", "test item", "test item",
            "test item", "test item"
        };

        JList<String> list1 = new JList<>(items);

        list1.setOpaque(true);
        list1.setBackground(Color.GREEN);
        list1.setForeground(Color.YELLOW);

        OriginalMapList mapList = new OriginalMapList(list1);

        this.add(mapList);

        this.addMouseListener(this);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        if (this.randomMapButton.isSelected()) {
            graphics.drawImage(this.randomMapImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }

        else {
            graphics.drawImage(this.mapPreviewImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }

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