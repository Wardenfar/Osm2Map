package com.wardenfar.osm2map.map.entity;

import com.wardenfar.osm2map.map.Mercator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Vector2i {
    public int x;
    public int y;

    public Vector2i() {
        this(0, 0);
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public LatLon transform() {
        double lat = Mercator.y2lat(y);
        double lon = Mercator.x2lon(x);
        return new LatLon(lat, lon);
    }

    public Vector2i add(Vector2i p) {
        x += p.x;
        y += p.y;
        return this;
    }

    public Vector2i sub(int n) {
        x -= n;
        y -= n;
        return this;
    }

    public Vector2i add(int n) {
        x += n;
        y += n;
        return this;
    }

    public Vector2i multiply(Vector2i p) {
        x *= p.x;
        y *= p.y;
        return this;
    }

    public Vector2i multiply(double n) {
        x *= n;
        y *= n;
        return this;
    }

    public Vector2d toDouble(){
        return new Vector2d(x,y);
    }

    public Vector2i copy() {
        return new Vector2i(x, y);
    }

    public String toString() {
        return "Vector2i[x: " + x + " y: " + y + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object != null) {
            if (object instanceof Vector2d) {
                Vector2d o = (Vector2d) object;
                return x == o.getX() && y == o.getY();
            }
            if (object instanceof Vector2i) {
                Vector2i o = (Vector2i) object;
                return x == o.x && y == o.y;
            }
        }
        return false;
    }

    public void read(DataInputStream data) throws IOException {
        x = data.readInt();
        y = data.readInt();
    }

    public void write(DataOutputStream data) throws IOException {
        data.writeInt(x);
        data.writeInt(y);
    }

    public Vector2i div(float count) {
        this.x = (int)((float)x / count);
        this.y = (int)((float)y / count);
        return this;
    }
}
