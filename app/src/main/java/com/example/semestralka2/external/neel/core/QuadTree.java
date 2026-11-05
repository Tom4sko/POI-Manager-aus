package com.example.semestralka2.external.neel.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.example.semestralka2.external.neel.model.Point;
import com.example.semestralka2.external.neel.model.Rectangle;

public class QuadTree {

    private static final int CAPACITY = 4;

    private final Rectangle boundary;
    private List<Point> points;
    private boolean divided;

    private QuadTree northWest;
    private QuadTree northEast;
    private QuadTree southWest;
    private QuadTree southEast;

    public QuadTree(Rectangle boundary) {
        this.boundary = boundary;
        this.points = new ArrayList<>();
    }

    public boolean insert(Point point) {
        if (!boundary.contains(point)) {
            return false;
        }

        if (!divided && points.size() < CAPACITY) {
            points.add(point);
            return true;
        }

        if (!divided) {
            subdivide();
        }

        return insertIntoChild(point);
    }

    public List<Point> query(Rectangle range) {
        List<Point> result = new ArrayList<>();
        query(range, result);
        return result;
    }

    public Point nearest(Point target) {
        NearestResult result = new NearestResult();
        nearest(target, result);
        return result.bestPoint;
    }

    private boolean insertIntoChild(Point point) {
        if (northWest.insert(point)) return true;
        if (northEast.insert(point)) return true;
        if (southWest.insert(point)) return true;
        if (southEast.insert(point)) return true;
        return false;
    }

    private void subdivide() {
    int x = boundary.x;
    int y = boundary.y;

    int w = boundary.width / 2;
    int h = boundary.height / 2;

    int rightWidth = boundary.width - w;
    int bottomHeight = boundary.height - h;

    northWest = new QuadTree(new Rectangle(x, y, w, h));
    northEast = new QuadTree(new Rectangle(x + w, y, rightWidth, h));
    southWest = new QuadTree(new Rectangle(x, y + h, w, bottomHeight));
    southEast = new QuadTree(new Rectangle(x + w, y + h, rightWidth, bottomHeight));

    divided = true;

    List<Point> oldPoints = points;
    points = new ArrayList<>();

    for (Point p : oldPoints) {
        if (!insertIntoChild(p)) {
            throw new IllegalStateException(
                    "Bod sa nepodarilo vlozit do ziadneho dietata: " + p
            );
        }
    }
}

    private void query(Rectangle range, List<Point> result) {
        if (!boundary.intersects(range)) {
            return;
        }

        for (Point p : points) {
            if (range.contains(p)) {
                result.add(p);
            }
        }

        if (!divided) {
            return;
        }

        northWest.query(range, result);
        northEast.query(range, result);
        southWest.query(range, result);
        southEast.query(range, result);
    }

    private void nearest(Point target, NearestResult result) {
        for (Point p : points) {
            double d = distance(target, p);
            if (d < result.bestDistance) {
                result.bestDistance = d;
                result.bestPoint = p;
            }
        }

        if (!divided) {
            return;
        }

        List<QuadTree> children = new ArrayList<>();
        children.add(northWest);
        children.add(northEast);
        children.add(southWest);
        children.add(southEast);

        children.sort(
            Comparator.comparingDouble(child -> distanceToRectangle(target, child.boundary))
        );

        for (QuadTree child : children) {
            double minPossible = distanceToRectangle(target, child.boundary);
            if (minPossible >= result.bestDistance) {
                continue;
            }

            child.nearest(target, result);
        }
    }

    private double distance(Point a, Point b) {
        long dx = a.x - b.x;
        long dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double distanceToRectangle(Point target, Rectangle rect) {
        int closestX = Math.max(rect.x, Math.min(target.x, rect.x + rect.width));
        int closestY = Math.max(rect.y, Math.min(target.y, rect.y + rect.height));

        long dx = target.x - closestX;
        long dy = target.y - closestY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static class NearestResult {
        private Point bestPoint;
        private double bestDistance = Double.MAX_VALUE;
    }
}