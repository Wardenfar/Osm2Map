package com.wardenfar.osm2map.terrainGeneration.sampler;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Algorithm based on <emph>Fast Poisson Disk Sampling in Arbitrary Dimensions</emph> by Robert Bridson, but with an
 * arbitrary minimum distance function. See also the paper <emph>A Spatial Data Structure for Fast Poisson-Disk Sample
 * Generation</emph> Daniel Dunbar and Greg Humphreys for other algorithms and a comparrisson.
 *
 * @author Herman Tulleken
 */
public class PoissonDiskSampler {
    
    private final static int DEFAULT_Vector2dS_TO_GENERATE = 30;
    private final int Vector2dsToGenerate; // k in literature
    private final Vector2d p0, p1;
    private final Vector2d dimensions;
    private final double cellSize; // r / sqrt(n), for 2D: r / sqrt(2)
    private final double minDist; // r
    private final int gridWidth, gridHeight;
    private Random random;

    /**
     * A safety measure - no more than this number of Vector2ds are produced by ther algorithm.
     */
    public final static int MAX_Vector2dS = 100000;

    private RealFunction2DDouble distribution;

    /**
     * Construct a new PoissonDisk object, with a given domain and minimum distance between Vector2ds.
     *
     * @param x0           x-coordinate of bottom left corner of domain.
     * @param y0           x-coordinate of bottom left corner of domain.
     * @param x1           x-coordinate of bottom left corner of domain.
     * @param y1           x-coordinate of bottom left corner of domain.
     * @param distribution A function that gives the minimum radius between Vector2ds in the vicinity of a Vector2d.
     */
    public PoissonDiskSampler(Random random, double x0, double y0, double x1, double y1, double minDist, RealFunction2DDouble distribution, int Vector2dsToGenerate) {
        this.random = random;
        p0 = new Vector2d(x0, y0);
        p1 = new Vector2d(x1, y1);
        dimensions = new Vector2d(x1 - x0, y1 - y0);

        this.minDist = minDist;
        this.distribution = distribution;
        this.Vector2dsToGenerate = Vector2dsToGenerate;
        cellSize = minDist / Math.sqrt(2);
        gridWidth = (int) (dimensions.getX() / cellSize) + 1;
        gridHeight = (int) (dimensions.getY() / cellSize) + 1;
    }

    /**
     * Construct a new PoissonDisk object, with a given domain and minimum distance between Vector2ds.
     *
     * @param x0           x-coordinate of bottom left corner of domain.
     * @param y0           x-coordinate of bottom left corner of domain.
     * @param x1           x-coordinate of bottom left corner of domain.
     * @param y1           x-coordinate of bottom left corner of domain.
     * @param distribution A function that gives the minimum radius between Vector2ds in the vicinity of a Vector2d.
     */
    public PoissonDiskSampler(Random random, double x0, double y0, double x1, double y1, double minDist,
                              RealFunction2DDouble distribution) {
        this(random, x0, y0, x1, y1, minDist, distribution, DEFAULT_Vector2dS_TO_GENERATE);
    }

    /**
     * Generates a list of Vector2ds following the Poisson distribution. No more than MAX_Vector2dS are produced.
     *
     * @return The sample set.
     */
    @SuppressWarnings("unchecked")
    public List<Vector2d> sample() {
        List<Vector2d> activeList = new LinkedList<Vector2d>();
        List<Vector2d> Vector2dList = new LinkedList<Vector2d>();
        List<Vector2d> grid[][] = new List[gridWidth][gridHeight];

        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                grid[i][j] = new LinkedList<Vector2d>();
            }
        }

        addFirstVector2d(grid, activeList, Vector2dList);

        while (!activeList.isEmpty() && (Vector2dList.size() < MAX_Vector2dS)) {
            int listIndex = random.nextInt(activeList.size());

            Vector2d Vector2d = activeList.get(listIndex);
            boolean found = false;

            for (int k = 0; k < Vector2dsToGenerate; k++) {
                found |= addNextVector2d(grid, activeList, Vector2dList, Vector2d);
            }

            if (!found) {
                activeList.remove(listIndex);
            }
        }

        return Vector2dList;
    }

    private boolean addNextVector2d(List<Vector2d>[][] grid, List<Vector2d> activeList,
                                 List<Vector2d> Vector2dList, Vector2d Vector2d) {
        boolean found = false;
        double fraction = distribution.getDouble((int) Vector2d.getX(), (int) Vector2d.getY());
        Vector2d q = generateRandomAround(Vector2d, fraction * minDist);

        if ((q.getX() >= p0.getX()) && (q.getX() < p1.getX()) && (q.getY() > p0.getY()) && (q.getY() < p1.getY())) {
            Vector2i qIndex = Vector2dDoubleToInt(q, p0, cellSize);

            boolean tooClose = false;

            for (int i = Math.max(0, qIndex.getX() - 2); (i < Math.min(gridWidth, qIndex.getX() + 3)) && !tooClose; i++) {
                for (int j = Math.max(0, qIndex.getY() - 2); (j < Math.min(gridHeight, qIndex.getY() + 3)) && !tooClose; j++) {
                    for (Vector2d gridVector2d : grid[i][j]) {
                        if (gridVector2d.distance(q) < minDist * fraction) {
                            tooClose = true;
                        }
                    }
                }
            }

            if (!tooClose) {
                found = true;
                activeList.add(q);
                Vector2dList.add(q);
                grid[qIndex.getX()][qIndex.getY()].add(q);
            }
        }

        return found;
    }

    private void addFirstVector2d(List<Vector2d>[][] grid, List<Vector2d> activeList,
                               List<Vector2d> Vector2dList) {
        double d = random.nextDouble();
        double xr = p0.getX() + dimensions.getX() * (d);

        d = random.nextDouble();
        double yr = p0.getY() + dimensions.getY() * (d);

        Vector2d p = new Vector2d(xr, yr);
        Vector2i index = Vector2dDoubleToInt(p, p0, cellSize);

        grid[index.getX()][index.getY()].add(p);
        activeList.add(p);
        Vector2dList.add(p);
    }

    /**
     * Converts a Vector2dDouble to a Vector2dInt that represents the index coordinates of the Vector2d in the background grid.
     */
    Vector2i Vector2dDoubleToInt(Vector2d Vector2dDouble, Vector2d origin, double cellSize) {
        return new Vector2i((int) ((Vector2dDouble.getX() - origin.getX()) / cellSize),
                (int) ((Vector2dDouble.getY() - origin.getY()) / cellSize));
    }

    /**
     * Generates a random Vector2d in the analus around the given Vector2d. The analus has inner radius minimum distance and
     * outer radius twice that.
     *
     * @param centre The Vector2d around which the random Vector2d should be.
     * @return A new Vector2d, randomly selected.
     */
    Vector2d generateRandomAround(Vector2d centre, double minDist) {
        double d = random.nextDouble();
        double radius = (minDist + minDist * (d));

        d = random.nextDouble();
        double angle = 2 * Math.PI * (d);

        double newX = radius * Math.sin(angle);
        double newY = radius * Math.cos(angle);

        Vector2d randomVector2d = new Vector2d(centre.getX() + newX, centre.getY() + newY);

        return randomVector2d;
    }
}