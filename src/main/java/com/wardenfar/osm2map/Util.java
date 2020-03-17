package com.wardenfar.osm2map;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wardenfar.osm2map.config.Worlds;
import com.wardenfar.osm2map.map.entity.Vector2d;
import com.wardenfar.osm2map.map.entity.Vector2i;
import com.wardenfar.osm2map.pluginInterface.Interface;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.locationtech.jts.geom.Polygon;

import java.awt.Rectangle;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Util {

    public static GeometryFactory geometryFactory = new GeometryFactory();
    public static org.locationtech.jts.geom.GeometryFactory geometryFactoryLt = new org.locationtech.jts.geom.GeometryFactory();

    public static TextComponent builder(String message) {
        return new TextComponent(message);
    }

    public static TextComponent builder(TextComponent... texts) {
        return new TextComponent(texts);
    }

    public static TextComponent builder(String message, ChatColor color) {
        TextComponent msg = new TextComponent(message);
        msg.setColor(color);
        return msg;
    }

    public static void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, builder(message));
    }

    public static void sendMessage(CommandSender sender, String message, ChatColor color) {
        sendMessage(sender, builder(message, color));
    }

    public static void sendMessage(CommandSender sender, TextComponent msg) {
        sender.spigot().sendMessage(msg);
    }

    public static Vector3d toVector(Location location) {
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    public static Object getConfigBlock(Interface inter, String block) {
        Object result = inter.getBlock(block.endsWith(":0") ? block.substring(0, block.length() - 2) : block);
        if (result == null) {
            if (Pattern.matches("[0-9]+(:[01]?[0-9])?", block)) {
                String[] args = block.split(":");
                if (args.length == 1) {
                    return inter.getBlock(Integer.parseInt(args[0]));
                } else if (args.length == 2) {
                    return inter.getBlock(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                } else {
                    System.out.println(block + " invalid !");
                    return null;
                }
            } else {
                System.out.println(block + " invalid !");
                return null;
            }
        } else {
            return result;
        }
    }

    public static List<String> getResourceFiles(ClassLoader classLoader, String path) throws IOException {
        List<String> filenames = new ArrayList<>();
        try (InputStream in = classLoader.getResourceAsStream(path); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filenames;
    }

    public static List<Vector2i> getHollowed(List<Vector2i> vset) {
        java.util.List<Vector2i> returnset = new ArrayList<>();
        for (Vector2i v : vset) {
            int x = v.x, y = v.y;
            if (!(vset.contains(new Vector2i(x + 1, y)) &&
                    vset.contains(new Vector2i(x - 1, y)) &&
                    vset.contains(new Vector2i(x, y + 1)) &&
                    vset.contains(new Vector2i(x, y - 1)))) {
                returnset.add(v);
            }
        }
        return returnset;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static CuboidClipboard cloneCuboidClipboard(CuboidClipboard clip) {
        CuboidClipboard result = new CuboidClipboard(clip.getSize(), clip.getOrigin(), clip.getOffset());
        for (int x = 0; x < clip.getWidth(); x++) {
            for (int z = 0; z < clip.getLength(); z++) {
                for (int y = 0; y < clip.getHeight(); y++) {
                    BaseBlock block = clip.getBlock(new Vector(x, y, z));
                    if (block != null) {
                        result.setBlock(new Vector(x, y, z), block);
                    }
                }
            }
        }
        return result;
    }

    public static String getFileNameWithoutExtension(File file) {
        String fileName = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }

        return fileName;

    }

    public static Worlds readOrCreateWorlds(String configFile) throws Exception {
        return readOrCreateWorldsJson(configFile);
    }

    public static Worlds readOrCreateWorldsJson(String configFile) throws Exception {
        Worlds worlds;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(configFile);
        if (file.exists()) {
            try{
                worlds = gson.fromJson(new FileReader(file), Worlds.class);
            }catch (Exception e){
                worlds = null;
            }
            if(worlds == null){
                worlds = new Worlds();
            }
            for (com.wardenfar.osm2map.config.World w : worlds.worlds) {
                w.forceInitSave = w.forceInit;
                w.forceInit = false;
            }
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(worlds, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            worlds = new Worlds();
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(worlds, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return worlds;
    }

    /*public static Worlds readOrCreateWorldsYaml(String configFile) throws Exception {
        Worlds worlds;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File file = new File(configFile);
        if (file.exists()) {
            worlds = mapper.readValue(file, Worlds.class);
            for (com.wardenfar.osm2map.config.World w : worlds.worlds) {
                w.forceInitSave = w.forceInit;
                w.forceInit = false;
            }
            mapper.writeValue(file, worlds);
        } else {
            worlds = new Worlds();
            file.getParentFile().mkdirs();
            file.createNewFile();
            mapper.writeValue(file, worlds);
        }
        return worlds;
    }*/

    public static org.locationtech.jts.geom.Geometry convertGeometry(Geometry g) {
        try {
            return new org.locationtech.jts.io.WKTReader().read(toViWkt(g));
        } catch (org.locationtech.jts.io.ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<?>[] computePixels(Vector2i min, Vector2i max, Geometry g, boolean computeLines) {
        org.locationtech.jts.geom.Geometry geometry = convertGeometry(g);
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        if (min == null || max == null) {
            for (Coordinate c : g.getBoundary().getCoordinates()) {
                minX = (int) Math.min(minX, c.x);
                maxX = (int) Math.max(maxX, c.x);
                minY = (int) Math.min(minY, c.y);
                maxY = (int) Math.max(maxY, c.y);
            }
        } else {
            minX = min.x;
            maxX = max.x;
            minY = min.y;
            maxY = max.y;
        }

        List<Vector2i> pixels = new ArrayList<>();
        List<Vector2i> linesPixels = new ArrayList<>();

        for (int y = minY; y <= maxY; y++) {
            try {
                org.locationtech.jts.geom.Geometry line = geometryFactoryLt.createLineString(new org.locationtech.jts.geom.Coordinate[]{new org.locationtech.jts.geom.Coordinate(minX - 1, y), new org.locationtech.jts.geom.Coordinate(maxX + 1, y)});
                org.locationtech.jts.geom.Geometry inter = geometry.intersection(line);
                for (int i = 0; i < inter.getNumGeometries(); i++) {
                    org.locationtech.jts.geom.Geometry l = inter.getGeometryN(i);
                    Rectangle bbox = bboxLt(l);
                    for (int x = bbox.x; x <= bbox.x + bbox.width; x++) {
                        Vector2i v = new Vector2i(x, y);
                        pixels.add(v);
                    }
                }
                if (computeLines) {
                    org.locationtech.jts.geom.Coordinate[] coords = inter.getCoordinates();
                    for (org.locationtech.jts.geom.Coordinate c : coords) {
                        Vector2i v = new Vector2i((int) c.x, (int) c.y);
                        if (minX <= v.x && minY <= v.y && v.x <= maxX && v.y <= maxY && !linesPixels.contains(v)) {
                            linesPixels.add(v);
                            if (!pixels.contains(v)) {
                                pixels.add(v);
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        if (computeLines) {
            for (int x = minX; x <= maxX; x++) {
                try {
                    org.locationtech.jts.geom.Geometry line = geometryFactoryLt.createLineString(new org.locationtech.jts.geom.Coordinate[]{new org.locationtech.jts.geom.Coordinate(x, minY - 1), new org.locationtech.jts.geom.Coordinate(x, maxY + 1)});
                    org.locationtech.jts.geom.Geometry inter = geometry.intersection(line);
                    org.locationtech.jts.geom.Coordinate[] coords = inter.getCoordinates();
                    for (org.locationtech.jts.geom.Coordinate c : coords) {
                        Vector2i v = new Vector2i((int) c.x, (int) c.y);
                        if (minX <= v.x && minY <= v.y && v.x <= maxX && v.y <= maxY && !linesPixels.contains(v)) {
                            linesPixels.add(v);
                            if (!pixels.contains(v)) {
                                pixels.add(v);
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
        return new List[]{pixels, linesPixels};
    }

    public static Geometry geometryFromRectangle(double x, double y, double width, double height) {
        return geometryFactory.createPolygon(new com.vividsolutions.jts.geom.Coordinate[]{
                new com.vividsolutions.jts.geom.Coordinate(x, y),
                new com.vividsolutions.jts.geom.Coordinate(x + width, y),
                new com.vividsolutions.jts.geom.Coordinate(x + width, y + height),
                new com.vividsolutions.jts.geom.Coordinate(x, y + height),
                new com.vividsolutions.jts.geom.Coordinate(x, y),
        });
    }

    public static Geometry geometryFromPoint(double x, double y) {
        return geometryFactory.createPoint(new com.vividsolutions.jts.geom.Coordinate(x, y));
    }

    public static Geometry geometryFromRectCorners(double minX, double minY, double maxX, double maxY) {
        return geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(minX, minY),
                new Coordinate(maxX, minY),
                new Coordinate(maxX, maxY),
                new Coordinate(minX, maxY),
                new Coordinate(minX, minY),
        });
    }

    public static Polygon geometryFromRectangleLt(double x, double y, double width, double height) {
        return geometryFactoryLt.createPolygon(new org.locationtech.jts.geom.Coordinate[]{
                new org.locationtech.jts.geom.Coordinate(x, y),
                new org.locationtech.jts.geom.Coordinate(x + width, y),
                new org.locationtech.jts.geom.Coordinate(x + width, y + height),
                new org.locationtech.jts.geom.Coordinate(x, y + height),
                new org.locationtech.jts.geom.Coordinate(x, y),
        });
    }

    public static boolean intersectsRect(Geometry geometry, double x, double y, double width, double height) {
        if (width == 1 && height == 1) {
            Vector2i vector = new Vector2d(x, y).toInt();
            return computePixels(vector, vector, geometry, true)[0].contains(vector);
        } else {
            return geometry.intersects(geometryFromRectangle(x, y, width, height));
        }
    }

    public static boolean isWithinDistance(com.vividsolutions.jts.geom.Geometry
                                                   a, com.vividsolutions.jts.geom.Geometry b, double distance) {
        return a.isWithinDistance(b, distance);
    }

    public static boolean isPointWithinDistance(com.vividsolutions.jts.geom.Geometry a, double x, double y,
                                                double distance) {
        return a.isWithinDistance(geometryFactory.createPoint(new Coordinate(x, y)), distance);
    }

    public static com.vividsolutions.jts.geom.Geometry buffer(com.vividsolutions.jts.geom.Geometry geometry,
                                                              double distance) {
        return geometry.buffer(distance);
    }

    public static boolean intersects(com.vividsolutions.jts.geom.Geometry a, com.vividsolutions.jts.geom.Geometry b) {
        return a.intersects(b);
    }

    public static String toViWkt(com.vividsolutions.jts.geom.Geometry geometry) {
        return new com.vividsolutions.jts.io.WKTWriter().write(geometry);
    }

    public static com.vividsolutions.jts.geom.Geometry fromViWkt(String wkt) {
        try {
            return new com.vividsolutions.jts.io.WKTReader().read(wkt);
        } catch (com.vividsolutions.jts.io.ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getAverageHeight(Osm2map plugin, String worldName, List<Vector2i> pixels) {
        int total = pixels.stream().map(v -> plugin.getElevByBlockXZ(worldName, v.x, v.y)).reduce(Integer::sum).orElse(0);
        return Math.round((float) total / (float) pixels.size());
    }

    public static Map<String, String> blockMap = new HashMap<>();

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static Vector3d correctPosByWorldBorders(World world, Vector3d prev) {
        Vector3d pos = new Vector3d(prev);
        Location center = world.getWorldBorder().getCenter();
        double size = world.getWorldBorder().getSize() / 2;
        pos = pos.max(center.getX() - size, pos.getY(), center.getZ() - size);
        pos = pos.min(center.getX() + size, pos.getY(), center.getZ() + size);
        return pos;
    }

    public static Map<Vector3i, Object> getBlocksColors(String externFile, Interface inter) {
        Map<Vector3i, Object> blocks = new HashMap<>();
        try {
            File groundBlocksFile = new File(externFile);
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(groundBlocksFile)));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.split("#")[0].trim();
                if (line.equals("")) {
                    continue;
                }
                String[] args = line.split(" ");
                String block = args[0];
                String[] channels = args[1].split(",");

                Object blockObject = Util.getConfigBlock(inter, block);

                if (blockObject == null) {
                    System.err.println(line);
                }else{
                    blocks.put(new Vector3i(Integer.parseInt(channels[0]), Integer.parseInt(channels[1]), Integer.parseInt(channels[2])), blockObject);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blocks;
    }

    public static Integer getBlockId(String strId) {
        String id = blockMap.get(strId);
        return id == null ? null : Integer.parseInt(id.split(":")[0]);
    }

    public static Integer getVariantId(String strId) {
        String id = blockMap.get(strId);
        if (id != null) {
            String[] split = id.split(":");
            if (split.length > 1) {
                return Integer.parseInt(split[1]);
            }
        }
        return 0;
    }

    public static Vector2i getTile(final double lat, final double lon, final int zoom) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);
        return new Vector2i(xtile, ytile);
    }

    public static Vector2d getLatLonFromTile(final int x, final int y, final int zoom) {
        double lon = (x / Math.pow(2, zoom) * 360 - 180);
        double n = Math.PI - 2 * Math.PI * y / Math.pow(2, zoom);
        double lat = (180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))));
        return new Vector2d(lat, lon);

    }

    public static int lerpInt(int a, int b, float f) {
        return a + (int) (f * (float) (b - a));
    }

    public static void createMap() {
        blockMap.put("minecraft:air", "0");
        blockMap.put("minecraft:stone", "1");
        blockMap.put("minecraft:stone:1", "1:1");
        blockMap.put("minecraft:stone:2", "1:2");
        blockMap.put("minecraft:stone:3", "1:3");
        blockMap.put("minecraft:stone:4", "1:4");
        blockMap.put("minecraft:stone:5", "1:5");
        blockMap.put("minecraft:stone:6", "1:6");
        blockMap.put("minecraft:grass", "2");
        blockMap.put("minecraft:dirt", "3");
        blockMap.put("minecraft:dirt:1", "3:1");
        blockMap.put("minecraft:dirt:2", "3:2");
        blockMap.put("minecraft:cobblestone", "4");
        blockMap.put("minecraft:planks", "5");
        blockMap.put("minecraft:planks:1", "5:1");
        blockMap.put("minecraft:planks:2", "5:2");
        blockMap.put("minecraft:planks:3", "5:3");
        blockMap.put("minecraft:planks:4", "5:4");
        blockMap.put("minecraft:planks:5", "5:5");
        blockMap.put("minecraft:sapling", "6");
        blockMap.put("minecraft:sapling:1", "6:1");
        blockMap.put("minecraft:sapling:2", "6:2");
        blockMap.put("minecraft:sapling:3", "6:3");
        blockMap.put("minecraft:sapling:4", "6:4");
        blockMap.put("minecraft:sapling:5", "6:5");
        blockMap.put("minecraft:bedrock", "7");
        blockMap.put("minecraft:flowing_water", "8");
        blockMap.put("minecraft:water", "9");
        blockMap.put("minecraft:flowing_lava", "10");
        blockMap.put("minecraft:lava", "11");
        blockMap.put("minecraft:sand", "12");
        blockMap.put("minecraft:sand:1", "12:1");
        blockMap.put("minecraft:gravel", "13");
        blockMap.put("minecraft:gold_ore", "14");
        blockMap.put("minecraft:iron_ore", "15");
        blockMap.put("minecraft:coal_ore", "16");
        blockMap.put("minecraft:log", "17");
        blockMap.put("minecraft:log:1", "17:1");
        blockMap.put("minecraft:log:2", "17:2");
        blockMap.put("minecraft:log:3", "17:3");
        blockMap.put("minecraft:leaves", "18");
        blockMap.put("minecraft:leaves:1", "18:1");
        blockMap.put("minecraft:leaves:2", "18:2");
        blockMap.put("minecraft:leaves:3", "18:3");
        blockMap.put("minecraft:sponge", "19");
        blockMap.put("minecraft:sponge:1", "19:1");
        blockMap.put("minecraft:glass", "20");
        blockMap.put("minecraft:lapis_ore", "21");
        blockMap.put("minecraft:lapis_block", "22");
        blockMap.put("minecraft:dispenser", "23");
        blockMap.put("minecraft:sandstone", "24");
        blockMap.put("minecraft:sandstone:1", "24:1");
        blockMap.put("minecraft:sandstone:2", "24:2");
        blockMap.put("minecraft:noteblock", "25");
        blockMap.put("minecraft:bed", "26");
        blockMap.put("minecraft:golden_rail", "27");
        blockMap.put("minecraft:detector_rail", "28");
        blockMap.put("minecraft:sticky_piston", "29");
        blockMap.put("minecraft:web", "30");
        blockMap.put("minecraft:tallgrass", "31");
        blockMap.put("minecraft:tallgrass:1", "31:1");
        blockMap.put("minecraft:tallgrass:2", "31:2");
        blockMap.put("minecraft:deadbush", "32");
        blockMap.put("minecraft:piston", "33");
        blockMap.put("minecraft:piston_head", "34");
        blockMap.put("minecraft:wool", "35");
        blockMap.put("minecraft:wool:1", "35:1");
        blockMap.put("minecraft:wool:2", "35:2");
        blockMap.put("minecraft:wool:3", "35:3");
        blockMap.put("minecraft:wool:4", "35:4");
        blockMap.put("minecraft:wool:5", "35:5");
        blockMap.put("minecraft:wool:6", "35:6");
        blockMap.put("minecraft:wool:7", "35:7");
        blockMap.put("minecraft:wool:8", "35:8");
        blockMap.put("minecraft:wool:9", "35:9");
        blockMap.put("minecraft:wool:10", "35:10");
        blockMap.put("minecraft:wool:11", "35:11");
        blockMap.put("minecraft:wool:12", "35:12");
        blockMap.put("minecraft:wool:13", "35:13");
        blockMap.put("minecraft:wool:14", "35:14");
        blockMap.put("minecraft:wool:15", "35:15");
        blockMap.put("minecraft:yellow_flower", "37");
        blockMap.put("minecraft:red_flower", "38");
        blockMap.put("minecraft:red_flower:1", "38:1");
        blockMap.put("minecraft:red_flower:2", "38:2");
        blockMap.put("minecraft:red_flower:3", "38:3");
        blockMap.put("minecraft:red_flower:4", "38:4");
        blockMap.put("minecraft:red_flower:5", "38:5");
        blockMap.put("minecraft:red_flower:6", "38:6");
        blockMap.put("minecraft:red_flower:7", "38:7");
        blockMap.put("minecraft:red_flower:8", "38:8");
        blockMap.put("minecraft:brown_mushroom", "39");
        blockMap.put("minecraft:red_mushroom", "40");
        blockMap.put("minecraft:gold_block", "41");
        blockMap.put("minecraft:iron_block", "42");
        blockMap.put("minecraft:double_stone_slab", "43");
        blockMap.put("minecraft:double_stone_slab:1", "43:1");
        blockMap.put("minecraft:double_stone_slab:2", "43:2");
        blockMap.put("minecraft:double_stone_slab:3", "43:3");
        blockMap.put("minecraft:double_stone_slab:4", "43:4");
        blockMap.put("minecraft:double_stone_slab:5", "43:5");
        blockMap.put("minecraft:double_stone_slab:6", "43:6");
        blockMap.put("minecraft:double_stone_slab:7", "43:7");
        blockMap.put("minecraft:stone_slab", "44");
        blockMap.put("minecraft:stone_slab:1", "44:1");
        blockMap.put("minecraft:stone_slab:2", "44:2");
        blockMap.put("minecraft:stone_slab:3", "44:3");
        blockMap.put("minecraft:stone_slab:4", "44:4");
        blockMap.put("minecraft:stone_slab:5", "44:5");
        blockMap.put("minecraft:stone_slab:6", "44:6");
        blockMap.put("minecraft:stone_slab:7", "44:7");
        blockMap.put("minecraft:brick_block", "45");
        blockMap.put("minecraft:tnt", "46");
        blockMap.put("minecraft:bookshelf", "47");
        blockMap.put("minecraft:mossy_cobblestone", "48");
        blockMap.put("minecraft:obsidian", "49");
        blockMap.put("minecraft:torch", "50");
        blockMap.put("minecraft:fire", "51");
        blockMap.put("minecraft:mob_spawner", "52");
        blockMap.put("minecraft:oak_stairs", "53");
        blockMap.put("minecraft:chest", "54");
        blockMap.put("minecraft:redstone_wire", "55");
        blockMap.put("minecraft:diamond_ore", "56");
        blockMap.put("minecraft:diamond_block", "57");
        blockMap.put("minecraft:crafting_table", "58");
        blockMap.put("minecraft:wheat", "59");
        blockMap.put("minecraft:farmland", "60");
        blockMap.put("minecraft:furnace", "61");
        blockMap.put("minecraft:lit_furnace", "62");
        blockMap.put("minecraft:standing_sign", "63");
        blockMap.put("minecraft:wooden_door", "64");
        blockMap.put("minecraft:ladder", "65");
        blockMap.put("minecraft:rail", "66");
        blockMap.put("minecraft:stone_stairs", "67");
        blockMap.put("minecraft:wall_sign", "68");
        blockMap.put("minecraft:lever", "69");
        blockMap.put("minecraft:stone_pressure_plate", "70");
        blockMap.put("minecraft:iron_door", "71");
        blockMap.put("minecraft:wooden_pressure_plate", "72");
        blockMap.put("minecraft:redstone_ore", "73");
        blockMap.put("minecraft:lit_redstone_ore", "74");
        blockMap.put("minecraft:unlit_redstone_torch", "75");
        blockMap.put("minecraft:redstone_torch", "76");
        blockMap.put("minecraft:stone_button", "77");
        blockMap.put("minecraft:snow_layer", "78");
        blockMap.put("minecraft:ice", "79");
        blockMap.put("minecraft:snow", "80");
        blockMap.put("minecraft:cactus", "81");
        blockMap.put("minecraft:clay", "82");
        blockMap.put("minecraft:reeds", "83");
        blockMap.put("minecraft:jukebox", "84");
        blockMap.put("minecraft:fence", "85");
        blockMap.put("minecraft:pumpkin", "86");
        blockMap.put("minecraft:netherrack", "87");
        blockMap.put("minecraft:soul_sand", "88");
        blockMap.put("minecraft:glowstone", "89");
        blockMap.put("minecraft:portal", "90");
        blockMap.put("minecraft:lit_pumpkin", "91");
        blockMap.put("minecraft:cake", "92");
        blockMap.put("minecraft:unpowered_repeater", "93");
        blockMap.put("minecraft:powered_repeater", "94");
        blockMap.put("minecraft:stained_glass", "95");
        blockMap.put("minecraft:stained_glass:1", "95:1");
        blockMap.put("minecraft:stained_glass:2", "95:2");
        blockMap.put("minecraft:stained_glass:3", "95:3");
        blockMap.put("minecraft:stained_glass:4", "95:4");
        blockMap.put("minecraft:stained_glass:5", "95:5");
        blockMap.put("minecraft:stained_glass:6", "95:6");
        blockMap.put("minecraft:stained_glass:7", "95:7");
        blockMap.put("minecraft:stained_glass:8", "95:8");
        blockMap.put("minecraft:stained_glass:9", "95:9");
        blockMap.put("minecraft:stained_glass:10", "95:10");
        blockMap.put("minecraft:stained_glass:11", "95:11");
        blockMap.put("minecraft:stained_glass:12", "95:12");
        blockMap.put("minecraft:stained_glass:13", "95:13");
        blockMap.put("minecraft:stained_glass:14", "95:14");
        blockMap.put("minecraft:stained_glass:15", "95:15");
        blockMap.put("minecraft:trapdoor", "96");
        blockMap.put("minecraft:monster_egg", "97");
        blockMap.put("minecraft:monster_egg:1", "97:1");
        blockMap.put("minecraft:monster_egg:2", "97:2");
        blockMap.put("minecraft:monster_egg:3", "97:3");
        blockMap.put("minecraft:monster_egg:4", "97:4");
        blockMap.put("minecraft:monster_egg:5", "97:5");
        blockMap.put("minecraft:stonebrick", "98");
        blockMap.put("minecraft:stonebrick:1", "98:1");
        blockMap.put("minecraft:stonebrick:2", "98:2");
        blockMap.put("minecraft:stonebrick:3", "98:3");
        blockMap.put("minecraft:brown_mushroom_block", "99");
        blockMap.put("minecraft:red_mushroom_block", "100");
        blockMap.put("minecraft:iron_bars", "101");
        blockMap.put("minecraft:glass_pane", "102");
        blockMap.put("minecraft:melon_block", "103");
        blockMap.put("minecraft:pumpkin_stem", "104");
        blockMap.put("minecraft:melon_stem", "105");
        blockMap.put("minecraft:vine", "106");
        blockMap.put("minecraft:fence_gate", "107");
        blockMap.put("minecraft:brick_stairs", "108");
        blockMap.put("minecraft:stone_brick_stairs", "109");
        blockMap.put("minecraft:mycelium", "110");
        blockMap.put("minecraft:waterlily", "111");
        blockMap.put("minecraft:nether_brick", "112");
        blockMap.put("minecraft:nether_brick_fence", "113");
        blockMap.put("minecraft:nether_brick_stairs", "114");
        blockMap.put("minecraft:nether_wart", "115");
        blockMap.put("minecraft:enchanting_table", "116");
        blockMap.put("minecraft:brewing_stand", "117");
        blockMap.put("minecraft:cauldron", "118");
        blockMap.put("minecraft:end_portal", "119");
        blockMap.put("minecraft:end_portal_frame", "120");
        blockMap.put("minecraft:end_stone", "121");
        blockMap.put("minecraft:dragon_egg", "122");
        blockMap.put("minecraft:redstone_lamp", "123");
        blockMap.put("minecraft:lit_redstone_lamp", "124");
        blockMap.put("minecraft:double_wooden_slab", "125");
        blockMap.put("minecraft:double_wooden_slab:1", "125:1");
        blockMap.put("minecraft:double_wooden_slab:2", "125:2");
        blockMap.put("minecraft:double_wooden_slab:3", "125:3");
        blockMap.put("minecraft:double_wooden_slab:4", "125:4");
        blockMap.put("minecraft:double_wooden_slab:5", "125:5");
        blockMap.put("minecraft:wooden_slab", "126");
        blockMap.put("minecraft:wooden_slab:1", "126:1");
        blockMap.put("minecraft:wooden_slab:2", "126:2");
        blockMap.put("minecraft:wooden_slab:3", "126:3");
        blockMap.put("minecraft:wooden_slab:4", "126:4");
        blockMap.put("minecraft:wooden_slab:5", "126:5");
        blockMap.put("minecraft:cocoa", "127");
        blockMap.put("minecraft:sandstone_stairs", "128");
        blockMap.put("minecraft:emerald_ore", "129");
        blockMap.put("minecraft:ender_chest", "130");
        blockMap.put("minecraft:tripwire_hook", "131");
        blockMap.put("minecraft:string", "132");
        blockMap.put("minecraft:emerald_block", "133");
        blockMap.put("minecraft:spruce_stairs", "134");
        blockMap.put("minecraft:birch_stairs", "135");
        blockMap.put("minecraft:jungle_stairs", "136");
        blockMap.put("minecraft:command_block", "137");
        blockMap.put("minecraft:beacon", "138");
        blockMap.put("minecraft:cobblestone_wall", "139");
        blockMap.put("minecraft:cobblestone_wall:1", "139:1");
        blockMap.put("minecraft:flower_pot", "140");
        blockMap.put("minecraft:carrots", "141");
        blockMap.put("minecraft:potatoes", "142");
        blockMap.put("minecraft:wooden_button", "143");
        blockMap.put("minecraft:skull", "144");
        blockMap.put("minecraft:anvil", "145");
        blockMap.put("minecraft:trapped_chest", "146");
        blockMap.put("minecraft:light_weighted_pressure_plate", "147");
        blockMap.put("minecraft:heavy_weighted_pressure_plate", "148");
        blockMap.put("minecraft:unpowered_comparator", "149");
        blockMap.put("minecraft:powered_comparator", "150");
        blockMap.put("minecraft:daylight_detector", "151");
        blockMap.put("minecraft:redstone_block", "152");
        blockMap.put("minecraft:quartz_ore", "153");
        blockMap.put("minecraft:hopper", "154");
        blockMap.put("minecraft:quartz_block", "155");
        blockMap.put("minecraft:quartz_block:1", "155:1");
        blockMap.put("minecraft:quartz_block:2", "155:2");
        blockMap.put("minecraft:quartz_stairs", "156");
        blockMap.put("minecraft:activator_rail", "157");
        blockMap.put("minecraft:dropper", "158");
        blockMap.put("minecraft:stained_hardened_clay", "159");
        blockMap.put("minecraft:stained_hardened_clay:1", "159:1");
        blockMap.put("minecraft:stained_hardened_clay:2", "159:2");
        blockMap.put("minecraft:stained_hardened_clay:3", "159:3");
        blockMap.put("minecraft:stained_hardened_clay:4", "159:4");
        blockMap.put("minecraft:stained_hardened_clay:5", "159:5");
        blockMap.put("minecraft:stained_hardened_clay:6", "159:6");
        blockMap.put("minecraft:stained_hardened_clay:7", "159:7");
        blockMap.put("minecraft:stained_hardened_clay:8", "159:8");
        blockMap.put("minecraft:stained_hardened_clay:9", "159:9");
        blockMap.put("minecraft:stained_hardened_clay:10", "159:10");
        blockMap.put("minecraft:stained_hardened_clay:11", "159:11");
        blockMap.put("minecraft:stained_hardened_clay:12", "159:12");
        blockMap.put("minecraft:stained_hardened_clay:13", "159:13");
        blockMap.put("minecraft:stained_hardened_clay:14", "159:14");
        blockMap.put("minecraft:stained_hardened_clay:15", "159:15");
        blockMap.put("minecraft:stained_glass_pane", "160");
        blockMap.put("minecraft:stained_glass_pane:1", "160:1");
        blockMap.put("minecraft:stained_glass_pane:2", "160:2");
        blockMap.put("minecraft:stained_glass_pane:3", "160:3");
        blockMap.put("minecraft:stained_glass_pane:4", "160:4");
        blockMap.put("minecraft:stained_glass_pane:5", "160:5");
        blockMap.put("minecraft:stained_glass_pane:6", "160:6");
        blockMap.put("minecraft:stained_glass_pane:7", "160:7");
        blockMap.put("minecraft:stained_glass_pane:8", "160:8");
        blockMap.put("minecraft:stained_glass_pane:9", "160:9");
        blockMap.put("minecraft:stained_glass_pane:10", "160:10");
        blockMap.put("minecraft:stained_glass_pane:11", "160:11");
        blockMap.put("minecraft:stained_glass_pane:12", "160:12");
        blockMap.put("minecraft:stained_glass_pane:13", "160:13");
        blockMap.put("minecraft:stained_glass_pane:14", "160:14");
        blockMap.put("minecraft:stained_glass_pane:15", "160:15");
        blockMap.put("minecraft:leaves2", "161");
        blockMap.put("minecraft:leaves2:1", "161:1");
        blockMap.put("minecraft:log2", "162");
        blockMap.put("minecraft:log2:1", "162:1");
        blockMap.put("minecraft:acacia_stairs", "163");
        blockMap.put("minecraft:dark_oak_stairs", "164");
        blockMap.put("minecraft:slime", "165");
        blockMap.put("minecraft:barrier", "166");
        blockMap.put("minecraft:iron_trapdoor", "167");
        blockMap.put("minecraft:prismarine", "168");
        blockMap.put("minecraft:prismarine:1", "168:1");
        blockMap.put("minecraft:prismarine:2", "168:2");
        blockMap.put("minecraft:sea_lantern", "169");
        blockMap.put("minecraft:hay_block", "170");
        blockMap.put("minecraft:carpet", "171");
        blockMap.put("minecraft:carpet:1", "171:1");
        blockMap.put("minecraft:carpet:2", "171:2");
        blockMap.put("minecraft:carpet:3", "171:3");
        blockMap.put("minecraft:carpet:4", "171:4");
        blockMap.put("minecraft:carpet:5", "171:5");
        blockMap.put("minecraft:carpet:6", "171:6");
        blockMap.put("minecraft:carpet:7", "171:7");
        blockMap.put("minecraft:carpet:8", "171:8");
        blockMap.put("minecraft:carpet:9", "171:9");
        blockMap.put("minecraft:carpet:10", "171:10");
        blockMap.put("minecraft:carpet:11", "171:11");
        blockMap.put("minecraft:carpet:12", "171:12");
        blockMap.put("minecraft:carpet:13", "171:13");
        blockMap.put("minecraft:carpet:14", "171:14");
        blockMap.put("minecraft:carpet:15", "171:15");
        blockMap.put("minecraft:hardened_clay", "172");
        blockMap.put("minecraft:coal_block", "173");
        blockMap.put("minecraft:packed_ice", "174");
        blockMap.put("minecraft:double_plant", "175");
        blockMap.put("minecraft:double_plant:1", "175:1");
        blockMap.put("minecraft:double_plant:2", "175:2");
        blockMap.put("minecraft:double_plant:3", "175:3");
        blockMap.put("minecraft:double_plant:4", "175:4");
        blockMap.put("minecraft:double_plant:5", "175:5");
        blockMap.put("minecraft:standing_banner", "176");
        blockMap.put("minecraft:wall_banner", "177");
        blockMap.put("minecraft:daylight_detector_inverted", "178");
        blockMap.put("minecraft:red_sandstone", "179");
        blockMap.put("minecraft:red_sandstone:1", "179:1");
        blockMap.put("minecraft:red_sandstone:2", "179:2");
        blockMap.put("minecraft:red_sandstone_stairs", "180");
        blockMap.put("minecraft:double_stone_slab2", "181");
        blockMap.put("minecraft:stone_slab2", "182");
        blockMap.put("minecraft:spruce_fence_gate", "183");
        blockMap.put("minecraft:birch_fence_gate", "184");
        blockMap.put("minecraft:jungle_fence_gate", "185");
        blockMap.put("minecraft:dark_oak_fence_gate", "186");
        blockMap.put("minecraft:acacia_fence_gate", "187");
        blockMap.put("minecraft:spruce_fence", "188");
        blockMap.put("minecraft:birch_fence", "189");
        blockMap.put("minecraft:jungle_fence", "190");
        blockMap.put("minecraft:dark_oak_fence", "191");
        blockMap.put("minecraft:acacia_fence", "192");
        blockMap.put("minecraft:spruce_door", "193");
        blockMap.put("minecraft:birch_door", "194");
        blockMap.put("minecraft:jungle_door", "195");
        blockMap.put("minecraft:acacia_door", "196");
        blockMap.put("minecraft:dark_oak_door", "197");
        blockMap.put("minecraft:end_rod", "198");
        blockMap.put("minecraft:chorus_plant", "199");
        blockMap.put("minecraft:chorus_flower", "200");
        blockMap.put("minecraft:purpur_block", "201");
        blockMap.put("minecraft:purpur_pillar", "202");
        blockMap.put("minecraft:purpur_stairs", "203");
        blockMap.put("minecraft:purpur_double_slab", "204");
        blockMap.put("minecraft:purpur_slab", "205");
        blockMap.put("minecraft:end_bricks", "206");
        blockMap.put("minecraft:beetroots", "207");
        blockMap.put("minecraft:grass_path", "208");
        blockMap.put("minecraft:end_gateway", "209");
        blockMap.put("minecraft:repeating_command_block", "210");
        blockMap.put("minecraft:chain_command_block", "211");
        blockMap.put("minecraft:frosted_ice", "212");
        blockMap.put("minecraft:magma", "213");
        blockMap.put("minecraft:nether_wart_block", "214");
        blockMap.put("minecraft:red_nether_brick", "215");
        blockMap.put("minecraft:bone_block", "216");
        blockMap.put("minecraft:structure_void", "217");
        blockMap.put("minecraft:observer", "218");
        blockMap.put("minecraft:white_shulker_box", "219");
        blockMap.put("minecraft:orange_shulker_box", "220");
        blockMap.put("minecraft:magenta_shulker_box", "221");
        blockMap.put("minecraft:light_blue_shulker_box", "222");
        blockMap.put("minecraft:yellow_shulker_box", "223");
        blockMap.put("minecraft:lime_shulker_box", "224");
        blockMap.put("minecraft:pink_shulker_box", "225");
        blockMap.put("minecraft:gray_shulker_box", "226");
        blockMap.put("minecraft:silver_shulker_box", "227");
        blockMap.put("minecraft:cyan_shulker_box", "228");
        blockMap.put("minecraft:purple_shulker_box", "229");
        blockMap.put("minecraft:blue_shulker_box", "230");
        blockMap.put("minecraft:brown_shulker_box", "231");
        blockMap.put("minecraft:green_shulker_box", "232");
        blockMap.put("minecraft:red_shulker_box", "233");
        blockMap.put("minecraft:black_shulker_box", "234");
        blockMap.put("minecraft:white_glazed_terracotta", "235");
        blockMap.put("minecraft:orange_glazed_terracotta", "236");
        blockMap.put("minecraft:magenta_glazed_terracotta", "237");
        blockMap.put("minecraft:light_blue_glazed_terracotta", "238");
        blockMap.put("minecraft:yellow_glazed_terracotta", "239");
        blockMap.put("minecraft:lime_glazed_terracotta", "240");
        blockMap.put("minecraft:pink_glazed_terracotta", "241");
        blockMap.put("minecraft:gray_glazed_terracotta", "242");
        blockMap.put("minecraft:light_gray_glazed_terracotta", "243");
        blockMap.put("minecraft:cyan_glazed_terracotta", "244");
        blockMap.put("minecraft:purple_glazed_terracotta", "245");
        blockMap.put("minecraft:blue_glazed_terracotta", "246");
        blockMap.put("minecraft:brown_glazed_terracotta", "247");
        blockMap.put("minecraft:green_glazed_terracotta", "248");
        blockMap.put("minecraft:red_glazed_terracotta", "249");
        blockMap.put("minecraft:black_glazed_terracotta", "250");
        blockMap.put("minecraft:concrete", "251");
        blockMap.put("minecraft:concrete:1", "251:1");
        blockMap.put("minecraft:concrete:2", "251:2");
        blockMap.put("minecraft:concrete:3", "251:3");
        blockMap.put("minecraft:concrete:4", "251:4");
        blockMap.put("minecraft:concrete:5", "251:5");
        blockMap.put("minecraft:concrete:6", "251:6");
        blockMap.put("minecraft:concrete:7", "251:7");
        blockMap.put("minecraft:concrete:8", "251:8");
        blockMap.put("minecraft:concrete:9", "251:9");
        blockMap.put("minecraft:concrete:10", "251:10");
        blockMap.put("minecraft:concrete:11", "251:11");
        blockMap.put("minecraft:concrete:12", "251:12");
        blockMap.put("minecraft:concrete:13", "251:13");
        blockMap.put("minecraft:concrete:14", "251:14");
        blockMap.put("minecraft:concrete:15", "251:15");
        blockMap.put("minecraft:concrete_powder", "252");
        blockMap.put("minecraft:concrete_powder:1", "252:1");
        blockMap.put("minecraft:concrete_powder:2", "252:2");
        blockMap.put("minecraft:concrete_powder:3", "252:3");
        blockMap.put("minecraft:concrete_powder:4", "252:4");
        blockMap.put("minecraft:concrete_powder:5", "252:5");
        blockMap.put("minecraft:concrete_powder:6", "252:6");
        blockMap.put("minecraft:concrete_powder:7", "252:7");
        blockMap.put("minecraft:concrete_powder:8", "252:8");
        blockMap.put("minecraft:concrete_powder:9", "252:9");
        blockMap.put("minecraft:concrete_powder:10", "252:10");
        blockMap.put("minecraft:concrete_powder:11", "252:11");
        blockMap.put("minecraft:concrete_powder:12", "252:12");
        blockMap.put("minecraft:concrete_powder:13", "252:13");
        blockMap.put("minecraft:concrete_powder:14", "252:14");
        blockMap.put("minecraft:concrete_powder:15", "252:15");
        blockMap.put("minecraft:structure_block", "255");
    }

    public static void copyCuboidClipboard(CuboidClipboard schem, CuboidClipboard schemResized) {
        for (int x = 0; x < schem.getWidth(); x++) {
            for (int z = 0; z < schem.getLength(); z++) {
                for (int y = 0; y < schem.getHeight(); y++) {
                    BaseBlock block = schem.getBlock(new Vector(x, y, z));
                    if (block != null) {
                        schemResized.setBlock(new Vector(x, y, z), block);
                    }
                }
            }
        }
    }

    public static CuboidClipboard copyCuboidClipboard(CuboidClipboard schem) {
        CuboidClipboard copy = new CuboidClipboard(schem.getSize(), schem.getOrigin(), schem.getOffset());
        for (int x = 0; x < schem.getWidth(); x++) {
            for (int z = 0; z < schem.getLength(); z++) {
                for (int y = 0; y < schem.getHeight(); y++) {
                    BaseBlock block = schem.getBlock(new com.sk89q.worldedit.Vector(x, y, z));
                    if (block != null) {
                        copy.setBlock(new com.sk89q.worldedit.Vector(x, y, z), block);
                    }
                }
            }
        }
        return copy;
    }

    public static String getStringId(int id) {
        for (Map.Entry<String, String> e : blockMap.entrySet()) {
            String[] split = e.getValue().split(":");
            if (split.length == 1) {
                if (split[0].equals(Integer.toString(id))) {
                    return e.getKey();
                }
            }
        }
        return null;
    }

    public static Rectangle bbox(Geometry g) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (Coordinate c : g.getCoordinates()) {
            minX = (int) Math.min(minX, c.x);
            maxX = (int) Math.max(maxX, c.x);
            minY = (int) Math.min(minY, c.y);
            maxY = (int) Math.max(maxY, c.y);
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public static Rectangle bboxLt(org.locationtech.jts.geom.Geometry g) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (org.locationtech.jts.geom.Coordinate c : g.getCoordinates()) {
            minX = (int) Math.min(minX, c.x);
            maxX = (int) Math.max(maxX, c.x);
            minY = (int) Math.min(minY, c.y);
            maxY = (int) Math.max(maxY, c.y);
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public static Vector3d transform2D(Vector3d v, double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = v.getX() - aboutX;
        double z = v.getZ() - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);

        return new Vector3d(
                x2 + aboutX + translateX,
                v.getY(),
                z2 + aboutZ + translateZ
        );
    }
}
