package com.carpentersblocks.client.model;

/**
 * A point on a face, in place of javax.vecmath's Point2d (1.12.2 got it from the game's libraries).
 */
public final class Point2d {

    public final double x;
    public final double y;

    public Point2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double distance(Point2d other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Point2d other && Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(x) + Double.hashCode(y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
