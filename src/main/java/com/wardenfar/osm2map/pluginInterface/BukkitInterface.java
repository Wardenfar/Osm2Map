package com.wardenfar.osm2map.pluginInterface;

import com.wardenfar.osm2map.Util;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public class BukkitInterface extends Interface {

    public BukkitInterface() {
        Util.createMap();
    }

    private Object getMaterialData(int id, int data) {
        return new MaterialData(Material.getMaterial(id), (byte) data);
    }

    @Override
    public Object getBlock(int id) {
        return getMaterialData(id, 0);
    }

    @Override
    public Object getBlock(int id, int variant) {
        return getMaterialData(id, variant);
    }

    @Override
    public Object getBlock(String id) {
        Integer intId = Util.getBlockId(id);
        Integer varId = Util.getVariantId(id);
        if (intId == null) {
            return null;
        }
        return getBlock(intId, varId);
    }

    @Override
    public Integer getBlockId(Object block) {
        if (block instanceof MaterialData) {
            MaterialData md = (MaterialData) block;
            return getBlockId(md.getItemType());
        }
        return null;
    }

    public Integer getBlockId(Material block) {
        return block.getId();
    }

    @Override
    public Class getBlockClass() {
        return MaterialData.class;
    }
}
