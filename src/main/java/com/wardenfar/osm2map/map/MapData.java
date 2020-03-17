package com.wardenfar.osm2map.map;

public class MapData {
/*
    private Database db;
    private DSLContext context;

    private int height;

    private LatLon topLeft;
    private LatLon bottomRight;
    ;
    private double zoom;

    /*private Vector2d rotationCenter;
    private float rotation;*/

  //  private Vector2d topLeftPos;

    /*private Map<Long, ENode> nodes;
    private List<EWay> ways;
    private Map<Long, EZone> zones;*/

    /*private List<String> modos;

    private List<Poi> pois;

    private Map<String, List<Long>> zonesByOwner = new HashMap<>();
    private Map<String, List<Long>> zonesByFriends = new HashMap<>();
    private Map<Long, String> ownerByZone = new HashMap<>();
    private Map<Long, List<String>> friendsByZone = new HashMap<>();

    private Map<Long, EZone.State> stateByZone = new HashMap<>();

    private List<MapElement> mapElements;

    public MapData(Osm2map plugin, Database db) {
        this(plugin, db, 0, new LatLon(), new LatLon(), 1, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public MapData(Osm2map plugin, Database db, int height, LatLon topLeft, LatLon bottomRight, double zoom, List<String> modos, List<Poi> pois, List<MapElement> mapElements) {
        this.height = height;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.zoom = zoom;
        this.topLeftPos = topLeft.transform();
        /*this.nodes = nodes;
        this.ways = ways;
        this.zones = zones;*/
/*
        this.db = db;

        this.modos = modos;
        this.pois = pois;
        this.mapElements = mapElements;

        /*ENode center = nodes.get(plugin.getConfig().rotateCenterNodeId);
        ENode other = nodes.get(plugin.getConfig().rotateOtherNodeId);

        if(center == null || other == null){
            rotation = 0;
            center = null;
        }else{
            Vector2d cPos = center.latLon.transform();
            Vector2d oPos = other.latLon.transform();
        }
    }

    public Set<Long> getOwnedZones() {
        return ownerByZone.keySet();
    }

    public String getOwnerByZone(long zoneId) {
        return ownerByZone.get(zoneId);
    }

    public List<String> getFriendsByZone(long zoneId) {
        return friendsByZone.getOrDefault(zoneId, new ArrayList<>());
    }

    public List<String> getOwnerAndFriendsByZone(long zoneId) {
        List<String> result = new ArrayList<>();
        String owner = getOwnerByZone(zoneId);
        if (owner != null) {
            result.add(owner);
        }
        result.addAll(getFriendsByZone(zoneId));
        return result.stream().distinct().collect(Collectors.toList());
    }

    public List<Long> getZonesByOwnerOrFriend(String player) {
        return getZonesByOwnerOrFriend(player, false);
    }

    public List<Long> getZonesByOwnerOrFriend(String player, boolean addPublicZones) {
        List<Long> result = new ArrayList<>(getZonesByOwner(player));
        if (addPublicZones) {
            result.addAll(getZonesByOwner("*"));
        }
        result.addAll(getZonesByFriend(player));
        return result.stream().distinct().collect(Collectors.toList());
    }

    public List<Long> getZonesByOwner(String player) {
        List<Long> zoneIds = zonesByOwner.get(player);
        return zoneIds == null ? new ArrayList<>() : zoneIds.stream().distinct().collect(Collectors.toList());
    }

    public List<Long> getZonesByFriend(String player) {
        List<Long> zoneIds = zonesByFriends.get(player);
        return zoneIds == null ? new ArrayList<>() : zoneIds.stream().distinct().collect(Collectors.toList());
    }

    public List<EZone> getZonesFromPlayerAndPoint(String player, Vector2i pos, boolean addPublicZones) {
        List<EZone> containsZones = new ArrayList<>();
        for (long zoneId : getZonesByOwnerOrFriend(player, addPublicZones)) {
            EZone z = zones.get(zoneId);
            if (isZoneContainsPoint(z, pos)) {
                containsZones.add(z);
            }
        }
        return containsZones;
    }

    public static MapData readFromSave(Osm2map plugin, Logger logger) throws Exception {
        MapData mapData = new MapData(plugin);

        if (isSaveExist()) {
            DataInputStream mapDataIn = new DataInputStream(new FileInputStream(getDataFile()));

            // HEIGHT
            mapData.height = mapDataIn.readInt();

            // BBOX
            mapData.setTopLeft(new LatLon(mapDataIn.readDouble(), mapDataIn.readDouble()));
            mapData.setBottomRight(new LatLon(mapDataIn.readDouble(), mapDataIn.readDouble()));

            // ZOOM
            mapData.setZoom(mapDataIn.readDouble());

            // NODES
            int count = mapDataIn.readInt();
            for (int i = 0; i < count; i++) {
                ENode n = new ENode();
                n.read(mapDataIn);
                mapData.nodes.put(n.id, n);
            }

            // WAYS
            count = mapDataIn.readInt();
            for (int i = 0; i < count; i++) {
                EWay w = new EWay();
                w.read(mapDataIn, mapData.nodes);
                mapData.getWays().add(w);
            }

            // ZONES
            logger.info("Read Zones ...");
            count = mapDataIn.readInt();
            for (int i = 0; i < count; i++) {
                EZone z = new StaticZone();
                z.read(mapDataIn, mapData.nodes);
                mapData.getZones().put(z.id, z);
            }

            count = mapDataIn.readInt();
            for (int i = 0; i < count; i++) {
                EZone z = new AddedZone();
                z.read(mapDataIn, mapData.nodes);
                mapData.getZones().put(z.id, z);
            }

            // ZONES
            logger.info("Read Map Elements ...");
            count = mapDataIn.readInt();
            for (int i = 0; i < count; i++) {
                MapElement me = new MapElement();
                me.read(mapDataIn, mapData.nodes);
                mapData.getMapElements().add(me);
            }

            mapDataIn.close();

            mapData.calculateZonesPolygons(logger);
            mapData.calculateMapElements(logger);
            mapData.calculateWaysLines(logger);
        }

        File vDataFile = getVolatileDataFile();
        if (vDataFile.exists()) {
            DataInputStream vDataIn = new DataInputStream(new FileInputStream(vDataFile));

            // MODO
            int count = vDataIn.readInt();
            for (int i = 0; i < count; i++) {
                mapData.getModos().add(vDataIn.readUTF());
            }

            // POIS
            count = vDataIn.readInt();
            for (int i = 0; i < count; i++) {
                Poi poi = new Poi();
                poi.read(vDataIn);
                mapData.getPois().add(poi);
            }

            // PLAYER INDEX
            LinkedList<String> playersIndex = new LinkedList<>();
            count = vDataIn.readInt();
            for (int i = 0; i < count; i++) {
                playersIndex.add(vDataIn.readUTF());
            }

            // OWNER
            mapData.ownerByZone.clear();
            count = vDataIn.readInt();
            for (int i = 0; i < count; i++) {
                long zoneid = vDataIn.readLong();
                String owner = playersIndex.get(vDataIn.readInt());
                mapData.setOwnerOfZone(owner, zoneid);
            }

            // FRIENDS
            mapData.friendsByZone.clear();
            count = vDataIn.readInt();
            for (int i = 0; i < count; i++) {
                long zoneId = vDataIn.readLong();
                int countB = vDataIn.readInt();
                for (int j = 0; j < countB; j++) {
                    String friend = playersIndex.get(vDataIn.readInt());
                    mapData.addFriendToZone(friend, zoneId);
                }
            }
        }

        return mapData;
    }

    public List<MapElement> getMapElementsFromChunk(Vector2i min, Vector2i max) {
        List<MapElement> containsMe = new ArrayList<>();
        Rectangle rec = new Rectangle(min.x - 1, min.y - 1, max.x - min.x + 2, max.y - min.y + 2);
        for (MapElement me : mapElements) {
            if (me.geometry.poly.intersects(rec)) {
                containsMe.add(me);
            }
        }
        return containsMe.stream().distinct().collect(Collectors.toList());
    }

    public Map<EWay, List<Line2D>> getWayLinesFromChunk(Vector2i min, Vector2i max) {
        Map<EWay, List<Line2D>> containsLines = new HashMap<>();
        Rectangle rec = new Rectangle(min.x - 1, min.y - 1, max.x - min.x + 2, max.y - min.y + 2);
        for (EWay w : ways) {
            Stroke stroke = new BasicStroke(w.getThickness());
            for (Line2D line : w.lines) {
                Shape lineStroked = stroke.createStrokedShape(line);
                if (lineStroked.contains(rec) || lineStroked.intersects(rec)) {
                    if (containsLines.containsKey(w)) {
                        containsLines.get(w).add(line);
                    } else {
                        List<Line2D> ls = new ArrayList<>();
                        ls.add(line);
                        containsLines.put(w, ls);
                    }
                }
            }
        }
        return containsLines;
    }

    public List<EZone> getZonesFromPoint(Vector2i pos) {
        List<EZone> containsZones = new ArrayList<>();
        for (EZone z : zones.values()) {
            if (isZoneContainsPoint(z, pos)) {
                containsZones.add(z);
            }
        }
        return containsZones;
    }

    public static MapData readOsmXml(Osm2map plugin, Logger logger, InputStream in, int height, double zoom) {
        Database db = Database.create();

        OsmIterator iterator = new OsmXmlIterator(in, false);

        double top = 0, bottom = 0; // lat
        double left = 0, right = 0; // lon

        /* double top = 49.0, bottom = 48.9; // lat
        double left = 2.1, right = 2.2; // lon*/
/*
        Map<Long, ENode> nodes = new HashMap<>();
        List<EWay> ways = new ArrayList<>();
        List<MapElement> mapElements = new ArrayList<>();
        Map<Long, EZone> zones = new HashMap<>();

        Map<Long, EZone> zonesForMultipolygons = new HashMap<>();
        List<Long> outerZones = new ArrayList<>();

        for (EntityContainer container : iterator) {
            if (container.getType() == EntityType.Node) {
                OsmNode osmNode = (OsmNode) container.getEntity();
                double lat = osmNode.getLatitude();
                double lon = osmNode.getLongitude();
                ENode n = new ENode(osmNode.getId(), lat, lon);
                nodes.put(n.id, n);
            } else if (container.getType() == EntityType.Way) {
                OsmWay osmWay = (OsmWay) container.getEntity();
                List<ENode> nodesOfWay = new ArrayList<>();
                for (int i = 0; i < osmWay.getNumberOfNodes(); i++) {
                    ENode n = nodes.get(osmWay.getNodeId(i));
                    if (n != null) {
                        nodesOfWay.add(n);
                    }
                }
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(osmWay);
                if (tags.get("building") != null) { // || tags.get("building:part") != null
                    EZone z = new StaticZone(osmWay.getId(), EZone.Type.BUILDING, nodesOfWay);
                    zones.put(z.id, z);
                } else if (tags.get("landuse") != null) {
                    String[] values = new String[]{
                            "grass",
                            "forest",
                            "bassin",
                    };
                    for (String value : values) {
                        if (value.equals(tags.get("landuse"))) {
                            EZone z = new StaticZone(osmWay.getId(), EZone.Type.GARDEN, nodesOfWay);
                            zones.put(z.id, z);
                            break;
                        }
                    }
                } else if (tags.get("leisure") != null) {
                    String[] values = new String[]{
                            "garden",
                            "park",
                    };
                    for (String value : values) {
                        if (value.equals(tags.get("leisure"))) {
                            EZone z = new StaticZone(osmWay.getId(), EZone.Type.GARDEN, nodesOfWay);
                            zones.put(z.id, z);
                            break;
                        }
                    }
                } else if (tags.get("building:part") != null) {
                    MapElement part = new MapElement(osmWay.getId(), MapElement.Type.BUILDING_PART, nodesOfWay, tags);
                    mapElements.add(part);
                } else if (tags.get("waterway") != null && tags.get("waterway").equals("riverbank")) {
                    MapElement waterWay = new MapElement(osmWay.getId(), MapElement.Type.WATERWAY, nodesOfWay, tags);
                    mapElements.add(waterWay);
                } else if (tags.get("highway") != null) {
                    EWay w = new EWay(osmWay.getId(), nodesOfWay, tags.get("name"), tags.get("highway"));
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
                } else {
                    EZone z = new StaticZone(osmWay.getId(), null, nodesOfWay);
                    zonesForMultipolygons.put(z.id, z);
                }
            } else if (container.getType() == EntityType.Relation) {
                OsmRelation osmRelation = (OsmRelation) container.getEntity();
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(osmRelation);
                if (tags.get("type") != null && tags.get("type").equals("multipolygon") && tags.get("building") != null) {
                    for (int i = 0; i < osmRelation.getNumberOfMembers(); i++) {
                        OsmRelationMember member = osmRelation.getMember(i);
                        if ("outer".equals(member.getRole())) {
                            long id = member.getId();
                            outerZones.add(id);
                        }
                    }
                }
            }
        }
        for (long id : outerZones) {
            EZone z = zonesForMultipolygons.get(id);
            if (z != null) {
                z.type = EZone.Type.BUILDING;
                zones.put(z.id, z);
                zonesForMultipolygons.remove(id);
            }
        }


        // INSERT IN DB
        for (ENode node : nodes.values()) {
            db.insertNode(node);
        }
        for (EWay way : ways) {
            db.insertWay(way);
        }
        for (EZone zone : zones.values()) {
            if(zone instanceof  StaticZone){
                ((StaticZone) zone).computePixels();
            }
            db.insertZone(zone);
        }

        return new MapData(plugin, db, height, new LatLon(top, left), new LatLon(bottom, right), zoom, new ArrayList<>(), new ArrayList<>(), mapElements);
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
        Vector2d projected = pos.copy().substract(topLeftPos).divide(zoom).multiply(new Vector2d(1, -1));
        return projected;
    }

    public LatLon unproject(Vector2i pos) {
        return unproject(pos, topLeftPos, zoom);
    }

    private static LatLon unproject(Vector2i pos, Vector2d topLeftPos, double zoom) {
        Vector2d v = pos.toDouble().copy().multiply(new Vector2d(1, -1)).multiply(zoom).add(topLeftPos);
        return v.transform();
    }

    private static File getSaveFolder() {
        File saveFolder = new File("osm2map/");
        //noinspection ResultOfMethodCallIgnored
        saveFolder.mkdirs();
        return saveFolder;
    }

    public void saveVolatileData() throws Exception {
        DataOutputStream vDataOut = new DataOutputStream(new FileOutputStream(getVolatileDataFile()));

        // MODO
        vDataOut.writeInt(modos.size());
        for (String modo : modos) {
            vDataOut.writeUTF(modo);
        }

        // POIS
        vDataOut.writeInt(pois.size());
        for (Poi poi : pois) {
            poi.write(vDataOut);
        }

        // PLAYER INDEX
        LinkedList<String> playersIndex = friendsByZone.values().stream().flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
        playersIndex.addAll(ownerByZone.values());

        vDataOut.writeInt(playersIndex.size());
        for (String p : playersIndex) {
            vDataOut.writeUTF(p);
        }

        // OWNER
        Set<Map.Entry<Long, String>> ownerEntrySet = ownerByZone.entrySet();
        vDataOut.writeInt(ownerEntrySet.size());
        for (Map.Entry<Long, String> e : ownerEntrySet) {
            vDataOut.writeLong(e.getKey());
            vDataOut.writeInt(playersIndex.indexOf(e.getValue()));
        }

        // FRIENDS
        Set<Map.Entry<Long, List<String>>> friendsEntrySet = friendsByZone.entrySet();
        vDataOut.writeInt(friendsEntrySet.size());
        for (Map.Entry<Long, List<String>> e : friendsEntrySet) {
            vDataOut.writeLong(e.getKey());
            vDataOut.writeInt(e.getValue().size());
            for (String friend : e.getValue()) {
                vDataOut.writeInt(playersIndex.indexOf(friend));
            }
        }

        vDataOut.close();
    }

    public static boolean isSaveExist() {
        return getDataFile().exists();
    }

    public void setStateByZone(long zoneId, EZone.State state) {
        if (state != null) {
            stateByZone.put(zoneId, state);
        } else {
            stateByZone.remove(zoneId);
        }
    }

    public List<EZone> getZonesFromChunk(Vector2i min, Vector2i max) {
        List<EZone> containsZones = new ArrayList<>();
        Rectangle rec = new Rectangle(min.x - 1, min.y - 1, max.x - min.x + 2, max.y - min.y + 2);
        for (EZone z : zones.values()) {
            if (z.intersects(rec)) {
                containsZones.add(z);
            }
        }
        return containsZones.stream().distinct().collect(Collectors.toList());
    }

    public void setOwnerOfZone(String newOwner, long zoneId) {
        String prevOwner = ownerByZone.get(zoneId);
        if (newOwner != null) { // set
            ownerByZone.put(zoneId, newOwner);

            if (zonesByOwner.containsKey(newOwner)) {
                List<Long> zones = zonesByOwner.get(newOwner);
                if (!zones.contains(zoneId)) {
                    zones.add(zoneId);
                }
            } else {
                ArrayList<Long> zones = new ArrayList<>();
                zones.add(zoneId);
                zonesByOwner.put(newOwner, zones);
            }

            // remove prevOwner
            if (prevOwner != null) {
                List<Long> zones = zonesByOwner.get(prevOwner);
                if (zones != null) {
                    zones.remove(zoneId);
                    if (zones.isEmpty()) {
                        zonesByOwner.remove(prevOwner);
                    }
                }
            }

        } else { // remove
            ownerByZone.remove(zoneId);

            List<Long> zones = zonesByOwner.get(prevOwner);
            if (zones != null) {
                zones.remove(zoneId);
                if (zones.isEmpty()) {
                    zonesByOwner.remove(prevOwner);
                }
            }
        }
    }

    public void addFriendToZone(String p, long zoneId) {
        // playersByZone
        if (friendsByZone.containsKey(zoneId)) {
            List<String> players = friendsByZone.get(zoneId);
            if (!players.contains(p)) {
                players.add(p);
            }
        } else {
            ArrayList<String> players = new ArrayList<>();
            players.add(p);
            friendsByZone.put(zoneId, players);
        }
        // zonesByPlayer
        if (zonesByFriends.containsKey(p)) {
            List<Long> zones = zonesByFriends.get(p);
            if (!zones.contains(zoneId)) {
                zones.add(zoneId);
            }
        } else {
            ArrayList<Long> zones = new ArrayList<>();
            zones.add(zoneId);
            zonesByFriends.put(p, zones);
        }
    }

    public void removeFriendFromZone(String player, long zoneId) {
        // playersByZone
        if (friendsByZone.containsKey(zoneId)) {
            List<String> players = friendsByZone.get(zoneId);
            players.remove(player);
            if (players.isEmpty()) {
                friendsByZone.remove(zoneId);
            }
        }
        // zonesByPlayer
        if (zonesByFriends.containsKey(player)) {
            List<Long> zones = zonesByFriends.get(player);
            zones.remove(zoneId);
            if (zones.isEmpty()) {
                zonesByFriends.remove(player);
            }
        }
    }
/*
    public int removePlayerFromAllZones(String player) {
        int removed = 0;

        if (zonesByPlayer.containsKey(player)) {
            // playersByZone
            for (Long zoneId : zonesByPlayer.get(player)) {
                List<String> players = playersByZone.get(zoneId);
                players.remove(player);
                if (players.isEmpty()) {
                    playersByZone.remove(zoneId);
                }
            }

            // zonesByPlayer
            removed = zonesByPlayer.get(player).size();
            zonesByPlayer.remove(player);
        }
        return removed;
    }

    private boolean isZoneContainsPoint(EZone z, Vector2i pos) {
        return z.contains(pos.x, pos.y);
    }

    private void setTopLeft(LatLon topLeft) {
        this.topLeft = topLeft;
        this.topLeftPos = topLeft.transform();
    }

    private void setBottomRight(LatLon bottomRight) {
        this.bottomRight = bottomRight;
    }

    private void setZoom(double zoom) {
        this.zoom = zoom;
    }

    private void calculateZonesPolygons(Logger logger) {
        logger.info("Calculate pixels of all building zones geometry");
        int i = 0;
        for (EZone z : zones.values()) {
            if (z instanceof StaticZone) {
                StaticZone sz = (StaticZone) z;
                sz.createPolygon(this);
                sz.computePixels();
                if (i % 1000 == 0) {
                    logger.info("EZone " + i + "/" + zones.size());
                }
                i++;
            }
        }
    }

    public void claim(CommandSource src, long zoneId, String owner, Collection<String> friends) {
        setOwnerOfZone(owner, zoneId);
        for (String friend : friends) {
            addFriendToZone(friend, zoneId);
        }
        setStateByZone(zoneId, EZone.State.BUILDING);
        saveVolatileDataWithTextMessage(src, "Cette zone a été claim !");
    }

    private void unclaimWithoutSaving(long zoneId) {
        setOwnerOfZone(null, zoneId);
        List<String> friends = new ArrayList<>(getFriendsByZone(zoneId));
        for (String friend : friends) {
            removeFriendFromZone(friend, zoneId);
        }
        setStateByZone(zoneId, EZone.State.FINISH);
    }

    public void unclaim(CommandSource src, long zoneId) {
        unclaimWithoutSaving(zoneId);
        saveVolatileDataWithTextMessage(src, "Cette zone a été unclaim !");
    }

    public void unclaimAllOf(CommandSource src, String player) {
        List<Long> zoneIds = new ArrayList<>(getZonesByOwner(player));
        for (long zoneId : zoneIds) {
            unclaimWithoutSaving(zoneId);
        }
        saveVolatileDataWithTextMessage(src, "Unclaim " + zoneIds.size() + " zones of owner:" + player + " success !");
    }

    public void unclaim(CommandSource src, Vector3i pos, boolean admin) {
        List<EZone> zones = getZonesFromPoint(new Vector2i(pos.getX(), pos.getZ()));
        if (admin || zones.size() == 1) {
            for (EZone z : zones) {
                unclaimWithoutSaving(z.id);
            }
            saveVolatileDataWithTextMessage(src, admin ? zones.stream().map(z -> Long.toString(z.id)).collect(Collectors.joining(" ")) + " unclaim(s) !" : "Cette zone a été unclaim !");
        } else if (zones.size() == 0) {
            sendMessage(src, "Vous étes sur aucunes zones");
        } else {
            sendMessage(src, "Vous étes sur un coté commun à plusieurs zones ou à l' intérieur de plusieurs zones");
            sendMessage(src, "Aller sur les blocs de planches de bois ou changer de position");
        }
    }


    public void claim(CommandSource src, Vector3i pos, String owner, Collection<String> friends, boolean admin) {
        List<EZone> zones = getZonesFromPoint(new Vector2i(pos.getX(), pos.getZ()));
        if (admin || zones.size() == 1) {
            for (EZone zone : zones) {
                String currentOwner = getOwnerByZone(zone.id);
                boolean canClaim = false;
                if (currentOwner != null) {
                    if (currentOwner.equals(owner)) {
                        sendMessage(src, "Vous êtes déjà le propriétaire de cette zone !");
                    } else {
                        sendMessage(src, "EZone a déjà été claim !");
                    }
                } else {
                    canClaim = true;
                }
                if (canClaim) {
                    sendMessage(src, "Propriétaire : " + owner, TextColors.GOLD);
                    sendMessage(src, Text.builder(friends.size() + " Amis : ").color(TextColors.BLUE).append(Text.builder(String.join(", ", friends)).build()).build());
                    claim(src, zone.id, owner, friends);
                }

            }
        } else if (zones.size() == 0) {
            sendMessage(src, "Vous étes sur aucunes zones");
        } else {
            sendMessage(src, "Vous étes sur un coté commun à plusieurs zones ou à l' intérieur de plusieurs zones");
            sendMessage(src, "Aller sur les blocs de planches de bois ou changer de position");
        }
    }

    private void saveVolatileDataWithTextMessage(CommandSource src, String success) {
        try {
            saveVolatileData();
            sendMessage(src, success, TextColors.GREEN);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(src, "Error when saving volatile data !");
        }
    }

    public void addZone(CommandSource src, List<Vector2i> points, Iterable<Vector2D> pixels2D) {
        long zoneId = zones.keySet().stream().max(Long::compareTo).get() + 1;
        List<Vector2i> pixels = new ArrayList<>();
        for (Vector2D pos : pixels2D) {
            pixels.add(new Vector2i(pos.getBlockX(), pos.getBlockZ()));
        }
        AddedZone z = new AddedZone(zoneId, EZone.Type.ADDED, points, pixels);
        zones.put(z.id, z);
        try {
            sendMessage(src, "Save zones ...", TextColors.GREEN);
            saveOsmData(null);
            sendMessage(src, "Succesfuly zone(" + z.id + ") created !", TextColors.GREEN);
        } catch (Exception e) {
            sendMessage(src, "Error when saving !");
            e.printStackTrace();
        }
    }

    public void removeZone(CommandSource src, long zoneId) {
        setOwnerOfZone(null, zoneId);
        for (String friend : getFriendsByZone(zoneId)) {
            removeFriendFromZone(friend, zoneId);
        }
        setStateByZone(zoneId, null);

        zones.remove(zoneId);
        try {
            sendMessage(src, "Save zones ...", TextColors.GREEN);
            saveOsmData(null);
            saveVolatileData();
            sendMessage(src, "Succesfuly removed zone(" + zoneId + ") !", TextColors.GREEN);
        } catch (Exception e) {
            sendMessage(src, "Error when saving !");
            e.printStackTrace();
        }
    }

    protected void sendMessage(CommandSource src, String message) {
        sendMessage(src, Text.of(message));
    }

    protected void sendMessage(CommandSource src, String message, TextColor color) {
        sendMessage(src, Text.builder(message).color(color).build());
    }

    protected void sendMessage(CommandSource src, Text text) {
        src.sendMessage(text);
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

    public List<String> getModos() {
        return modos;
    }

    public List<Poi> getPois() {
        return pois;
    }

    public List<MapElement> getMapElements() {
        return mapElements;
    }
*/
}
