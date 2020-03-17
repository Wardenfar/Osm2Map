package com.wardenfar.osm2map.map.entity;

import com.wardenfar.osm2map.map.Mercator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Vector2d {
    public double x;
    public double y;

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d substract(Vector2d p) {
        x -= p.x;
        y -= p.y;
        return this;
    }

    public Vector2d multiply(Vector2d p) {
        x *= p.x;
        y *= p.y;
        return this;
    }

    public Vector2d divide(double d) {
        x /= d;
        y /= d;
        return this;
    }

    public Vector2d multiply(double d) {
        x *= d;
        y *= d;
        return this;
    }

    public Vector2d add(Vector2d v) {
        x += v.x;
        y += v.y;
        return this;
    }

    public void restrict(double minX, double minY, double maxX, double maxY) {
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }

    public LatLon transform() {
        double lon = Mercator.x2lon(x);
        double lat = Mercator.y2lat(y);
        return new LatLon(lat, lon);
    }

    public Vector2d copy() {
        return new Vector2d(x, y);
    }

    public String toString() {
        return "[x: " + x + ", y: " + y + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object != null) {
            if (object instanceof Vector2d) {
                Vector2d o = (Vector2d) object;
                return x == o.x && y == o.y;
            }
            if (object instanceof Vector2i) {
                Vector2i o = (Vector2i) object;
                return getX() == o.x && getY() == o.y;
            }
        }
        return false;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public void read(DataInputStream data) throws IOException {
        x = data.readDouble();
        y = data.readDouble();
    }

    public void write(DataOutputStream data) throws IOException {
        data.writeDouble(x);
        data.writeDouble(y);
    }

    public Vector2i toInt() {
        return new Vector2i(getX(), getY());
    }
}
