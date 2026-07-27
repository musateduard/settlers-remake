package org.example.assetmanager;


public enum AssetType {

    Text(0),
    Landscape(1),
    Menu(2),
    Sprite(3),
    ColorSprite(4),
    Shadow(5),
    Animation(6),
    Palette(7),
    Undefined(-1);

    public final int value;


    AssetType(int sectionIndex) {
        this.value = sectionIndex;
        return;
    }
}