package org.example.glfwrenderer;

import java.io.File;
import java.nio.file.Files;


public class SettlersMap {

    public byte[] mapData;


    public SettlersMap() throws Exception {

        // load raw byte data
        File mapLocation = new File("C:\\games\\Settlers 3 Ultimate\\Map\\User\\384-2-Brueckenkopf.map");
        this.mapData = Files.readAllBytes(mapLocation.toPath());

        // decrypt map data

        // contains terrain grid
        // units grid
        // buildings grid
        // objects grid
        // constructed from map files or auto generated
        // implements entity component system?
        // only SettlersGame changes map state
        // only renderer reads map state

        return;
    }
}