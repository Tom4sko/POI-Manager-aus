package com.example.semestralka2;

import com.example.semestralka2.external.fragosti.AbstractNode;
import com.example.semestralka2.external.fragosti.PointQuadTree;
import com.example.semestralka2.external.mgruben.KdTree;
import com.example.semestralka2.external.morkev.KdTreeST;
import com.example.semestralka2.external.neel.core.QuadTree;
import com.example.semestralka2.external.russellbrown.KdNode;
import com.example.semestralka2.external.russellbrown.RussellBrownKdTree;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import com.example.semestralka2.external.danysk.FlexibleQuadTree;
import com.example.semestralka2.external.danysk.SpatialIndex;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExternalLibrariesTest {

    @Test
    void test1_mgruben_KdTree() {
        KdTree tree = new KdTree(0, 0, 100, 100);
        tree.insert(new Point2D(10, 20));
        tree.insert(new Point2D(50, 50));
        tree.insert(new Point2D(80, 90));

        RectHV queryRect = new RectHV(5, 5, 60, 60);
        List<Point2D> matched = new ArrayList<>();
        for (Point2D p : tree.range(queryRect)) {
            matched.add(p);
        }

        assertEquals(2, matched.size(), "mgruben KdTree should return 2 points within [5,5]..[60,60]");
    }

    @Test
    void test2_morkev_KdTreeST() {
        KdTreeST<String> tree = new KdTreeST<>();
        tree.put(new Point2D(10, 20), "P1");
        tree.put(new Point2D(50, 50), "P2");
        tree.put(new Point2D(80, 90), "P3");

        RectHV queryRect = new RectHV(5, 5, 60, 60);
        List<Point2D> matched = new ArrayList<>();
        for (Point2D p : tree.range(queryRect)) {
            matched.add(p);
        }

        assertEquals(2, matched.size(), "morkev KdTreeST should return 2 points within [5,5]..[60,60]");
    }

    @Test
    void test3_russellbrown_KdTree() {
        RussellBrownKdTree tree = new RussellBrownKdTree(2);
        tree.insert(new long[]{10, 20}, "P1");
        tree.insert(new long[]{50, 50}, "P2");
        tree.insert(new long[]{80, 90}, "P3");

        List<KdNode> matched = tree.search(new long[]{5, 5}, new long[]{60, 60});
        assertNotNull(matched);
        assertEquals(2, matched.size(), "russellbrown KdTree should return 2 nodes within [5,5]..[60,60]");
    }

    @Test
    void test4_fragosti_QuadTree() {
        PointQuadTree<String> tree = new PointQuadTree<>(new Rectangle(0, 0, 100, 100));
        tree.insert(new java.awt.Point(10, 20), "P1");
        tree.insert(new java.awt.Point(50, 50), "P2");
        tree.insert(new java.awt.Point(80, 90), "P3");

        List<AbstractNode<String>> matched = tree.queryRange(new Rectangle(5, 5, 56, 56));
        assertEquals(2, matched.size(), "fragosti PointQuadTree should return 2 nodes within bounds");
    }

    @Test
    void test5_neel_QuadTree() {
        QuadTree tree = new QuadTree(new com.example.semestralka2.external.neel.model.Rectangle(0, 0, 100, 100));
        tree.insert(new com.example.semestralka2.external.neel.model.Point(10, 20));
        tree.insert(new com.example.semestralka2.external.neel.model.Point(50, 50));
        tree.insert(new com.example.semestralka2.external.neel.model.Point(80, 90));

        List<com.example.semestralka2.external.neel.model.Point> matched =
                tree.query(new com.example.semestralka2.external.neel.model.Rectangle(5, 5, 56, 56));
        assertEquals(2, matched.size(), "neel QuadTree should return 2 points within bounds");
    }

    @Test
    void test6_danysk_FlexibleQuadTree() {
        SpatialIndex<String> tree = new FlexibleQuadTree<>();
        tree.insert("P1", 10, 20);
        tree.insert("P2", 50, 50);
        tree.insert("P3", 80, 90);

        List<String> matched = tree.query(new double[]{5, 5}, new double[]{60, 60});
        assertEquals(2, matched.size(), "DanySK FlexibleQuadTree should return 2 items within [5,5]..[60,60]");
    }
}
