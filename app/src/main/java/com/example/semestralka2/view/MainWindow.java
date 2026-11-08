
package com.example.semestralka2.view;

import com.example.semestralka2.model.POI;
import com.example.semestralka2.service.TreeType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainWindow extends JFrame {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private DefaultTableModel tableModel;
    private JTable table;

    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton loadButton;
    private JButton saveButton;

    private JLabel statusLabel;

    private JComboBox<TreeType> structureSelector;

    private MapPanel mapPanel;

    public MainWindow() {

        setTitle("POI Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1400, 850);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(new Color(30, 45, 65));
        header.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        JLabel title = new JLabel("POI Manager");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        titlePanel.add(title);

        structureSelector = new JComboBox<>(TreeType.values());
        structureSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof TreeType type) {
                    setText(switch (type) {
                        case KD_TREE -> "KDTree";
                        case QUAD_TREE -> "QuadTree";
                        case QUAD_TREE_LIST -> "QuadTreeList";
                    });
                }

                return this;
            }
        });

        JLabel structureLabel = new JLabel("Štruktúra:");
        structureLabel.setForeground(Color.WHITE);

        loadButton = new JButton("Načítať CSV");
        saveButton = new JButton("Uložiť");

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setOpaque(false);

        controls.add(structureLabel);
        controls.add(structureSelector);
        controls.add(loadButton);
        controls.add(saveButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(35, 49, 69));

        sidebar.setBorder(
                BorderFactory.createEmptyBorder(20, 12, 20, 12)
        );

        sidebar.setPreferredSize(new Dimension(210, 0));

        addButton = createSidebarButton("Pridať POI");
        editButton = createSidebarButton("Upraviť POI");
        deleteButton = createSidebarButton("Vymazať POI");
        refreshButton = createSidebarButton("Obnoviť zoznam");

        sidebar.add(addButton);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(editButton);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(deleteButton);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(refreshButton);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {

        JButton button = new JButton(text);

        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(190, 42));
        button.setPreferredSize(new Dimension(190, 42));

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JPanel createMainContent() {

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(new Color(245, 247, 250));

        content.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        String[] columns = {
                "ID",
                "Názov",
                "Popis",
                "Latitude",
                "Longitude",
                "Adresa",
                "Created at",
                "Updated at"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(230);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);

        JScrollPane tableScroll = new JScrollPane(table);

        tableScroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Evidované body záujmu"
                )
        );

        mapPanel = new MapPanel();

        mapPanel.setBorder(
                BorderFactory.createTitledBorder("Mapa POI")
        );

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                tableScroll,
                mapPanel
        );

        splitPane.setResizeWeight(0.55);
        splitPane.setDividerLocation(650);

        content.add(splitPane, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatusBar() {

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT));
        status.setBackground(new Color(235, 239, 245));

        statusLabel = new JLabel("Počet bodov: 0 | Pripravený");
        status.add(statusLabel);

        return status;
    }

    public void displayPOIs(List<POI> pois) {

        tableModel.setRowCount(0);

        for (POI poi : pois) {

            tableModel.addRow(new Object[]{
                    poi.getId(),
                    poi.getName(),
                    poi.getDescription(),
                    poi.getLatitude(),
                    poi.getLongitude(),
                    poi.getAddress(),
                    poi.getCreatedAt().format(DATE_FORMAT),
                    poi.getUpdatedAt().format(DATE_FORMAT)
            });
        }

        mapPanel.displayPOIs(pois);

        statusLabel.setText(
                "Počet bodov: " + pois.size()
                        + " | Zoznam aktualizovaný"
        );
    }

    public JComboBox<TreeType> getStructureSelector() {
        return structureSelector;
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    public String getSelectedPOIId() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        return tableModel.getValueAt(modelRow, 0).toString();
    }

    public JButton getAddButton() {
        return addButton;
    }

    public JButton getEditButton() {
        return editButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getLoadButton() {
        return loadButton;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JTable getTable() {
        return table;
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Chyba",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
