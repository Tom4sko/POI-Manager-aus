
package com.example.semestralka2.service;

import com.example.semestralka2.model.POI;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class POIServiceTest {

    private POI createPOI(
            String id,
            String name,
            double latitude,
            double longitude) {

        LocalDateTime now = LocalDateTime.now();

        return new POI(
                id,
                name,
                "",
                latitude,
                longitude,
                "",
                now,
                now
        );
    }

    private POIService createService(TreeType type) {

        POIService service = new POIService();

        service.addPOI(createPOI(
                "694986", "POI 694986", 48.180464, 17.237571
        ));

        service.addPOI(createPOI(
                "694987", "POI 694987", 48.1805, 17.2376
        ));

        service.addPOI(createPOI(
                "694994", "POI 694994", 48.1819743, 17.2439434
        ));

        service.changeTreeType(type);

        return service;
    }

    @Test
    void testExactPositionForAllTrees() {

        for (TreeType type : TreeType.values()) {

            POIService service = createService(type);

            List<POI> result = service.findAtPosition(
                    48.180464,
                    17.237571
            );

            assertEquals(1, result.size());
            assertEquals("694986", result.get(0).getId());
        }
    }

    @Test
    void testRadiusSearchForAllTrees() {

        for (TreeType type : TreeType.values()) {

            POIService service = createService(type);

            List<POI> result = service.findWithinRadius(
                    48.180464,
                    17.237571,
                    100
            );

            assertEquals(2, result.size());
        }
    }

    @Test
    void testPointOutsideRadiusIsExcluded() {

        for (TreeType type : TreeType.values()) {

            POIService service = createService(type);

            List<POI> result = service.findWithinRadius(
                    48.180464,
                    17.237571,
                    100
            );

            boolean containsFarPoint = result.stream()
                    .anyMatch(poi -> poi.getId().equals("694994"));

            assertFalse(containsFarPoint);
        }
    }

    @Test
    void testInvalidCoordinatesAreRejected() {

        POIService service = new POIService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findAtPosition(100, 17)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findWithinRadius(48, 17, -100)
        );
    }
}
