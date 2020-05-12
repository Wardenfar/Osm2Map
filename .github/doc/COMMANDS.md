- [Installation](.github/doc/INSTALL.md)
- [Configuration](.github/doc/CONFIG.md)
- [Commands and Permissions](.github/doc/COMMANDS.md)
- [Guard Mode](.github/doc/GUARD.md)
- [Satellite Images (ground coloring)](.github/doc/TILES.md)
- [OpenStreetMap Parsing](.github/doc/OSM.md)

# General Commands

## Teleportation
| Command | Description | Permission |
| --- | --- | --- |
| /o2m otp way \[search\] | Search a way and list or tp directly | _`osm2map.otp.way`_ |
| /o2m otp poi list | List all POIs | _`osm2map.otp.poi`_ |
| /o2m otp poi \[search\] | Search a POI and list or tp directly | _`osm2map.otp.poi`_ |
| /o2m otp coord \[lat\] \[lon\] | Tp to coordinates | _`osm2map.otp.coord`_ |

## POIs
| Command | Description | Permission |
| --- | --- | --- |
| /o2m poi add \[POI name\] | Add a POI | _`osm2map.poi.add`_ |
| /o2m poi list | List all POIs | _`osm2map.poi.list`_ |
| /o2m poi remove \[POI name\] | Remove a POI | _`osm2map.poi.remove`_ |

# Guard Mode Commands

## General

| Command | Description | Permission |
| --- | --- | --- |
| /o2m claim (friends)  | Claim current Zone with optional friends | _`osm2map.claim`_ |
| /o2m unclaim                 | Unclaim current Zone                     | _`osm2map.unclaim`_ |
| /o2m otp zone list | List your zones | _`osm2map.otp.zone`_ |

## Zone Managing Commands

| Command | Description | Permission |
| --- | --- | --- |
| /o2m zone info | Info of the current Zone | _`osm2map.zone.info`_ |
| /o2m zone status (player) | Info of all Claimed Zones | _`osm2map.zone.status`_ |
| /o2m zone remove | Remove the current Zone from DB | _`osm2map.zone.remove`_ |
| /o2m zone create \[BUILDING|GARDEN\] \[bool:gen\] | Create a Zone from WorldEdit Selection | _`osm2map.zone.create`_ |

## Admin Commands

| Command | Description | Permission |
| --- | --- | --- |
| /o2m admin getcoord | Current Location (Lat/Lon) | _`osm2map.admin.getcoord`_ |
| /o2m admin claim as \[asPlayer\] (friend) (friend) | Claim the current zone as a Player | _`osm2map.admin.claim.as`_ |
| /o2m admin claim public | Claim the current zone in Public Mode | _`osm2map.admin.claim.public`_ |
| /o2m admin unclaim as | Unclaim the current zone | _`osm2map.admin.unclaim.as`_ |
| /o2m admin unclaim allof (Player) | Unclaim all zone of a Player | _`osm2map.admin.unclaim.allof`_ |

## Modo Commands (Modos don't have zone restriction)

| Command | Description | Permission |
| --- | --- | --- |
| /o2m modo list | List Modos (per World) | _`osm2map.modo.list`_ |
| /o2m modo add \[player\] | Add Modo (per World) | _`osm2map.modo.add`_ |
| /o2m modo remove \[player\] | Remove Modo (per World) | _`osm2map.modo.remove`_ |

## POIs
| Command | Description | Permission |
| --- | --- | --- |
| /o2m poi add \[POI name\] | Add a POI | _`osm2map.poi.add`_ |
| /o2m poi list | List all POIs | _`osm2map.poi.list`_ |
| /o2m poi remove \[POI name\] | Remove a POI | _`osm2map.poi.remove`_ |
