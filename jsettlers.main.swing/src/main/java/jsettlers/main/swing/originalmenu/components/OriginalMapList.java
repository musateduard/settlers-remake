package jsettlers.main.swing.originalmenu.components;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicScrollPaneUI;

import jsettlers.logic.map.loading.MapLoader;


public class OriginalMapList extends JScrollPane {

    public OriginalMapList(JList<String> list1) {

        /*
        note:

        JScrollPane is being overridden by project look and feel in SettlersScrollPanelUi
        you need to override paintComponent
        other JScrollPane components are being overridden from ScrollbarUi and SettlerScrollPanelUi
        how do i bypass look and feel completely for all list components?

        todo: completely disable look and feel for all components of original menu
        */

        super(list1);
        // this.setUI(new BasicScrollPaneUI());

        // list1.setOpaque(true);
        // list1.setBackground(Color.GREEN);
        // list1.setForeground(Color.YELLOW);
        // list1.setLayout(null);
        // list1.setBounds(0, 0, 1000, 1000);

        JPanel panel1 = new JPanel(null);
        panel1.setLayout(null);
        panel1.setBackground(Color.GREEN);
        panel1.setOpaque(true);
        panel1.setBounds(0, 0, 500, 600);

        this.setLayout(null);
        this.setOpaque(true);
        this.setBorder(null);
        this.setBackground(Color.PINK);
        this.setBounds(22, 145, 118, 195);
        // this.setBounds(22, 145, 200, 200);
        // this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // this.setViewportView(panel1);
        // this.setLayoutOrientation(JList.VERTICAL);
        // this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        // this.setVisibleRowCount(-1);

        // JViewport viewport = this.getViewport();

        // viewport.setOpaque(false);
        // viewport.setBackground(new Color(0, 0, 0, 0));
        // viewport.setView(panel1);

        // this.setViewport(viewport);

        return;
    }


    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        return;
    }
}