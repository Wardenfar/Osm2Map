package com.wardenfar.osm2map.command;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.JtsZone;
import com.wardenfar.osm2map.map.entity.Vector2i;
import com.wardenfar.osm2map.request.ZoneCreateRequest;
import com.wardenfar.osm2map.request.ZoneRemoveRequest;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.wardenfar.osm2map.Util.builder;
import static com.wardenfar.osm2map.Util.sendMessage;

public class ZoneCommands extends Commands {

    public ZoneCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {"create"},
            desc = "Zone create",
            usage = ""
    )
    @Require("osm2map.zone.create")
    public void zoneCreate(@Sender Player src, JtsZone.Type type, boolean doGen) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        try {
            WorldEdit worldEdit = WorldEdit.getInstance();
            Region region = worldEdit.getSessionManager().findByName(src.getName()).getSelection(worldEdit.getServer().getWorlds().get(0));
            List<Vector2i> points = new ArrayList<>();
            if (region instanceof CuboidRegion) {
                sendMessage(src, "CuboidRegion", ChatColor.LIGHT_PURPLE);
                CuboidRegion r = (CuboidRegion) region;
                Vector min = r.getMinimumPoint();
                Vector max = r.getMaximumPoint();
                points.add(new Vector2i(min.getBlockX(), min.getBlockZ()));
                points.add(new Vector2i(min.getBlockX(), max.getBlockZ()));
                points.add(new Vector2i(max.getBlockX(), max.getBlockZ()));
                points.add(new Vector2i(max.getBlockX(), min.getBlockZ()));
            } else if (region instanceof Polygonal2DRegion) {
                sendMessage(src, "PolygonalRegion", ChatColor.LIGHT_PURPLE);
                Polygonal2DRegion r = (Polygonal2DRegion) region;
                for (BlockVector2D p : r.getPoints()) {
                    points.add(new Vector2i(p.getBlockX(), p.getBlockZ()));
                }
            } else {
                sendMessage(src, "Unsupported Region Type", ChatColor.RED);
            }

            plugin.getRequestManager().postRequest(src, new ZoneCreateRequest(src.getWorld().getName(), type, doGen, points));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(src, "An error occured ! " + e.getMessage(), ChatColor.RED);
        }
    }

    @Command(
            aliases = {"info"},
            desc = "Zone info",
            usage = ""
    )
    @Require("osm2map.zone.info")
    public void zoneInfo(@Sender Player src) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        Set<Long> zoneIds = data.getZonesFromPos(new Vector2i(src.getLocation().getBlockX(), src.getLocation().getBlockZ()));
        sendMessage(src, zoneIds.size() + " results");
        printZones(data, src, zoneIds);
    }

    @Command(
            aliases = {"status"},
            desc = "Zone status",
            usage = ""
    )
    @Require("osm2map.zone.status")
    public void zoneStatus(@Sender Player src, @Optional String playerValue) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        if (playerValue == null) {
            Set<Long> allZones = data.getOwnedZones();
            sendMessage(src, allZones.size() + " result(s)");
            printZones(data, src, allZones);
        } else {
            Set<Long> playersZones = data.getZonesByOwnerOrFriend(playerValue, false);
            sendMessage(src, playersZones.size() + " result(s)");
            printZones(data, src, playersZones);
        }
    }


    @Command(
            aliases = {"remove"},
            desc = "Zone remove",
            usage = ""
    )
    @Require("osm2map.zone.remove")
    public void zoneRemove(@Sender Player src) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        Set<Long> zoneIds = data.getZonesFromPos(new Vector2i(src.getLocation().getBlockX(),src.getLocation().getBlockZ()));
        if (zoneIds.size() == 1) {
            plugin.getRequestManager().postRequest(src, new ZoneRemoveRequest(src.getWorld().getName(), (Long) zoneIds.toArray()[0]));
        } else if (zoneIds.size() > 1) {
            sendMessage(src, "More than one zone !", ChatColor.RED);
        } else {
            sendMessage(src, "No zone !", ChatColor.RED);
        }
    }

    void printZones(DBMapData data, CommandSender commandSource, Collection<Long> zoneIds) {
        for (Long zoneId : zoneIds) {
            printZone(data, commandSource, zoneId);
        }
    }

    void printZone(DBMapData data, CommandSender commandSource, Long zoneId) {
        JtsZone zone = data.getZone(zoneId);
        String owner = zone.owner;

        TextComponent ownerAndFriendText;
        if (owner == null) {
            ownerAndFriendText = builder(" this zone has not been claim", ChatColor.AQUA);
        } else {
            TextComponent friendsBuilder = builder(" friends : ", ChatColor.WHITE);
            for (String friend : zone.friends) {
                TextComponent t1 = builder(friend, ChatColor.BLUE);
                t1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map zone status " + friend));
                friendsBuilder.addExtra(t1);
                friendsBuilder.addExtra(builder(", ", ChatColor.WHITE));
            }
            ownerAndFriendText = builder(" owner : ", ChatColor.WHITE);
            TextComponent t2 = builder(owner, ChatColor.GOLD);
            t2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map zone status " + owner));
            ownerAndFriendText.addExtra(t2);
            ownerAndFriendText.addExtra(friendsBuilder);
        }
        TextComponent t3 = builder(zoneId.toString(), ChatColor.GOLD);
        t3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map otp zone " + zoneId));
        t3.addExtra(ownerAndFriendText);

        sendMessage(commandSource, builder(builder("zoneId : ", ChatColor.WHITE), t3));
    }
}
