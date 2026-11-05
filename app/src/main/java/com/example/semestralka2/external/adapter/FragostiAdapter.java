package com.example.semestralka2.external.adapter;

import com.example.semestralka2.Point;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.external.fragosti.AbstractNode;
import com.example.semestralka2.external.fragosti.PointNode;
import com.example.semestralka2.external.fragosti.PointQuadTree;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter pre QuadTree od Francesco Agosti (fragosti/QuadTree).
 * Udrziava zoznam hodnot pre duplicitne suradnice a vola queryRange.
 */
public class FragostiAdapter implements SpatialTree<Integer, String> {

    private final PointQuadTree<List<String>> tree;
    private final Map<Long, List<String>> payloads = new HashMap<>();

    public FragostiAdapter() {
        this(0, 0, 1_000_000, 1_000_000);
    }

    public FragostiAdapter(int x, int y, int width, int height) {
        this.tree = new PointQuadTree<>(new Rectangle(x, y, width, height));
    }

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    @Override
    public void insert(Point<Integer, String> point) {
        long k = key(point.getX(), point.getY());
        List<String> list = payloads.get(k);
        if (list == null) {
            list = new ArrayList<>();
            payloads.put(k, list);
            list.add(point.getData());
            tree.insert(new java.awt.Point(point.getX(), point.getY()), list);
        } else {
            list.add(point.getData());
        }
    }

    @Override
    public ArrayList<Point<Integer, String>> find(Integer minX, Integer minY, Integer maxX, Integer maxY) {
        ArrayList<Point<Integer, String>> result = new ArrayList<>();
        int width = Math.max(1, maxX - minX + 1);
        int height = Math.max(1, maxY - minY + 1);
        Rectangle searchArea = new Rectangle(minX, minY, width, height);

        List<AbstractNode<List<String>>> found = tree.queryRange(searchArea);
        if (found != null) {
            for (AbstractNode<List<String>> node : found) {
                java.awt.Point pt = node.getLocation();
                if (node instanceof PointNode<List<String>> pn) {
                    List<String> values = pn.getValue();
                    if (values != null) {
                        for (String val : values) {
                            result.add(new Point<>(pt.x, pt.y, val));
                        }
                    }
                }
            }
        }
        return result;
    }
}
