
package com.example.semestralka2.view;

import com.example.semestralka2.model.BenchmarkResult;
import com.example.semestralka2.model.POI;
import com.example.semestralka2.service.BenchmarkService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BenchmarkWindow extends JFrame {

    private final BenchmarkService benchmarkService;
    private final List<POI> pois;

    private DefaultTableModel tableModel;
    private JTable resultsTable;

    private JLabel statusLabel;
    private JLabel pointsLabel;

    private JButton runButton;

    private BenchmarkChartPanel creationChart;
    private BenchmarkChartPanel insertChart;
    private BenchmarkChartPanel findChart;

    public BenchmarkWindow(
            BenchmarkService benchmarkService,
            List<POI> pois) {

        this.benchmarkService = benchmarkService;
        this.pois = List.copyOf(pois);

        setTitle("POI Manager - Benchmark");
        setSize(1100, 850);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(12, 12));

        JPanel header = createHeader();
        JPanel center = createCenter();
        JPanel footer = createFooter();

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        runBenchmark();
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        panel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Benchmark priestorových štruktúr");

        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(new Color(35, 40, 55));

        JLabel subtitle = new JLabel(
                "Porovnanie výkonu KDTree, QuadTree a QuadTreeList"
        );

        subtitle.setForeground(new Color(110, 115, 125));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);

        pointsLabel = new JLabel(
                "Testované POI: " + pois.size()
        );

        pointsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        runButton = new JButton("Spustiť benchmark");

        rightPanel.add(pointsLabel);
        rightPanel.add(runButton);

        panel.add(titlePanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenter() {

        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        panel.setBackground(new Color(245, 247, 250));

        String[] columns = {
                "Štruktúra",
                "Počet POI",
                "Vyhľadávania",
                "Vytvorenie (ms)",
                "Vkladanie (ms)",
                "Vyhľadávanie (ms)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(30);
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultsTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Výsledky merania")
        );

        JPanel chartsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        chartsPanel.setBackground(new Color(245, 247, 250));

        creationChart = new BenchmarkChartPanel(
                "Vytvorenie štruktúry",
                "Vytvorenie (ms)"
        );

        insertChart = new BenchmarkChartPanel(
                "Vkladanie bodov",
                "Vkladanie (ms)"
        );

        findChart = new BenchmarkChartPanel(
                "Priestorové vyhľadávanie",
                "Vyhľadávanie (ms)"
        );

        chartsPanel.add(wrapChart(creationChart));
        chartsPanel.add(wrapChart(insertChart));
        chartsPanel.add(wrapChart(findChart));

        panel.add(tableScroll, BorderLayout.NORTH);
        panel.add(chartsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel wrapChart(BenchmarkChartPanel chart) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 235))
        );

        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooter() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));
        panel.setBackground(Color.WHITE);

        statusLabel = new JLabel("Pripravený.");
        statusLabel.setForeground(new Color(100, 105, 115));

        JLabel info = new JLabel(
                "1 zahrievacie kolo | 3 meracie kolá | výsledok = medián"
        );

        info.setForeground(new Color(120, 125, 135));

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(info, BorderLayout.EAST);

        return panel;
    }

    public JButton getRunButton() {
        return runButton;
    }

    public void runBenchmark() {

        runButton.setEnabled(false);
        statusLabel.setText("Benchmark prebieha...");

        tableModel.setRowCount(0);

        SwingWorker<List<BenchmarkResult>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<BenchmarkResult> doInBackground() {
                return benchmarkService.runBenchmark(pois);
            }

            @Override
            protected void done() {

                try {

                    List<BenchmarkResult> results = get();

                    displayResults(results);

                    statusLabel.setText(
                            "Benchmark úspešne dokončený."
                    );

                } catch (Exception exception) {

                    Throwable cause = exception.getCause();

                    String message = cause != null
                            ? cause.getMessage()
                            : exception.getMessage();

                    statusLabel.setText("Benchmark zlyhal.");

                    JOptionPane.showMessageDialog(
                            BenchmarkWindow.this,
                            message,
                            "Chyba benchmarku",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {
                    runButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void displayResults(List<BenchmarkResult> results) {

        tableModel.setRowCount(0);

        for (BenchmarkResult result : results) {

            tableModel.addRow(new Object[]{
                    result.getTreeName(),
                    result.getPointCount(),
                    result.getQueryCount(),
                    String.format("%.4f", result.getCreationMillis()),
                    String.format("%.4f", result.getInsertMillis()),
                    String.format("%.4f", result.getFindMillis())
            });
        }

        creationChart.setResults(results);
        insertChart.setResults(results);
        findChart.setResults(results);
    }
}
