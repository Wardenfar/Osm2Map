package com.wardenfar.osm2map.pluginInterface;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.wardenfar.osm2map.Util;

public class SchemInterface extends Interface {

    public SchemInterface() {
        Util.createMap();
    }

    @Override
    public Object getBlock(int id) {
        return new BaseBlock(id);
    }

    @Override
    public Object getBlock(int id, int variant) {
        return new BaseBlock(id, variant);
    }

    @Override
    public Object getBlock(String id) {
        Integer blockId = Util.getBlockId(id);
        return blockId == null ? null : new BaseBlock(blockId, Util.getVariantId(id));
    }

    @Override
    public Integer getBlockId(Object block) {
        if (block instanceof BaseBlock) {
            return ((BaseBlock) block).getId();
        }
        return null;
    }

    @Override
    public Class getBlockClass() {
        return BaseBlock.class;
    }
}
