package com.example.semestralka2;

import java.util.ArrayList;

public interface SpatialTree<K, T> {

    /* Vlozi bod do stromu (duplicitne suradnice su povolene). */
    void insert(Point<K, T> point);

    /* Vrati vsetky body v oblasti (vratane hranice). */
    ArrayList<Point<K, T>> find(K minX, K minY, K maxX, K maxY);

    /* Vrati vsetky body v k-rozmernej oblasti (vratane hranice). */
    default ArrayList<Point<K, T>> find(K[] min, K[] max) {
        throw new UnsupportedOperationException(
                "Tato struktura podporuje iba 2D vyhladavanie.");
    }
}