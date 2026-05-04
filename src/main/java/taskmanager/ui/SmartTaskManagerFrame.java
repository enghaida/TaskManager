package taskmanager.ui;

import taskmanager.api.TaskManager;
import taskmanager.api.TaskService;
import taskmanager.impl.TaskManagerImpl;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;
import taskmanager.service.WeatherService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main application window for the Smart Task Manager.
 *
 * <h2>Architecture highlights</h2>
 * <ul>
 *   <li>All reactive subscriptions run on background (bounded-elastic) threads.</li>
 *   <li>Every Swing component mutation is dispatched via
 *       {@link SwingUtilities#invokeLater} to satisfy the single-threaded
 *       Swing Event Dispatch Thread (EDT) contract.</li>
 *   <li>The {@link TaskManager.ChangeListener} nested interface drives table
 *       updates: the button actions trigger reactive operations; the listener
 *       callbacks (invoked from reactive threads) enqueue EDT updates.</li>
 *   <li>The {@link TaskService.TaskFilter} nested interface is used to power
 *       the search/filter feature without exposing implementation details.</li>
 * </ul>
 *
 * <h2>Layout</h2>
 * <pre>
 *  +--------------------------------------------------+
 *  |  Header banner                                   |
 *  |  Form row (title, description, date/time, etc.)  |
 *  |  Button row (Add / Update / Delete / Recommend)  |
 *  |  Search row (search field + Search / Show All)   |
 *  +-----------------------------+--------------------+
 *  |  Task table (LEFT)          |  Weather panel     |
 *  |                             |  Recommendation    |
 *  +-----------------------------+--------------------+
 *  |  Status bar                                      |
 *  +--------------------------------------------------+
 * </pre>
 */
public class SmartTaskManagerFrame extends JFrame {

    // ------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------
    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Color C_OK      = new Color(0x27AE60);
    private static final Color C_WARN    = new Color(0xE67E22);
    private static final Color C_ERROR   = new Color(0xC0392B);
    private static final Color C_INFO    = new Color(0x2980B9);
    private static final Color C_PANEL   = new Color(0xF0F3F4);
    private static final Color C_HEADER  = new Color(0x2C3E50);

    // ------------------------------------------------------------------
    // Core API
    // ------------------------------------------------------------------
    private final TaskManagerImpl manager      = new TaskManagerImpl();
    private final WeatherService  weatherSvc   = new WeatherService();

    // ------------------------------------------------------------------
    // Form components
    // ------------------------------------------------------------------
    private final JTextField titleField    = new JTextField(16);
    private final JTextField descField     = new JTextField(16);
    private final JTextField dateTimeField = new JTextField(16);
    private final JCheckBox  outdoorBox    = new JCheckBox("Outdoor");
    private final JTextField searchField   = new JTextField(16);

    // ------------------------------------------------------------------
    // Table
    // ------------------------------------------------------------------
    private final TaskTableModel tableModel = new TaskTableModel();
    private final JTable         table      = new JTable(tableModel);

    // ------------------------------------------------------------------
    // Info-panel components
    // ------------------------------------------------------------------
    private final JLabel    cityLabel = new JLabel("—");
    private final JLabel    tempLabel = new JLabel("—");
    private final JLabel    condLabel = new JLabel("—");
    private final JLabel    humLabel  = new JLabel("—");
    private final JTextArea recArea   = new JTextArea(6, 22);

    // ------------------------------------------------------------------
    // Status bar
    // ------------------------------------------------------------------
    private final JLabel  statusLabel = new JLabel("  Ready");
    private final JPanel  statusBar   = new JPanel(new BorderLayout());

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Constructs and displays the Smart Task Manager window.
     *
     * <p>Wires all component listeners, registers the
     * {@link TaskManager.ChangeListener} for reactive table updates, and
     * triggers the initial weather load.
     *
     * <p>Must be called on the Swing EDT (via
     * {@link SwingUtilities#invokeLater}).
     */
    public SmartTaskManagerFrame() {
        super("Smart Task Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 580);
        setMinimumSize(new Dimension(850, 500));
        getContentPane().setBackground(C_PANEL);

        registerChangeListener();

        setLayout(new BorderLayout(0, 0));
        add(buildNorthPanel(), BorderLayout.NORTH);
        add(buildCenterSplit(), BorderLayout.CENTER);
        add(buildStatusBar(),   BorderLayout.SOUTH);

        // Pre-fill date/time field with current time
        dateTimeField.setText(LocalDateTime.now().format(DT_FMT));

        setLocationRelativeTo(null);
        setVisible(true);

        // Initial weather load (runs on background thread)
        loadWeather();
    }

    // ------------------------------------------------------------------
    // ChangeListener registration
    // ------------------------------------------------------------------

    /**
     * Registers a {@link TaskManager.ChangeListener} that bridges reactive
     * events to EDT-safe table mutations.
     *
     * <p>This demonstrates the <em>nested interface</em> pattern: the listener
     * type is defined inside {@link TaskManager}, keeping it close to its
     * consumer without polluting the top-level package.
     */
    private void registerChangeListener() {
        manager.addChangeListener(new TaskManager.ChangeListener() {
            @Override
            public void onTaskCreated(Task task) {
                SwingUtilities.invokeLater(() -> tableModel.addTask(task));
            }

            @Override
            public void onTaskDeleted(String id) {
                SwingUtilities.invokeLater(() -> tableModel.removeTask(id));
            }

            @Override
            public void onTaskUpdated(Task task) {
                SwingUtilities.invokeLater(() -> tableModel.updateTask(task));
            }
        });
    }

    // ------------------------------------------------------------------
    // Layout builders
    // ------------------------------------------------------------------

    /** Builds the header banner + form + button + search rows. */
    private JPanel buildNorthPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());

        // ---- Header banner ----
        JLabel header = new JLabel("  Smart Task Manager", JLabel.LEFT);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(C_HEADER);
        header.setForeground(Color.WHITE);
        header.setBorder(new EmptyBorder(10, 14, 10, 14));
        wrapper.add(header, BorderLayout.NORTH);

        // ---- Form row ----
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.setBackground(C_PANEL);
        form.add(label("Title:"));      form.add(titleField);
        form.add(label("Description:")); form.add(descField);
        form.add(label("Date/Time (yyyy-MM-dd HH:mm):")); form.add(dateTimeField);
        form.add(outdoorBox);

        // ---- Button row ----
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setBackground(C_PANEL);
        JButton addBtn  = styledBtn("Add Task",   C_OK);
        JButton editBtn = styledBtn("Update",     C_INFO);
        JButton delBtn  = styledBtn("Delete",     C_ERROR);
        JButton recBtn  = styledBtn("Recommend",  C_WARN);
        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(delBtn);
        buttons.add(recBtn);

        addBtn .addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        delBtn .addActionListener(e -> onDelete());
        recBtn .addActionListener(e -> onRecommend());

        // ---- Search row ----
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchRow.setBackground(C_PANEL);
        JButton searchBtn  = styledBtn("Search",   C_INFO);
        JButton showAllBtn = styledBtn("Show All", C_INFO);
        searchRow.add(label("Filter by title:"));
        searchRow.add(searchField);
        searchRow.add(searchBtn);
        searchRow.add(showAllBtn);

        searchBtn .addActionListener(e -> onSearch());
        showAllBtn.addActionListener(e -> onShowAll());

        JPanel formArea = new JPanel(new BorderLayout());
        formArea.setBackground(C_PANEL);
        formArea.add(form,      BorderLayout.NORTH);
        formArea.add(buttons,   BorderLayout.CENTER);
        formArea.add(searchRow, BorderLayout.SOUTH);
        wrapper.add(formArea, BorderLayout.CENTER);

        return wrapper;
    }

    /** Builds the horizontal split between the task table and the info panel. */
    private JSplitPane buildCenterSplit() {
        // Table setup
        table.setRowHeight(26);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(0xDFE6E9));
        table.setShowVerticalLines(false);

        // Alternate row colouring
        table.setDefaultRenderer(Object.class, new StripedRenderer());

        // Populate form when a row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromSelection();
        });

        // Column widths
        int[] widths = {160, 130, 130, 70, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new TitledBorder("Tasks"));

        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, tableScroll, buildInfoPanel());
        split.setDividerLocation(570);
        split.setResizeWeight(0.65);
        return split;
    }

    /** Builds the right-hand weather + recommendation panel. */
    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(C_PANEL);
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        // Weather section
        JPanel weatherGrid = new JPanel(new GridLayout(0, 2, 6, 4));
        weatherGrid.setBorder(new TitledBorder("Current Weather — Makkah"));
        weatherGrid.setBackground(C_PANEL);

        weatherGrid.add(label("City:"));        weatherGrid.add(cityLabel);
        weatherGrid.add(label("Temperature:")); weatherGrid.add(tempLabel);
        weatherGrid.add(label("Condition:"));   weatherGrid.add(condLabel);
        weatherGrid.add(label("Humidity:"));    weatherGrid.add(humLabel);

        JButton refreshBtn = styledBtn("Refresh Weather", C_INFO);
        refreshBtn.addActionListener(e -> loadWeather());
        weatherGrid.add(new JLabel());
        weatherGrid.add(refreshBtn);

        // Recommendation section
        recArea.setEditable(false);
        recArea.setLineWrap(true);
        recArea.setWrapStyleWord(true);
        recArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        recArea.setBackground(new Color(0xFFFDE7));
        recArea.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane recScroll = new JScrollPane(recArea);
        recScroll.setBorder(new TitledBorder("Scheduling Recommendation"));

        panel.add(weatherGrid, BorderLayout.NORTH);
        panel.add(recScroll,   BorderLayout.CENTER);
        return panel;
    }

    /** Builds the status bar at the bottom of the window. */
    private JPanel buildStatusBar() {
        statusBar.setBackground(new Color(0xDFE6E9));
        statusBar.setBorder(new EmptyBorder(3, 10, 3, 10));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.add(statusLabel, BorderLayout.WEST);
        return statusBar;
    }

    // ------------------------------------------------------------------
    // Button action handlers
    // ------------------------------------------------------------------

    /**
     * Validates the form, creates a new {@link Task}, calls
     * {@link taskmanager.api.TaskManager#createTask}, then fetches a
     * recommendation — all via the reactive API.
     *
     * <p>The table update is driven by the {@link TaskManager.ChangeListener}
     * registered in {@link #registerChangeListener()}.
     */
    private void onAdd() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showStatus("Title is required.", C_ERROR);
            return;
        }
        LocalDateTime dt = parseDateTimeField();
        if (dt == null) return;

        Task task = new Task(
            UUID.randomUUID().toString(),
            title,
            descField.getText().trim(),
            dt,
            outdoorBox.isSelected());

        manager.createTask(task)
            .flatMap(manager::getRecommendation)
            .subscribe(
                rec -> SwingUtilities.invokeLater(() -> {
                    showRecommendation(rec);
                    showStatus("Task added: " + title, C_OK);
                }),
                err -> SwingUtilities.invokeLater(() ->
                    showStatus("Error: " + err.getMessage(), C_ERROR))
            );
    }

    /**
     * Updates the task for the selected table row using data from the form.
     */
    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) { showStatus("Select a task to update.", C_WARN); return; }

        String id    = tableModel.getTaskAt(row).getId();
        String title = titleField.getText().trim();
        if (title.isEmpty()) { showStatus("Title is required.", C_ERROR); return; }

        LocalDateTime dt = parseDateTimeField();
        if (dt == null) return;

        Task updated = new Task(
            id, title, descField.getText().trim(), dt, outdoorBox.isSelected());

        manager.updateTask(updated)
            .flatMap(manager::getRecommendation)
            .subscribe(
                rec -> SwingUtilities.invokeLater(() -> {
                    showRecommendation(rec);
                    showStatus("Task updated: " + title, C_OK);
                }),
                err -> SwingUtilities.invokeLater(() ->
                    showStatus("Error: " + err.getMessage(), C_ERROR))
            );
    }

    /** Deletes the selected task after user confirmation. */
    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { showStatus("Select a task to delete.", C_WARN); return; }

        Task selected = tableModel.getTaskAt(row);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete task \"" + selected.getTitle() + "\"?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        manager.deleteTask(selected.getId())
            .subscribe(
                null,
                err -> SwingUtilities.invokeLater(() ->
                    showStatus("Error: " + err.getMessage(), C_ERROR)),
                () -> SwingUtilities.invokeLater(() -> {
                    recArea.setText("");
                    showStatus("Deleted: " + selected.getTitle(), C_OK);
                })
            );
    }

    /** Fetches a scheduling recommendation for the selected task. */
    private void onRecommend() {
        int row = table.getSelectedRow();
        if (row == -1) { showStatus("Select a task first.", C_WARN); return; }

        Task task = tableModel.getTaskAt(row);
        showStatus("Fetching recommendation...", C_INFO);

        manager.getRecommendation(task)
            .subscribe(
                rec -> SwingUtilities.invokeLater(() -> {
                    showRecommendation(rec);
                    showStatus("Recommendation ready.", C_OK);
                }),
                err -> SwingUtilities.invokeLater(() ->
                    showStatus("Error: " + err.getMessage(), C_ERROR))
            );
    }

    /**
     * Filters the table using the {@link TaskService.TaskFilter} nested interface.
     *
     * <p>This demonstrates the nested interface in action: the lambda
     * {@code t -> t.getTitle().toLowerCase().contains(query)} is a valid
     * {@link TaskService.TaskFilter} implementation.
     */
    private void onSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) { onShowAll(); return; }

        // Use the nested TaskService.TaskFilter functional interface
        TaskService.TaskFilter filter = t ->
            t.getTitle().toLowerCase().contains(query)
            || t.getDescription().toLowerCase().contains(query);

        manager.getFilteredTasks(filter)
            .collectList()
            .subscribe(
                list -> SwingUtilities.invokeLater(() -> {
                    tableModel.setTasks(list);
                    showStatus("Showing " + list.size() + " matching task(s).", C_INFO);
                }),
                err -> SwingUtilities.invokeLater(() ->
                    showStatus("Search error: " + err.getMessage(), C_ERROR))
            );
    }

    /** Reloads all tasks from the manager (clears any active filter). */
    private void onShowAll() {
        manager.getTasks()
            .collectList()
            .subscribe(
                list -> SwingUtilities.invokeLater(() -> {
                    tableModel.setTasks(list);
                    showStatus("Showing all " + list.size() + " task(s).", C_INFO);
                }),
                err -> SwingUtilities.invokeLater(() ->
                    showStatus("Error: " + err.getMessage(), C_ERROR))
            );
    }

    // ------------------------------------------------------------------
    // Weather loading
    // ------------------------------------------------------------------

    /**
     * Fetches the current weather on a background thread and updates the
     * weather panel on the EDT.
     */
    private void loadWeather() {
        showStatus("Loading weather data...", C_INFO);
        weatherSvc.getWeather()
            .subscribe(
                w  -> SwingUtilities.invokeLater(() -> displayWeather(w)),
                err -> SwingUtilities.invokeLater(() -> {
                    cityLabel.setText("Unavailable");
                    showStatus("Weather error: " + err.getMessage(), C_ERROR);
                })
            );
    }

    private void displayWeather(WeatherForecast w) {
        cityLabel.setText(w.getCityName());
        tempLabel.setText(String.format("%.1f °C", w.getTemperature()));
        condLabel.setText(w.getCondition());
        humLabel .setText(w.getHumidity() + " %");
        showStatus("Weather updated for " + w.getCityName() + ".", C_OK);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Shows a recommendation in the panel and marks the table row's advice column. */
    private void showRecommendation(ScheduleRecommendation rec) {
        recArea.setText(rec.getMessage());
        recArea.setBackground(rec.isWarning()
            ? new Color(0xFFEBEE) : new Color(0xE8F5E9));
        tableModel.setAdvice(rec.getTask().getId(),
            rec.isWarning() ? "WARNING" : "OK");
    }

    /** Updates the status bar text and background colour. */
    private void showStatus(String msg, Color color) {
        statusLabel.setText("  " + msg);
        statusBar.setBackground(lighter(color));
        statusBar.repaint();
    }

    /** Populates the form fields from the currently selected table row. */
    private void populateFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= tableModel.getRowCount()) return;
        Task t = tableModel.getTaskAt(row);
        titleField   .setText(t.getTitle());
        descField    .setText(t.getDescription());
        dateTimeField.setText(t.getDateTime().format(DT_FMT));
        outdoorBox   .setSelected(t.isOutdoor());
    }

    /**
     * Parses the date/time text field.
     *
     * @return the parsed {@link LocalDateTime}, or {@code null} if the input
     *         is invalid (a status message is shown in that case)
     */
    private LocalDateTime parseDateTimeField() {
        String text = dateTimeField.getText().trim();
        try {
            return LocalDateTime.parse(text, DT_FMT);
        } catch (DateTimeParseException e) {
            showStatus("Invalid date/time. Expected format: yyyy-MM-dd HH:mm", C_ERROR);
            return null;
        }
    }

    // ------------------------------------------------------------------
    // UI factory helpers
    // ------------------------------------------------------------------

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private static JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));
        return btn;
    }

    /** Returns a lighter version of the given colour (for status bar backgrounds). */
    private static Color lighter(Color c) {
        return new Color(
            Math.min(255, c.getRed()   + 100),
            Math.min(255, c.getGreen() + 100),
            Math.min(255, c.getBlue()  + 100));
    }

    // ------------------------------------------------------------------
    // Inner class: TaskTableModel
    // ------------------------------------------------------------------

    /**
     * Custom {@link AbstractTableModel} backed by a {@link List} of {@link Task}
     * objects.
     *
     * <p>An "Advice" column is updated separately via {@link #setAdvice} once a
     * recommendation has been fetched for a task, without forcing a full table
     * refresh.
     *
     * <p>This inner class demonstrates the Java record API:
     * {@link Task} (a record) is stored directly and its components are accessed
     * via the record accessor aliases ({@code getTitle()}, etc.).
     */
    private static final class TaskTableModel extends AbstractTableModel {

        private static final String[] COLS =
            {"Title", "Description", "Date / Time", "Type", "Advice"};
        private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        private final List<Task>         tasks  = new ArrayList<>();
        private final Map<String,String> advice = new HashMap<>();

        // ---- Mutation methods (called on EDT) ----

        void addTask(Task task) {
            tasks.add(task);
            int row = tasks.size() - 1;
            fireTableRowsInserted(row, row);
        }

        void removeTask(String id) {
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId().equals(id)) {
                    tasks.remove(i);
                    advice.remove(id);
                    fireTableRowsDeleted(i, i);
                    return;
                }
            }
        }

        void updateTask(Task updated) {
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId().equals(updated.getId())) {
                    tasks.set(i, updated);
                    fireTableRowsUpdated(i, i);
                    return;
                }
            }
        }

        /** Replaces the entire table contents (used by search/filter). */
        void setTasks(List<Task> newTasks) {
            tasks.clear();
            advice.clear();
            tasks.addAll(newTasks);
            fireTableDataChanged();
        }

        /**
         * Sets the advice text for the given task ID and triggers a cell repaint.
         *
         * @param taskId the task whose advice column should be updated
         * @param text   the advice string to display (e.g., "OK" or "WARNING")
         */
        void setAdvice(String taskId, String text) {
            advice.put(taskId, text);
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId().equals(taskId)) {
                    fireTableCellUpdated(i, 4);
                    return;
                }
            }
        }

        Task getTaskAt(int row) { return tasks.get(row); }

        // ---- AbstractTableModel contract ----

        @Override public int    getRowCount()              { return tasks.size(); }
        @Override public int    getColumnCount()           { return COLS.length; }
        @Override public String getColumnName(int col)     { return COLS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Task t = tasks.get(row);
            return switch (col) {
                case 0 -> t.getTitle();
                case 1 -> t.getDescription();
                case 2 -> t.getDateTime().format(FMT);
                case 3 -> t.isOutdoor() ? "Outdoor" : "Indoor";
                case 4 -> advice.getOrDefault(t.getId(), "—");
                default -> "";
            };
        }
    }

    // ------------------------------------------------------------------
    // Inner class: StripedRenderer
    // ------------------------------------------------------------------

    /**
     * Alternates row background colours for readability and highlights
     * warning rows in light red.
     */
    private static final class StripedRenderer extends DefaultTableCellRenderer {

        private static final Color ROW_A = Color.WHITE;
        private static final Color ROW_B = new Color(0xEBF5FB);
        private static final Color WARN  = new Color(0xFFEBEE);

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean selected,
                boolean focused, int row, int col) {

            super.getTableCellRendererComponent(t, value, selected, focused, row, col);
            if (!selected) {
                String advice = (String) t.getModel().getValueAt(row, 4);
                if ("WARNING".equals(advice)) {
                    setBackground(WARN);
                } else {
                    setBackground(row % 2 == 0 ? ROW_A : ROW_B);
                }
            }
            return this;
        }
    }
}
