package com.wardenfar.osm2map.db;

public class OptionsRecord {

    public int height;
    public double zoom;
    public double tlLat;
    public double tlLon;
    public double brLat;
    public double brLon;

    public OptionsRecord(int height, double zoom, double tlLat, double tlLon, double brLat, double brLon) {
        this.height = height;
        this.zoom = zoom;
        this.tlLat = tlLat;
        this.tlLon = tlLon;
        this.brLat = brLat;
        this.brLon = brLon;
    }
}
