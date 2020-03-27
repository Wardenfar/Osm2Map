package com.wardenfar.osm2map.command;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.entity.Vector2i;
import li.l1t.common.intake.provider.annotation.Merged;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wardenfar.osm2map.Util.sendMessage;

public class ClaimCommand extends Commands {

    public ClaimCommand(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {""},
            desc = "Claim",
            usage = "(ami) (ami) ..."
    )
    @Require("osm2map.claim")
    public void root(@Sender Player src, @Optional @Merged String args) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        int maxClaim = plugin.getConfig(src.getWorld().getName()).guard.maxClaim;
        if (maxClaim != -1 && plugin.getMapData(src.getWorld().getName()).getZonesOwnedByPlayer(src.getName()).size() >= maxClaim) {
            sendMessage(src, "You already own a maximum of zones", ChatColor.RED);
        } else {
            List<String> friends = args == null ? new ArrayList<>() : Arrays.asList(args.split(" "));
            int maxFriend = plugin.getConfig(src.getWorld().getName()).guard.maxFriend;
            if (maxFriend != -1 && friends.size() > maxFriend) {
                sendMessage(src, "The maximum number of friends per zone is " + maxFriend, ChatColor.RED);
            } else {
                Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
                for (String friend : friends) {
                    Matcher matcher = pattern.matcher(friend);
                    if (!matcher.find()) {
                        sendMessage(src, "'" + friend + "' is not a valid nickname !", ChatColor.RED);
                    }
                }
                Vector3i pos = Util.toVector(src.getLocation()).toInt();
                plugin.getMapData(src.getWorld().getName()).claim(src, new Vector2i(pos.getX(), pos.getZ()), src.getName(), friends, false);
            }
        }
    }
}
