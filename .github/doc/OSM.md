# Get a OSM XML Map file

- For small areas :
  - Go to [https://www.openstreetmap.org/export](https://www.openstreetmap.org/export)
  - Select a zone and click export
  - Save the file where you want

- For larger areas :
  - Download bigger Area using :
     - [http://download.geofabrik.de/](http://download.geofabrik.de/)
     - OR
     - [https://extract.bbbike.org/](https://extract.bbbike.org/)
  - Then cut the area you want with [osmconvert](https://wiki.openstreetmap.org/wiki/Osmconvert)
  - `osmconvert [inputFile] -b="[lon West],[lat South],[lon East],[lat North]" --complete-ways --complete-multipolygons -o="world.osm"`
  
Then update `config.json` the correct path of your .osm file