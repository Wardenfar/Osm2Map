package com.wardenfar.osm2map.map.entity;

public class ENode {

    public long id;
    public LatLon latLon;

    public ENode(long id, double lat, double lon) {
        this.id = id;
        this.latLon = new LatLon(lat,lon);
    }
}
