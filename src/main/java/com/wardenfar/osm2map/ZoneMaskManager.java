package com.wardenfar.osm2map;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.FawePlayer;
import com.boydti.fawe.regions.FaweMask;
import com.boydti.fawe.regions.FaweMaskManager;
import com.boydti.fawe.regions.SimpleRegion;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;
import com.wardenfar.osm2map.config.World;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.Vector2d;
import com.wardenfar.osm2map.map.entity.Vector2i;

public class ZoneMaskManager extends FaweMaskManager {

    Osm2map plugin;

    public ZoneMaskManager(Osm2map plugin) {
        super("Osm2Map");
        this.plugin = plugin;
    }

    @Override
    public FaweMask getMask(FawePlayer fp, MaskType type) {
        String player = fp.getName();
        String worldName = fp.getWorld().getName();

        World pluginWorld = plugin.getWorlds().getWorld(worldName);
        if(pluginWorld != null && pluginWorld.config.guard.active){
            DBMapData mapData = plugin.getMapData(worldName);

            Vector2d tl = mapData.getTopLeftPos();
            Vector2d br = mapData.getBottomRightPos();

            Region maskedRegion = new SimpleRegion(FaweAPI.getWorld(worldName), new Vector(tl.x, 0, tl.y), new Vector(br.x, 256, br.y)) {
                @Override
                public boolean contains(int x, int y, int z) {
                    if(contains(x, z)){
                        int dirtLayerSize = pluginWorld.config.generation.dirtLayerSize;
                        int elev = plugin.getElevByBlockXZ(worldName, x, z);
                        int bedrockTopY = Math.max(0, elev - dirtLayerSize - 2) + 1;
                        return y > bedrockTopY;
                    }
                    return false;
                }

                @Override
                public boolean contains(int x, int z) {
                    return mapData.isPlayerCanChangeAt(player, new Vector2i(x, z), true);
                }
            };

            return new FaweMask(maskedRegion, "Osm2Map") {
                @Override
                public boolean isValid(FawePlayer player, MaskType type) {
                    //return mapData.isPlayerCanChangeAt(player.getName(), new Vector2i(player.getLocation().x, player.getLocation().z), true);
                    return true;
                }
            };
        }

        return null;
    }
}
