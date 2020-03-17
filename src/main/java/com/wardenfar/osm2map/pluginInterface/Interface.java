package com.wardenfar.osm2map.pluginInterface;

public abstract class Interface {

    public abstract Object getBlock(int id);

    public abstract Object getBlock(int id, int variant);

    public abstract Object getBlock(String id);

    public abstract Integer getBlockId(Object block);

    public Integer getBlockIdFromStringid(String strId) {
        return getBlockId(getBlock(strId));
    }

    public abstract Class getBlockClass();
}