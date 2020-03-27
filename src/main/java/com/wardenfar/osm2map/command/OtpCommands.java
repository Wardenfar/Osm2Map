package com.wardenfar.osm2map.command;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.*;
import li.l1t.common.intake.provider.annotation.Merged;
import li.l1t.common.intake.provider.annotation.OnlinePlayer;
import li.l1t.common.intake.provider.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

import static com.wardenfar.osm2map.Util.builder;
import static com.wardenfar.osm2map.Util.sendMessage;

public class OtpCommands extends Commands {

    public OtpCommands(Osm2map plugin) {
        super(plugin);
    }

    @Command(
            aliases = {"way"},
            desc = "Tp to a way",
            usage = "[way]"
    )
    @Require("osm2map.otp.way")
    public void otpWay(@Sender Player src, @Merged String args) {
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        String value = String.join(" ", args);
        List<JtsWay> result = data.getWaysFromNameLower(value);
        // enleve tous les doubles
        result = result.stream().filter(Util.distinctByKey(JtsWay::getName)).collect(Collectors.toList());

        sendMessage(src, result.size() + " results");
        if (result.size() == 1) {
            JtsWay w = result.get(0);
            Vector2i firstPos = w.points.get(0);
            tp(src, firstPos.toDouble());

            if (w.points.size() > 1) {
                Vector2d nextPos = w.points.get(1).toDouble();
                        /*int playerHeight = player.getPosition().getFloorY() - plugin.getElevByBlockXZ(player.getPosition().getFloorX(), player.getPosition().getFloorZ());
                        if(playerHeight < 5){
                            playerHeight = 5;
                        }
                        int height = plugin.getElevByBlockXZ(nextPos.getX(), nextPos.getY()) + playerHeight;*/
                //player.lookAt(new Vector3d(nextPos.x, plugin.getElevByBlockXZ(nextPos.getX(), nextPos.getY()), nextPos.y));
            }
        } else {
            for (JtsWay w : result) {
                TextComponent builder = builder(w.name, ChatColor.GOLD);
                builder.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{builder("Teleport to " + w.name, ChatColor.AQUA)}));
                builder.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map otp way " + w.name));
                sendMessage(src, builder);
            }
        }
    }

    @Command(
            aliases = {"player"},
            desc = "Tp to a player",
            usage = "[player]"
    )
    @Require("osm2map.otp.player")
    public void otpPlayer(@Sender Player src, @OnlinePlayer Player target) {
        tp(src, target.getWorld(), Util.toVector(target.getLocation()));
    }

    @Command(
            aliases = {"coord", "latlon"},
            desc = "Tp coord",
            usage = "[lat] [lon]"
    )
    @Require("osm2map.otp.coord")
    public void otpCoord(@Sender Player src, double lat, double lon) {
        DBMapData data = plugin.getMapData(src.getWorld().getName());
        Vector2d pos = data.project(new LatLon(lat, lon));
        tp(src, pos);
    }

    @Command(
            aliases = {"xyz", "xz", "pos"},
            desc = "Tp xyz",
            usage = "[x] (y) [z]"
    )
    @Require("osm2map.otp.xyz")
    public void otpXYZ(@Sender Player player, @Merged String args) {
        String[] xyz = args.split(" ");
        if (xyz.length == 2) {
            int x = Integer.parseInt(xyz[0]);
            int z = Integer.parseInt(xyz[1]);
            tp(player, new Vector2d(x, z));
        } else if (xyz.length == 3) {
            int x = Integer.parseInt(xyz[0]);
            int y = Integer.parseInt(xyz[1]);
            int z = Integer.parseInt(xyz[2]);
            int elev = plugin.getElevByBlockXZ(player.getWorld().getName(), x, z) - plugin.getMapData(player.getWorld().getName()).getHeight();
            if (y <= elev) {
                y = elev + plugin.getMapData(player.getWorld().getName()).getHeight() * 2 + 10;
            }
            tp(player, player.getWorld(), new Vector3d(x, y, z));
        } else {
            sendMessage(player, "Syntax Error", ChatColor.RED);
        }
    }

    @Command(
            aliases = {"poi"},
            desc = "Tp poi",
            usage = "list | [poi]"
    )
    @Require("osm2map.otp.poi")
    public void otpPoi(@Sender Player player, @Merged String value) {
        DBMapData data = plugin.getMapData(player.getWorld().getName());
        String[] valueArgs = value.split(" ");
        if (!valueArgs[0].equals("list")) {
            List<Poi> pois = data.getAllPoisByName("%" + value + "%");
            pois = pois.stream().filter(Util.distinctByKey(Poi::getName)).collect(Collectors.toList());
            if (pois.size() == 1) {
                Poi poi = pois.get(0);
                if (poi.pos.getY() == -1) {
                    tp(player, new Vector2d(poi.pos.getX(), poi.pos.getZ()));
                } else {
                    tp(player, player.getWorld(), poi.pos);
                }
                if (poi.rot != null) {
                    setRotation(player, poi.rot);
                }
                sendMessage(player, "Teleported to " + poi.name + "!");
            } else if (pois.size() == 0) {
                sendMessage(player, "No Poi with name : '" + value + "' was found");
            } else {
                if (pois.size() > Osm2map.POI_PAGE_SIZE) {
                    int originalSize = pois.size();
                    pois = pois.subList(0, Osm2map.POI_PAGE_SIZE);
                    sendMessage(player, pois.size() + "/" + originalSize + " result(s)");
                } else {
                    sendMessage(player, pois.size() + " result(s)");
                }
                printPois(player, pois);
            }
        } else {
            int pageNumber = 1;
            if (valueArgs.length > 1 && isStringInt(valueArgs[1])) {
                pageNumber = Integer.parseInt(valueArgs[1]);
            }
            List<Poi> pois = data.getPoiFromPage(pageNumber);
            sendMessage(player, pois.size() + " result(s)      page: " + pageNumber + "/" + data.getNbPages());
            printPois(player, pois);
        }

    }

    @Command(
            aliases = {"zone"},
            desc = "Tp zone",
            usage = "list | [zoneId]"
    )
    @Require("osm2map.otp.zone")
    public void otpZone(@Sender Player player, @Merged String value) {
        if(!plugin.getConfig(player.getWorld().getName()).guard.active){
            Util.sendMessage(player, "Guard not active !", ChatColor.RED);
            return;
        }
        DBMapData data = plugin.getMapData(player.getWorld().getName());
        String[] valueArgs = value.split(" ");
        if (valueArgs.length == 1) {
            if (valueArgs[0].equals("list")) {
                Set<Long> zonesId = data.getZonesByOwnerOrFriend(player.getName(), true);
                sendMessage(player, zonesId.size() + " results");
                for (Long id : zonesId) {
                    JtsZone z = data.getZone(id);
                    Vector2i pos = z.getPoints().get(0);
                    TextComponent tpButton = builder("[tp]", ChatColor.GOLD);
                    tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map otp zone " + z.id));
                    sendMessage(player, builder(
                            builder((z.owner.equals("*") ? "Public " : "") + "EZone " + id + " (x: " + pos.x + " ,z: " + pos.y + " )"),
                            tpButton
                    ));
                }
            } else {
                JtsZone z = data.getZone(Long.parseLong(value));
                Vector2i max = new Vector2i();
                int count = 0;
                for (Vector2i pixel : z.getPoints()) {
                    max.add(pixel);
                    count++;
                }
                Vector2i tpPoint = max.div(count);

                tp(player, tpPoint.toDouble());

                z.computePixels(null, null);

                List<Vector2i> effectPoint = new ArrayList<>(z.getPoints()).stream().distinct().collect(Collectors.toList());
                List<Vector2i> effectLinesTmp = new ArrayList<>(z.getLinePixels()).stream().distinct().filter(l -> !effectPoint.contains(l)).collect(Collectors.toList());

                final List<Vector2i> effectLines;
                if (effectLinesTmp.size() > 50) {
                    Collections.shuffle(effectLinesTmp);
                    effectLines = effectLinesTmp.subList(0, 50);
                } else {
                    effectLines = effectLinesTmp;
                }

                int countParticle = 50;
                double offset = 0.25;
                Random random = new Random();
                Runnable runnable = () -> {
                    for (Vector2i pixel : effectPoint) {
                        for (int i = 0; i < countParticle; i++) {
                            double posX = pixel.x + 0.5 + (random.nextFloat() * offset * 2 - offset);
                            double posY = plugin.getElevByBlockXZ(player.getWorld().getName(), pixel.x, pixel.y) + 3 + (random.nextFloat() * offset * 2 - offset);
                            double posZ = pixel.y + 0.5 + (random.nextFloat() * offset * 2 - offset);
                            player.spawnParticle(Particle.REDSTONE, posX, posY, posZ, 0, 183d / 256d, 28d / 256d, 28d / 256d, 1);
                        }
                    }
                    for (Vector2i pixel : effectLines) {
                        for (int i = 0; i < countParticle; i++) {
                            double posX = pixel.x + 0.5 + (random.nextFloat() * offset * 2 - offset);
                            double posY = plugin.getElevByBlockXZ(player.getWorld().getName(), pixel.x, pixel.y) + 3 + (random.nextFloat() * offset * 2 - offset);
                            double posZ = pixel.y + 0.5 + (random.nextFloat() * offset * 2 - offset);
                            player.spawnParticle(Particle.REDSTONE, posX, posY, posZ, 0, 3d / 256d, 169d / 256d, 244d / 256d, 1);
                        }
                    }
                };

                runnable.run();

                for (int i = 1; i <= 4; i++) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, i * 20);
                }
            }
        } else {
            sendMessage(player, "Syntax Error !", ChatColor.RED);
        }
    }

    private void printPois(Player src, List<Poi> pois) {
        for (Poi poi : pois) {
            TextComponent extra = builder("[x=" + poi.pos.getX() + ", y=" + poi.pos.getY() + ", z=" + poi.pos.getZ() + "]", ChatColor.GOLD);
            extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/osm2map otp poi " + poi.name));
            sendMessage(src, builder(builder(poi.name + " "), extra));
        }
    }

    private void tp(Player player, Vector2d pos) {
        int y = plugin.getElevByBlockXZ(player.getWorld().getName(), (int) pos.x, (int) pos.y) + 10;
        tp(player, player.getWorld(), new Vector3d(pos.x, y, pos.y));
    }

    private void tp(Player player, World world, Vector3d prev) {
        Vector3d pos = Util.correctPosByWorldBorders(world, prev);
        if (prev.compareTo(pos) != 0) {
            sendMessage(player, "Cannot tp over World Borders", ChatColor.RED);
        }
        player.teleport(new Location(world, pos.getX(), pos.getY(), pos.getZ()));
    }

    public boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    void setRotation(Player player, Vector3d rot) {
        player.getLocation().setDirection(new Vector(rot.getX(), rot.getY(), rot.getZ()));
    }
}
