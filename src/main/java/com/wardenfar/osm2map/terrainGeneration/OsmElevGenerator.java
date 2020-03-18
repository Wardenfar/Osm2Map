package com.wardenfar.osm2map.terrainGeneration;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.vividsolutions.jts.geom.Point;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.config.BlocksConfig;
import com.wardenfar.osm2map.config.Config;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.*;
import com.wardenfar.osm2map.pluginInterface.Interface;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import java.awt.Rectangle;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class OsmElevGenerator extends ChunkGenerator {

    private Osm2map plugin;

    private boolean init = false;

    private SchemList trees = new SchemList();
    //private SchemList cars = new SchemList();
    //private SchemList fountains = new SchemList();

    //private List<Object> types;

    private Map<Object, Integer> maxYByBuffer = new HashMap<>();
    private Map<Object, Interface> interfaceByBuffer;

    public OsmElevGenerator(Osm2map plugin) {
        this.plugin = plugin;
        interfaceByBuffer = new HashMap<>();
    }

    public void init(com.wardenfar.osm2map.config.World world) {
        plugin.initWorld(world.name);
        if (!init) {
            init = true;
            if(plugin.getConfig(world.name).generation.treeSchematicsEnabled) {
                trees.addAllFiles(new File(plugin.getConfig(world.name).generation.treeSchematicsFolder), true);
            }
            //cars.addAllFilesRotateTag(new File("osm/schematic/car"));
            //fountains.addAllFiles(new File("osm/schematic/fountain"), false);
        }
    }


    @Override
    public ChunkData generateChunkData(World worldBukkit, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                biome.setBiome(x, z, Biome.JUNGLE);
            }
        }

        com.wardenfar.osm2map.config.World world = plugin.getWorlds().getWorld(worldBukkit.getName());

        if (world == null) {
            return null;
        } else if (!world.init) {
            init(world);
        }

        ChunkData chunkData = createChunkData(worldBukkit);

        Vector2i min = new Vector2i(chunkX * 16, chunkZ * 16);
        Vector2i max = min.copy().add(15);

        SuperChunkData superChunkData = new SuperChunkData(chunkData, min.copy().multiply(-1));
        interfaceByBuffer.put(superChunkData, plugin.getInterface());

        populate(world, superChunkData, min, max);
        return chunkData;
    }

    public Integer populate(String worldName, CuboidClipboard schem, Interface inter) {
        com.wardenfar.osm2map.config.World world = plugin.getWorlds().getWorld(worldName);
        interfaceByBuffer.put(schem, inter);
        Vector2i min = new Vector2i(schem.getOrigin().getBlockX(), schem.getOrigin().getBlockZ());
        Vector2i max = new Vector2i(min.x + schem.getWidth(), min.y + schem.getLength());
        populate(world, schem, min, max);
        return maxYByBuffer.get(schem);
    }

    public void populate(com.wardenfar.osm2map.config.World world, Object buffer, Vector2i min, Vector2i max) {
        init(world);

        /*Map<Object, Integer> percents = new HashMap<>();
        percents.put(interfaceByBuffer.get(buffer).getBlock("minecraft:air"), 60);
        percents.put(interfaceByBuffer.get(buffer).getBlock("minecraft:red_flower"), 10);
        percents.put(interfaceByBuffer.get(buffer).getBlock("minecraft:yellow_flower"), 10);

        types = new ArrayList<>();
        for (Map.Entry<Object, Integer> e : percents.entrySet()) {
            for (int i = 0; i < e.getValue(); i++) {
                types.add(e.getKey());
            }
        }*/

        DBMapData mapData = world.mapData;
        Config config = world.config;
        BlocksConfig blocks = config.blocks;
        Interface inter = interfaceByBuffer.get(buffer);

        Vector2d mapTL = mapData.project(mapData.getTopLeft());
        Vector2d mapBR = mapData.project(mapData.getBottomRight());

        Rectangle mapDataRect = new Rectangle(mapTL.getX(), mapTL.getY(), mapBR.getX() - mapTL.getX(), mapBR.getY() - mapTL.getY());
        if (!mapDataRect.contains(min.x, min.y) && !mapDataRect.contains(min.x, max.y) && !mapDataRect.contains(max.x, min.y) && !mapDataRect.contains(max.x, max.y)) {
            return;
        }

        Object wayBlock = Util.getConfigBlock(inter, blocks.wayBlock);
        Object wayBorderBlock = Util.getConfigBlock(inter, blocks.wayBorderBlock);
        Object wayTrackBlock = Util.getConfigBlock(inter, blocks.wayTrackBlock);

        Object buildingsBorderBlock = Util.getConfigBlock(inter, blocks.buildingsBorderBlock);
        Object buildingsCornerBlock = Util.getConfigBlock(inter, blocks.buildingsCornerBlock);
        Object buildingsFillBlock = Util.getConfigBlock(inter, blocks.buildingsFillBlock);
        /*Object buildingsRoofBlock = Util.getConfigBlock(inter, blocks.buildingsRoofBlock);
        Object buildingsActiveBlock = Util.getConfigBlock(inter, blocks.buildingsActiveBlock);*/

        Object gardensCornerBlock = Util.getConfigBlock(inter, blocks.gardensCornerBlock);
        Object gardensBorderBlock = Util.getConfigBlock(inter, blocks.gardensBorderBlock);

        Object grassBlock = Util.getConfigBlock(inter, blocks.groundTopBlock);
        Object dirtBlock = Util.getConfigBlock(inter, blocks.groundMiddleBlock);
        Object bedrockBlock = Util.getConfigBlock(inter, blocks.groundBottomBlock);

        Object waterWaysBlock = Util.getConfigBlock(inter, blocks.waterWaysBlock);

        List<JtsZone> zones = mapData.getZonesFromChunk(min, max);

        int dirtLayerSize = config.generation.dirtLayerSize;
        for (int x = min.x; x <= max.x; x++) {
            for (int z = min.y; z <= max.y; z++) {
                int y = plugin.getElevByBlockXZ(world.name, x, z);
                int yaStart = Math.max(0, y - dirtLayerSize - 2);
                setBlock(buffer, x, yaStart, z, bedrockBlock);
                setBlock(buffer, x, yaStart + 1, z, bedrockBlock);
                for (int ya = yaStart + 2; ya < y; ya++) {
                    setBlock(buffer, x, ya, z, dirtBlock);
                }

                Object block = null;
                for (JtsZone zone : zones) {
                    if (zone.type == JtsZone.Type.WATERWAY && zone.intersects(new Rectangle(x, z, 1, 1))) {
                        block = waterWaysBlock;
                        break;
                    }
                }

                if (block == null) {
                    if (config.tile.active) {
                        block = plugin.getSatelliteBlock(world.name, new Vector2i(x, z), inter);
                    } else {
                        block = grassBlock;
                    }
                }
                setBlock(buffer, x, y, z, block);
            }
        }

        //int maxBuffer = Math.max(trees.getBuffer(), fountains.getBuffer());
        if(config.generation.treeSchematicsEnabled) {
            int maxBuffer = trees.getBuffer();
            List<JtsMapElement> mapElements = mapData.getMapElementsFromChunk(min.copy().sub(maxBuffer), max.copy().add(maxBuffer));
            for (JtsMapElement me : mapElements) {
                if (me.type.equals("tree") && !trees.schems.isEmpty()) {
                    if (me.geometry instanceof Point) {
                        Point p = (Point) me.geometry;
                        pasteTree(world, buffer, config.generation.seed, (int) p.getX(), (int) p.getY());
                    }
                }/* else if (me.type.equals("fountain") && !fountains.schems.isEmpty()) {
                Point centroid = me.geometry.getCentroid();
                Vector3i center = new Vector3i(centroid.getX(), plugin.getElevByBlockXZ(world.name, (int) centroid.getX(), (int) centroid.getY()) + 1, (int) centroid.getY());
                SuperSchematic fountain = fountains.schems.get(0);
                Vector3i origin = center.sub(fountain.getWidth() / 2, 0, fountain.getLength() / 2);
                pasteSchem(buffer, fountain, origin);
            }*/
            }
        }

        List<String> typesIgnored = Arrays.asList("footway");
        List<JtsWay> ways = mapData.getWaysFromChunk(min, max);
        ways = ways.stream().filter(w -> !typesIgnored.contains(w.type)).collect(Collectors.toList());
        for (JtsWay way : ways) {
            way.computePixels(min.sub(2), max.add(2));
        }
        for (JtsWay way : ways) {
            Object block = wayBlock;
            if (way.type.equals("track")) {
                block = wayTrackBlock;
            }
            for (Vector2i p : way.pixels) {
                setBlockWithElevation(world, buffer, p.x, 0, p.y, block);
            }
            for (Vector2i p : way.linePixels) {
                boolean set = true;
                for (JtsWay w2 : ways) {
                    if (way != w2 && w2.pixels.contains(p)) {
                        set = false;
                        break;
                    }
                }
                if (set) {
                    setBlockWithElevation(world, buffer, p.x, 1, p.y, wayBorderBlock);
                }
            }
            /*if (way.thickness >= 3) {
                List<Coordinate> coords = Arrays.asList(way.geometry.getCoordinates());
                for (int i = 0; i < coords.size() - 1; i++) {
                    Coordinate c1 = coords.get(i);
                    Coordinate c2 = coords.get(i + 1);
                    LineSegment segment = new LineSegment(c1, c2);
                    double angle = segment.angle() * 180 / Math.PI;
                    int roundAngle = (int) (Math.round(angle / 90d) * 90);
                    if (segment.getLength() > 30 && Math.abs(angle - roundAngle) < 10) {
                        Coordinate midPoint = segment.midPoint();
                        Random random = new Random(Objects.hash(config.generation.seed, midPoint.x, midPoint.y));
                        random.nextLong();
                        Vector3i center = new Vector3i(midPoint.x, plugin.getElevByBlockXZ(world.name, (int) midPoint.x, (int) midPoint.y) + 1, (int) midPoint.y);
                        float rf = random.nextFloat();
                        int index = Math.round(rf * (cars.schems.size() - 1));
                        SuperSchematic car = cars.schems.get(index).rotate(roundAngle);
                        Vector3i origin = center.sub(car.getWidth() / 2, 0, car.getLength() / 2);
                        pasteSchem(buffer, car, origin);
                    }
                }
            }*/
        }

        Random random = new Random();
        for (JtsZone z : zones) {
            if (z.type == JtsZone.Type.BUILDING) {
                z.computePixels(min, max);
                int height = Util.getAverageHeight(plugin, world.name, z.getPoints());
                fillPolygon(buffer, buildingsFillBlock, height, z, true);
                drawPolygon(buffer, buildingsBorderBlock, buildingsCornerBlock, height + 1, z.getLinePixels(), z.getPoints(), true);
                /*if (config.building.active) {
                    Float elev = plugin.getHeightBuilding(world.name, z.geometry);
                    int buildingHeight = (int) (config.building.levelHeight * config.building.defaultLevels / config.generation.zoom);
                    if (elev != null) {
                        buildingHeight = (int) (elev / config.generation.zoom);
                    }
                    for (int i = 1; i < buildingHeight; i++) {
                        drawPolygon(buffer, buildingsActiveBlock, null, height + i, z.getLinePixels(), new ArrayList<>(), true);
                    }
                    fillPolygon(buffer, buildingsRoofBlock, height + buildingHeight - 1, z, false);
                }*/
            } else if (z.type == JtsZone.Type.GARDEN) {
                z.computePixels(min, max);

                for (Vector2i p : z.getLinePixels()) {
                    setBlockWithElevation(world, buffer, p.x, 1, p.y, gardensBorderBlock);
                }
                for (Vector2i p : z.getPoints()) {
                    setBlockWithElevation(world, buffer, p.x, 1, p.y, gardensCornerBlock);
                }
            } else if (z.type == JtsZone.Type.FOREST) {
                /*Geometry area = z.geometry.intersection(Util.geometryFromRectCorners(min.x, min.y, max.x, max.y));
                if (!area.isEmpty()) {
                    Rectangle bbox = Util.bbox(area);
                    if (bbox.width > 1 && bbox.height > 1) {
                        PoissonDiskSampler sampler = new PoissonDiskSampler(new Random(config.generation.seed), bbox.x, bbox.y, bbox.x + bbox.width, bbox.y + bbox.height, trees.getBuffer(), new RealFunction2DDouble() {
                            @Override
                            public double getDouble(double x, double y) {
                                return 1;
                            }
                        }, (max.x - min.x) * (max.y - min.y) / (trees.getBuffer() * trees.getBuffer()));
                        List<com.flowpowered.math.vector.Vector2d> points = sampler.sample();
                        for (com.flowpowered.math.vector.Vector2d p : points) {
                            Geometry point = Util.geometryFromPoint(p.getFloorX(), p.getFloorY());
                            if (z.geometry.contains(point) && area.contains(point)) {
                                boolean paste = true;
                                for (JtsZone z2 : zones) {
                                    if (z == z2 || z2.type == JtsZone.Type.GARDEN || z2.type == JtsZone.Type.FOREST) {
                                        continue;
                                    }
                                    if (Util.isPointWithinDistance(z2.geometry, p.getFloorX(), p.getFloorY(), (float) trees.getBuffer() / 2f)) {
                                        paste = false;
                                        break;
                                    }
                                }
                                if (paste) {
                                    for (JtsWay way : ways) {
                                        if (Util.isPointWithinDistance(way.getBufferedGeometry(), p.getFloorX(), p.getFloorY(), (float) trees.getBuffer() / 2f)) {
                                            paste = false;
                                            break;
                                        }
                                    }
                                }
                                if (paste) {
                                    pasteTree(world, buffer, config.generation.seed, p.getFloorX(), p.getFloorY());
                                }
                            }
                        }
                    }
                }*/
            }
        }
    }

    private void pasteTree(com.wardenfar.osm2map.config.World world, Object buffer, long seed, int x, int y) {
        Vector3i center = new Vector3i(x, plugin.getElevByBlockXZ(world.name, x, y) + 1, y);
        Random random = new Random(Objects.hash(seed, x, y));
        random.nextLong();
        float r = random.nextFloat();
        int index = Math.round(r * (trees.schems.size() - 1));
        SuperSchematic tree = trees.schems.get(index);
        Vector3i origin = center.sub(tree.getWidth() / 2, 0, tree.getLength() / 2);
        pasteSchem(buffer, tree, origin);
    }

    private void pasteSchem(Object buffer, SuperSchematic schem, Vector3i offset) {
        for (int x = 0; x < schem.getWidth(); x++) {
            for (int z = 0; z < schem.getLength(); z++) {
                for (int y = 0; y < schem.getHeight(); y++) {
                    BaseBlock block = schem.getBlock(new Vector(x, y, z));
                    if (block != null) {
                        Object blockObject = interfaceByBuffer.get(buffer).getBlock(block.getId(), block.getData());
                        setBlockReplaceAir(buffer, offset.getX() + x, offset.getY() + y, offset.getZ() + z, blockObject);
                    }
                }
            }
        }
    }

    private void fillBelow(Object buffer, int height, List<Vector2i> pixels, Object block) {
        for (Vector2i p : pixels) {
            for (int i = height - 1; i >= 0; i--) {
                Integer id = interfaceByBuffer.get(buffer).getBlockId(getBlock(buffer, p.x, i, p.y));
                if (id != null && id.equals(0)) { // 0 = air
                    setBlock(buffer, p.x, i, p.y, block);
                } else {
                    break;
                }
            }
        }
    }

    private void cleanAbove(Object buffer, int height, List<Vector2i> pixels, Object cleanBlock) {
        for (Vector2i p : pixels) {
            for (int i = height + 1; i < 256; i++) {
                Integer id = interfaceByBuffer.get(buffer).getBlockId(getBlock(buffer, p.x, i, p.y));
                if (id != null && !id.equals(0)) { // 0 = air
                    setBlock(buffer, p.x, i, p.y, cleanBlock);
                } else {
                    break;
                }
            }
        }
    }


    private void fillPolygon(Object buffer, Object block, int height, JtsZone zone, boolean fillAndClean) {
        List<Vector2i> pixels = zone.getPixels();
        for (Vector2i p : pixels) {
            setBlock(buffer, p.x, height, p.y, block);
        }
        if (fillAndClean) {
            fillBelow(buffer, height, pixels, interfaceByBuffer.get(buffer).getBlock("minecraft:dirt"));
            cleanAbove(buffer, height, pixels, interfaceByBuffer.get(buffer).getBlock("minecraft:air"));
        }
    }

    private void drawPolygon(Object buffer, Object block, Object corner, int height, List<Vector2i> linePixels, List<Vector2i> points, boolean fillAndClean) {
        for (Vector2i pixels : linePixels) {
            setBlock(buffer, pixels.x, height, pixels.y, block);
        }
        for (Vector2i pixels : points) {
            setBlock(buffer, pixels.x, height, pixels.y, corner);
        }
        if (fillAndClean) {
            fillBelow(buffer, height, linePixels, interfaceByBuffer.get(buffer).getBlock("minecraft:dirt"));
            cleanAbove(buffer, height, linePixels, interfaceByBuffer.get(buffer).getBlock("minecraft:air"));
        }
    }

    private void setBlockWithElevationReplaceAir(com.wardenfar.osm2map.config.World world, Object buffer, int x, int offset, int z, Object block) {
        int y = plugin.getElevByBlockXZ(world.name, x, z);
        if (y != -1) {
            Vector3i pos = new Vector3i(x, y + offset, z);
            Object currentBlock = getBlock(buffer, pos.getX(), pos.getY(), pos.getZ());
            if (currentBlock != null && !interfaceByBuffer.get(buffer).getBlockId(currentBlock).equals(0)) {
                return;
            }
            setBlock(buffer, pos.getX(), pos.getY(), pos.getZ(), block);
        }
    }

    private void setBlockWithElevation(com.wardenfar.osm2map.config.World world, Object buffer, int x, int offset, int z, Object block) {
        int y = plugin.getElevByBlockXZ(world.name, x, z);
        if (y != -1) {
            Vector3i pos = new Vector3i(x, y + offset, z);
            setBlock(buffer, pos.getX(), pos.getY(), pos.getZ(), block);
        }
    }

    private Object getBlock(Object o, int x, int y, int z) {
        if (o instanceof SuperChunkData) {
            SuperChunkData buffer = (SuperChunkData) o;
            x += buffer.offset.x;
            z += buffer.offset.y;
            if (0 <= x && 0 <= z && 0 <= y && x <= 15 && z <= 15 && y <= 255) {
                return buffer.data.getTypeAndData(x, y, z);
            }
        }
        if (o instanceof CuboidClipboard) {
            CuboidClipboard buffer = (CuboidClipboard) o;
            x -= buffer.getOrigin().getBlockX();
            z -= buffer.getOrigin().getBlockZ();
            if (0 <= x && 0 <= z && 0 <= y && x < buffer.getWidth() && z < buffer.getLength() && y < buffer.getHeight()) {
                return buffer.getBlock(new Vector(x, y, z));
            }
        }
        return null;
    }


    private void setBlockReplaceAir(Object buffer, int x, int y, int z, Object block) {
        Object currentBlock = getBlock(buffer, x, y, z);
        if (currentBlock != null && !interfaceByBuffer.get(buffer).getBlockId(currentBlock).equals(0)) {
            return;
        }
        setBlock(buffer, x, y, z, block);
    }

    private void setBlock(Object o, int x, int y, int z, Object block) {
        boolean placed = false;
        if (o instanceof SuperChunkData) {
            SuperChunkData buffer = (SuperChunkData) o;
            x += buffer.offset.x;
            z += buffer.offset.y;
            if (0 <= x && 0 <= z && 0 <= y && x <= 15 && z <= 15 && y <= 255) {
                buffer.data.setBlock(x, y, z, (MaterialData) block);
                placed = true;
            }
        }
        if (o instanceof CuboidClipboard) {
            CuboidClipboard buffer = (CuboidClipboard) o;
            x -= buffer.getOrigin().getBlockX();
            z -= buffer.getOrigin().getBlockZ();
            if (0 <= x && 0 <= z && 0 <= y && x < buffer.getWidth() && z < buffer.getLength() && y < buffer.getHeight()) {
                buffer.setBlock(new Vector(x, y, z), (BaseBlock) block);
                placed = true;
            }
        }
        if (placed) {
            int currentMaxY = maxYByBuffer.getOrDefault(o, -1);
            if (y > currentMaxY) {
                maxYByBuffer.put(o, y);
            }
        }
    }
}
