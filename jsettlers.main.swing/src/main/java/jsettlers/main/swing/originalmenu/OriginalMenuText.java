package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Font;


public class OriginalMenuText {

    public final Font textFont;
    public final Color textColor;
    public final String textString;
    public final int offsetX;
    public final int offsetY;
    public final boolean shadow;


    public OriginalMenuText(String textString, int posX, int posY, TextProps props) {

        this.textFont = props.textFont();
        this.textColor = props.textColor();
        this.shadow = props.shadow();
        this.textString = textString;
        this.offsetX = posX;
        this.offsetY = posY;

        return;
    }
}