# Installation

## Requirements (FAWE)
Install Fast Async WorldEdit :
[https://intellectualsites.github.io/download/fawe.html](https://intellectualsites.github.io/download/fawe.html)

Run the server once for Fawe to install correctly

## Requirements (OpenStreetMap)
You need to download the map you want with the `format OSM XML`

[How to download a OSM XML map ?](OSM.md#get-a-osm-xml-map-file)

## Osm2Map

- Download and put `osm2map.jar` in `plugins/` folder
- Execute `osm2map.jar` or `java -jar osm2map.jar` to generate the default config 
- Edit the config file [(CONFIG.md)](CONFIG.md) in `plugins/osm2map/config/config.json`
- Put `Github : assets/groundBlocks.txt` in `plugins/osm2map/groundBlocks.txt`
> :warning: **If you change the .osm**: Delete the world and set **forceInit** to **true** in the world's config to force reload the .osm file

#### Configure Bukkit
```yaml
# end of bukkit.yml
worlds:
  world: # replace "world" by the name of your world
    generator: osm2map
```
