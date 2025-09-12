package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import jsettlers.main.swing.JSettlersFrame;


public abstract class OriginalMenuBase extends JPanel {

    public final JSettlersFrame mainFrame;


    public OriginalMenuBase(JSettlersFrame mainFrame) {

        this.mainFrame = mainFrame;

        this.setOpaque(true);
        this.setBackground(Color.BLACK);
        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 600));

        return;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = this.getMinimumSize();
        return preferredSize;
    }
}