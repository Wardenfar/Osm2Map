package com.wardenfar.osm2map.command;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class AdminUnclaimCommands extends Commands {

    public AdminUnclaimCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {"as"},
            desc = "Admin unclaim as",
            usage = ""
    )
    @Require("osm2map.admin.unclaim.as")
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
    @Require("osm2map.admin.unclaim.allof")
    public void adminUnclaimAllOf(@Sender Player src, String allOfPlayer) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        plugin.getMapData(src.getWorld().getName()).unclaimAllOf(src, allOfPlayer);
    }
}
