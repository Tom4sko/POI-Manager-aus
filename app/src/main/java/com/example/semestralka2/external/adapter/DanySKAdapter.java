package com.example.semestralka2.external.adapter;

import com.example.semestralka2.Point;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.external.danysk.FlexibleQuadTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre FlexibleQuadTree od Danilo Pianini (DanySK/java-quadtree).
 * Dynamicky rozsirovany strom bez fixnych hranic.
 */
public class DanySKAdapter implements SpatialTree<Integer, String> {

    private final FlexibleQuadTree<Point<Integer, String>> tree = new FlexibleQuadTree<>();

    @Override
    public void insert(Point<Integer, String> point) {
        tree.insert(point, point.getX(), point.getY());
    }

    @Override
    public ArrayList<Point<Integer, String>> find(Integer minX, Integer minY, Integer maxX, Integer maxY) {
        List<Point<Integer, String>> found = tree.query(minX, minY, (double) maxX + 1, (double) maxY + 1);
        return found == null ? new ArrayList<>() : new ArrayList<>(found);
    }
}
