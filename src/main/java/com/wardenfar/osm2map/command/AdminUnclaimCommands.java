package com.wardenfar.osm2map.command;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.Vector2i;
import li.l1t.common.intake.provider.annotation.Merged;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminUnclaimCommands extends Commands {

    public AdminUnclaimCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {"as"},
            desc = "Admin unclaim as",
            usage = ""
    )
    @Require("osm2map.admin.claim.as")
    public void adminUnclaimAs(@Sender Player src) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        Vector3i pos = Util.toVector(src.getLocation()).toInt();
        data.unclaimAdmin(src, pos);
    }

    @Command(
            aliases = {"allof"},
            desc = "Admin unclaim allof",
            usage = ""
    )
    @Require("osm2map.admin.claim.as")
    public void adminUnclaimAllOf(@Sender Player src, String allOfPlayer) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        plugin.getMapData(src.getWorld().getName()).unclaimAllOf(src, allOfPlayer);
    }
}
