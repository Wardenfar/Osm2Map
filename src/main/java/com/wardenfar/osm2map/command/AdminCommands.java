package com.wardenfar.osm2map.command;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.LatLon;
import com.wardenfar.osm2map.map.entity.Poi;
import com.wardenfar.osm2map.map.entity.Vector2i;
import li.l1t.common.intake.provider.annotation.Merged;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.List;

import static com.wardenfar.osm2map.Util.*;

public class AdminCommands extends Commands {

    public AdminCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {"getcoord"},
            desc = "Admin getcoord",
            usage = ""
    )
    @Require("osm2map.admin.getcoord")
    public void adminGetCoord(@Sender Player player) {
        DBMapData data = plugin.getMapData(player.getWorld().getName());
        Vector3i pos = Util.toVector(player.getLocation()).toInt();
        LatLon latLon = plugin.getMapData(player.getWorld().getName()).unproject(new Vector2i(pos.getX(), pos.getZ()).toDouble());
        Vector2i tile = Util.getTile(latLon.lat, latLon.lon, plugin.getConfig(player.getWorld().getName()).tile.tilesZoom);
        sendMessage(player, latLon.lat + " , " + latLon.lon + "  tile:" + tile.x + "," + tile.y);
    }
}
