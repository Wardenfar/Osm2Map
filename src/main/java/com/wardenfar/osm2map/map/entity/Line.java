package com.wardenfar.osm2map.map.entity;

import java.util.ArrayList;
import java.util.List;

public class Line {

    // x = (y-height) / slope
    //
    // y = slope * x + height
    // height = y - (slope * x)
    public Vector2d a, b;

    public double minX, maxX;
    public double minY, maxY;

    List<Vector2i> pixels;

    public Line(Vector2d a, Vector2d b) {
        this.a = a;
        this.b = b;
        this.minX = Math.min(a.x, b.x);
        this.maxX = Math.max(a.x, b.x);
        this.minY = Math.min(a.y, b.y);
        this.maxY = Math.max(a.y, b.y);
    }

    public boolean isHorizontal() {
        return a.y == b.y;
    }

    public boolean isVertical() {
        return a.x == b.x;
    }

    public int[] getX(int y) {
        if (isVertical()) { // vertical
            return new int[]{a.getX()};
        } else if (isHorizontal()) { // horizontal
            return new int[]{a.getX(), b.getX()};
        } else {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (Vector2i p : pixels) {
                if (p.y == y) {
                    if (p.x < min) {
                        min = p.x;
                    }
                    if (p.x > max) {
                        max = p.x;
                    }
                }
            }
            return new int[]{min, max};
        }
    }

    public List<Vector2i> getPixels() {
        return pixels;
    }

    public void computePixels() {
        pixels = new ArrayList<>();
        int x1 = (int) a.x, x2 = (int) b.x, y1 = (int) a.y, y2 = (int) b.y;

        // delta of exact value and rounded value of the dependent variable
        int d = 0;

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int dx2 = 2 * dx; // slope scaling factors to
        int dy2 = 2 * dy; // avoid floating point

        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;

        int x = x1;
        int y = y1;

        if (dx >= dy) {
            while (true) {
                pixels.add(new Vector2i(x,y));
                if (x == x2)
                    break;
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                pixels.add(new Vector2i(x,y));
                if (y == y2)
                    break;
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
    }
    /*public void computePixels() {
        pixels = new ArrayList<>();
        boolean notdrawn = true;

        int x1 = a.getX(), y1 = a.getY(), z1 = 0;
        int x2 = b.getX(), y2 = b.getY(), z2 = 0;
        int tipx = x1, tipy = y1, tipz = z1;
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);

        if (dx + dy + dz == 0) {
            pixels.add(new Vector2i(tipx, tipy));
            notdrawn = false;
        }

        if (Math.max(Math.max(dx, dy), dz) == dx && notdrawn) {
            for (int domstep = 0; domstep <= dx; domstep++) {
                tipx = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
                tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dx) * (y2 - y1 > 0 ? 1 : -1));
                pixels.add(new Vector2i(tipx, tipy));
            }
            notdrawn = false;
        }

        if (Math.max(Math.max(dx, dy), dz) == dy && notdrawn) {
            for (int domstep = 0; domstep <= dy; domstep++) {
                tipy = y1 + domstep * (y2 - y1 > 0 ? 1 : -1);
                tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dy) * (x2 - x1 > 0 ? 1 : -1));

                pixels.add(new Vector2i(tipx, tipy));
            }
            notdrawn = false;
        }
    }*/
}