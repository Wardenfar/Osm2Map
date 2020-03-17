package com.wardenfar.osm2map.map;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.vividsolutions.jts.geom.Geometry;
import com.wardenfar.osm2map.Util;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;

import java.awt.Rectangle;
import java.io.*;
import java.util.logging.Logger;

public class ElevFile {

    public Vector2d min, max;
    public double step;
    public double[][] values;

    public Vector2i size;

    private double[] xval;
    private double[] yval;
    private BicubicInterpolatingFunction interpolateFunc;

    public ElevFile(Vector2d XYMin, Vector2d XYMax, double step) {
        Vector2i sizeTmp = XYMax.sub(XYMin).div(step).toInt();
        init(XYMin, XYMax, step, sizeTmp, new double[sizeTmp.getX()][sizeTmp.getY()]);
    }

    public ElevFile(Vector2d XYMin, Vector2d XYMax, double step, Vector2i size, double[][] values) {
        init(XYMin, XYMax, step, size, values);
    }

    private void init(Vector2d XYMin, Vector2d XYMax, double step, Vector2i size, double[][] values) {
        this.min = XYMin;
        this.max = XYMax;
        this.step = step;
        this.size = size;
        this.values = values;

        initInterpolation();
    }

    public void print() {
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                System.out.print(String.format("%1$" + 3 + "s", (int) values[x][y]) + " ");
            }
            System.out.println();
        }
    }

    public void addLatLon(Vector2d latlon, short v) {
        //System.out.println(latlon);
        Vector2i pos = Mercator.latLon2XY(latlon).sub(min).div(step).toInt();
        if (pos.getX() < 0 || pos.getX() >= size.getX() || pos.getY() < 0 || pos.getY() >= size.getY()) {
            //System.out.println("out");
            return;
        }
        //System.out.println(pos);
        if (values[pos.getX()][pos.getY()] != 0) {
            //System.out.println(values[pos.getX()][pos.getY()] + " -> "+v);
        }
        values[pos.getX()][pos.getY()] = v;
    }

    public void initInterpolation() {
        xval = new double[size.getX()];
        yval = new double[size.getY()];

        for (int i = 0; i < size.getX(); i++) {
            xval[i] = (double) i * step;
        }
        for (int i = 0; i < size.getY(); i++) {
            yval[i] = (double) i * step;
        }

        BicubicInterpolator interpolator = new BicubicInterpolator();
        interpolateFunc = interpolator.interpolate(xval, yval, values);
    }

    public boolean isInRangeByLatLon(Vector2d latlon) {
        Vector2d pos = Mercator.latLon2XY(latlon).sub(min);
        if (pos.getX() < xval[0]) {
            return false;
        } else if (pos.getX() > xval[xval.length - 1]) {
            return false;
        } else if (pos.getY() < yval[0]) {
            return false;
        } else if (pos.getY() > yval[yval.length - 1]) {
            return false;
        } else {
            return true;
        }
    }

    public double getDistanceToCenterByLatLon(Vector2d latlon) {
        Vector2d dataPos = Mercator.latLon2XY(latlon).sub(min).div(step);
        return dataPos.distance(size.toDouble().div(2.0));
    }

    public double getElevByLatLon(Vector2d latlon) {
        return getElevByXY(Mercator.latLon2XY(latlon));
    }

    public double getElevByXY(Vector2d xy) {
        Vector2d pos = xy.sub(min);
        if (pos.getX() < xval[0]) {
            return getElevByXY(new Vector2d(xval[0] + min.getX() + 1, xy.getY()));
        } else if (pos.getX() > xval[xval.length - 1]) {
            return getElevByXY(new Vector2d(xval[xval.length - 1] + min.getX() - 1, xy.getY()));
        } else if (pos.getY() < yval[0]) {
            return getElevByXY(new Vector2d(xy.getX(), yval[0] + min.getY() + 1));
        } else if (pos.getY() > yval[yval.length - 1]) {
            return getElevByXY(new Vector2d(xy.getX(), yval[yval.length - 1] + min.getY() - 1));
        } else {
            return interpolateFunc.value(pos.getX(), pos.getY());
        }
    }

    public void exportToFile(File file) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
            // min max
            out.writeDouble(min.getX());
            out.writeDouble(min.getY());
            out.writeDouble(max.getX());
            out.writeDouble(max.getY());
            // step
            out.writeDouble(step);
            // values
            out.writeInt(size.getX());
            out.writeInt(size.getY());
            for (int x = 0; x < size.getX(); x++) {
                for (int y = 0; y < size.getY(); y++) {
                    out.writeShort((short) values[x][y]);
                }
                if (x % 100 == 0) {
                    System.out.println(x + "/" + size.getX());
                }
            }
            out.close();
            System.out.println("Exported ! " + file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Geometry getBoundaryRect(File file) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            Vector2d min = new Vector2d(in.readDouble(), in.readDouble());
            Vector2d max = new Vector2d(in.readDouble(), in.readDouble());
            in.close();

            return getBoundaryRect(min, max);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Geometry getBoundaryRect(Vector2d min, Vector2d max) {
        return Util.geometryFromRectCorners(min.getX(), min.getY(), max.getX(), max.getY());
    }

    public static boolean intersects(File file, DBMapData mapData) {
        Geometry mapGeo = mapData.getBoundaryRect();
        Geometry fileGeo = getBoundaryRect(file);
        if (fileGeo == null) {
            return false;
        }
        return mapGeo.intersects(fileGeo);
    }

    public static ElevFile importFile(Logger logger, File file, DBMapData mapData) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(file));

            Vector2d min = new Vector2d(in.readDouble(), in.readDouble());
            Vector2d max = new Vector2d(in.readDouble(), in.readDouble());

            double step = in.readDouble();
            int sizeX = in.readInt();
            int sizeY = in.readInt();

            Geometry mapGeo = mapData.getBoundaryRect();
            Geometry fileGeo = getBoundaryRect(min, max);
            if (fileGeo == null) {
                return null;
            }
            Geometry rectGeo = mapGeo.intersection(fileGeo);
            Rectangle rect = Util.bbox(rectGeo);

            Vector2d minRect = new Vector2d(rect.x, rect.y);
            Vector2d maxRect = new Vector2d(rect.x + rect.width, rect.y + rect.height);

            Vector2d startXY = minRect.sub(min).div(step);
            int startX = startXY.getFloorX();
            int startY = startXY.getFloorY();
            int newSizeX = (int) (rect.width / step);
            int newSizeY = (int) (rect.height / step);

            double[][] values = new double[newSizeX][newSizeY];
            int count = 0;
            float stepPercents = 0.2f;
            int countPerStep = (int) ((float) (newSizeX * newSizeY) * stepPercents);

            in.skipBytes(2 * startX * sizeY);
            for (int x = startX; x < startX + newSizeX; x++) {
                in.skipBytes(2 * startY);
                for (int y = startY; y < startY + newSizeY; y++) {
                    short value = in.readShort();
                    values[x - startX][y - startY] = value;

                    if (count % countPerStep == 0) {
                        logger.info((int) (count / countPerStep * (stepPercents * 100)) + " %");
                    }
                    count++;
                }
                in.skipBytes(2 * (sizeY - (startY + newSizeY)));
            }
            //in.skipBytes(2 * (sizeX - (startX + newSizeX)) * sizeY);
            in.close();

            return new ElevFile(minRect, maxRect, step, new Vector2i(newSizeX, newSizeY), values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
