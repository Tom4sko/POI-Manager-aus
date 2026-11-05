package com.example.semestralka2.external.adapter;

import com.example.semestralka2.Point;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.external.neel.core.QuadTree;
import com.example.semestralka2.external.neel.model.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter pre QuadTree od Neel Deb (Neel-stack-deb/QuadTree).
 * Chráni strom pred zacyklením (StackOverflowError) pri viac než 4 identických
 * bodoch ukladaním duplikátov do mapy hodnôt.
 */
public class NeelAdapter implements SpatialTree<Integer, String> {

    private final QuadTree tree;
    private final Map<Long, List<String>> payloads = new HashMap<>();

    public NeelAdapter() {
        this(0, 0, 1_000_000, 1_000_000);
    }

    public NeelAdapter(int x, int y, int width, int height) {
        this.tree = new QuadTree(new Rectangle(x, y, width, height));
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
            tree.insert(new com.example.semestralka2.external.neel.model.Point(point.getX(), point.getY()));
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

        List<com.example.semestralka2.external.neel.model.Point> found = tree.query(searchArea);
        if (found != null) {
            for (com.example.semestralka2.external.neel.model.Point pt : found) {
                List<String> values = payloads.get(key(pt.x, pt.y));
                if (values != null) {
                    for (String val : values) {
                        result.add(new Point<>(pt.x, pt.y, val));
                    }
                }
            }
        }
        return result;
    }
}
