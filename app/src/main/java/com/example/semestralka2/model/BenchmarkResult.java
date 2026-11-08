
package com.example.semestralka2.model;

public class BenchmarkResult {

    private final String treeName;
    private final int pointCount;
    private final int queryCount;

    private final long creationNanos;
    private final long insertNanos;
    private final long findNanos;

    public BenchmarkResult(
            String treeName,
            int pointCount,
            int queryCount,
            long creationNanos,
            long insertNanos,
            long findNanos) {

        this.treeName = treeName;
        this.pointCount = pointCount;
        this.queryCount = queryCount;
        this.creationNanos = creationNanos;
        this.insertNanos = insertNanos;
        this.findNanos = findNanos;
    }

    public String getTreeName() {
        return treeName;
    }

    public int getPointCount() {
        return pointCount;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public long getCreationNanos() {
        return creationNanos;
    }

    public long getInsertNanos() {
        return insertNanos;
    }

    public long getFindNanos() {
        return findNanos;
    }

    public double getCreationMillis() {
        return creationNanos / 1_000_000.0;
    }

    public double getInsertMillis() {
        return insertNanos / 1_000_000.0;
    }

    public double getFindMillis() {
        return findNanos / 1_000_000.0;
    }
}
