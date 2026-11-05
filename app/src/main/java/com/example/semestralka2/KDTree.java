package com.example.semestralka2;

import java.util.ArrayList;
import java.util.Comparator;

public class KDTree<K, T> implements SpatialTree<K, T> {

    private class Node {
        private final Point<K, T> point;
        private final int depth;
        private Node left;
        private Node right;

        private Node(Point<K, T> point, int depth) {
            this.point = point;
            this.depth = depth;
        }
    }

    private final Comparator<K> comparator;
    private final int dimensions;
    private Node root;

    public KDTree(Comparator<K> comparator) {
        this(comparator, 2);
    }

    public KDTree(Comparator<K> comparator, int dimensions) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator nesmie byt null.");
        }
        if (dimensions < 1) {
            throw new IllegalArgumentException("Pocet dimenzii musi byt aspon 1.");
        }
        this.comparator = comparator;
        this.dimensions = dimensions;
    }

    @Override
    public void insert(Point<K, T> point) {
        if (point == null) {
            throw new IllegalArgumentException("Bod nesmie byt null.");
        }
        if (point.dimensions() != dimensions) {
            throw new IllegalArgumentException(
                    "Bod ma " + point.dimensions() + " dimenzii, strom ocakava " + dimensions + ".");
        }

        if (root == null) {
            root = new Node(point, 0);
            return;
        }

        Node current = root;
        while (true) {
            int axis = current.depth % dimensions;
            boolean goRight = comparator.compare(point.get(axis), current.point.get(axis)) >= 0;
            Node next = goRight ? current.right : current.left;

            if (next == null) {
                Node created = new Node(point, current.depth + 1);
                if (goRight) {
                    current.right = created;
                } else {
                    current.left = created;
                }
                return;
            }
            current = next;
        }
    }

    @Override
    public ArrayList<Point<K, T>> find(K minX, K minY, K maxX, K maxY) {
        if (dimensions != 2) {
            throw new IllegalStateException(
                    "2D vyhladavanie je len pre 2 dimenzie. Pouzi find(K[] min, K[] max).");
        }
        return find(boxMin(minX, minY), boxMax(maxX, maxY));
    }

    @Override
    public ArrayList<Point<K, T>> find(K[] min, K[] max) {
        ArrayList<Point<K, T>> result = new ArrayList<>();

        if (min == null || max == null || min.length != dimensions || max.length != dimensions) {
            throw new IllegalArgumentException("Oblast musi mat " + dimensions + " dimenzii.");
        }
        if (root == null) {
            return result;
        }

        ArrayList<Node> stack = new ArrayList<>();
        stack.add(root);

        while (!stack.isEmpty()) {
            Node current = stack.remove(stack.size() - 1);
            
            if (current.point.isInside(comparator, min, max)) {
                result.add(current.point);
            }

            int axis = current.depth % dimensions;
            K split = current.point.get(axis);

            // lavy podstrom: hodnoty < split
            if (current.left != null && comparator.compare(min[axis], split) < 0) {
                stack.add(current.left);
            }
            // pravy podstrom: hodnoty >= split
            if (current.right != null && comparator.compare(max[axis], split) >= 0) {
                stack.add(current.right);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private K[] boxMin(K minX, K minY) {
        K[] min = (K[]) new Object[dimensions];
        min[0] = minX;
        if (dimensions > 1) {
            min[1] = minY;
        }
        return min;
    }

    @SuppressWarnings("unchecked")
    private K[] boxMax(K maxX, K maxY) {
        K[] max = (K[]) new Object[dimensions];
        max[0] = maxX;
        if (dimensions > 1) {
            max[1] = maxY;
        }
        return max;
    }
}