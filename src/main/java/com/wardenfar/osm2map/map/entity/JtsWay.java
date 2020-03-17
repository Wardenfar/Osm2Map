package com.wardenfar.osm2map.map.entity;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;

import java.util.ArrayList;
import java.util.List;

public class JtsWay {

    public long id;
    public String name;
    public String type;

    private List<ENode> nodes;

    public List<Vector2i> points = new ArrayList<>();
    public Geometry geometry;
    public float thickness;

    public List<Vector2i> pixels = new ArrayList<>();
    public List<Vector2i> linePixels = new ArrayList<>();

    public JtsWay(long id, String name, String type) {
        this.id = id;
        this.name = name == null ? null : name.toLowerCase();
        this.type = type;
    }

    public JtsWay(long id, String name, String type, List<ENode> nodes) {
        this(id, name, type);
        this.nodes = nodes;
    }

    public JtsWay(long id, String name, String type, float thickness, Geometry geometry) {
        this(id, name, type);
        this.thickness = thickness;
        this.geometry = geometry;
        for (Coordinate coord : geometry.getCoordinates()) {
            points.add(new Vector2i((int) coord.x, (int) coord.y));
        }
    }

    public void computePixels(Vector2i min, Vector2i max) {
        if (pixels.isEmpty()) {
            List[] result = Util.computePixels(min, max, getBufferedGeometry(), true);
            pixels = (List<Vector2i>) result[0];
            linePixels = (List<Vector2i>) result[1];
            pixels.removeAll(linePixels);
        }
    }

    public Geometry getBufferedGeometry(){
        return geometry.buffer(thickness / 2f + 1, 1, BufferOp.CAP_SQUARE);
    }

    public void createLineString(DBMapData mapData) {
        Coordinate[] coords = new Coordinate[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            Vector2d xz = mapData.project(nodes.get(i).latLon);
            coords[i] = new Coordinate(xz.getX(), xz.getY());
            points.add(new Vector2i((int) xz.x, (int) xz.y));

        }
        geometry = Util.geometryFactory.createLineString(coords);
    }

    public static int getThickness(String type) {
        switch (type) {
            case "motorway":
            case "motorway_link":
                return 5;
            case "trunk":
            case "trunk_link":
            case "primary":
            case "primary_link":
            case "secondary":
            case "secondary_link":
            case "tertiary":
            case "tertiary_link":
            case "residential":
                return 3;
            case "unclassified":
            case "track":
            default:
                return 2;
        }
    }

    public String getName() {
        return name;
    }

    /*switch (type) {
            case "motorway":
            case "motorway_link":
                return 5;
            case "trunk":
            case "trunk_link":
            case "primary":
            case "primary_link":
                return 4;
            case "secondary":
            case "secondary_link":
                return 3;
            case "tertiary":
            case "tertiary_link":
            case "unclassified":
            case "residential":
                return 2;
            default:
                return 2;
        }*/
}
