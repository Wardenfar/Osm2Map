package com.wardenfar.osm2map.tile;

import com.flowpowered.math.vector.Vector3i;
import com.wardenfar.osm2map.Osm2map;
import com.wardenfar.osm2map.Util;
import com.wardenfar.osm2map.config.Config;
import com.wardenfar.osm2map.map.DBMapData;
import com.wardenfar.osm2map.map.entity.LatLon;
import com.wardenfar.osm2map.map.entity.Vector2d;
import com.wardenfar.osm2map.map.entity.Vector2i;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Tiles {

    Osm2map plugin;
    Config config;
    DBMapData mapData;
    private Map<String, Tile> tiles = new HashMap<>();

    public Tiles(Osm2map plugin, String worldName) {
        this.config = plugin.getConfig(worldName);
        this.mapData = plugin.getMapData(worldName);
    }

    public Color getColor(Vector2i block, boolean processed) {
        Vector2d xz = block.toDouble();
        LatLon latLon = mapData.unproject(xz);
        int zoom = config.tile.tilesZoom;
        double lat = latLon.lat;
        double lon = latLon.lon;

        Vector2i pos = Util.getTile(lat, lon, zoom);

        BufferedImage image = getTile(pos, processed);

        Vector2d topleftLatLon = Util.getLatLonFromTile(pos.x, pos.y, zoom);
        Vector2d bottomRightLatLon = Util.getLatLonFromTile(pos.x + 1, pos.y + 1, zoom);
        Vector2d topLeft = mapData.project(new LatLon(topleftLatLon.x, topleftLatLon.y));
        Vector2d bottomRight = mapData.project(new LatLon(bottomRightLatLon.x, bottomRightLatLon.y));
        double width = bottomRight.x - topLeft.x;
        double height = bottomRight.y - topLeft.y;

        int pixelX = (int) ((xz.x - topLeft.x) * (double) (image.getWidth()) / width);
        int pixelY = (int) ((xz.y - topLeft.y) * (double) (image.getHeight()) / height);
        if (pixelX >= 0 && pixelY >= 0 && pixelX < image.getWidth() && pixelY < image.getHeight()) {
            return new Color(image.getRGB(pixelX, pixelY));
        } else {
            System.err.println(xz + " " + bottomRight);
            System.err.println(processed + " " + pixelX + "," + pixelY + " " + image.getWidth() + "," + image.getHeight());
            return null;
        }
    }

    private BufferedImage getTile(Vector2i pos, boolean processed) {
        int zoom = config.tile.tilesZoom;

        String key = pos.x + "," + pos.y;
        //System.out.println(key + " " + processed);
        Tile tile;
        if (tiles.containsKey(key)) {
            tile = tiles.get(key);
        } else {
            tile = new Tile(pos.x, pos.y);
            tiles.put(key, tile);
        }

        try {
            if (tile.tile == null) {
                File tileFile = getTileFile(tile);
                tileFile.getParentFile().mkdirs();
                if (tileFile.exists()) {
                    tile.tile = ImageIO.read(tileFile);
                } else {
                    String url = config.tile.tilesUrl
                            .replace("{zoom}", Integer.toString(zoom))
                            .replace("{x}", Integer.toString(pos.x))
                            .replace("{y}", Integer.toString(pos.y));
                    tile.tile = ImageIO.read(new URL(url));
                    ImageIO.write(tile.tile, "png", tileFile);
                }
            }
            if (processed && tile.tileProcessed == null) {
                File tileProcessedFile = getTileProcessedFile(tile);
                tileProcessedFile.getParentFile().mkdirs();
                if (tileProcessedFile.exists()) {
                    tile.tileProcessed = ImageIO.read(tileProcessedFile);
                } else {
                    tile.tileProcessed = mulColors(tile.tile, config.tile.mulR, config.tile.mulG, config.tile.mulB);
                    if (config.tile.blur.active) {
                        tile.tileProcessed = blur(pos, tile.tileProcessed);
                    }
                    ImageIO.write(tile.tileProcessed, "png", tileProcessedFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processed ? tile.tileProcessed : tile.tile;
    }

    public File getTileFile(Tile tile) {
        return new File(config.tile.tilesFolder, config.tile.tilesZoom + "/" + tile.tileX + "-" + tile.tileY + ".png");
    }

    public File getTileProcessedFile(Tile tile) {
        return new File(config.tile.tilesFolder, "processed/" + config.tile.tilesZoom + "/" + tile.tileX + "-" + tile.tileY + "-" + config.tile.mulR + "-" + config.tile.mulG + "-" + config.tile.mulB + "-" + config.tile.blur.blurSize + "-" + config.tile.blur.blurStrength + ".png");
    }

    public BufferedImage mulColors(BufferedImage image, float mulR, float mulG, float mulB) {
        BufferedImage dest = createCompatibleDestImage(image, null);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color c = new Color(image.getRGB(x, y));
                int newR = (int) Util.clamp(c.getRed() * mulR, 0, 255);
                int newG = (int) Util.clamp(c.getGreen() * mulG, 0, 255);
                int newB = (int) Util.clamp(c.getBlue() * mulB, 0, 255);
                Color newC = new Color(newR, newG, newB);
                dest.setRGB(x, y, newC.getRGB());
            }
        }
        return dest;
    }

    public BufferedImage blur(Vector2i tile, BufferedImage image) {
        BufferedImage dest = createCompatibleDestImage(image, null);
        int size = config.tile.blur.blurSize;
        if (size % 2 == 0) {
            size++;
        }
        int radius = size - 1 / 2;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color ori = new Color(image.getRGB(x, y));
                Vector3i sum = new Vector3i();
                int count = 0;
                for (int i = x - radius; i < x + radius; i++) {
                    for (int j = y - radius; j < y + radius; j++) {
                        Color c;
                        if (i >= 0 && j >= 0 && i < image.getWidth() && j < image.getHeight()) {
                            c = new Color(image.getRGB(i, j));
                        } else {
                            Vector2i offset = new Vector2i();
                            Vector2i nextPixel = new Vector2i();
                            if (i < 0) {
                                offset.x = -1;
                                nextPixel.x = image.getWidth() + i;
                            } else if (i >= image.getWidth()) {
                                offset.x = 1;
                                nextPixel.x = i - image.getWidth();
                            } else {
                                nextPixel.x = i;
                            }
                            if (j < 0) {
                                offset.y = -1;
                                nextPixel.y = image.getHeight() + j;
                            } else if (j >= image.getHeight()) {
                                offset.y = 1;
                                nextPixel.y = j - image.getHeight();
                            } else {
                                nextPixel.y = j;
                            }

                            BufferedImage nextTile = getTile(tile.copy().add(offset), false);
                            c = new Color(nextTile.getRGB(nextPixel.x, nextPixel.y));
                            int newR = (int) Util.clamp(c.getRed() * config.tile.mulR, 0, 255);
                            int newG = (int) Util.clamp(c.getGreen() * config.tile.mulG, 0, 255);
                            int newB = (int) Util.clamp(c.getBlue() * config.tile.mulB, 0, 255);
                            c = new Color(newR, newG, newB);
                        }
                        sum = sum.add(c.getRed(), c.getGreen(), c.getBlue());
                        count++;
                    }
                }
                Color destC;
                if (count != 0) {
                    Vector3i color = sum.toDouble().div(count).toInt();
                    destC = new Color(
                            Util.lerpInt(ori.getRed(), color.getX(), config.tile.blur.blurStrength),
                            Util.lerpInt(ori.getGreen(), color.getY(), config.tile.blur.blurStrength),
                            Util.lerpInt(ori.getBlue(), color.getZ(), config.tile.blur.blurStrength)
                    );
                } else {
                    System.err.println("count == 0");
                    destC = null;
                }
                dest.setRGB(x, y, destC.getRGB());
            }
        }
        return dest;
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null)
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }
}
