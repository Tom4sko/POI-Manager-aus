package com.example.semestralka2.external.adapter;

import com.example.semestralka2.Point;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.external.morkev.KdTreeST;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pre 2D K-d strom od Kevin Mora et al. (morkev/kd-trees).
 * Uchovava zoznam hodnot pri duplicitnych suradniciach pomocou Symbol Table.
 */
public class MorkevAdapter implements SpatialTree<Integer, String> {

    private final KdTreeST<List<String>> tree = new KdTreeST<>();

    @Override
    public void insert(Point<Integer, String> point) {
        Point2D p2d = new Point2D(point.getX(), point.getY());
        List<String> values = tree.get(p2d);
        if (values == null) {
            values = new ArrayList<>();
            tree.put(p2d, values);
        }
        values.add(point.getData());
    }

    @Override
    public ArrayList<Point<Integer, String>> find(Integer minX, Integer minY, Integer maxX, Integer maxY) {
        ArrayList<Point<Integer, String>> result = new ArrayList<>();
        RectHV rect = new RectHV(minX, minY, maxX, maxY);

        for (Point2D pt : tree.range(rect)) {
            List<String> values = tree.get(pt);
            if (values != null) {
                int x = (int) Math.round(pt.x());
                int y = (int) Math.round(pt.y());
                for (String val : values) {
                    result.add(new Point<>(x, y, val));
                }
            }
        }
        return result;
    }
}
