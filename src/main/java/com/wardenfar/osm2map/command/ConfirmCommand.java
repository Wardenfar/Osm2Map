package com.wardenfar.osm2map.command;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.wardenfar.osm2map.Osm2map;
import li.l1t.common.intake.provider.annotation.Merged;
import li.l1t.common.intake.provider.annotation.Sender;
import org.bukkit.entity.Player;

public class ConfirmCommand extends Commands {

    public ConfirmCommand(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {""},
            desc = "Confirm Request",
            usage = ""
    )
    @Require("osm2map.request.confirm")
    public void root(@Sender Player src) {
        plugin.getRequestManager().confirm(src);
    }
}
