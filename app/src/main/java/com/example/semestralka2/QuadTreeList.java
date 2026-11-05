package com.example.semestralka2;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Klasicky Quad strom, ktory rovnake body (duplikaty na suradniciach vrcholu)
 * uchovava v zozname (ArrayList).
 */
public class QuadTreeList<K, T> implements SpatialTree<K, T> {

    private static final int DEFAULT_MAX_HEIGHT = 16;

    private class Node {
        private final Point<K, T> splitPoint;
        private final ArrayList<Point<K, T>> points;
        private final Node[] children;

        @SuppressWarnings("unchecked")
        private Node(Point<K, T> point) {
            this.splitPoint = point;
            this.points = new ArrayList<>();
            this.points.add(point);
            this.children = (Node[]) new QuadTreeList.Node[4];
        }
    }

    private final Comparator<K> comparator;
    private final int maxHeight;
    private Node root;

    public QuadTreeList(Comparator<K> comparator) {
        this(comparator, DEFAULT_MAX_HEIGHT);
    }

    public QuadTreeList(Comparator<K> comparator, int maxHeight) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator nesmie byt null.");
        }
        if (maxHeight < 1) {
            throw new IllegalArgumentException("Limit vysky musi byt aspon 1.");
        }
        this.comparator = comparator;
        this.maxHeight = maxHeight;
    }

    @Override
    public void insert(Point<K, T> point) {
        if (point == null) {
            throw new IllegalArgumentException("Bod nesmie byt null.");
        }

        if (root == null) {
            root = new Node(point);
            return;
        }

        Node current = root;
        int depth = 0;

        while (true) {
            int quadrant = 0;

            if (comparator.compare(point.getX(), current.splitPoint.getX()) >= 0) {
                quadrant += 1;
            }

            if (comparator.compare(point.getY(), current.splitPoint.getY()) >= 0) {
                quadrant += 2;
            }

            if (comparator.compare(point.getX(), current.splitPoint.getX()) == 0
                    && comparator.compare(point.getY(), current.splitPoint.getY()) == 0) {
                current.points.add(point);
                return;
            }

            if (depth + 1 >= maxHeight) {
                current.points.add(point);
                return;
            }

            if (current.children[quadrant] == null) {
                current.children[quadrant] = new Node(point);
                return;
            }

            current = current.children[quadrant];
            depth++;
        }
    }

    @Override
    public ArrayList<Point<K, T>> find(K minX, K minY, K maxX, K maxY) {
        ArrayList<Point<K, T>> result = new ArrayList<>();

        if (root == null) {
            return result;
        }

        ArrayList<Node> stack = new ArrayList<>();
        stack.add(root);

        while (!stack.isEmpty()) {
            Node current = stack.remove(stack.size() - 1);

            for (Point<K, T> pt : current.points) {
                if (pt.isInside(comparator, minX, minY, maxX, maxY)) {
                    result.add(pt);
                }
            }

            K x = current.splitPoint.getX();
            K y = current.splitPoint.getY();

            boolean canWest = comparator.compare(minX, x) < 0;
            boolean canEast = comparator.compare(maxX, x) >= 0;
            boolean canSouth = comparator.compare(minY, y) < 0;
            boolean canNorth = comparator.compare(maxY, y) >= 0;

            for (int q = 0; q < 4; q++) {
                Node child = current.children[q];

                boolean east = q % 2 == 1;
                boolean north = q >= 2;

                if (child != null
                        && (east ? canEast : canWest)
                        && (north ? canNorth : canSouth)) {
                    stack.add(child);
                }
            }
        }

        return result;
    }
}
