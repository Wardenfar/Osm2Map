package com.wardenfar.osm2map.map;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.db.Database;
import com.wardenfar.osm2map.db.OptionsRecord;
import com.wardenfar.osm2map.map.entity.*;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy;
import de.topobyte.osm4j.geometry.MissingWayNodeStrategy;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import static com.wardenfar.osm2map.Util.*;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DBMapData {

    private Database db;

    private int height;

    private LatLon topLeft;
    private LatLon bottomRight;

    private double zoom;

    private Vector2d topLeftPos;
    private Vector2d bottomRightPos;

    public static DBMapData readFromDB(Osm2map plugin, Database db) {
        OptionsRecord options = db.getOptions();
        return new DBMapData(plugin, db, options.height, new LatLon(options.tlLat, options.tlLon), new LatLon(options.brLat, options.brLon), options.zoom);
    }

    private DBMapData(Osm2map plugin, Database db, int height, LatLon topLeft, LatLon bottomRight, double zoom) {
        this.height = height;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.zoom = zoom;
        this.topLeftPos = topLeft.transform();
        this.bottomRightPos = bottomRight.transform();
        this.db = db;
    }

    private void addMapElement(JtsMapElement me) {
        db.insertMapElement(me);
    }

    private static boolean canBeAPoi(Map<String, String> tags) {
        if (tags.containsKey("wikipedia") && tags.containsKey("name")) {
            if (tags.containsKey("boundary")) {
                return false;
            }
            if (tags.containsKey("brand")) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public Geometry getBoundaryRect(){
        com.flowpowered.math.vector.Vector2d minMap = new com.flowpowered.math.vector.Vector2d(Math.min(getTopLeftPos().x, getBottomRightPos().x), Math.min(getTopLeftPos().y, getBottomRightPos().y));
        com.flowpowered.math.vector.Vector2d maxMap = new com.flowpowered.math.vector.Vector2d(Math.max(getTopLeftPos().x, getBottomRightPos().x), Math.max(getTopLeftPos().y, getBottomRightPos().y));
        return Util.geometryFromRectCorners(minMap.getX(), minMap.getY(), maxMap.getX(), maxMap.getY());
    }

    public static DBMapData readFromOsmXML(Osm2map plugin, Database db, Logger logger, InputStream in, int height, double zoom) {
        OsmIterator iterator = new OsmXmlIterator(in, false);

        double top = 0, bottom = 0; // lat
        double left = 0, right = 0; // lon

        Map<Long, ENode> nodes = new HashMap<>();
        List<JtsWay> ways = new ArrayList<>();
        List<JtsZone> zones = new ArrayList<>();
        Map<ENode, String> nodePois = new HashMap<>();
        Map<JtsZone, String> zonesPois = new HashMap<>();
        Map<ENode, String> nodeElements = new HashMap<>();
        Map<List<ENode>, String> wayElements = new HashMap<>();

        InMemoryMapDataSet osmData = null;
        try {
            osmData = MapDataSetLoader.read(iterator, true, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (OsmNode osmNode : osmData.getNodes().valueCollection()) {
            Map<String, String> tags = OsmModelUtil.getTagsAsMap(osmNode);
            double lat = osmNode.getLatitude();
            double lon = osmNode.getLongitude();
            ENode n = new ENode(osmNode.getId(), lat, lon);
            nodes.put(n.id, n);
            if (canBeAPoi(tags)) {
                nodePois.put(n, tags.get("name"));
            }
            if (tags.containsKey("natural") && tags.get("natural").equals("tree")) {
                nodeElements.put(n, "tree");
            }
            if (tags.containsKey("amenity") && tags.get("amenity").equals("fountain")) {
                nodeElements.put(n, "fountain");
            }
        }

        for (OsmWay osmWay : osmData.getWays().valueCollection()) {
            List<ENode> nodesOfWay = new ArrayList<>();
            for (int i = 0; i < osmWay.getNumberOfNodes(); i++) {
                ENode n = nodes.get(osmWay.getNodeId(i));
                if (n != null) {
                    nodesOfWay.add(n);
                }
            }
            if (nodesOfWay.size() < 2) {
                continue;
            }
            Map<String, String> tags = OsmModelUtil.getTagsAsMap(osmWay);
            if (tags.get("building") != null) { // || tags.get("building:part") != null
                if (nodesOfWay.size() < 3) {
                    continue;
                }
                JtsZone z = new JtsZone(osmWay.getId(), JtsZone.Type.BUILDING, nodesOfWay);
                zones.add(z);
                if (canBeAPoi(tags)) {
                    zonesPois.put(z, tags.get("name"));
                }
            } else if (tags.get("landuse") != null) {
                if (nodesOfWay.size() < 3) {
                    continue;
                }
                JtsZone.Type type = null;
                if ("grass".equals(tags.get("landuse"))) {
                    type = JtsZone.Type.GARDEN;
                } else if ("forest".equals(tags.get("landuse"))) {
                    type = JtsZone.Type.FOREST;
                }
                if(type != null) {
                    JtsZone z = new JtsZone(osmWay.getId(), type, nodesOfWay);
                    zones.add(z);
                }
            } else if (tags.get("leisure") != null) {
                if (nodesOfWay.size() < 3) {
                    continue;
                }
                String[] values = new String[]{
                        "garden",
                        "park",
                };
                for (String value : values) {
                    if (value.equals(tags.get("leisure"))) {
                        JtsZone z = new JtsZone(osmWay.getId(), JtsZone.Type.GARDEN, nodesOfWay);
                        zones.add(z);
                        break;
                    }
                }
                values = new String[]{
                        "marina",
                };
                for (String value : values) {
                    if (value.equals(tags.get("leisure"))) {
                        JtsZone z = new JtsZone(osmWay.getId(), JtsZone.Type.WATERWAY, nodesOfWay);
                        zones.add(z);
                        break;
                    }
                }
            } else if (tags.get("building:part") != null) {
                    /*if (nodesOfWay.size() < 3) {
                        continue;
                    }
                    MapElement part = new MapElement(osmWay.getId(), MapElement.Type.BUILDING_PART, nodesOfWay, tags);
                    mapElements.add(part);*/
            } else if (tags.get("amenity") != null && tags.get("amenity").equals("fountain")) {
                wayElements.put(nodesOfWay, "fountain");
            } else if ((tags.get("waterway") != null && tags.get("waterway").equals("riverbank")) || (tags.get("water") != null) || (tags.get("natural") != null && tags.get("natural").equals("water"))) {
                if (nodesOfWay.size() < 3) {
                    continue;
                }
                JtsZone waterWay = new JtsZone(osmWay.getId(), JtsZone.Type.WATERWAY, nodesOfWay);
                zones.add(waterWay);
            } else if (tags.get("highway") != null) {
                JtsWay w = new JtsWay(osmWay.getId(), tags.get("name"), tags.get("highway"), nodesOfWay);
                ways.add(w);

                for (ENode n : nodesOfWay) {
                    double lat = n.latLon.lat;
                    double lon = n.latLon.lon;
                    if (top == 0 && bottom == 0 && left == 0 && right == 0) {
                        top = lat;
                        bottom = lat;
                        left = lon;
                        right = lon;
                    } else {
                        //lat
                        if (lat > top) {
                            top = lat;
                        }
                        if (lat < bottom) {
                            bottom = lat;
                        }
                        //lon
                        if (lon < left) {
                            left = lon;
                        }
                        if (lon > right) {
                            right = lon;
                        }
                    }
                }
            }
        }

        DBMapData mapData = new DBMapData(plugin, db, height, new LatLon(top, left), new LatLon(bottom, right), zoom);
        db.setOptions(new OptionsRecord(height, zoom, top, left, bottom, right));

        for (JtsZone z : zones) {
            z.createPolygonFromNodes(mapData);
        }

        for (Map.Entry<ENode, String> e : nodePois.entrySet()) {
            Vector2d pos = mapData.project(e.getKey().latLon);
            mapData.addPoi(new Poi(-1, e.getValue(), new Vector3d(pos.getX(), -1, pos.getY()), null));
        }

        for (Map.Entry<ENode, String> e : nodeElements.entrySet()) {
            Vector2d pos = mapData.project(e.getKey().latLon);
            mapData.addMapElement(new JtsMapElement(-1, e.getValue(), Util.geometryFactory.createPoint(new Coordinate(pos.x, pos.y))));
        }

        for (Map.Entry<List<ENode>, String> e : wayElements.entrySet()) {
            Coordinate[] coords = new Coordinate[e.getKey().size()];
            for (int i = 0; i < e.getKey().size(); i++) {
                Vector2d xz = mapData.project(e.getKey().get(i).latLon);
                coords[i] = new Coordinate(xz.getX(), xz.getY());
            }
            Geometry geometry = Util.geometryFactory.createLineString(coords);
            mapData.addMapElement(new JtsMapElement(-1, e.getValue(), geometry));
        }

        for (Map.Entry<JtsZone, String> e : zonesPois.entrySet()) {
            Point centroid = e.getKey().geometry.getCentroid();
            mapData.addPoi(new Poi(-1, e.getValue(), new Vector3d(centroid.getX(), -1, centroid.getY()), null));
        }

        GeometryBuilder builder = new GeometryBuilder();
        builder.setMissingEntitiesStrategy(MissingEntitiesStrategy.BUILD_PARTIAL);
        builder.setMissingWayNodeStrategy(MissingWayNodeStrategy.OMIT_VERTEX_FROM_POLYLINE);
        for (OsmRelation osmRelation : osmData.getRelations().valueCollection()) {
            Map<String, String> tags = OsmModelUtil.getTagsAsMap(osmRelation);
            if (tags.get("type") != null && tags.get("type").equals("multipolygon")) {
                JtsZone.Type type = null;
                if (tags.get("building") != null) {
                    type = JtsZone.Type.BUILDING;
                } else if ((tags.get("waterway") != null && tags.get("waterway").equals("riverbank")) || (tags.get("leisure") != null && tags.get("leisure").equals("marina")) || (tags.get("water") != null)) {
                    type = JtsZone.Type.WATERWAY;
                } else if (tags.get("leisure") != null) {
                    if (tags.get("leisure").equals("park")) {
                        type = JtsZone.Type.GARDEN;
                    } else if (tags.get("leisure").equals("garden")) {
                        type = JtsZone.Type.GARDEN;
                    }
                } else if (tags.get("landuse") != null && (tags.get("landuse").equals("grass") || tags.get("landuse").equals("forest"))) {
                    if (tags.get("landuse").equals("grass")) {
                        type = JtsZone.Type.GARDEN;
                    } else if (tags.get("landuse").equals("forest")) {
                        type = JtsZone.Type.FOREST;
                    }
                }
                if (type == null) {
                    continue;
                }
                if (osmRelation.getNumberOfMembers() > 0 && tags.get("type") != null && tags.get("type").equals("multipolygon")) {
                    try {
                        Geometry g = builder.build(osmRelation, osmData);
                        mapData.projectGeometryInversed(g);
                        JtsZone zone = new JtsZone(-1, type, null, null, g);
                        zones.add(zone);
                        if (canBeAPoi(tags)) {
                            Point centroid = g.getCentroid();
                            mapData.addPoi(new Poi(-1, tags.get("name"), new Vector3d(centroid.getX(), -1, centroid.getY()), null));
                        }
                    } catch (EntityNotFoundException e) {

                    }
                }
            }
        }

        // INSERT IN DB
        db.prepareBigImport();
        int count = 0;
        for (JtsWay way : ways) {
            way.thickness = (float) (JtsWay.getThickness(way.type) / mapData.zoom);
            way.createLineString(mapData);
            db.insertWay(way);
            if (count % 1000 == 0) {
                System.out.println("Ways : " + count + "/" + ways.size());
            }
            count++;
        }

        db.insertZones(zones);

        db.resetBigImport();

        return mapData;
    }

    public boolean isPlayerCanChangeAt(String player, Vector2i pos, boolean addPublic) {
        Set<Long> zonesId = db.getZonesByPosAndPlayer(player, pos, true, addPublic);
        for (Long id : zonesId) {
            JtsZone zone = db.getZone(id);
            return zone.type.canConstruct;
        }
        return false;
    }

    public List<JtsZone> getZonesFromChunk(Vector2i min, Vector2i max) {
        List<JtsZone> zones = new ArrayList<>();
        for (Long zoneId : db.getZonesFromChunk(min, max)) {
            JtsZone zone = db.getZone(zoneId);
            zones.add(zone);
        }
        return zones;
    }

    public List<JtsMapElement> getMapElementsFromChunk(Vector2i min, Vector2i max) {
        List<JtsMapElement> mes = new ArrayList<>();
        for (Long zoneId : db.getMapElementsFromChunk(min, max)) {
            JtsMapElement me = db.getMapElement(zoneId);
            mes.add(me);
        }
        return mes;
    }

    public List<JtsWay> getWaysFromChunk(Vector2i min, Vector2i max) {
        List<JtsWay> ways = new ArrayList<>();
        for (Long wayId : db.getWaysFromChunk(min, max)) {
            JtsWay way = db.getWay(wayId);
            ways.add(way);
        }
        return ways;
    }

    private void claim(long zoneId, String owner, Collection<String> friends) {
        db.setOwnerOfZone(zoneId, owner);
        for (String friend : friends) {
            db.addFriend(zoneId, friend);
        }
    }

    public Poi getOnePoiByName(String name) {
        return db.getOnePoiByName(name);
    }

    public List<Poi> getAllPoisByName(String name) {
        return db.getAllPoisByName(name);
    }

    public List<Poi> getAllPois() {
        return db.getPois().stream().map(db::getPoi).collect(Collectors.toList());
    }

    public int getNbPages() {
        return (db.getPois().size() / Osm2map.POI_PAGE_SIZE) + 1;
    }

    public List<Poi> getPoiFromPage(int page) {
        if (page <= 0) {
            return new ArrayList<>();
        }
        List<Long> ids = db.getPois();
        List<Long> pageids = new ArrayList<>();
        for (int i = (page - 1) * Osm2map.POI_PAGE_SIZE; i < page * Osm2map.POI_PAGE_SIZE; i++) {
            if (i < ids.size()) {
                pageids.add(ids.get(i));
            }
        }
        return pageids.stream().map(db::getPoi).collect(Collectors.toList());
    }

    public void claim(CommandSender src, Vector2i pos, String owner, Collection<String> friends, boolean admin) {
        Set<Long> zones = db.getZonesFromPos(pos);
        if ((admin && zones.size() >= 1) || zones.size() == 1) {
            for (Long zone : zones) {
                String currentOwner = db.getOwnerByZone(zone);
                boolean canClaim = false;
                if (!admin) {
                    if (currentOwner != null) {
                        if (currentOwner.equals(owner)) {
                            sendMessage(src, "You are already the owner of this zone !");
                        } else {
                            sendMessage(src, "This zone is already claimed !");
                        }
                    } else {
                        canClaim = true;
                    }
                } else {
                    canClaim = true;
                }
                if (canClaim) {
                    sendMessage(src, "Owner : " + owner);
                    sendMessage(src, friends.size() + " Amis : " + String.join(", ", friends));
                    claim(zone, owner, friends);
                    sendMessage(src, "The zone (" + zone + ") has been claimed by " + owner + " !");
                }

            }
        } else if (zones.size() == 0) {
            sendMessage(src, "You are not in any zones");
        } else {
            sendMessage(src, "You are on a side common to several zones or within several zones");
            sendMessage(src, "Go on the blocks of wooden planks or change position");
        }
    }

    public Vector2d project(LatLon latlon) {
        return project(latlon, topLeftPos, zoom);
    }

    public Vector2d project(Vector2d pos) {
        return project(pos, topLeftPos, zoom);
    }

    private static Vector2d project(LatLon latlon, Vector2d topLeftPos, double zoom) {
        return project(latlon.transform(), topLeftPos, zoom);
    }

    private static Vector2d project(Vector2d pos, Vector2d topLeftPos, double zoom) {
        return pos.copy().substract(topLeftPos).divide(zoom).multiply(new Vector2d(1, -1));
    }

    public LatLon unproject(Vector2d pos) {
        Vector2d v = pos.copy().multiply(new Vector2d(1, -1)).multiply(zoom).add(topLeftPos);
        return v.transform();
    }

    public void projectGeometry(Geometry g) {
        g.apply((CoordinateFilter) coord -> {
            Vector2d projected = project(new LatLon(coord.x, coord.y));
            coord.x = projected.getX();
            coord.y = projected.getY();
        });
    }

    public void projectGeometryInversed(Geometry g) {
        g.apply((CoordinateFilter) coord -> {
            Vector2d projected = project(new LatLon(coord.y, coord.x));
            coord.x = projected.getX();
            coord.y = projected.getY();
        });
    }

    public int getHeight() {
        return height;
    }

    public LatLon getTopLeft() {
        return topLeft;
    }

    public LatLon getBottomRight() {
        return bottomRight;
    }

    public double getZoom() {
        return zoom;
    }

    public Vector2d getTopLeftPos() {
        return topLeftPos;
    }

    public Set<Long> getZonesOwnedByPlayer(String player) {
        return db.getZonesOwnedByPlayer(player);
    }

    private void unclaim(long zoneId) {
        db.setOwnerOfZone(zoneId, null);
        db.removeAllFriend(zoneId);
    }

    public void unclaim(CommandSender src, String executor, Vector2i pos) {
        Set<Long> zones = db.getZonesFromPos(pos);
        if (zones.size() == 0) {
            sendMessage(src, "You are on any zones");
        } else if (zones.size() == 1) {
            Long zoneId = (Long) zones.toArray()[0];
            String owner = db.getOwnerByZone(zoneId);
            if (owner != null) {
                if (owner.equals(executor)) {
                    unclaim(zoneId);
                    sendMessage(src, "The zone (" + zoneId + ") was unclaim !");
                } else {
                    sendMessage(src, "This zone is not yours !");
                }
            } else {
                sendMessage(src, "This zone is not claim !");
            }
        } else {
            sendMessage(src, "You are on one side common to several zones or within several zones");
            sendMessage(src, "Go on the blocks of wooden planks or change position");
        }
    }

    public Set<Long> getZonesByOwnerOrFriend(String player, boolean addPublic) {
        return db.getZonesByOwnerOrFriend(player, addPublic);
    }

    public JtsZone getZone(Long id) {
        return db.getZone(id);
    }

    public List<JtsWay> getWaysFromNameLower(String name) {
        Set<Long> ids = db.getWaysFromNameLower(name.toLowerCase());
        return ids.stream().map(db::getWay).collect(Collectors.toList());
    }

    public void addPoi(Poi poi) {
        db.insertPoi(poi);
    }

    public void removePoi(long id) {
        db.removePoi(id);
    }

    public String getOwnerByZone(Long zoneId) {
        return db.getOwnerByZone(zoneId);
    }

    public Set<Long> getZonesFromPos(Vector2i pos) {
        return db.getZonesFromPos(pos);
    }

    public Set<Long> getOwnedZones() {
        return db.getOwnedZones();
    }

    public JtsZone addZone(JtsZone.Type type, List<Vector2i> points) {
        Coordinate[] coords = new Coordinate[points.size() + 1];
        for (int i = 0; i < points.size(); i++) {
            Coordinate c = new Coordinate(points.get(i).x, points.get(i).y);
            coords[i] = c;
            if (i == 0) {
                coords[points.size()] = c;
            }
        }
        Geometry geometry = Util.geometryFactory.createPolygon(coords);
        JtsZone zone = new JtsZone(-1, type, null, null, geometry);
        System.out.println(geometry.getCentroid());
        for (Coordinate c : geometry.getCoordinates()) {
            System.out.println(c);
        }
        db.insertZone(zone);
        return zone;
    }

    public void removeZone(long zoneId) {
        db.removeAllFriend(zoneId);
        db.removeZone(zoneId);
    }

    public List<String> getModos() {
        return db.getModos();
    }

    public void addModo(String player) {
        db.addModo(player);
    }

    public void removeModo(String player) {
        db.removeModo(player);
    }

    public void unclaimAllOf(CommandSender src, String player) {
        Set<Long> zoneIds = db.getZonesOwnedByPlayer(player);
        for (long zoneId : zoneIds) {
            db.setOwnerOfZone(zoneId, null);
            db.removeAllFriend(zoneId);
            sendMessage(src, "Unclaim Zone (" + zoneId + ")", ChatColor.GREEN);
        }
    }

    public void unclaimAdmin(CommandSender src, Vector3i pos) {
        Set<Long> zoneIds = db.getZonesFromPos(new Vector2i(pos.getX(), pos.getZ()));
        if (zoneIds.size() >= 1) {
            for (long zoneId : zoneIds) {
                db.setOwnerOfZone(zoneId, null);
                sendMessage(src, "Unclaim Zone (" + zoneId + ")", ChatColor.GREEN);
            }
        } else if (zoneIds.size() == 0) {
            sendMessage(src, "Vous étes sur aucunes zones");
        } else {
            sendMessage(src, "Vous étes sur un coté commun à plusieurs zones ou à l' intérieur de plusieurs zones");
            sendMessage(src, "Aller sur les blocs de planches de bois ou changer de position");
        }
    }

    public Vector2d getBottomRightPos() {
        return bottomRightPos;
    }
}
