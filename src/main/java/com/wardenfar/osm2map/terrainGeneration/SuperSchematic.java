package com.wardenfar.osm2map.terrainGeneration;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockData;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.CuboidRegion;

public class SuperSchematic {

    Schematic schem;

    public SuperSchematic(Schematic schem) {
        this.schem = schem;
    }

    public int getWidth() {
        return getClipboard().getDimensions().getBlockX();
    }

    public int getHeight() {
        return getClipboard().getDimensions().getBlockY();
    }

    public int getLength() {
        return getClipboard().getDimensions().getBlockZ();
    }

    public Clipboard getClipboard() {
        return schem.getClipboard();
    }

    public BaseBlock getBlock(Vector vector) {
        return schem.getClipboard().getBlock(vector);
    }

    public Vector getSize() {
        return new Vector(getWidth(), getHeight(), getLength());
    }

    public SuperSchematic rotate(float angle) {
        try {
            angle = angle % 360;
            if (angle % 90 != 0) { // Can only rotate 90 degrees at the moment
                return null;
            }
            final boolean reverse = angle < 0;
            final int numRotations = Math.abs((int) Math.floor(angle / 90.0));

            final int width = getWidth();
            final int length = getLength();
            final int height = getHeight();
            final Vector sizeRotated = getSize().transform2D(angle, 0, 0, 0, 0);
            final int shiftX = sizeRotated.getX() < 0 ? -sizeRotated.getBlockX() - 1 : 0;
            final int shiftZ = sizeRotated.getZ() < 0 ? -sizeRotated.getBlockZ() - 1 : 0;

            Clipboard rotatedClip = new BlockArrayClipboard(new CuboidRegion(new Vector(), new Vector(Math.abs(sizeRotated.getBlockX()),
                    Math.abs(sizeRotated.getBlockY()),
                    Math.abs(sizeRotated.getBlockZ()))));

            for (int x = 0; x < width; ++x) {
                for (int z = 0; z < length; ++z) {
                    final Vector2D v = new Vector2D(x, z).transform2D(angle, 0, 0, shiftX, shiftZ);
                    final int newX = v.getBlockX();
                    final int newZ = v.getBlockZ();
                    for (int y = 0; y < height; ++y) {
                        final BaseBlock block = getBlock(new Vector(x, y, z));

                        if (block == null) {
                            continue;
                        }
                        if (reverse) {
                            for (int i = 0; i < numRotations; ++i) {
                                block.setData(BlockData.rotate90Reverse(block.getType(), block.getData()));
                            }
                        } else {
                            for (int i = 0; i < numRotations; ++i) {
                                block.setData(BlockData.rotate90(block.getType(), block.getData()));
                            }
                        }

                        rotatedClip.setBlock(null, new Vector(newX, y, newZ), block);
                    }
                }
            }

            return new SuperSchematic(new Schematic(rotatedClip));
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        return null;
    }
}
