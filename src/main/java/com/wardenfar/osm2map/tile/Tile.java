package com.wardenfar.osm2map.tile;

import java.awt.image.BufferedImage;

public class Tile {

    public BufferedImage tile;
    public BufferedImage tileProcessed;
    int tileX;
    int tileY;

    public Tile(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
    }
}
