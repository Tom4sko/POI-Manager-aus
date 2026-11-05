package com.example.semestralka2.external.adapter;

import com.example.semestralka2.Point;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.external.mgruben.KdTree;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter pre K-d strom od Michael Gruben (mgruben/Kd-Trees).
 * Pociatocny strom nema ukladanie dat a nepodporuje duplikaty bodov v strome.
 * Adapter udrziava priradenie dat k suradniciam.
 */
public class MgrubenAdapter implements SpatialTree<Integer, String> {

    private final KdTree tree;
    private final Map<Long, List<String>> payloads = new HashMap<>();

    public MgrubenAdapter() {
        this(0, 0, 1_000_000, 1_000_000);
    }

    public MgrubenAdapter(double minX, double minY, double maxX, double maxY) {
        this.tree = new KdTree(minX, minY, maxX, maxY);
    }

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    @Override
    public void insert(Point<Integer, String> point) {
        payloads.computeIfAbsent(key(point.getX(), point.getY()), k -> new ArrayList<>())
                .add(point.getData());
        tree.insert(new Point2D(point.getX(), point.getY()));
    }

    @Override
    public ArrayList<Point<Integer, String>> find(Integer minX, Integer minY, Integer maxX, Integer maxY) {
        ArrayList<Point<Integer, String>> result = new ArrayList<>();
        RectHV rect = new RectHV(minX, minY, maxX, maxY);

        for (Point2D pt : tree.range(rect)) {
            int x = (int) Math.round(pt.x());
            int y = (int) Math.round(pt.y());
            List<String> list = payloads.get(key(x, y));
            if (list != null) {
                for (String data : list) {
                    result.add(new Point<>(x, y, data));
                }
            }
        }
        return result;
    }
}
