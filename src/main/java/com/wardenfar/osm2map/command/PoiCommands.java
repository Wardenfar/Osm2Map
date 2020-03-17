package com.wardenfar.osm2map.command;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.Poi;
import li.l1t.common.intake.provider.annotation.Merged;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.List;

import static com.wardenfar.osm2map.Util.*;

public class PoiCommands extends Commands {

    public PoiCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {"add"},
            desc = "Poi add",
            usage = "[poi]"
    )
    @Require("osm2map.poi.add")
    public void poiAdd(@Sender Player player, @Merged String poiNameValue) {
        DBMapData data = plugin.getMapData(player.getWorld().getName());
        if (data.getOnePoiByName(poiNameValue) == null) {
            Poi poi = new Poi(-1, poiNameValue, toVector(player.getLocation()), new Vector3d());
            data.addPoi(poi);
            sendMessage(player, poi.name + " added !", ChatColor.GREEN);
        } else {
            sendMessage(player, poiNameValue + " already exist !", ChatColor.RED);
        }
    }

    @Command(
            aliases = {"list"},
            desc = "Poi list",
            usage = ""
    )
    @Require("osm2map.poi.list")
    public void poiList(@Sender Player player, @Optional Integer pageNumber) {
        pageNumber = pageNumber == null ? 1 : pageNumber;

        DBMapData data = plugin.getMapData(player.getWorld().getName());
        List<Poi> pois = data.getPoiFromPage(pageNumber);
        sendMessage(player, pois.size() + " result(s)      page: " + pageNumber + "/" + data.getNbPages());
        for (Poi poi : pois) {
            TextComponent textpos = builder("[x=" + poi.pos.getX() + ", y=" + poi.pos.getY() + ", z=" + poi.pos.getZ() + "] ", ChatColor.GOLD);
            textpos.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map otp poi " + poi.name));

            TextComponent textRemove = builder("[remove] ", ChatColor.RED);
            textRemove.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map poi remove " + poi.name));
            sendMessage(player, builder(builder(poi.name + " "), textpos, textRemove));
        }
    }

    @Command(
            aliases = {"remove"},
            desc = "Poi remove",
            usage = "[poi]"
    )
    @Require("osm2map.poi.remove")
    public void poiRemove(@Sender Player player, @Merged String poiNameValue) {
        DBMapData data = plugin.getMapData(player.getWorld().getName());
        Poi search = data.getOnePoiByName(poiNameValue);
        if (search != null) {
            data.removePoi(search.id);
            sendMessage(player, search.name + " removed !", ChatColor.GREEN);
        } else {
            sendMessage(player, poiNameValue + " not exist !", ChatColor.RED);
        }
    }
}
