package com.wardenfar.osm2map.request;

import com.wardenfar.osm2map.Osm2map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import static com.wardenfar.osm2map.Util.*;

public class ZoneRemoveRequest extends Request {

    String worldName;
    long zoneId;

    public ZoneRemoveRequest(String worldName, long zoneId) {
        this.worldName = worldName;
        this.zoneId = zoneId;
    }

    @Override
    public TextComponent[] getMessage() {
        return new TextComponent[]{builder("Veux-tu vraiment supprimer cette zone (" + zoneId + ") ?")};
    }

    @Override
    public void confirm(Osm2map plugin) {
        sendMessage(src, "Confirmed zone suppression", ChatColor.LIGHT_PURPLE);
        plugin.getMapData(worldName).removeZone(zoneId);
        sendMessage(src, "Zone removed !", ChatColor.GREEN);
    }

    @Override
    public void cancel(Osm2map plugin) {
        sendMessage(src, "Canceled zone suppression", ChatColor.LIGHT_PURPLE);
    }
}
