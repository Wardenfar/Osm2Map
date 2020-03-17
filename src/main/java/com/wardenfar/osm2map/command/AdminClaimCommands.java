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

public class AdminClaimCommands extends Commands {

    public AdminClaimCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {"as"},
            desc = "Admin claim as",
            usage = ""
    )
    @Require("osm2map.admin.claim.as")
    public void adminClaimAs(@Sender Player src, String asPlayer, @Optional @Merged String args) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        List<String> friends = args == null || args.equals("") ? new ArrayList<>() : Arrays.asList(args.split(" "));
        Vector3i pos = Util.toVector(src.getLocation()).toInt();
        data.claim(src, new Vector2i(pos.getX(), pos.getZ()), asPlayer, friends, true);
    }

    @Command(
            aliases = {"public"},
            desc = "Admin claim public",
            usage = ""
    )
    @Require("osm2map.admin.claim.public")
    public void adminClaimPublic(@Sender Player src) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        Vector3i pos = Util.toVector(src.getLocation()).toInt();
        data.claim(src, new Vector2i(pos.getX(), pos.getZ()), "*", new ArrayList<>(), true);
    }
}
