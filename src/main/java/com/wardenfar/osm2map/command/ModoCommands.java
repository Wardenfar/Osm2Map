package com.wardenfar.osm2map.command;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;
import li.l1t.common.intake.provider.annotation.Merged;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import static com.wardenfar.osm2map.Util.builder;
import static com.wardenfar.osm2map.Util.sendMessage;

public class ModoCommands extends Commands {

    public ModoCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {"add"},
            desc = "Modo add",
            usage = "[player]"
    )
    @Require("osm2map.modo.add")
    public void modoAdd(@Sender Player src, String playerValue) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        if (data.getModos().contains(playerValue)) {
            sendMessage(src, playerValue + " est déjà modo", ChatColor.RED);
        } else {
            data.addModo(playerValue);
        }
    }

    @Command(
            aliases = {"list"},
            desc = "Modos list",
            usage = ""
    )
    @Require("osm2map.modo.list")
    public void poiList(@Sender Player src) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        sendMessage(src, data.getModos().size() + " results");
        for (String adminPlayer : data.getModos()) {
            TextComponent removeButton = builder("[enlever]",ChatColor.GOLD);
            removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/osm2map modo remove " + adminPlayer));
            sendMessage(src, builder(builder(adminPlayer + " "),removeButton));
        }
    }

    @Command(
            aliases = {"remove"},
            desc = "Modo remove",
            usage = "[player]"
    )
    @Require("osm2map.modo.remove")
    public void poiRemove(@Sender Player src, @Merged String playerValue) {
        if(!plugin.getConfig(src.getWorld().getName()).guard.active){
            Util.sendMessage(src, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        if (!data.getModos().contains(playerValue)) {
            sendMessage(src, playerValue + " n' est pas modo", ChatColor.RED);
        } else {
            data.removeModo(playerValue);
        }
    }
}
