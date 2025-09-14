package jsettlers.main.swing.originalmenu;

import java.awt.Color;
import java.awt.Font;


public class MenuText {

    public final Font textFont;
    public final Color textColor;
    public final String textString;
    public final int offsetX;
    public final int offsetY;
    public final int shadowX;
    public final int shadowY;
    public final int[] letterSpacing;


    public MenuText(TextProps props, String textString, int posX, int posY, int[] letterOffsets) {

        this.textFont = props.textFont();
        this.textColor = props.textColor();
        this.shadowX = props.shadowX();
        this.shadowY = props.shadowY();
        this.textString = textString;
        this.offsetX = posX;
        this.offsetY = posY;
        this.letterSpacing = letterOffsets;

        return;
    }
}