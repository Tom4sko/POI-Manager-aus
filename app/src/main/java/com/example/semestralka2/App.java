
package com.example.semestralka2;

import com.example.semestralka2.controller.POIController;
import com.example.semestralka2.service.POIService;
import com.example.semestralka2.view.MainWindow;

import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            POIService poiService = new POIService();

            MainWindow mainWindow = new MainWindow();

            new POIController(poiService, mainWindow);

            mainWindow.setVisible(true);
        });
    }
}
