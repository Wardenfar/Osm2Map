package com.wardenfar.osm2map.map.entity;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class GeFile {

    public Geometry geometry;

    public Map<Point, Float> buildingData;
    public Map<Point, Float> wayData;

    public GeFile(Map<Point, Float> buildingData, Map<Point, Float> wayData) {
        this.buildingData = buildingData;
        this.wayData = wayData;
    }

    public static GeFile fromFile(DBMapData mapData, File file) {
        Map<Point, Float> buildingData = new HashMap<>();
        Map<Point, Float> wayData = new HashMap<>();
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                float lat = in.readFloat();
                float lon = in.readFloat();
                float elev = in.readShort() / 10f;
                Vector2d pos = mapData.project(new LatLon(lat, lon));
                Point point = Util.geometryFactory.createPoint(new Coordinate(pos.x, pos.y));
                if (!mapData.getZonesFromPos(pos.toInt()).isEmpty()) {
                    buildingData.put(point, elev);
                } else {
                    wayData.put(point, elev);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("BuildingData : " + buildingData.size());
        System.out.println("WayData : " + wayData.size());
        return new GeFile(buildingData, wayData);
    }
}
