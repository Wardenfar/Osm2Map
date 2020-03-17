# Osm2Map (Real world in Minecraft)

Osm2Map is only available for `1.12.2` for now

## Installation

### Requirements (FAWE)
Install Fast Async WorldEdit :
[https://intellectualsites.github.io/download/fawe.html](https://intellectualsites.github.io/download/fawe.html)

Run the server once for Fawe to install correctly

### Requirements (map.osm OpenStreetMap)
You need to download the map you want with the `format OSM XML`

[How to download a OSM XML map ?](#get-osm-xml-map-file)

### Osm2Map
First, download and put `osm2map.jar` in `plugins/` folder

---

#### plugin config
execute `osm2map.jar` or `java -jar osm2map.jar` to generate the default config 
then edit the config file in `plugins/osm2map/config/config.json`

[see Advanced Config](#advanced-config)

[see Guard Mode](#guard)

---

#### bukkit config
```yaml
# end of bukkit.yml
worlds:
  worldName: # replace "worldName" by the name of your world
    generator: osm2map
```

## Get OSM XML Map File

- For small maps :
  - Go to [https://www.openstreetmap.org/export](https://www.openstreetmap.org/export)
  - Select a zone and click export
  - Save the file where you want

- For Larger Areas :
  - d

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

#### POIs
| Command | Description | Permission |
| --- | --- | --- |
| /poi add \[POI name\] | Add a POI | _`osm2map.poi.add`_ |
| /poi list | List all POIs | _`osm2map.poi.list`_ |
| /poi remove \[POI name\] | Remove a POI | _`osm2map.poi.remove`_ |


## Advanced Config

## Guard