package com.wardenfar.osm2map;

import com.boydti.fawe.FaweAPI;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.wardenfar.osm2map.command.*;
import com.wardenfar.osm2map.command.provider.ZoneTypeProvider;
import com.wardenfar.osm2map.config.Config;
import com.wardenfar.osm2map.config.World;
import com.wardenfar.osm2map.config.Worlds;
import com.wardenfar.osm2map.db.Database;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.ElevFile;
import com.wardenfar.osm2map.map.entity.JtsZone;
import com.wardenfar.osm2map.map.entity.LatLon;
import com.wardenfar.osm2map.map.entity.Vector2d;
import com.wardenfar.osm2map.map.entity.Vector2i;
import com.wardenfar.osm2map.pluginInterface.BukkitInterface;
import com.wardenfar.osm2map.pluginInterface.Interface;
import com.wardenfar.osm2map.request.RequestManager;
import com.wardenfar.osm2map.terrainGeneration.OsmElevGenerator;
import com.wardenfar.osm2map.tile.Tiles;
import li.l1t.common.CommandRegistrationManager;
import li.l1t.common.intake.CommandsManager;
import li.l1t.common.intake.IntakeCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.wardenfar.osm2map.Util.sendMessage;

public class Osm2map extends JavaPlugin implements Listener {

    public static String VERSION = "3.2";

    public static int POI_PAGE_SIZE = 15;

    public static String configFile = "plugins/osm2map/config/config.json";
    private Worlds worlds;

    private Interface inter;
    private Map<Class<?>, Map<Vector3i, Object>> blocksByInterfaceBlockType = new HashMap<>();

    private OsmElevGenerator osmElevGenerator;

    private RequestManager requestManager;

    private String[] blacklistedPlaceBlocksIDs = new String[]{
            "minecraft:powered_comparator",
            "minecraft:redstone_wire",
            "minecraft:redstone_torch",
            "minecraft:powered_repeater",
            "minecraft:observer",
            "minecraft:piston",
            "minecraft:sticky_piston",
            "minecraft:tnt",
            "minecraft:bedrock",
            "minecraft:fire",
            "minecraft:redstone_block",
            "minecraft:lava",
            "minecraft:flowing_lava",
            "minecraft:water",
            "minecraft:flowing_water",
            "minecraft:portal",
            "minecraft:end_portal",
            "minecraft:rail",
            "minecraft:golden_rail",
            "minecraft:detector_rail",
            "minecraft:activator_rail",
            "minecraft:command_block",
            "minecraft:chain_command_block",
            "minecraft:repeating_command_block",
            "minecraft:structure_block",
    };
    private String[] blacklistedBreakBlocksIDs = new String[]{
            "minecraft:bedrock",
    };
    private Material[] blacklistedItemsIDs = new Material[]{
            Material.FLINT_AND_STEEL,
            Material.BUCKET,
            Material.WATER_BUCKET,
            Material.LAVA_BUCKET,
            Material.FIREBALL,
            Material.ACTIVATOR_RAIL
    };
    private Class<?>[] whitelistEntityClasses = new Class[]{
            ItemFrame.class,
            FallingBlock.class,
    };

    @Override
    public void onDisable() {
        getLogger().info("Close Database");
        for (World world : worlds.worlds) {
            if (world.db != null) {
                world.db.close();
            }
        }
    }

