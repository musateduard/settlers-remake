package org.example.mainwindow;

import java.util.ArrayList;


public class EventManager {

    public final ArrayList<MouseListener> mouseListenerList;
    public final ArrayList<CursorListener> cursorListenerList;
    public final ArrayList<KeyListener> keyListenerList;


    public EventManager() {

        this.mouseListenerList = new ArrayList<>();
        this.cursorListenerList = new ArrayList<>();
        this.keyListenerList = new ArrayList<>();

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


    public void emitMouseEvent(MouseEvent event) {

        for (MouseListener item : this.mouseListenerList) {
            item.onMouseEvent(event);
        }

        return;
    }


    public void emitCursorEvent(CursorEvent event) {

        for (CursorListener item : this.cursorListenerList) {
            item.onCursorEvent(event);
        }

        return;
    }


    public void emitKeyEvent(KeyEvent event) {

        for (KeyListener item : this.keyListenerList) {
            item.onKeyEvent(event);
        }

        return;
    }
}