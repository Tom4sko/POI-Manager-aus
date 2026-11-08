
package com.example.semestralka2.storage;

import com.example.semestralka2.model.POI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CSVManager {

    private static final String SIMPLE_HEADER = "id;lat;lon";

    private static final String FULL_HEADER =
            "id;name;description;lat;lon;address;created_at;updated_at";

    public List<POI> load(Path file) throws IOException {

        List<POI> pois = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String header = reader.readLine();

            if (header != null && header.startsWith("\uFEFF")) {
                header = header.substring(1);
            }

            boolean simpleFormat = SIMPLE_HEADER.equalsIgnoreCase(header);
            boolean fullFormat = FULL_HEADER.equalsIgnoreCase(header);

            if (!simpleFormat && !fullFormat) {
                throw new IOException("Nepodporovaný formát CSV súboru.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                try {
                    List<String> values = parseLine(line);

                    POI poi;

                    if (simpleFormat) {

                        if (values.size() != 3) {
                            throw new IllegalArgumentException(
                                    "Očakávajú sa 3 stĺpce: id;lat;lon.");
                        }

                        String id = values.get(0);
                        double latitude = Double.parseDouble(values.get(1));
                        double longitude = Double.parseDouble(values.get(2));

                        LocalDateTime now = LocalDateTime.now();

                        poi = new POI(
                                id,
                                "POI " + id,
                                "",
                                latitude,
                                longitude,
                                "",
                                now,
                                now
                        );

                    } else {

                        if (values.size() != 8) {
                            throw new IllegalArgumentException(
                                    "Očakáva sa 8 stĺpcov.");
                        }

                        poi = new POI(
                                values.get(0),
                                values.get(1),
                                values.get(2),
                                Double.parseDouble(values.get(3)),
                                Double.parseDouble(values.get(4)),
                                values.get(5),
                                LocalDateTime.parse(values.get(6)),
                                LocalDateTime.parse(values.get(7))
                        );
                    }

                    pois.add(poi);

                } catch (RuntimeException exception) {
                    throw new IOException(
                            "Chyba na riadku " + lineNumber + ": "
                                    + exception.getMessage(),
                            exception
                    );
                }
            }
        }

        return pois;
    }

    public void save(Path file, List<POI> pois) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            writer.write(FULL_HEADER);
            writer.newLine();

            for (POI poi : pois) {

                List<String> values = List.of(
                        poi.getId(),
                        poi.getName(),
                        poi.getDescription(),
                        Double.toString(poi.getLatitude()),
                        Double.toString(poi.getLongitude()),
                        poi.getAddress(),
                        poi.getCreatedAt().toString(),
                        poi.getUpdatedAt().toString()
                );

                writer.write(formatLine(values));
                writer.newLine();
            }
        }
    }

    private List<String> parseLine(String line) {

        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char character = line.charAt(i);

            if (character == '"') {

                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    value.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (character == ';' && !insideQuotes) {

                values.add(value.toString());
                value.setLength(0);

            } else {
                value.append(character);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException("Neukončené úvodzovky.");
        }

        values.add(value.toString());

        return values;
    }

    private String formatLine(List<String> values) {

        List<String> escaped = new ArrayList<>();

        for (String value : values) {

            String safeValue = value.replace("\"", "\"\"");

            if (safeValue.contains(";")
                    || safeValue.contains("\"")
                    || safeValue.contains("\n")
                    || safeValue.contains("\r")) {

                safeValue = "\"" + safeValue + "\"";
            }

            escaped.add(safeValue);
        }

        return String.join(";", escaped);
    }
}
