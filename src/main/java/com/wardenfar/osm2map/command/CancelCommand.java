package com.wardenfar.osm2map.command;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.wardenfar.osm2map.Osm2map;
import li.l1t.common.intake.provider.annotation.Sender;
import org.bukkit.entity.Player;

public class CancelCommand extends Commands {

    public CancelCommand(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {""},
            desc = "Cancel Request",
            usage = ""
    )
    @Require("osm2map.request.cancel")
    public void root(@Sender Player src) {
        plugin.getRequestManager().cancel(src);
    }
}
