package com.example.semestralka2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TreeTester {
    private static final int SCALE = 10_000_000;
    private static final int MIN_LAT = 470_000_000;
    private static final int MIN_LON = 160_000_000;
    private static final int MAX_LAT = 500_000_000;
    private static final int MAX_LON = 230_000_000;

    private static class InsertedPoint {
        private final int x;
        private final int y;
        private final String data;

        private InsertedPoint(int x, int y, String data) {
            this.x = x;
            this.y = y;
            this.data = data;
        }
    }

    private static class TestArea {
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        private TestArea(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    private static class TestData {
        private final List<InsertedPoint> points;
        private final List<TestArea> areas;
        private final List<Map<String, Integer>> expectedResults;

        private TestData(List<InsertedPoint> points, List<TestArea> areas) {
            this.points = points;
            this.areas = areas;
            this.expectedResults = expectedCounts(points, areas);
        }
    }

    public static void main(String[] args) {
        try {
            TestData data = loadData();
            System.out.printf(
                    "Spustam TreeTester: (uzly: %,d; oblasti: %,d)%n",
                    data.points.size(),
                    data.areas.size());

            runOwnTrees(data);
            runExternalTrees(data);
        } catch (Exception e) {
            System.out.println("Chyba pri nacitani testovacich dat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runOwnTrees(TestData data) {
          runTest("Vlastny KDTree", () -> new KDTree<>(Integer::compare), data);
          runTest("Vlastny QuadTree", () -> new QuadTree<>(Integer::compare), data);
    }

    private static void runExternalTrees(TestData data) {
        runTest("Mgruben KdTree",
                () -> new com.example.semestralka2.external.adapter.MgrubenAdapter(
                        MIN_LAT, MIN_LON, MAX_LAT, MAX_LON),
                data);
        runTest("Morkev KdTreeST",
                com.example.semestralka2.external.adapter.MorkevAdapter::new,
                data);
        runTest("RussellBrown KdTree",
                com.example.semestralka2.external.adapter.RussellBrownAdapter::new,
                data);
        runTest("Fragosti PointQuadTree",
                () -> new com.example.semestralka2.external.adapter.FragostiAdapter(
                        MIN_LAT, MIN_LON, MAX_LAT - MIN_LAT, MAX_LON - MIN_LON),
                data);
        runTest("Neel QuadTree",
                () -> new com.example.semestralka2.external.adapter.NeelAdapter(
                        MIN_LAT, MIN_LON, MAX_LAT - MIN_LAT, MAX_LON - MIN_LON),
                data);
        runTest("DanySK FlexibleQuadTree",
                com.example.semestralka2.external.adapter.DanySKAdapter::new,
                data);
    }

    private static TestData loadData() throws IOException {
        List<InsertedPoint> points = loadPoints(findFile("slovakia_nodes.csv"));
        List<TestArea> areas = loadAreas(findFile("testovacie_oblasti.csv"));

        if (points.isEmpty()) {
            throw new IOException("slovakia_nodes.csv neobsahuje ziadne uzly.");
        }
        if (areas.isEmpty()) {
            throw new IOException("testovacie_oblasti.csv neobsahuje ziadne oblasti.");
        }
        return new TestData(points, areas);
    }

    private static List<InsertedPoint> loadPoints(Path file) throws IOException {
        List<InsertedPoint> points = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            boolean header = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] columns = line.split(";");
                long id = Long.parseLong(columns[0].trim());
                int lat = toScaledInt(columns[1]);
                int lon = toScaledInt(columns[2]);
                points.add(new InsertedPoint(lat, lon, String.valueOf(id)));
            }
        }
        return points;
    }

    private static List<TestArea> loadAreas(Path file) throws IOException {
        List<TestArea> areas = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            boolean header = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] columns = line.split(";");
                areas.add(new TestArea(
                        toScaledInt(columns[1]),
                        toScaledInt(columns[2]),
                        toScaledInt(columns[3]),
                        toScaledInt(columns[4])));
            }
        }
        return areas;
    }

    private static int toScaledInt(String value) {
        return (int) Math.round(Double.parseDouble(value.trim()) * SCALE);
    }

    private static List<Map<String, Integer>> expectedCounts(
            List<InsertedPoint> points,
            List<TestArea> areas) {
        List<InsertedPoint> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingInt(point -> point.x));

        List<Map<String, Integer>> expectedResults = new ArrayList<>();
        for (TestArea area : areas) {
            Map<String, Integer> expected = new HashMap<>();
            int index = lowerBound(sorted, area.minX);
            while (index < sorted.size()) {
                InsertedPoint point = sorted.get(index);
                if (point.x > area.maxX) {
                    break;
                }
                if (point.y >= area.minY && point.y <= area.maxY) {
                    expected.merge(point.data, 1, Integer::sum);
                }
                index++;
            }
            expectedResults.add(expected);
        }
        return expectedResults;
    }

    private static int lowerBound(List<InsertedPoint> points, int x) {
        int left = 0;
        int right = points.size();
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (points.get(mid).x < x) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    private static Path findFile(String fileName) throws IOException {
        List<Path> candidates = List.of(
                Path.of(fileName),
                Path.of("testovaciedata", fileName),
                Path.of("app", "testovaciedata", fileName),
                Path.of("src", "main", "java", "com", "example",
                        "semestralka2", "testovaciedata", fileName),
                Path.of("app", "src", "main", "java", "com", "example",
                        "semestralka2", "testovaciedata", fileName)
        );

        for (Path path : candidates) {
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        throw new IOException(
                "Subor " + fileName + " sa nenasiel. Pracovny adresar: "
                        + Path.of("").toAbsolutePath());
    }

    private static void runTest(
            String name,
            Supplier<SpatialTree<Integer, String>> supplier,
            TestData data) {
        try {
            long totalStart = System.nanoTime();
            SpatialTree<Integer, String> tree = supplier.get();

            long insertStart = System.nanoTime();
            insertAll(tree, data.points);
            long insertNanos = System.nanoTime() - insertStart;

            long findNanos = queryAndCheck(tree, data);

            printPassed(name, data, totalStart, insertNanos, findNanos);
        } catch (Throwable t) {
            System.out.printf(
                    "  --> ZLYHALO pre %s: %s: %s%n%n",
                    name,
                    t.getClass().getSimpleName(),
                    t.getMessage());
        }
    }

    private static void insertAll(
            SpatialTree<Integer, String> tree,
            List<InsertedPoint> points) {
        for (InsertedPoint point : points) {
            tree.insert(new Point<>(point.x, point.y, point.data));
        }
    }

    private static long queryAndCheck(
            SpatialTree<Integer, String> tree,
            TestData data) {
        long findNanos = 0;
        for (int i = 0; i < data.areas.size(); i++) {
            TestArea area = data.areas.get(i);
            long findStart = System.nanoTime();
            List<Point<Integer, String>> found = tree.find(
                    area.minX, area.minY, area.maxX, area.maxY);
            findNanos += System.nanoTime() - findStart;
            checkArea(i, area, found, data.expectedResults.get(i));
        }
        return findNanos;
    }

    private static void checkArea(
            int index,
            TestArea area,
            List<Point<Integer, String>> found,
            Map<String, Integer> expected) {
        Map<String, Integer> actual = new HashMap<>();
        for (Point<Integer, String> point : found) {
            if (point.getX() < area.minX || point.getX() > area.maxX
                    || point.getY() < area.minY || point.getY() > area.maxY) {
                throw new AssertionError("Bod mimo oblast, query " + index);
            }
            actual.merge(point.getData(), 1, Integer::sum);
        }
        if (!actual.equals(expected)) {
            throw new AssertionError(
                    "Nespravne vysledky oblasti " + index
                            + ", expected=" + expected.size()
                            + ", actual=" + actual.size());
        }
    }

    private static void printPassed(
            String name,
            TestData data,
            long totalStart,
            long insertNanos,
            long findNanos) {
        long totalMillis = (System.nanoTime() - totalStart) / 1_000_000;
        System.out.println(name + ": passed; points: " + data.points.size()
                + "; areas: " + data.areas.size());
        System.out.printf("  --> Insert: %,d ms%n", insertNanos / 1_000_000);
        System.out.printf("  --> Find:   %,d ms%n", findNanos / 1_000_000);
        System.out.printf("  --> Celkovy cas pre %s: %,d ms%n%n", name, totalMillis);
    }
}
