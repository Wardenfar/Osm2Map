package com.wardenfar.osm2map.config;

import com.wardenfar.osm2map.map.ElevFile;

import java.util.HashMap;
import java.util.Map;

public class Worlds {

    public World[] worlds = new World[]{new World()};
    public transient Map<String, ElevFile> elevFileMap = new HashMap<>();

    public World getWorld(String world){
        for(World c : worlds){
            if(c.name.equals(world)){
                return c;
            }
        }
        return null;
    }
}
