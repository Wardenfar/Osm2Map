package com.wardenfar.osm2map.config;

public class TileConfig {

    public boolean active = true;
    public String tilesFolder = "plugins/osm2map/tiles";
    public String tilesUrl = "https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/{zoom}/{y}/{x}";
    public String blockColorsFile = "plugins/osm2map/groundBlocks.txt";
    public int tilesZoom = 17;
    public int offsetX = 0;
    public int offsetY = 0;
    public float mulR = 1.2f;
    public float mulG = 1.5f;
    public float mulB = 1.2f;
    public BlurConfig blur = new BlurConfig();

}
