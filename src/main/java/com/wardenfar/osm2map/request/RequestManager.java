package com.wardenfar.osm2map.request;

import com.wardenfar.osm2map.Osm2map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.wardenfar.osm2map.Util.builder;
import static com.wardenfar.osm2map.Util.sendMessage;

public class RequestManager {

    public static int TIMOUT_MS = 10000;

    Osm2map plugin;

    List<Request> requests;

    public RequestManager(Osm2map plugin) {
        this.plugin = plugin;
        requests = new ArrayList<>();
    }

    public void postRequest(CommandSender src, Request request) {
        request.src = src;
        request.submitMs = System.currentTimeMillis();
        Request last = getLastRequestFrom(request.src);
        if (last != null) {
            last.cancel(plugin);
            requests.remove(last);
        }
        requests.add(request);

        for (TextComponent tc : request.getMessage()) {
            sendMessage(request.src, tc);
        }

        TextComponent confirm = builder("[Confirm or /confirm]", ChatColor.GREEN);
        TextComponent cancel = builder(" [Cancel or /cancel]", ChatColor.RED);

        confirm.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map confirm"));
        cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map cancel"));

        sendMessage(request.src, builder(confirm, cancel));

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::checkTimoutRequest, (TIMOUT_MS + 500) / 1000 * 20);
    }

    private void checkTimoutRequest() {
        long currentMs = System.currentTimeMillis();
        List<Request> toRemove = new ArrayList<>();
        for (Request r : requests) {
            if (currentMs - r.submitMs > TIMOUT_MS) {
                toRemove.add(r);
            }
        }
        requests.removeAll(toRemove);
    }

    public void confirm(CommandSender src) {
        Request last = getLastRequestFrom(src);
        if (last != null) {
            requests.remove(last);
            last.confirm(plugin);
        } else {
            sendMessage(src, getNoLastRequestText());
        }
        checkTimoutRequest();
    }

    public void cancel(CommandSender src) {
        Request last = getLastRequestFrom(src);
        if (last != null) {
            requests.remove(last);
            last.cancel(plugin);
        } else {
            sendMessage(src, getNoLastRequestText());
        }
        checkTimoutRequest();
    }

    private TextComponent getNoLastRequestText() {
        return builder("No request found !", ChatColor.RED);
    }

    private Request getLastRequestFrom(CommandSender src) {
        for (Request request : requests) {
            if (request.src.getName().equals(src.getName())) {
                return request;
            }
        }
        return null;
    }
}
