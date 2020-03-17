package com.wardenfar.osm2map.map.entity;

import com.flowpowered.math.vector.Vector3d;

public class Poi {

    public long id;
    public String name;
    public Vector3d pos;
    public Vector3d rot;

    public Poi() {
        this(-1, "", new Vector3d(), new Vector3d());
    }

    public Poi(long id, String name, Vector3d pos, Vector3d rot) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.rot = rot;
    }

    public String getName() {
        return name;
    }
}
