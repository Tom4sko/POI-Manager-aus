
package com.example.semestralka2.view;

import com.example.semestralka2.model.POI;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapPanel extends JPanel {

    private final JXMapViewer mapViewer;
    private final ClusterPainter clusterPainter;

    private List<POI> currentPOIs = new ArrayList<>();
    private boolean initialFitDone = false;

    public MapPanel() {

        setLayout(new BorderLayout());

        mapViewer = new JXMapViewer();

        OSMTileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        mapViewer.setTileFactory(tileFactory);

        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(new GeoPosition(48.7, 19.7));

        PanMouseInputListener panListener =
                new PanMouseInputListener(mapViewer);

        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);

        mapViewer.addMouseWheelListener(
                new ZoomMouseWheelListenerCursor(mapViewer)
        );

        clusterPainter = new ClusterPainter();
        mapViewer.setOverlayPainter(clusterPainter);

        add(createMapControls(), BorderLayout.NORTH);
        add(mapViewer, BorderLayout.CENTER);
        add(createAttribution(), BorderLayout.SOUTH);
    }

    private JPanel createMapControls() {

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");
        JButton fitButton = new JButton("Zobraziť všetky");

        zoomInButton.setToolTipText("Priblížiť mapu");
        zoomOutButton.setToolTipText("Oddialiť mapu");

        zoomInButton.addActionListener(e -> zoomIn());
        zoomOutButton.addActionListener(e -> zoomOut());
        fitButton.addActionListener(e -> fitAllPOIs());

        controls.add(zoomOutButton);
        controls.add(zoomInButton);
        controls.add(fitButton);

        return controls;
    }

    private JLabel createAttribution() {

        JLabel attribution = new JLabel(
                "Map data © OpenStreetMap contributors",
                SwingConstants.RIGHT
        );

        attribution.setFont(new Font("SansSerif", Font.PLAIN, 10));

        attribution.setBorder(
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
        );

        return attribution;
    }

    private void zoomIn() {

        int minimumZoom =
                mapViewer.getTileFactory().getInfo().getMinimumZoomLevel();

        mapViewer.setZoom(
                Math.max(minimumZoom, mapViewer.getZoom() - 1)
        );
    }

    private void zoomOut() {

        int maximumZoom =
                mapViewer.getTileFactory().getInfo().getMaximumZoomLevel();

        mapViewer.setZoom(
                Math.min(maximumZoom, mapViewer.getZoom() + 1)
        );
    }

    public void displayPOIs(List<POI> pois) {

        currentPOIs = new ArrayList<>(pois);

        clusterPainter.setPOIs(currentPOIs);

        mapViewer.repaint();

        // Automatické priblíženie iba pri prvom načítaní bodov.
        if (!initialFitDone && !currentPOIs.isEmpty()) {

            initialFitDone = true;

            SwingUtilities.invokeLater(this::fitAllPOIs);
        }
    }

    public void fitAllPOIs() {

        if (currentPOIs.isEmpty()) {
            return;
        }

        Set<GeoPosition> positions = new HashSet<>();

        for (POI poi : currentPOIs) {

            positions.add(
                    new GeoPosition(
                            poi.getLatitude(),
                            poi.getLongitude()
                    )
            );
        }

        if (positions.size() == 1) {

            GeoPosition position = positions.iterator().next();

            mapViewer.setZoom(10);
            mapViewer.setAddressLocation(position);
            mapViewer.recenterToAddressLocation();

        } else {

            mapViewer.zoomToBestFit(positions, 0.8);
        }

        mapViewer.repaint();
    }

    public void focusOnPOI(POI poi) {

        if (poi == null) {
            return;
        }

        GeoPosition position = new GeoPosition(
                poi.getLatitude(),
                poi.getLongitude()
        );

        mapViewer.setZoom(10);
        mapViewer.setAddressLocation(position);
        mapViewer.recenterToAddressLocation();

        mapViewer.repaint();
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

    // Vlastný vykresľovač zoskupených markerov.
    private static class ClusterPainter implements Painter<JXMapViewer> {

        private List<POI> pois = new ArrayList<>();

        private static final int CLUSTER_SIZE = 55;

        public void setPOIs(List<POI> pois) {
            this.pois = new ArrayList<>(pois);
        }

        @Override
        public void paint(
                Graphics2D graphics,
                JXMapViewer map,
                int width,
                int height) {

            if (pois.isEmpty()) {
                return;
            }

            Graphics2D g = (Graphics2D) graphics.create();

            try {

                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Rectangle viewport = map.getViewportBounds();

                Map<Long, Cluster> clusters = new HashMap<>();

                for (POI poi : pois) {

                    GeoPosition position = new GeoPosition(
                            poi.getLatitude(),
                            poi.getLongitude()
                    );

                    Point2D worldPoint =
                            map.getTileFactory().geoToPixel(
                                    position,
                                    map.getZoom()
                            );

                    int x = (int) (worldPoint.getX() - viewport.x);
                    int y = (int) (worldPoint.getY() - viewport.y);

                    // Body mimo obrazovky nevykresľujeme.
                    if (x < -CLUSTER_SIZE || x > width + CLUSTER_SIZE
                            || y < -CLUSTER_SIZE || y > height + CLUSTER_SIZE) {
                        continue;
                    }

                    int cellX = x / CLUSTER_SIZE;
                    int cellY = y / CLUSTER_SIZE;

                    long key = ((long) cellX << 32)
                            | (cellY & 0xffffffffL);

                    clusters.computeIfAbsent(
                            key,
                            ignored -> new Cluster()
                    ).add(x, y);
                }

                for (Cluster cluster : clusters.values()) {

                    int x = (int) (cluster.sumX / cluster.count);
                    int y = (int) (cluster.sumY / cluster.count);

                    boolean grouped = cluster.count > 1;

                    int diameter = grouped ? 38 : 16;

                    int drawX = x - diameter / 2;
                    int drawY = y - diameter / 2;

                    g.setColor(
                            grouped
                                    ? new Color(35, 105, 190, 220)
                                    : new Color(220, 65, 65, 230)
                    );

                    g.fillOval(
                            drawX,
                            drawY,
                            diameter,
                            diameter
                    );

                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(2));

                    g.drawOval(
                            drawX,
                            drawY,
                            diameter,
                            diameter
                    );

                    if (grouped) {

                        String count = String.valueOf(cluster.count);

                        Font font = new Font(
                                "SansSerif",
                                Font.BOLD,
                                12
                        );

                        g.setFont(font);

                        FontMetrics metrics = g.getFontMetrics();

                        int textX = x
                                - metrics.stringWidth(count) / 2;

                        int textY = y
                                + (metrics.getAscent()
                                - metrics.getDescent()) / 2;

                        g.drawString(count, textX, textY);
                    }
                }

            } finally {
                g.dispose();
            }
        }

        private static class Cluster {

            private int count;
            private double sumX;
            private double sumY;

            void add(int x, int y) {
                count++;
                sumX += x;
                sumY += y;
            }
        }
    }
}
