package com.wardenfar.osm2map.map;

import com.wardenfar.osm2map.map.entity.ENode;
import com.wardenfar.osm2map.map.entity.Line;
import com.wardenfar.osm2map.map.entity.Vector2d;
import com.wardenfar.osm2map.map.entity.Vector2i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.wardenfar.osm2map.Util.getHollowed;

public class PolygonFiller {
    private List<Line> lines;

    public Polygon poly;
    private List<Vector2i> points = new ArrayList<>();

    private double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
    private double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

    List<Vector2i> pixels;
    private List<Vector2i> linePixels;

    public static PolygonFiller fromNodeList(List<ENode> nodes, DBMapData data) {
        List<Vector2d> positions = new ArrayList<>();
        for (ENode n : nodes) {
            positions.add(data.project(n.latLon));
        }
        return new PolygonFiller(positions);
    }

    public PolygonFiller(List<Vector2d> positions) {
        lines = new ArrayList<>();
        poly = new Polygon();
        Vector2d prev, next;
        for (int i = 1; i <= positions.size(); i++) {
            if (i == positions.size()) {
                prev = positions.get(positions.size() - 1);
                next = positions.get(0);

            } else {
                prev = positions.get(i - 1);
                next = positions.get(i);
            }
            poly.addPoint((int) prev.x, (int) prev.y);
            points.add(new Vector2i((int) prev.x, (int) prev.y));

            Line line = new Line(prev, next);
            minX = Math.min(minX, line.minX);
            maxX = Math.max(maxX, line.maxX);
            minY = Math.min(minY, line.minY);
            maxY = Math.max(maxY, line.maxY);
            lines.add(line);
        }
    }

    /*public boolean contains(int x,int y){
        if(poly.contains(x,y)){
            return true;
        }
        for(Line l:lines){
            if(l.getPixels().contains(new Vector2i(x,y))){
                return true;
            }
        }
        return false;
    }*/

    public List<Vector2i> getPoints() {
        return points;
    }

    public boolean contains(int x, int y) {
        if(poly.contains(x,y)){
            return true;
        }else if (poly.intersects(x,y,1,1)){
            return true;
        }else if(points.contains(new Vector2i(x,y))){
            return true;
        }else{
            return false;
        }

        /*if (points.size() < 3) {
            return false;
        }

        boolean inside = false;
        int npoints = points.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossproduct;
        int i;

        xOld = points.get(npoints - 1).x;
        zOld = points.get(npoints - 1).y;

        for (i = 0; i < npoints; ++i) {
            xNew = points.get(i).x;
            zNew = points.get(i).y;
            //Check for corner
            if (xNew == x && zNew == y) {
                return true;
            }
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
            if (x1 <= x && x <= x2) {
                crossproduct = ((long) y - (long) z1) * (long) (x2 - x1)
                        - ((long) z2 - (long) z1) * (long) (x - x1);
                if (crossproduct == 0) {
                    if ((z1 <= y) == (y <= z2)) return true; //on edge
                } else if (crossproduct < 0 && (x1 != x)) {
                    inside = !inside;
                }
            }
            xOld = xNew;
            zOld = zNew;
        }

        return inside;*/
    }


    public List<Vector2i> getPixels() {
        return pixels;
    }

    public List<Vector2i> getLinesPixels() {
        return linePixels;
    }

    public void computePixels() {
        pixels = new ArrayList<>();

        for (int x = (int) minX; x <= (int) maxX; x++) {
            for (int y = (int) minY; y <= (int) maxY; y++) {
                if (contains(x, y)) {
                    pixels.add(new Vector2i(x, y));
                }
            }
        }

        linePixels = getHollowed(pixels);
    }

    /*public void computePixels() {
        pixels = new ArrayList<>();

        // INSIDE POLYGON
        for (int y = (int) minY; y <= (int) maxY; y++) {
            List<Integer> xs = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                Line l = lines.get(i);
                if (l.minY <= y && y <= l.maxY) {
                    int[] axs = l.getX(y);
                    for (int ax : axs) {
                        if (!xs.contains(ax)) {
                            xs.add(ax);
                        }
                    }
                }
            }
            Collections.sort(xs);

            if (xs.size() < 2) {
                continue;
            }

            for (int i = 1; i < xs.size(); i++) {
                int prev = xs.get(i - 1);
                int next = xs.get(i);

                Vector2i prevPos = new Vector2i(prev,y);
                Vector2i nextPos = new Vector2i(next,y);

                if(!pixels.contains(prevPos)) {
                    pixels.add(prevPos);
                }
                if(!pixels.contains(nextPos)) {
                    pixels.add(nextPos);
                }

                boolean paint = contains(prev + 1, y);
                if (paint) {
                    for (int x = prev; x <= next; x++) {
                        Vector2i current = new Vector2i(x,y);
                        if(!pixels.contains(current)) {
                            pixels.add(current);
                        }
                    }
                }
            }
        }

        // contours
        for(Line l:lines){
            for(Vector2i p : l.getPixels()){
                if(!pixels.contains(p)){
                    pixels.add(p);
                }
            }
        }
    }*/
}