    /* if (isInList(type.getId(), blacklistedItemsIDs)) {
                event.setCancelled(true);
                sendMessage(player, "Blacklisted Item (in Hand) :(", TextColors.RED);
                return;
            }*/

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!isGuardActive(p.getWorld())) {
            return;
        }
        Material type = p.getItemInHand().getType();

        boolean found = false;
        for (Material m : blacklistedItemsIDs) {
            if (type.equals(m)) {
                found = true;
                break;
            }
        }
        if (found) {
            event.setCancelled(true);
            sendMessage(p, "Blacklisted item !", ChatColor.RED);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (!isGuardActive(event.getEntity().getWorld())) {
            return;
        }
        event.setCancelled(isCancelledSpawnEntity(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!isGuardActive(event.getEntity().getWorld())) {
            return;
        }
        event.setCancelled(isCancelledSpawnEntity(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawnEvent(ItemSpawnEvent event) {
        if (!isGuardActive(event.getEntity().getWorld())) {
            return;
        }
        event.setCancelled(isCancelledSpawnEntity(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingPlaceEvent(HangingPlaceEvent event) {
        if (!isGuardActive(event.getEntity().getWorld())) {
            return;
        }
        event.setCancelled(isCancelledSpawnEntity(event.getEntity()));
        if (!event.isCancelled()) {
            World world = worlds.getWorld(event.getEntity().getWorld().getName());
            if (world != null) {
                String playerName = event.getPlayer().getName();
                if (world.mapData.getModos().contains(playerName)) {
                    event.setCancelled(false);
                } else if (world.mapData.isPlayerCanChangeAt(playerName, new Vector2i(event.getEntity().getLocation().getBlockX(), event.getEntity().getLocation().getBlockZ()), true)) {
                    event.setCancelled(false);
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(false);
            }
        }
    }

    private boolean isCancelledSpawnEntity(Entity e) {
        boolean found = false;
        for (Class c : whitelistEntityClasses) {
            if (c.isInstance(e)) {
                found = true;
                break;
            }
        }
        return !found;
    }

    @EventHandler()
    public void onWorldInitEvent(WorldInitEvent event) {
        initWorld(event.getWorld().getName());
    }

    @EventHandler()
    public void onWorldLoadEvent(WorldLoadEvent event) {
        Logger logger = getLogger();

        World world = getWorlds().getWorld(event.getWorld().getName());

        if (world != null && world.forceInitSave) {
            org.bukkit.World w = getServer().getWorld(world.name);
            Vector2d topLeftBlockPos = world.mapData.project(world.mapData.getTopLeft());
            Vector2d bottomRightBlockPos = world.mapData.project(world.mapData.getBottomRight());
            int xMin = (int) Math.min(topLeftBlockPos.x, bottomRightBlockPos.x);
            int zMin = (int) Math.min(topLeftBlockPos.y, bottomRightBlockPos.y);
            int xMax = (int) Math.max(topLeftBlockPos.x, bottomRightBlockPos.x);
            int zMax = (int) Math.max(topLeftBlockPos.y, bottomRightBlockPos.y);

            int centerX = (int) (((double) xMin + (double) xMax) / 2.0);
            int centerZ = (int) (((double) zMin + (double) zMax) / 2.0);

            logger.info("Init World (Spawn Point,Gamerules,Borders)");
            logger.info("Set Spawn Point (" + centerX + "," + centerZ + ")");

            w.setSpawnLocation(centerX, getElevByBlockXZ(world.name, centerX, centerZ) + 15, centerZ);

            if (!isGuardActive(world)) {
                w.getWorldBorder().setCenter(centerX, centerZ);
                w.getWorldBorder().setSize(Math.max(centerX, centerZ) * 2);
                w.setTime(6000);
                w.setGameRuleValue("doDaylightCycle", "false");
                w.setGameRuleValue("keepInventory", "true");
                w.setGameRuleValue("doWeatherCycle", "false");
                w.setGameRuleValue("doFireTick", "false");
                w.setGameRuleValue("mobGriefing", "false");
                w.setGameRuleValue("spawnRadius", "1");
                w.setGameRuleValue("mobGriefing", "false");
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        inter = new BukkitInterface();
        requestManager = new RequestManager(this);

        CommandsManager commandsManager = new CommandsManager(this);

        commandsManager.bind(JtsZone.Type.class).toProvider(new ZoneTypeProvider());

        // @formatter:off
        Dispatcher dispatcher = commandsManager.getCommandGraph().commands()
            .group("osm2map", "o2m")
                .registerMethods(new O2mCommands(this))
                .group("claim")
                    .registerMethods(new ClaimCommand(this))
                    .parent()
                .group("unclaim")
                    .registerMethods(new UnclaimCommand(this))
                    .parent()
                .group("confirm")
                    .registerMethods(new ConfirmCommand(this))
                    .parent()
                .group("cancel")
                    .registerMethods(new CancelCommand(this))
                    .parent()
                .group("otp")
                    .registerMethods(new OtpCommands(this))
                    .parent()
                .group("poi")
                    .registerMethods(new PoiCommands(this))
                    .parent()
                .group("modo")
                    .registerMethods(new ModoCommands(this))
                    .parent()
                .group("zone")
                    .registerMethods(new ZoneCommands(this))
                    .parent()
                .group("admin")
                    .registerMethods(new AdminCommands(this))
                    .group("claim")
                        .registerMethods(new AdminClaimCommands(this))
                        .parent()
                    .group("unclaim")
                        .registerMethods(new AdminUnclaimCommands(this))
                        .parent()
                    .parent()
                .parent()
            .graph().getDispatcher();
        // @formatter:on

        IntakeCommand command = new IntakeCommand(commandsManager, dispatcher, "o2m", Collections.singletonList("osm2map"));
        commandsManager.applyNamespaceTemplateTo(command.getNamespace());
        new CommandRegistrationManager().registerCommand(command, commandsManager.getFallbackPrefix());
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        try {
            worlds = Util.readOrCreateWorlds(configFile);

            for (World world : worlds.worlds) {
                if (isGuardActive(world)) {
                    FaweAPI.addMaskManager(new ZoneMaskManager(this));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initWorld(String worldName) {
        Logger logger = getLogger();
        World world = getWorlds().getWorld(worldName);
        if (world != null && !world.init) {
            try {
                Config config = world.config;

                File dbFile = new File(world.config.databaseFile);

                boolean init = world.forceInitSave;

                if (!init) {
                    logger.info("Read DB ... " + dbFile.getPath());
                } else {
                    logger.info("Create DB ..." + dbFile.getPath());
                }

                Database db = com.wardenfar.osm2map.db.Database.create(dbFile, init);
                world.db = db;

                DBMapData mapData;
                if (init) {
                    mapData = DBMapData.readFromOsmXML(this, db, logger, new FileInputStream(new File(config.generation.osmFile)), config.generation.height, config.generation.zoom);
                } else {
                    mapData = DBMapData.readFromDB(this, db);
                }
                world.mapData = mapData;

                world.elevFiles = new ArrayList<>();
                if (config.generation.elevation.enabled) {
                    logger.info("Load ElevationData ...");
                    File directory = new File(config.generation.elevation.folder);
                    for (File dataFile : directory.listFiles()) {
                        String filename = dataFile.getName();

                        if (!filename.endsWith(".elev")) {
                            logger.info("Don't end with .elev : " + filename);
                        } else if (!ElevFile.intersects(dataFile, mapData)) {
                            logger.info("Don't intersects : " + filename);
                        } else {
                            ElevFile elevFile = worlds.elevFileMap.get(filename);
                            if (elevFile != null) {
                                logger.info("Already Load " + filename);
                                world.elevFiles.add(elevFile);
                            } else {
                                logger.info("Load " + filename);
                                elevFile = ElevFile.importFile(logger, dataFile, mapData);
                                logger.info("Loaded !");
                                world.elevFiles.add(elevFile);
                                worlds.elevFileMap.put(filename, elevFile);
                            }
                        }
                    }

                    logger.info("Loaded " + world.elevFiles.size() + " Elevation Files");
                    if (world.elevFiles.isEmpty()) {
                        logger.info("Disable Elevation");
                        world.config.generation.elevation.enabled = false;
                    }
                }

                if (config.tile.active) {
                    world.tiles = new Tiles(this, world.name);
                }

                world.init = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        org.bukkit.World to = event.getTo().getWorld();
        if (!isGuardActive(to)) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("*")) {
            Vector3d prev = Util.toVector(event.getTo());
            Vector3d pos = Util.correctPosByWorldBorders(to, prev);
            if (prev.compareTo(pos) != 0) {
                sendMessage(event.getPlayer(), "Cannot tp over World Borders", ChatColor.RED);
                event.setTo(new Location(to, pos.getX(), pos.getY(), pos.getZ()));
            }
        }
    }

    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        if (!isGuardActive(event.getBlock().getWorld())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        if (!isGuardActive(event.getEntity().getWorld())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPortalCreateEvent(PortalCreateEvent event) {
        if (!isGuardActive(event.getWorld())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onFireworkExplodeEvent(FireworkExplodeEvent event) {
        if (!isGuardActive(event.getEntity().getWorld())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleCreateEvent(VehicleCreateEvent event) {
        if (!isGuardActive(event.getVehicle().getWorld())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onStructureGrowEvent(StructureGrowEvent event) {
        if (!isGuardActive(event.getWorld())) {
            return;
        }

        boolean cancelFinal = true;
        for (BlockState block : new ArrayList<>(event.getBlocks())) {
            boolean cancelled = isCancelledBlockChange(block.getLocation(), event.getPlayer(), block.getLocation().getBlock().getType(), block.getType());
            if (!cancelled) {
                cancelFinal = false;
            } else {
                event.getBlocks().remove(block);
            }
        }

        event.setCancelled(cancelFinal);
    }

    @EventHandler
    public void onBlockFormEvent(BlockFormEvent event) {
        if (!isGuardActive(event.getBlock().getWorld())) {
            return;
        }
        boolean cancelled = isCancelledBlockChange(event.getBlock().getLocation(), null, event.getBlock().getType(), event.getNewState().getType());
        event.setCancelled(cancelled);
    }

    @EventHandler
    public void onBlockFadeEvent(BlockFadeEvent event) {
        if (!isGuardActive(event.getBlock().getWorld())) {
            return;
        }
        boolean cancelled = isCancelledBlockChange(event.getBlock().getLocation(), null, event.getBlock().getType(), event.getNewState().getType());
        event.setCancelled(cancelled);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        boolean cancelled = isCancelledBlockChange(event.getBlock().getLocation(), event.getPlayer(), event.getBlock().getType(), Material.AIR);
        event.setCancelled(cancelled);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        boolean cancelled = isCancelledBlockChange(event.getBlock().getLocation(), event.getPlayer(), Material.AIR, event.getBlockPlaced().getType());
        event.setCancelled(cancelled);
    }

    private boolean isCancelledBlockChange(Location location, Player player, Material before, Material after) {
        Vector3i blockPos = Util.toVector(location).toInt();
        World world = worlds.getWorld(location.getWorld().getName());

        if (!isGuardActive(world)) {
            return false;
        }

        Vector2i pos = new Vector2i(blockPos.getX(), blockPos.getZ());

        Integer idBefore = ((BukkitInterface) inter).getBlockId(before);
        Integer idAfter = ((BukkitInterface) inter).getBlockId(after);

        if (isInList(Util.getStringId(idAfter), blacklistedPlaceBlocksIDs) || isInList(Util.getStringId(idBefore), blacklistedBreakBlocksIDs)) {
            if (player != null) {
                sendMessage(player, "BlackListed Block :(", ChatColor.RED);
            }
            return true;
        }
        if (player != null) {
            String playerName = player.getName();
            if (world.mapData.getModos().contains(playerName)) {
                return false;
            }
            if (world.mapData.isPlayerCanChangeAt(playerName, pos, true)) {
                return false;
            }

            sendMessage(player, "Restricted Area :(", ChatColor.RED);
        } else {
            //System.out.println("Restricted Area :(");
        }

        return true;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (osmElevGenerator == null) {
            osmElevGenerator = new OsmElevGenerator(this);
        }
        return osmElevGenerator;
    }

    public ElevFile getElevFileByLatLon(String worldName, double lat, double lon) {
        World world = worlds.getWorld(worldName);
        com.flowpowered.math.vector.Vector2d latlon = new com.flowpowered.math.vector.Vector2d(lat, lon);
        if (!world.elevFiles.isEmpty()) {
            ElevFile min = world.elevFiles.get(0);
            for (ElevFile elevFile : world.elevFiles) {
                if (elevFile.isInRangeByLatLon(latlon)) {
                    return elevFile;
                }
                if (elevFile.getDistanceToCenterByLatLon(latlon) < min.getDistanceToCenterByLatLon(latlon)) {
                    min = elevFile;
                }
            }
            return min;
        }
        return null;
    }

    public int getElevByBlockXZ(String worldName, int x, int z) {
        World world = worlds.getWorld(worldName);
        if (!world.config.generation.elevation.enabled) {
            return world.mapData.getHeight();
        } else {
            LatLon latlon = world.mapData.unproject(new Vector2i(x, z).toDouble());
            ElevFile elevFile = getElevFileByLatLon(worldName, latlon.lat, latlon.lon);
            if (elevFile == null) {
                System.out.println("ElevFile not found x=" + x + ",z=" + z);
                return world.mapData.getHeight();
            }
            int elev = (int) elevFile.getElevByLatLon(new com.flowpowered.math.vector.Vector2d(latlon.lat, latlon.lon));
            return elev + world.mapData.getHeight();
        }
    }

    public Object getSatelliteBlock(String worldName, Vector2i xz, Interface inter) {
        World world = worlds.getWorld(worldName);
        if (blocksByInterfaceBlockType.get(inter.getBlockClass()) == null) {
            blocksByInterfaceBlockType.put(inter.getBlockClass(), Util.getBlocksColors(world.config.tile.blockColorsFile, inter));
        }
        Color color = world.tiles.getColor(xz.add(new Vector2i(world.config.tile.offsetX, world.config.tile.offsetY)), true);
        Vector3i colorVector = new Vector3i(color.getRed(), color.getGreen(), color.getBlue());
        double distance = Double.MAX_VALUE;
        Object best = null;
        for (Map.Entry<Vector3i, Object> e : blocksByInterfaceBlockType.get(inter.getBlockClass()).entrySet()) {
            if (e.getKey().distance(colorVector) < distance) {
                distance = e.getKey().distance(colorVector);
                best = e.getValue();
            }
        }
        return best;
    }

    private boolean isInList(String id, String[] list) {
        for (String l : list) {
            if (l.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /*public Text[] getPluginDescTexts() {
        //Text t1 = Text.builder("Osm2Map ").color(TextColors.GOLD).append(Text.builder("1.3.2").color(TextColors.AQUA).build()).build();
        //Text t2 = Text.builder("by ").color(TextColors.AQUA).append(Text.builder("Wardenfar").color(TextColors.GOLD).build()).build();
        Text text = Text.of(TextColors.GOLD, "Osm2Map ", TextColors.AQUA, "1.5 ", TextColors.AQUA, "by ", TextColors.GOLD, "Wardenfar");
        //int size = Math.max(t1.toPlain().length(), t2.toPlain().length());
        Text separator = Text.builder(new String(new char[text.toPlain().length()]).replace("\0", "=")).color(TextColors.WHITE).build();
        return new Text[]{separator, text, separator};
    }

    public void sendPluginDesc(CommandSource src) {
        for (Text t : getPluginDescTexts()) {
            sendMessage(src, t);
        }
    }*/

    public boolean isGuardActive(org.bukkit.World w) {
        if (worlds.getWorld(w.getName()) == null) {
            return false;
        }
        return worlds.getWorld(w.getName()).config.guard.active;
    }

    public boolean isGuardActive(World w) {
        if (w == null) {
            return false;
        }
        return w.config.guard.active;
    }

    public DBMapData getMapData(String worldName) {
        World world = worlds.getWorld(worldName);
        return world.mapData;
    }

    public Config getConfig(String worldName) {
        World world = worlds.getWorld(worldName);
        return world.config;
    }

    public Interface getInterface() {
        return inter;
    }

    public Worlds getWorlds() {
        return worlds;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public OsmElevGenerator getOsmElevGenerator() {
        return osmElevGenerator;
    }
}
