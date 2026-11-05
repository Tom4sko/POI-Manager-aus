package com.example.semestralka2;

import java.util.Comparator;

public class Point<K, T> {
    private final K[] coords;
    private final T data;

    public Point(K x, K y, T data) {
        this(newArray(x, y), data);
    }

    public Point(K[] coords, T data) {
        if (coords == null || coords.length == 0) {
            throw new IllegalArgumentException("Bod musi mat aspon jednu suradnicu.");
        }
        this.coords = coords.clone();
        this.data = data;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    private static <K> K[] newArray(K... values) {
        return values;
    }

    public int dimensions() {
        return coords.length;
    }

    public K get(int dimension) {
        return coords[dimension];
    }

    public K getX() {
        return coords[0];
    }

    public K getY() {
        return coords.length > 1 ? coords[1] : coords[0];
    }

    public T getData() {
        return data;
    }

    public boolean isInside(Comparator<K> comparator,
                            K minX, K minY, K maxX, K maxY) {
        boolean insideX = comparator.compare(getX(), minX) >= 0
                && comparator.compare(getX(), maxX) <= 0;
        if (coords.length == 1) {
            return insideX;
        }
        return insideX
                && comparator.compare(getY(), minY) >= 0
                && comparator.compare(getY(), maxY) <= 0;
    }

    public boolean isInside(Comparator<K> comparator, K[] min, K[] max) {
        if (min.length != coords.length || max.length != coords.length) {
            throw new IllegalArgumentException("Pocet dimenzii oblasti nesedi s bodom.");
        }
        for (int d = 0; d < coords.length; d++) {
            if (comparator.compare(coords[d], min[d]) < 0
                    || comparator.compare(coords[d], max[d]) > 0) {
                return false;
            }
        }
        return true;
    }
}
