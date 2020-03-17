package com.wardenfar.osm2map.terrainGeneration;

import com.wardenfar.osm2map.map.entity.Vector2i;
import org.bukkit.generator.ChunkGenerator;

public class SuperChunkData {

    ChunkGenerator.ChunkData data;
    Vector2i offset;

    public SuperChunkData(ChunkGenerator.ChunkData data, Vector2i offset) {
        this.data = data;
        this.offset = offset;
    }
}
