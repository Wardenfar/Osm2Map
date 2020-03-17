package com.wardenfar.osm2map.request;

import com.wardenfar.osm2map.Osm2map;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public abstract class Request {

    protected CommandSender src;
    protected long submitMs;

    public Request() {

    }

    public abstract TextComponent[] getMessage();

    public abstract void confirm(Osm2map plugin);

    public abstract void cancel(Osm2map plugin);
}
