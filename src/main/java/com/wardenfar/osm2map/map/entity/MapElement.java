package com.wardenfar.osm2map.map.entity;

import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.PolygonFiller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapElement {

    public long id;
    public Type type;
    public List<ENode> nodes;
    public PolygonFiller polygon;
    public Map<String,String> tags;

    public MapElement() {
        this(-1, null, new ArrayList<>(),new HashMap<>());
    }

    public MapElement(long id, Type type, List<ENode> nodes, Map<String,String> tags) {
        this.id = id;
        this.type = type;
        this.nodes = nodes;
        this.tags = tags;
    }

    public void createPolygon(DBMapData data) {
        polygon = PolygonFiller.fromNodeList(nodes, data);
    }

    public void write(DataOutputStream data) throws IOException {
        data.writeLong(id);
        data.writeInt(type == null ? -1 : type.ordinal());

        data.writeInt(nodes.size());
        for (ENode n : nodes) {
            data.writeLong(n.id);
        }

        data.writeInt(tags.size());
        for(Map.Entry<String, String> e : tags.entrySet()){
            data.writeUTF(e.getKey());
            data.writeUTF(e.getValue());
        }
    }

    public void read(DataInputStream data, Map<Long, ENode> dataNodes) throws IOException {
        id = data.readLong();
        int ordinal = data.readInt();
        type = ordinal == -1 ? null : Type.values()[ordinal];

        nodes = new ArrayList<>();
        int count = data.readInt();
        for (int i = 0; i < count; i++) {
            nodes.add(dataNodes.get(data.readLong()));
        }

        tags = new HashMap<>();
        count = data.readInt();
        for (int i = 0; i < count; i++) {
            tags.put(data.readUTF(),data.readUTF());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MapElement)) {
            return false;
        }
        MapElement z = (MapElement) o;
        return this.id == z.id;
    }

    public enum Type {
        WATERWAY,
        BUILDING_PART,
    }
}
