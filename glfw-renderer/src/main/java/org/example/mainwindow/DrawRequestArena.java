package org.example.mainwindow;

import java.util.ArrayList;


public class DrawRequestArena {

    public int unitRequestIndex;
    public int animatedRequestIndex;
    public int staticRequestIndex;
    public int waveRequestIndex;
    public ArrayList<SpriteDrawRequest> unitRequestList;
    public ArrayList<SpriteDrawRequest> animatedRequestList;
    public ArrayList<SpriteDrawRequest> staticRequestList;
    public ArrayList<SpriteDrawRequest> waveRequestList;


    public DrawRequestArena() {

        int capacity = 500;

        this.unitRequestIndex = 0;
        this.animatedRequestIndex = 0;
        this.staticRequestIndex = 0;
        this.waveRequestIndex = 0;
        this.unitRequestList = new ArrayList<>(capacity);
        this.animatedRequestList = new ArrayList<>(capacity);
        this.staticRequestList = new ArrayList<>(capacity);
        this.waveRequestList = new ArrayList<>(capacity);

        DrawRequestArena.resize(this.unitRequestList, capacity);
        DrawRequestArena.resize(this.animatedRequestList, capacity);
        DrawRequestArena.resize(this.staticRequestList, capacity);
        DrawRequestArena.resize(this.waveRequestList, capacity);

        return;
    }


    public void clearRequestIndex() {

        this.unitRequestIndex = 0;
        this.animatedRequestIndex = 0;
        this.staticRequestIndex = 0;
        this.waveRequestIndex = 0;

        return;
    }


    public void checkArenaCapacity(ArrayList<SpriteDrawRequest> requestList, int desiredCapacity) {

        if (desiredCapacity >= requestList.size()) {
            requestList.ensureCapacity(requestList.size() * 2);
            DrawRequestArena.resize(requestList, desiredCapacity);
        }

        return;
    }


    public void addDrawRequest(
        DrawRequestType requestType, Texture texture,
        float x, float y, float width, float height,
        float fowLevel, float u0, float v0, float u1, float v1) {

        switch (requestType) {

            case WAVE: {

                this.checkArenaCapacity(this.waveRequestList, this.waveRequestIndex + 1);
                this.waveRequestList.get(this.waveRequestIndex).set(texture, x, y, width, height, fowLevel, u0, v0, u1, v1);
                this.waveRequestIndex += 1;

                break;
            }

            case STATIC: {

                this.checkArenaCapacity(this.staticRequestList, this.staticRequestIndex + 1);
                this.staticRequestList.get(this.staticRequestIndex).set(texture, x, y, width, height, fowLevel, u0, v0, u1, v1);
                this.staticRequestIndex += 1;

                break;
            }

            case ANIMATED: {

                this.checkArenaCapacity(this.animatedRequestList, this.animatedRequestIndex + 1);
                this.animatedRequestList.get(this.animatedRequestIndex).set(texture, x, y, width, height, fowLevel, u0, v0, u1, v1);
                this.animatedRequestIndex += 1;

                break;
            }

            case UNIT: {

                this.checkArenaCapacity(this.unitRequestList, this.unitRequestIndex + 1);
                this.unitRequestList.get(this.unitRequestIndex).set(texture, x, y, width, height, fowLevel, u0, v0, u1, v1);
                this.unitRequestIndex += 1;

                break;
            }
        }

        return;
    }


    public static void resize(ArrayList<SpriteDrawRequest> requestList, int desiredCapacity) {

        requestList.ensureCapacity(desiredCapacity);

        for (int item = 0; item < desiredCapacity - requestList.size(); item += 1) {
            requestList.add(new SpriteDrawRequest());
            continue;
        }

        return;
    }
}