package com.example.semestralka2.service;

import com.example.semestralka2.Point;
import com.example.semestralka2.SpatialTree;
import com.example.semestralka2.model.POI;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class POIService {

    private final List<POI> pois;

    private SpatialTree<Double, POI> spatialTree;
    private TreeType currentTreeType;

    // Približné geografické hranice Slovenska.
    private static final double MIN_LATITUDE = 47.7;
    private static final double MAX_LATITUDE = 49.6;
    private static final double MIN_LONGITUDE = 16.8;
    private static final double MAX_LONGITUDE = 22.6;

    public POIService() {
        pois = new ArrayList<>();
        currentTreeType = TreeType.KD_TREE;
        spatialTree = SpatialTreeFactory.create(currentTreeType);
    }

    public void addPOI(POI poi) {
        if (poi == null) {
            throw new IllegalArgumentException("POI cannot be null.");
        }

        pois.add(poi);
        insertIntoTree(poi);
    }

    public List<POI> getAllPOIs() {
        return new ArrayList<>(pois);
    }

    public void replaceAll(List<POI> newPois) {
        if (newPois == null) {
            throw new IllegalArgumentException("Zoznam POI nesmie byť null.");
        }

        pois.clear();
        pois.addAll(newPois);
        rebuildTree();
    }

    // Generovanie náhodných POI v geografickom rozsahu Slovenska.

    public void generateRandomPOIs(int count) {

        if (count < 1 || count > 100000) {
            throw new IllegalArgumentException(
                    "Počet generovaných bodov musí byť od 1 do 100 000."
            );
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < count; i++) {

            double latitude = random.nextDouble(
                    MIN_LATITUDE,
                    MAX_LATITUDE
            );

            double longitude = random.nextDouble(
                    MIN_LONGITUDE,
                    MAX_LONGITUDE
            );

            POI poi = new POI(
                    "Generovaný POI " + (pois.size() + 1),
                    "Automaticky vygenerovaný bod záujmu.",
                    latitude,
                    longitude,
                    "Testovacia adresa " + (pois.size() + 1)
            );

            addPOI(poi);
        }
    }

    public boolean updatePOI(
            String id,
            String name,
            String description,
            double latitude,
            double longitude,
            String address) {

        POI poi = findById(id);

        if (poi == null) {
            return false;
        }

        boolean coordinatesChanged =
                Double.compare(poi.getLatitude(), latitude) != 0
                || Double.compare(poi.getLongitude(), longitude) != 0;

        poi.updateDetails(
                name,
                description,
                latitude,
                longitude,
                address
        );

        if (coordinatesChanged) {
            rebuildTree();
        }

        return true;
    }

    public List<POI> findInRectangle(
            double minLongitude,
            double minLatitude,
            double maxLongitude,
            double maxLatitude) {

        validateCoordinates(minLatitude, minLongitude);
        validateCoordinates(maxLatitude, maxLongitude);

        if (minLongitude > maxLongitude || minLatitude > maxLatitude) {
            throw new IllegalArgumentException("Neplatné hranice obdĺžnika.");
        }

        return queryRectangle(
                minLongitude,
                minLatitude,
                maxLongitude,
                maxLatitude
        );
    }

    public List<POI> findByNameInRectangle(
            String searchName,
            double minLongitude,
            double minLatitude,
            double maxLongitude,
            double maxLatitude) {

        if (searchName == null || searchName.isBlank()) {
            throw new IllegalArgumentException(
                    "Vyhľadávaný názov nesmie byť prázdny."
            );
        }

        List<POI> candidates = findInRectangle(
                minLongitude,
                minLatitude,
                maxLongitude,
                maxLatitude
        );

        String normalizedSearch = normalizeText(searchName);
        List<POI> result = new ArrayList<>();

        for (POI poi : candidates) {
            String normalizedName = normalizeText(poi.getName());

            if (normalizedName.contains(normalizedSearch)) {
                result.add(poi);
            }
        }

        return result;
    }

    private String normalizeText(String text) {
        String normalized = Normalizer.normalize(
                text.toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        );

        return normalized.replaceAll("\\p{M}", "");
    }

    public List<POI> findAtPosition(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);

        return queryRectangle(
                longitude,
                latitude,
                longitude,
                latitude
        );
    }

    public List<POI> findWithinRadius(
            double latitude,
            double longitude,
            double radiusMeters) {

        validateCoordinates(latitude, longitude);

        if (radiusMeters < 0 || !Double.isFinite(radiusMeters)) {
            throw new IllegalArgumentException(
                    "Polomer musí byť platné nezáporné číslo."
            );
        }

        double latitudeDelta = radiusMeters / 110574.0;

        double minLatitude = Math.max(-90, latitude - latitudeDelta);
        double maxLatitude = Math.min(90, latitude + latitudeDelta);

        double cosLatitude = Math.cos(Math.toRadians(latitude));

        double longitudeDelta;

        if (cosLatitude < 0.000001) {
            longitudeDelta = 180;
        } else {
            longitudeDelta = radiusMeters / (110574.0 * cosLatitude);
        }

        List<POI> candidates = new ArrayList<>();

        if (longitudeDelta >= 180) {

            candidates.addAll(queryRectangle(
                    -180, minLatitude, 180, maxLatitude
            ));

        } else {

            double minLongitude = longitude - longitudeDelta;
            double maxLongitude = longitude + longitudeDelta;

            if (minLongitude < -180) {

                candidates.addAll(queryRectangle(
                        minLongitude + 360, minLatitude, 180, maxLatitude
                ));

                candidates.addAll(queryRectangle(
                        -180, minLatitude, maxLongitude, maxLatitude
                ));

            } else if (maxLongitude > 180) {

                candidates.addAll(queryRectangle(
                        minLongitude, minLatitude, 180, maxLatitude
                ));

                candidates.addAll(queryRectangle(
                        -180, minLatitude, maxLongitude - 360, maxLatitude
                ));

            } else {

                candidates.addAll(queryRectangle(
                        minLongitude, minLatitude, maxLongitude, maxLatitude
                ));
            }
        }

        Map<String, POI> uniqueCandidates = new LinkedHashMap<>();

        for (POI poi : candidates) {
            uniqueCandidates.put(poi.getId(), poi);
        }

        List<POI> result = new ArrayList<>();

        for (POI poi : uniqueCandidates.values()) {

            double distance = calculateDistance(
                    latitude,
                    longitude,
                    poi.getLatitude(),
                    poi.getLongitude()
            );

            if (distance <= radiusMeters) {
                result.add(poi);
            }
        }

        return result;
    }

    private double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final double EARTH_RADIUS = 6371000;

        double latitudeDifference = Math.toRadians(lat2 - lat1);
        double longitudeDifference = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudeDifference / 2)
                * Math.sin(longitudeDifference / 2);

        // Ochrana pred drobnou numerickou odchýlkou.
        a = Math.max(0, Math.min(1, a));

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return EARTH_RADIUS * c;
    }

    private List<POI> queryRectangle(
            double minLongitude,
            double minLatitude,
            double maxLongitude,
            double maxLatitude) {

        List<Point<Double, POI>> points = spatialTree.find(
                minLongitude,
                minLatitude,
                maxLongitude,
                maxLatitude
        );

        List<POI> result = new ArrayList<>();

        for (Point<Double, POI> point : points) {
            result.add(point.getData());
        }

        return result;
    }

    private void validateCoordinates(double latitude, double longitude) {

        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90
                || latitude > 90
                || longitude < -180
                || longitude > 180) {

            throw new IllegalArgumentException("Neplatné GPS súradnice.");
        }
    }

    public boolean deletePOI(String id) {

        boolean removed = pois.removeIf(
                poi -> poi.getId().equals(id)
        );

        if (removed) {
            rebuildTree();
        }

        return removed;
    }

    public POI findById(String id) {

        for (POI poi : pois) {
            if (poi.getId().equals(id)) {
                return poi;
            }
        }

        return null;
    }

    public void changeTreeType(TreeType type) {

        if (type == null) {
            throw new IllegalArgumentException("Typ stromu nesmie byť null.");
        }

        currentTreeType = type;
        rebuildTree();
    }

    private void rebuildTree() {

        spatialTree = SpatialTreeFactory.create(currentTreeType);

        for (POI poi : pois) {
            insertIntoTree(poi);
        }
    }

    private void insertIntoTree(POI poi) {

        spatialTree.insert(
                new Point<>(
                        poi.getLongitude(),
                        poi.getLatitude(),
                        poi
                )
        );
    }

    public TreeType getCurrentTreeType() {
        return currentTreeType;
    }

    public int getPOICount() {
        return pois.size();
    }

    public void clear() {
        pois.clear();
        rebuildTree();
    }
}
