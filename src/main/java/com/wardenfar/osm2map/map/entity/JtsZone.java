package com.wardenfar.osm2map.map.entity;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class JtsZone {

    public long id;
    public Type type;
    public String owner;
    public List<String> friends;

    private List<ENode> nodes;
    public Geometry geometry;

    private List<Vector2i> points = new ArrayList<>();
    private List<Vector2i> linePixels = new ArrayList<>();
    private List<Vector2i> pixels = new ArrayList<>();

    public JtsZone(long id, Type type, String owner, List<String> friends, Geometry geometry) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.friends = friends == null ? new ArrayList<>() : friends;
        this.geometry = geometry;
        for (Coordinate coord : geometry.getCoordinates()) {
            points.add(new Vector2i((int) coord.x, (int) coord.y));
        }
    }

    public JtsZone(long id, Type type, List<ENode> nodes) {
        this.id = id;
        this.type = type;
        this.owner = null;
        this.friends = new ArrayList<>();
        this.nodes = nodes;
    }

    public void createPolygonFromNodes(DBMapData mapData) {
        if (nodes != null) {
            Coordinate[] coords = new Coordinate[nodes.size() + 1];
            for (int i = 0; i < nodes.size(); i++) {
                Vector2d posD = mapData.project(nodes.get(i).latLon);
                Vector2i pos = new Vector2i(posD.getX(), posD.getY());
                points.add(pos);
                coords[i] = new Coordinate(pos.x, pos.y);
                if (i == 0) {
                    coords[nodes.size()] = new Coordinate(pos.x, pos.y);
                }
            }
            this.geometry = Util.geometryFactory.createPolygon(coords);
        }
    }

    public void computePixels(Vector2i min, Vector2i max) {
        if (pixels.isEmpty()) {
            List[] result = Util.computePixels(min, max, geometry, true);
            pixels = (List<Vector2i>) result[0];
            linePixels = (List<Vector2i>) result[1];
        }
    }

    public boolean contains(int x, int y) {
        return geometry.intersects(Util.geometryFactory.createPoint(new Coordinate(x, y)));
    }

    public List<Vector2i> getPixels() {
        return pixels;
    }

    public List<Vector2i> getLinePixels() {
        return linePixels;
    }

    public List<Vector2i> getPoints() {
        return points;
    }

    public boolean intersects(Rectangle rec) {
        return geometry.intersects(Util.geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(rec.x, rec.y),
                new Coordinate(rec.x + rec.width, rec.y),
                new Coordinate(rec.x + rec.width, rec.y + rec.height),
                new Coordinate(rec.x, rec.y + rec.height),
                new Coordinate(rec.x, rec.y),
        }));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof JtsZone)) {
            return false;
        }
        JtsZone z = (JtsZone) o;
        return this.id == z.id;
    }

    public enum Type {
        BUILDING(true),
        GARDEN(true),
        FOREST(true),
        WATERWAY(false);

        public boolean canConstruct;

        Type(boolean canConstruct) {
            this.canConstruct = canConstruct;
        }
    }

    public void setPoints(List<Vector2i> points) {
        this.points = points;
    }

    public void setLinePixels(List<Vector2i> linePixels) {
        this.linePixels = linePixels;
    }

    public void setPixels(List<Vector2i> pixels) {
        this.pixels = pixels;
    }
}
