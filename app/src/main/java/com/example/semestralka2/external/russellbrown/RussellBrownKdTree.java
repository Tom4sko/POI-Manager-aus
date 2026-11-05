package com.example.semestralka2.external.russellbrown;

import java.util.Collections;
import java.util.List;

/**
 * Convenience wrapper for Russell A. Brown's dynamic Kd-Tree.
 */
public class RussellBrownKdTree {
    private final KdTreeDynamic tree;
    private final int dimensions;

    public RussellBrownKdTree(int dimensions) {
        this.dimensions = dimensions;
        this.tree = new KdTreeDynamic(dimensions, null, -1);
    }

    public boolean insert(long[] coords, String value) {
        return tree.insert(new Pair(coords, value));
    }

    public List<KdNode> search(long[] queryLower, long[] queryUpper) {
        List<KdNode> results = tree.searchKdTree(queryLower, queryUpper, null, -1, 0, 0, true);
        return results == null ? Collections.emptyList() : results;
    }

    public KdTreeDynamic getUnderlyingTree() {
        return tree;
    }
}
