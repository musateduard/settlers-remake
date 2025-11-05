package org.example.mainwindow;

import java.util.ArrayList;


public class EventManager {

    public final ArrayList<MouseListener> mouseListenerList;
    public final ArrayList<CursorListener> cursorListenerList;
    public final ArrayList<KeyListener> keyListenerList;
    public final ArrayList<ResizeListener> resizeListenerList;


    public EventManager() {

        this.mouseListenerList = new ArrayList<>();
        this.cursorListenerList = new ArrayList<>();
        this.keyListenerList = new ArrayList<>();
        this.resizeListenerList = new ArrayList<>();

        return;
    }


    public void addMouseListener(MouseListener listener) {
        this.mouseListenerList.add(listener);
        return;
    }


    public void addCursorListener(CursorListener listener) {
        this.cursorListenerList.add(listener);
        return;
    }


    public void addKeyListener(KeyListener listener) {
        this.keyListenerList.add(listener);
        return;
    }


    public void addResizeListener(ResizeListener listener) {
        this.resizeListenerList.add(listener);
        return;
    }


    public void dispatchMouseEvent(MouseEvent event) {

        for (MouseListener item : this.mouseListenerList) {
            item.onMouseEvent(event);
        }

        return;
    }


    public void dispatchCursorEvent(CursorEvent event) {

        for (CursorListener item : this.cursorListenerList) {
            item.onCursorEvent(event);
        }

        return;
    }


    public void dispatchKeyEvent(KeyEvent event) {

        for (KeyListener item : this.keyListenerList) {
            item.onKeyEvent(event);
        }

        return;
    }


    public void dispatchResizeEvent(ResizeEvent event) {

        for (ResizeListener item : this.resizeListenerList) {
            item.onResizeEvent(event);
        }

        return;
    }
}