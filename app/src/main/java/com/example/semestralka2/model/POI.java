
package com.example.semestralka2.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class POI {

    private final String id;
    private String name;
    private String description;
    private double latitude;
    private double longitude;
    private String address;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public POI(String name, String description,
               double latitude, double longitude, String address) {

        this(
                UUID.randomUUID().toString(),
                name,
                description,
                latitude,
                longitude,
                address,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public POI(String id, String name, String description,
               double latitude, double longitude, String address,
               LocalDateTime createdAt, LocalDateTime updatedAt) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID nesmie byť prázdne.");
        }

        validateName(name);
        validateCoordinates(latitude, longitude);

        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Dátumy POI nesmú byť null.");
        }

        this.id = id;
        this.name = name;
        this.description = description == null ? "" : description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address == null ? "" : address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateDetails(
            String name,
            String description,
            double latitude,
            double longitude,
            String address) {

        validateName(name);
        validateCoordinates(latitude, longitude);

        this.name = name;
        this.description = description == null ? "" : description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address == null ? "" : address;

        updateTimestamp();
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
        updateTimestamp();
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
        updateTimestamp();
    }

    public void setLatitude(double latitude) {
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        updateTimestamp();
    }

    public void setLongitude(double longitude) {
        validateCoordinates(latitude, longitude);
        this.longitude = longitude;
        updateTimestamp();
    }

    public void setAddress(String address) {
        this.address = address == null ? "" : address;
        updateTimestamp();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Názov POI je povinný.");
        }
    }

    private static void validateCoordinates(double latitude, double longitude) {
        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {

            throw new IllegalArgumentException("Neplatné GPS súradnice.");
        }
    }

    private void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return name + " (" + latitude + ", " + longitude + ")";
    }
}
