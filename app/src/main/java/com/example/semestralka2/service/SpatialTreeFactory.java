
package com.example.semestralka2.service;

import com.example.semestralka2.KDTree;
import com.example.semestralka2.QuadTree;
import com.example.semestralka2.QuadTreeList;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.model.POI;

import java.util.Comparator;

public class SpatialTreeFactory {

    private static final Comparator<Double> COMPARATOR =
            Double::compare;

    public static SpatialTree<Double, POI> create(TreeType type) {

        if (type == null) {
            throw new IllegalArgumentException("Typ stromu nesmie byť null.");
        }

        return switch (type) {
            case KD_TREE -> new KDTree<>(COMPARATOR);
            case QUAD_TREE -> new QuadTree<>(COMPARATOR);
            case QUAD_TREE_LIST -> new QuadTreeList<>(COMPARATOR);
        };
    }
}
