package com.wardenfar.osm2map;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Create config file ...");
        Util.readOrCreateWorlds("../" + Osm2map.configFile);
        System.out.println("Done !");
    }
}
