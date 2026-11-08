
package com.example.semestralka2.view;

import com.example.semestralka2.model.BenchmarkResult;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkChartPanel extends JPanel {

    private String title;
    private String metric;
    private List<BenchmarkResult> results = new ArrayList<>();

    public BenchmarkChartPanel(String title, String metric) {
        this.title = title;
        this.metric = metric;

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(350, 260));
    }

    public void setResults(List<BenchmarkResult> results) {
        this.results = new ArrayList<>(results);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int width = getWidth();
        int height = getHeight();

        g.setColor(new Color(35, 40, 55));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(title, 20, 28);

        if (results.isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("Zatiaľ žiadne výsledky", 20, 70);
            g.dispose();
            return;
        }

        int left = 75;
        int right = width - 25;
        int top = 60;
        int bottom = height - 65;

        int chartWidth = right - left;
        int chartHeight = bottom - top;

        double maxValue = 0;

        for (BenchmarkResult result : results) {
            maxValue = Math.max(maxValue, getValue(result));
        }

        if (maxValue == 0) {
            maxValue = 1;
        }

        // Pomocné vodorovné čiary a hodnoty osi Y.
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));

        for (int i = 0; i <= 4; i++) {

            int y = bottom - (chartHeight * i / 4);

            g.setColor(new Color(225, 228, 235));
            g.drawLine(left, y, right, y);

            double value = maxValue * i / 4.0;

            g.setColor(new Color(100, 105, 115));
            g.drawString(
                    String.format("%.2f", value),
                    8,
                    y + 4
            );
        }

        g.setColor(new Color(130, 135, 145));
        g.drawLine(left, top, left, bottom);
        g.drawLine(left, bottom, right, bottom);

        // Stĺpce pre jednotlivé dátové štruktúry.
        int groupWidth = chartWidth / results.size();
        int barWidth = Math.min(55, groupWidth / 2);

        Color[] colors = {
                new Color(75, 130, 230),
                new Color(105, 180, 145),
                new Color(160, 125, 220)
        };

        for (int i = 0; i < results.size(); i++) {

            BenchmarkResult result = results.get(i);

            double value = getValue(result);

            int barHeight = (int) (
                    value / maxValue * chartHeight
            );

            int x = left
                    + i * groupWidth
                    + (groupWidth - barWidth) / 2;

            int y = bottom - barHeight;

            g.setColor(colors[i % colors.length]);
            g.fillRoundRect(
                    x,
                    y,
                    barWidth,
                    Math.max(barHeight, 2),
                    8,
                    8
            );

            g.setColor(new Color(45, 50, 65));
            g.setFont(new Font("SansSerif", Font.BOLD, 10));

            String valueText = String.format("%.2f", value);

            FontMetrics metrics = g.getFontMetrics();

            g.drawString(
                    valueText,
                    x + (barWidth - metrics.stringWidth(valueText)) / 2,
                    Math.max(top - 5, y - 7)
            );

            g.setFont(new Font("SansSerif", Font.PLAIN, 10));

            String name = result.getTreeName();

            int textWidth = g.getFontMetrics().stringWidth(name);

            g.drawString(
                    name,
                    x + (barWidth - textWidth) / 2,
                    bottom + 20
            );
        }

        g.setColor(new Color(100, 105, 115));
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString(metric, left, height - 12);

        g.dispose();
    }

    private double getValue(BenchmarkResult result) {

        return switch (metric) {
            case "Vytvorenie (ms)" -> result.getCreationMillis();
            case "Vkladanie (ms)" -> result.getInsertMillis();
            case "Vyhľadávanie (ms)" -> result.getFindMillis();
            default -> 0;
        };
    }
}
