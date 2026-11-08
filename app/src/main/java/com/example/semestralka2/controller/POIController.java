
package com.example.semestralka2.controller;

import com.example.semestralka2.model.POI;
import com.example.semestralka2.service.POIService;
import com.example.semestralka2.storage.CSVManager;
import com.example.semestralka2.view.MainWindow;
import com.example.semestralka2.service.TreeType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.nio.file.Path;

public class POIController {

    private final POIService poiService;
    private final MainWindow mainWindow;
    private final CSVManager csvManager;

    public POIController(POIService poiService, MainWindow mainWindow) {
        this.poiService = poiService;
        this.mainWindow = mainWindow;
        this.csvManager = new CSVManager();

        initializeListeners();
        refreshPOIs();
    }

    private void initializeListeners() {
        mainWindow.getAddButton().addActionListener(e -> addPOI());
        mainWindow.getEditButton().addActionListener(e -> editPOI());
        mainWindow.getDeleteButton().addActionListener(e -> deletePOI());
        mainWindow.getRefreshButton().addActionListener(e -> refreshPOIs());

        mainWindow.getLoadButton().addActionListener(e -> loadCSV());
        mainWindow.getSaveButton().addActionListener(e -> saveCSV());

        mainWindow.getStructureSelector().addActionListener(e -> {
            TreeType selectedType =
                    (TreeType) mainWindow.getStructureSelector().getSelectedItem();

            if (selectedType != null) {
                poiService.changeTreeType(selectedType);
                refreshPOIs();
            }
        });
    }

    private void addPOI() {

        JTextField nameField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField latitudeField = new JTextField();
        JTextField longitudeField = new JTextField();
        JTextField addressField = new JTextField();

        Object[] fields = {
                "Názov:", nameField,
                "Popis:", descriptionField,
                "Latitude:", latitudeField,
                "Longitude:", longitudeField,
                "Adresa:", addressField
        };

        int result = JOptionPane.showConfirmDialog(
                mainWindow,
                fields,
                "Pridať nový POI",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty()) {
                mainWindow.showError("Názov POI je povinný.");
                return;
            }

            double latitude = parseCoordinate(latitudeField.getText());
            double longitude = parseCoordinate(longitudeField.getText());

            POI poi = new POI(
                    name,
                    description,
                    latitude,
                    longitude,
                    address
            );

            poiService.addPOI(poi);
            refreshPOIs();

            mainWindow.showMessage("POI bol úspešne pridaný.");

        } catch (NumberFormatException exception) {
            mainWindow.showError("Súradnice musia byť platné čísla.");
        } catch (IllegalArgumentException exception) {
            mainWindow.showError(exception.getMessage());
        }
    }

    private void editPOI() {

        String selectedId = mainWindow.getSelectedPOIId();

        if (selectedId == null) {
            mainWindow.showError("Najskôr vyber POI v tabuľke.");
            return;
        }

        POI poi = poiService.findById(selectedId);

        if (poi == null) {
            mainWindow.showError("Vybraný POI sa už v systéme nenachádza.");
            refreshPOIs();
            return;
        }

        JTextField nameField = new JTextField(poi.getName());
        JTextField descriptionField = new JTextField(poi.getDescription());
        JTextField latitudeField = new JTextField(
                String.valueOf(poi.getLatitude())
        );
        JTextField longitudeField = new JTextField(
                String.valueOf(poi.getLongitude())
        );
        JTextField addressField = new JTextField(poi.getAddress());

        Object[] fields = {
                "Názov:", nameField,
                "Popis:", descriptionField,
                "Latitude:", latitudeField,
                "Longitude:", longitudeField,
                "Adresa:", addressField,
                "Created at:", poi.getCreatedAt(),
                "Updated at:", poi.getUpdatedAt()
        };

        int result = JOptionPane.showConfirmDialog(
                mainWindow,
                fields,
                "Upraviť POI",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty()) {
                mainWindow.showError("Názov POI je povinný.");
                return;
            }

            double latitude = parseCoordinate(latitudeField.getText());
            double longitude = parseCoordinate(longitudeField.getText());

            boolean updated = poiService.updatePOI(
                    selectedId,
                    name,
                    description,
                    latitude,
                    longitude,
                    address
            );

            if (!updated) {
                mainWindow.showError("POI sa nepodarilo nájsť.");
                return;
            }

            refreshPOIs();
            mainWindow.showMessage("POI bol úspešne upravený.");

        } catch (NumberFormatException exception) {
            mainWindow.showError("Súradnice musia byť platné čísla.");
        } catch (IllegalArgumentException exception) {
            mainWindow.showError(exception.getMessage());
        }
    }

    private double parseCoordinate(String value) {
        return Double.parseDouble(value.trim().replace(',', '.'));
    }

    private void deletePOI() {

        String selectedId = mainWindow.getSelectedPOIId();

        if (selectedId == null) {
            mainWindow.showError("Najskôr vyber POI v tabuľke.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                mainWindow,
                "Naozaj chceš vymazať vybraný POI?",
                "Potvrdenie vymazania",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {

            boolean deleted = poiService.deletePOI(selectedId);

            if (deleted) {
                refreshPOIs();
                mainWindow.showMessage("POI bol vymazaný.");
            } else {
                mainWindow.showError("POI sa nepodarilo vymazať.");
            }
        }
    }

    private void loadCSV() {

        JFileChooser chooser = createCSVChooser();

        int result = chooser.showOpenDialog(mainWindow);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();

        try {
            var loadedPOIs = csvManager.load(file);

            poiService.replaceAll(loadedPOIs);
            refreshPOIs();

            mainWindow.showMessage(
                    "Načítaných POI: " + loadedPOIs.size()
            );

        } catch (IOException | RuntimeException exception) {
            mainWindow.showError(
                    "Nepodarilo sa načítať CSV:\n" + exception.getMessage()
            );
        }
    }

    private void saveCSV() {

        JFileChooser chooser = createCSVChooser();

        int result = chooser.showSaveDialog(mainWindow);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();

        if (!file.toString().toLowerCase().endsWith(".csv")) {
            file = Path.of(file.toString() + ".csv");
        }

        if (java.nio.file.Files.exists(file)) {

            int overwrite = JOptionPane.showConfirmDialog(
                    mainWindow,
                    "Súbor už existuje. Chceš ho prepísať?",
                    "Potvrdenie",
                    JOptionPane.YES_NO_OPTION
            );

            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            csvManager.save(file, poiService.getAllPOIs());

            mainWindow.showMessage("CSV bolo úspešne uložené.");

        } catch (IOException exception) {
            mainWindow.showError(
                    "Nepodarilo sa uložiť CSV:\n" + exception.getMessage()
            );
        }
    }

    private JFileChooser createCSVChooser() {

        JFileChooser chooser = new JFileChooser();

        chooser.setFileFilter(
                new FileNameExtensionFilter("CSV súbory (*.csv)", "csv")
        );

        return chooser;
    }

    private void refreshPOIs() {

        var pois = poiService.getAllPOIs();

        mainWindow.displayPOIs(pois);

        mainWindow.setStatusMessage(
                "Počet bodov: " + pois.size()
                        + " | Aktívna štruktúra: "
                        + getTreeName(poiService.getCurrentTreeType())
        );
    }

    private String getTreeName(TreeType type) {

        return switch (type) {
            case KD_TREE -> "KDTree";
            case QUAD_TREE -> "QuadTree";
            case QUAD_TREE_LIST -> "QuadTreeList";
        };
    }
}
