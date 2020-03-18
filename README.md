# Osm2Map (Real world in Minecraft)

Osm2Map is only available for `Spigot 1.12.2` for now

Features :
- **Custom World Generator** based on OpenStreetMap data
   - Buildings
   - Gardens
   - Water
   - Trees
- **Teleporting to Ways or POIs** by name
- **Multi World Support**
   - One Different Config for each World
- **Builtin Guard System** (optional)
   - Claim/Unclaim zones (buildings/gardens)
   - Build with friends in the same zone
   - Support of FAWE's (Fast WorldEdit) Mask system 
- **Customizable**
   - Change all blocks used by the Generator
   - 

[InternLink](docs/CONTRIBUTING.md)

## Installation

### Requirements (FAWE)
Install Fast Async WorldEdit :
[https://intellectualsites.github.io/download/fawe.html](https://intellectualsites.github.io/download/fawe.html)

Run the server once for Fawe to install correctly

### Requirements (OpenStreetMap)
You need to download the map you want with the `format OSM XML`

[How to download a OSM XML map ?](#get-osm-xml-map-file)

### Osm2Map
First, download and put `osm2map.jar` in `plugins/` folder

put .osm

execute `osm2map.jar` or `java -jar osm2map.jar` to generate the default config 
then edit the config file in `plugins/osm2map/config/config.json`

[see Config Documentation](#config-documentation)

[see Guard Mode](#guard)

---

#### bukkit config
```yaml
# end of bukkit.yml
worlds:
  world: # replace "world" by the name of your world
    generator: osm2map
```

## Get OSM XML Map File

- For small maps :
  - Go to [https://www.openstreetmap.org/export](https://www.openstreetmap.org/export)
  - Select a zone and click export
  - Save the file where you want

- For Larger Areas :
  - Download containing Area using :
     - [http://download.geofabrik.de/](http://download.geofabrik.de/)
     - OR
     - [https://extract.bbbike.org/](https://extract.bbbike.org/)
  - Then cut the area you want with [osmconvert](https://wiki.openstreetmap.org/wiki/Osmconvert)
  - `osmconvert [inputFile] -b=[lon West],[lat South],[lon East],[lat North] --complete-ways --complete-multipolygons -o=world.osm`
  
  
Then set in `config.json` the correct path of your .osm file

## General Commands

#### Teleportation
| Command | Description | Permission |
| --- | --- | --- |
| /otp way \[search\] | Search a way and list or tp directly | _`osm2map.otp.way`_ |
| /otp poi list | List all POIs | _`osm2map.otp.poi`_ |
| /otp poi \[search\] | Search a POI and list or tp directly | _`osm2map.otp.poi`_ |

#### POIs
| Command | Description | Permission |
| --- | --- | --- |
| /poi add \[POI name\] | Add a POI | _`osm2map.poi.add`_ |
| /poi list | List all POIs | _`osm2map.poi.list`_ |
| /poi remove \[POI name\] | Remove a POI | _`osm2map.poi.remove`_ |

## Guard Commands

#### Zone Claim/Unclaim
| Command | Description | Permission |
| --- | --- | --- |
| /claim (friend) (friend) ... | Claim actual Zone with optional Friends | _`osm2map.otp.way`_ |
| /unclaim                     | Unclaim actual Zone                     | _`osm2map.otp.poi`_ |

Zone Managing Commands

Admin Commands

#### POIs
| Command | Description | Permission |
| --- | --- | --- |
| /poi add \[POI name\] | Add a POI | _`osm2map.poi.add`_ |
| /poi list | List all POIs | _`osm2map.poi.list`_ |
| /poi remove \[POI name\] | Remove a POI | _`osm2map.poi.remove`_ |

## Config Documentation

```json5
{
    "worlds": [
        {
            "name": "world", // Exact name of your world
            "forceInit": false, // If true : force init world Borders, world Spawn point, ... (reset automatically to false)
            "config": { // World Config
                "maxClaim": 3, // GUARD Mode : max claim at the same time
                "maxFriend": 3, // GUARD Mode : max friends by claim
                "databaseFile": "plugins/osm2map/paris-o2m.db", // All Data of o2m (different for each World) 
                "generation": { 
                    "osmFile": "plugins/osm2map/paris.osm", // OSM XML Map file
                    "zoom": 1.0, // Zoom divider : 0.5 => 2x bigger
                    "height": 10, // World gen min Height
                    "elevation": { // Elevation -> see Section "Elevation"
                        "enabled": false,
                        "folder": "plugins/osm2map/paris/elev"
                    },
                    "dirtLayerSize": 5,
                    "treeSchematicsEnabled": true, // Enable Tree Generation
                    "treeSchematicsFolder": "plugins/osm2map/schematic/tree/medium", // Folder of trees schematics
                    "seed": 1 // Random Seed (not very used) 
                },
                "guard": { // GUARD Mode
                    "active": true
                },
                "tile": { // Tile is for coloring the terrain based on satellite images
                    // see Section "Satellite Images (Tiles)"
                    "active": true,
                    "tilesFolder": "plugins/osm2map/tiles",
                    "tilesUrl": "https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/{zoom}/{y}/{x}",
                    "blockColorsFile": "plugins/osm2map/groundBlocks.txt",
                    "tilesZoom": 17,
                    "offsetX": 0, // if your map and satellite images are not exactly aligned 
                    "offsetY": 0,
                    "mulR": 1.2, // Color Channel multiplier
                    "mulG": 1.5, // More Greenish for better look
                    "mulB": 1.2,
                    "blur": { // Blur on satellite images (Recommended for better results)
                        "active": true,
                        "blurSize": 3,
                        "blurStrength": 0.5
                    }
                },
                "blocks": { // Generation Blocks Settings
                    "wayBlock": "minecraft:coal_block",
                    "wayBorderBlock": "minecraft:stone_slab",
                    "wayTrackBlock": "minecraft:planks",
                    "buildingsBorderBlock": "minecraft:cobblestone",
                    "buildingsCornerBlock": "minecraft:stonebrick",
                    "buildingsFillBlock": "minecraft:planks",
                    "gardensCornerBlock": "minecraft:fence",
                    "gardensBorderBlock": "minecraft:leaves",
                    "groundTopBlock": "minecraft:grass",
                    "groundMiddleBlock": "minecraft:dirt",
                    "groundBottomBlock": "minecraft:bedrock",
                    "waterWaysBlock": "minecraft:water"
                }
            }
        }//,
        // { name: "world2", ... }
    ]
}
```

## Satellite Images (Tiles)

blockColor.txt

## Guard

Built-in World Protector 
- Claims
- WorldEdit RegionMask
- Grieffing
- Disable Entities

## Elevation

**Work in-progress**

## OSM Parser

- zone : building
- water : ?
- garden : ?
- poi : ?

