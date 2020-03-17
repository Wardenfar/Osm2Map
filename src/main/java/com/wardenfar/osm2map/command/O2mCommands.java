package com.wardenfar.osm2map.command;

import com.sk89q.intake.Command;
import com.wardenfar.osm2map.Osm2map;
import li.l1t.common.intake.provider.annotation.Sender;
import org.bukkit.entity.Player;

import static com.wardenfar.osm2map.Util.sendMessage;

public class O2mCommands extends Commands {

    public O2mCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {""},
            desc = "Osm2Map Help",
            usage = ""
    )
    public void help(@Sender Player player) {
        sendMessage(player, "Osm2Map " + Osm2map.VERSION + " by Wardenfar");
    }
}
