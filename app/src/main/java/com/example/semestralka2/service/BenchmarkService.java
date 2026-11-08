
package com.example.semestralka2.service;

import com.example.semestralka2.Point;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.model.BenchmarkResult;
import com.example.semestralka2.model.POI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class BenchmarkService {

    private static final int WARMUP_ROUNDS = 1;
    private static final int MEASUREMENT_ROUNDS = 3;
    private static final int QUERY_COUNT = 100;

    private static final long RANDOM_SEED = 42L;

    private static class TestArea {

        private final double minLongitude;
        private final double minLatitude;
        private final double maxLongitude;
        private final double maxLatitude;

        private TestArea(
                double minLongitude,
                double minLatitude,
                double maxLongitude,
                double maxLatitude) {

            this.minLongitude = minLongitude;
            this.minLatitude = minLatitude;
            this.maxLongitude = maxLongitude;
            this.maxLatitude = maxLatitude;
        }
    }

    private static class Measurement {

        private final long creationNanos;
        private final long insertNanos;
        private final long findNanos;

        private Measurement(
                long creationNanos,
                long insertNanos,
                long findNanos) {

            this.creationNanos = creationNanos;
            this.insertNanos = insertNanos;
            this.findNanos = findNanos;
        }
    }

    public List<BenchmarkResult> runBenchmark(List<POI> sourcePOIs) {

        if (sourcePOIs == null || sourcePOIs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Na spustenie benchmarku musíš mať načítané POI."
            );
        }

        List<POI> pois = new ArrayList<>(sourcePOIs);

        List<TestArea> areas = generateTestAreas(pois);

        List<BenchmarkResult> results = new ArrayList<>();

        results.add(benchmarkTree(
                "KDTree",
                () -> SpatialTreeFactory.create(TreeType.KD_TREE),
                pois,
                areas
        ));

        results.add(benchmarkTree(
                "QuadTree",
                () -> SpatialTreeFactory.create(TreeType.QUAD_TREE),
                pois,
                areas
        ));

        results.add(benchmarkTree(
                "QuadTreeList",
                () -> SpatialTreeFactory.create(TreeType.QUAD_TREE_LIST),
                pois,
                areas
        ));

        return results;
    }

    private BenchmarkResult benchmarkTree(
            String treeName,
            Supplier<SpatialTree<Double, POI>> treeSupplier,
            List<POI> pois,
            List<TestArea> areas) {

        // Zahrievacie meranie, ktoré sa nezapočítava do výsledkov.
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            runSingleMeasurement(treeSupplier, pois, areas);
        }

        long[] creationTimes = new long[MEASUREMENT_ROUNDS];
        long[] insertTimes = new long[MEASUREMENT_ROUNDS];
        long[] findTimes = new long[MEASUREMENT_ROUNDS];

        for (int i = 0; i < MEASUREMENT_ROUNDS; i++) {

            Measurement measurement = runSingleMeasurement(
                    treeSupplier,
                    pois,
                    areas
            );

            creationTimes[i] = measurement.creationNanos;
            insertTimes[i] = measurement.insertNanos;
            findTimes[i] = measurement.findNanos;
        }

        return new BenchmarkResult(
                treeName,
                pois.size(),
                areas.size(),
                median(creationTimes),
                median(insertTimes),
                median(findTimes)
        );
    }

    private Measurement runSingleMeasurement(
            Supplier<SpatialTree<Double, POI>> treeSupplier,
            List<POI> pois,
            List<TestArea> areas) {

        // 1. Vytvorenie prázdnej dátovej štruktúry.
        long creationStart = System.nanoTime();

        SpatialTree<Double, POI> tree = treeSupplier.get();

        long creationNanos = System.nanoTime() - creationStart;

        // 2. Vkladanie bodov.
        long insertStart = System.nanoTime();

        for (POI poi : pois) {

            tree.insert(new Point<>(
                    poi.getLongitude(),
                    poi.getLatitude(),
                    poi
            ));
        }

        long insertNanos = System.nanoTime() - insertStart;

        // 3. Priestorové vyhľadávanie.
        long findNanos = 0;

        for (TestArea area : areas) {

            long findStart = System.nanoTime();

            List<Point<Double, POI>> found = tree.find(
                    area.minLongitude,
                    area.minLatitude,
                    area.maxLongitude,
                    area.maxLatitude
            );

            findNanos += System.nanoTime() - findStart;

            // Kontrola správnosti je mimo meraného času.
            checkResults(pois, area, found);
        }

        return new Measurement(
                creationNanos,
                insertNanos,
                findNanos
        );
    }

    private void checkResults(
            List<POI> allPOIs,
            TestArea area,
            List<Point<Double, POI>> found) {

        Set<String> expected = new HashSet<>();

        for (POI poi : allPOIs) {

            if (isInside(
                    poi.getLongitude(),
                    poi.getLatitude(),
                    area)) {

                expected.add(poi.getId());
            }
        }

        Set<String> actual = new HashSet<>();

        for (Point<Double, POI> point : found) {

            POI poi = point.getData();

            if (!isInside(
                    point.getX(),
                    point.getY(),
                    area)) {

                throw new IllegalStateException(
                        "Strom vrátil bod mimo vyhľadávaciu oblasť."
                );
            }

            actual.add(poi.getId());
        }

        if (!expected.equals(actual)) {
            throw new IllegalStateException(
                    "Nesprávne výsledky vyhľadávania. "
                    + "Očakávaných bodov: " + expected.size()
                    + ", nájdených bodov: " + actual.size()
            );
        }
    }

    private boolean isInside(
            double longitude,
            double latitude,
            TestArea area) {

        return longitude >= area.minLongitude
                && longitude <= area.maxLongitude
                && latitude >= area.minLatitude
                && latitude <= area.maxLatitude;
    }

    private List<TestArea> generateTestAreas(List<POI> pois) {

        double minLatitude = 90;
        double maxLatitude = -90;

        double minLongitude = 180;
        double maxLongitude = -180;

        for (POI poi : pois) {

            minLatitude = Math.min(minLatitude, poi.getLatitude());
            maxLatitude = Math.max(maxLatitude, poi.getLatitude());

            minLongitude = Math.min(minLongitude, poi.getLongitude());
            maxLongitude = Math.max(maxLongitude, poi.getLongitude());
        }

        double latitudeRange = Math.max(
                maxLatitude - minLatitude,
                0.001
        );

        double longitudeRange = Math.max(
                maxLongitude - minLongitude,
                0.001
        );

        double latitudeDelta = latitudeRange * 0.1;
        double longitudeDelta = longitudeRange * 0.1;

        Random random = new Random(RANDOM_SEED);

        List<TestArea> areas = new ArrayList<>();

        for (int i = 0; i < QUERY_COUNT; i++) {

            POI center = pois.get(random.nextInt(pois.size()));

            double centerLatitude = center.getLatitude();
            double centerLongitude = center.getLongitude();

            areas.add(new TestArea(
                    Math.max(minLongitude, centerLongitude - longitudeDelta),
                    Math.max(minLatitude, centerLatitude - latitudeDelta),
                    Math.min(maxLongitude, centerLongitude + longitudeDelta),
                    Math.min(maxLatitude, centerLatitude + latitudeDelta)
            ));
        }

        return areas;
    }

    private long median(long[] values) {

        long[] sorted = values.clone();
        Arrays.sort(sorted);

        return sorted[sorted.length / 2];
    }
}
