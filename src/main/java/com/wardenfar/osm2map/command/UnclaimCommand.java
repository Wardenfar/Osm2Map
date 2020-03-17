package com.wardenfar.osm2map.command;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.Vector2i;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class UnclaimCommand extends Commands {

    public UnclaimCommand(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {""},
            desc = "Unclaim",
            usage = ""
    )
    @Require("osm2map.unclaim")
    public void root(@Sender Player src) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData mapData = plugin.getMapData(src.getWorld().getName());
        Vector3i pos = Util.toVector(src.getLocation()).toInt();
        //plugin.sendPluginDesc(src);
        mapData.unclaim(src, src.getName(), new Vector2i(pos.getX(), pos.getZ()));
    }
}
