package com.wardenfar.osm2map.config;

public class GenerationConfig {

    public String osmFile = "plugins/osm2map/world.osm";

    public double zoom = 1.0;

    public int height = 10;
    public ElevConfig elevation = new ElevConfig();
    public int dirtLayerSize = 5;

    public boolean treeSchematicsEnabled = false;
    public String treeSchematicsFolder = "plugins/osm2map/schematic/tree/medium";
    public int seed = 1;
}
