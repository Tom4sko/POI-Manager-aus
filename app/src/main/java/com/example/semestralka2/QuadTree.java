package com.example.semestralka2;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Quad strom, ktory duplikaty a body nad limit vysky uchovava v K-d strome.
 */
public class QuadTree<K, T> implements SpatialTree<K, T> {

    private static final int DEFAULT_MAX_HEIGHT = 16;

    private class Node {
        private final Point<K, T> splitPoint;
        private KDTree<K, T> extra;
        private final Node[] children;

        @SuppressWarnings("unchecked")
        private Node(Point<K, T> point) {
            this.splitPoint = point;
            this.children = (Node[]) new QuadTree.Node[4];
        }

        private void addExtra(Point<K, T> point) {
            if (extra == null) {
                extra = new KDTree<>(comparator);
            }
            extra.insert(point);
        }
    }

    private final Comparator<K> comparator;
    private final int maxHeight;
    private Node root;

    public QuadTree(Comparator<K> comparator) {
        this(comparator, DEFAULT_MAX_HEIGHT);
    }

    public QuadTree(Comparator<K> comparator, int maxHeight) {
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
            int cx = comparator.compare(point.getX(), current.splitPoint.getX());
            int cy = comparator.compare(point.getY(), current.splitPoint.getY());

            if (cx == 0 && cy == 0) {
                current.addExtra(point);
                return;
            }

            if (depth + 1 >= maxHeight) {
                current.addExtra(point);
                return;
            }

            int quadrant = (cx >= 0 ? 1 : 0) + (cy >= 0 ? 2 : 0);

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

            if (current.splitPoint.isInside(comparator, minX, minY, maxX, maxY)) {
                result.add(current.splitPoint);
            }
            if (current.extra != null) {
                result.addAll(current.extra.find(minX, minY, maxX, maxY));
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