package org.example.mainwindow;

import java.util.ArrayList;


public class DrawRequestArena {

    public int unitRequestCount;
    public int animatedRequestCount;
    public int staticRequestCount;
    public ArrayList<SpriteDrawRequest> unitRequestList;
    public ArrayList<SpriteDrawRequest> animatedRequestList;
    public ArrayList<SpriteDrawRequest> staticRequestList;


    public DrawRequestArena() {

        this.unitRequestCount = 0;
        this.animatedRequestCount = 0;
        this.staticRequestCount = 0;
        this.unitRequestList = new ArrayList<>(500);
        this.animatedRequestList = new ArrayList<>(500);
        this.staticRequestList = new ArrayList<>(500);

        return;
    }


    public void clearRequestCount() {

        this.unitRequestCount = 0;
        this.animatedRequestCount = 0;
        this.staticRequestCount = 0;

        return;
    }
}