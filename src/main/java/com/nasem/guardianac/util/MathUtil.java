package com.nasem.guardianac.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class MathUtil {

    private MathUtil() {}

    /**
     * Distance between two locations (3D).
     */
    public static double distance(Location a, Location b) {
        if (a == null || b == null) return 0;
        if (a.getWorld() != b.getWorld()) return Double.MAX_VALUE;
        return a.distance(b);
    }

    /**
     * Horizontal distance (ignores Y).
     */
    public static double horizontalDistance(Location a, Location b) {
        if (a == null || b == null) return 0;
        if (a.getWorld() != b.getWorld()) return Double.MAX_VALUE;
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Angle between two vectors in degrees (0-180).
     */
    public static double angle(Vector a, Vector b) {
        if (a == null || b == null) return 0;
        if (a.lengthSquared() == 0 || b.lengthSquared() == 0) return 0;
        double dot = a.dot(b);
        double lengths = a.length() * b.length();
        double cos = dot / lengths;
        cos = Math.max(-1.0, Math.min(1.0, cos));
        return Math.toDegrees(Math.acos(cos));
    }

    /**
     * Round a double to N decimal places.
     */
    public static double round(double value, int places) {
        if (places < 0) return value;
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / (double) factor;
    }

    /**
     * Check if a value is within [min, max] range.
     */
    public static boolean inRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}
