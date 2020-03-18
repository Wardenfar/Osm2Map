package com.wardenfar.osm2map.config;

import com.wardenfar.osm2map.db.Database;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.ElevFile;
import com.wardenfar.osm2map.tile.Tiles;

import java.util.List;

public class World {

    public String name = "world";
    public boolean forceInit = false;
    public Config config = new Config();

    public transient boolean forceInitSave = false;

    public transient boolean init = false;
    public transient Database db;
    public transient DBMapData mapData;

    public transient Tiles tiles;
    public transient List<ElevFile> elevFiles;

}
