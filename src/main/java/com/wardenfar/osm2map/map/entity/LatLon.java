package com.wardenfar.osm2map.map.entity;

import com.wardenfar.osm2map.map.Mercator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LatLon {
    public double lat;
    public double lon;

    public LatLon() {
        this(0, 0);
    }

    public LatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Vector2d transform() {
        double x = Mercator.lon2x(lon);
        double y = Mercator.lat2y(lat);
        return new Vector2d(x, y);
    }

    public LatLon read(DataInputStream data) throws IOException {
        lat = data.readDouble();
        lon = data.readDouble();
        return this;
    }

    public void write(DataOutputStream data) throws IOException {
        data.writeDouble(lat);
        data.writeDouble(lon);
    }

    public String toString() {
        return "LatLon[lat: " + lat + " lon: " + lon + "]";
    }
}
