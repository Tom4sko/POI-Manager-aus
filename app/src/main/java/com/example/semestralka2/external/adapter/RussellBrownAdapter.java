package com.example.semestralka2.external.adapter;

import com.example.semestralka2.Point;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.external.russellbrown.KdNode;
import com.example.semestralka2.external.russellbrown.RussellBrownKdTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre K-d strom od Russell A. Brown (RussellABrown/kd-tree).
 * Pouziva dynamicky rebalansovany 2D strom.
 */
public class RussellBrownAdapter implements SpatialTree<Integer, String> {

    private final RussellBrownKdTree tree = new RussellBrownKdTree(2);

    @Override
    public void insert(Point<Integer, String> point) {
        long[] coords = new long[]{point.getX(), point.getY()};
        tree.insert(coords, point.getData());
    }

    @Override
    public ArrayList<Point<Integer, String>> find(Integer minX, Integer minY, Integer maxX, Integer maxY) {
        ArrayList<Point<Integer, String>> result = new ArrayList<>();
        List<KdNode> nodes = tree.search(new long[]{minX, minY}, new long[]{maxX, maxY});

        if (nodes != null) {
            for (KdNode node : nodes) {
                long[] tuple = node.getTuple();
                int x = (int) tuple[0];
                int y = (int) tuple[1];
                for (String val : node.getValues()) {
                    result.add(new Point<>(x, y, val));
                }
            }
        }
        return result;
    }
}
