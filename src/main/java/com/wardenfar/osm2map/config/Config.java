package com.wardenfar.osm2map.config;

public class Config {

    public String databaseFile = "plugins/osm2map/paris-o2m.db";
    public GenerationConfig generation = new GenerationConfig();
    public GuardConfig guard = new GuardConfig();
    public TileConfig tile = new TileConfig();
    public BlocksConfig blocks = new BlocksConfig();
}
