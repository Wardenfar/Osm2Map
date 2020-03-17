package com.wardenfar.osm2map.request;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.vividsolutions.jts.geom.Coordinate;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.map.entity.JtsZone;
import com.wardenfar.osm2map.map.entity.Vector2i;
import com.wardenfar.osm2map.pluginInterface.SchemInterface;
import com.wardenfar.osm2map.terrainGeneration.OsmElevGenerator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.List;

import static com.wardenfar.osm2map.Util.builder;
import static com.wardenfar.osm2map.Util.sendMessage;

public class ZoneCreateRequest extends Request {

    String worldName;
    JtsZone.Type type;
    boolean doGen;
    List<Vector2i> points;

    public ZoneCreateRequest(String worldName, JtsZone.Type type, boolean doGen, List<Vector2i> points) {
        this.worldName = worldName;
        this.type = type;
        this.doGen = doGen;
        this.points = points;
    }

    @Override
    public TextComponent[] getMessage() {
        return new TextComponent[]{builder("Do you really want to create this zone ?", ChatColor.DARK_PURPLE)};
    }

    @Override
    public void confirm(Osm2map plugin) {
        sendMessage(src, "Confirmed zone creation", ChatColor.LIGHT_PURPLE);
        JtsZone zone = plugin.getMapData(worldName).addZone(type, points);
        sendMessage(src, "Zone added !", ChatColor.GREEN);

        if (doGen) {
            sendMessage(src, "Generation started ...", ChatColor.GREEN);
            zone.computePixels(null, null);
            World world = ((Player) src).getWorld();

            OsmElevGenerator populator = plugin.getOsmElevGenerator();
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (Coordinate c : zone.geometry.getBoundary().getCoordinates()) {
                minX = (int) Math.min(minX, c.x);
                maxX = (int) Math.max(maxX, c.x);
                minY = (int) Math.min(minY, c.y);
                maxY = (int) Math.max(maxY, c.y);
            }
            int width = maxX - minX;
            int height = maxY - minY;
            Vector origin = new Vector(minX, 0, minY);
            CuboidClipboard clip = new CuboidClipboard(new Vector(width, 256, height), origin);
            populator.populate(worldName, clip, new SchemInterface());
            for (int x = 0; x < clip.getWidth(); x++) {
                for (int z = 0; z < clip.getLength(); z++) {
                    for (int y = 0; y < clip.getHeight(); y++) {
                        BaseBlock block = clip.getBlock(new Vector(x, y, z));
                        if (block != null) {
                            Object blockObject = plugin.getInterface().getBlock(block.getId(), block.getData());
                            setBlock(world, (int) (origin.getX() + x), (int) (origin.getY() + y), (int) (origin.getZ() + z), (MaterialData) blockObject);
                        }
                    }
                }
            }
            sendMessage(src, "Generation finished ...", ChatColor.GREEN);
        }
    }

    @Override
    public void cancel(Osm2map plugin) {
        sendMessage(src, "Canceled zone creation", ChatColor.LIGHT_PURPLE);
    }

    private MaterialData getBlock(World world, int x, int y, int z) {
        return world.getBlockAt(x, y, z).getState().getData();
    }

    private void setBlock(World world, int x, int y, int z, MaterialData block) {
        Block b = world.getBlockAt(x,y,z);
        b.setType(block.getItemType());
        b.setData(block.getData());
    }
}
