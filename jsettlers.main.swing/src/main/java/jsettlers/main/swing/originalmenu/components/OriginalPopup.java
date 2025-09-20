package jsettlers.main.swing.originalmenu.components;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ComboBoxModel;
import javax.swing.JList;


public class OriginalPopup extends JList<String> implements MouseListener {

    public OriginalPopup(OriginalDropdownList parent) {

        super(parent.optionList);

        ComboBoxModel<String> model = parent.getModel();

        this.setOpaque(false);
        this.setFont(parent.getFont());
        this.setBackground(Color.BLACK);
        this.setForeground(parent.getForeground());
        this.setBounds(parent.getX(), parent.getY() + parent.getHeight() + 8, parent.getWidth(), this.getCellBounds(0, 0).height * model.getSize());

        this.addMouseListener(this);

        return;
    }


    @Override
    public void mouseClicked(MouseEvent event) {
        return;
    }


    @Override
    public void mousePressed(MouseEvent event) {
        System.out.printf("event received on popup\n");
        return;
    }


    @Override
    public void mouseReleased(MouseEvent event) {
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