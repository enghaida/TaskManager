package taskmanager.ui.swing;

import taskmanager.api.TaskManager;
import taskmanager.impl.TaskManagerImpl;
import taskmanager.model.Task;
import taskmanager.service.WeatherService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class SmartTaskManagerFrame extends JFrame {

    private final TaskManager manager = new TaskManagerImpl();
    private final WeatherService weatherService = new WeatherService();

    private final JTextField titleField = new JTextField(15);
    private final JCheckBox outdoorBox = new JCheckBox("Outdoor");

    private final DefaultTableModel model = new DefaultTableModel();
    private final JTable table = new JTable(model);

    private final JLabel statusLabel = new JLabel(" ");

    private final JLabel cityLabel = new JLabel("City: —");
    private final JLabel tempLabel = new JLabel("Temperature: —");
    private final JLabel conditionLabel = new JLabel("Condition: —");
    private final JLabel humidityLabel = new JLabel("Humidity: —");

    public SmartTaskManagerFrame() {
        setTitle("Smart Task Manager");
        setSize(750, 400);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton weatherBtn = new JButton("Refresh Weather");

        top.add(new JLabel("Title:"));
        top.add(titleField);
        top.add(outdoorBox);
        top.add(addBtn);
        top.add(editBtn);
        top.add(deleteBtn);
        top.add(weatherBtn);

        add(top, BorderLayout.NORTH);

        model.addColumn("ID");
        model.addColumn("Title");

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel weatherPanel = new JPanel(new GridLayout(4, 1));
        weatherPanel.setBorder(BorderFactory.createTitledBorder("Current Weather"));
        weatherPanel.add(cityLabel);
        weatherPanel.add(tempLabel);
        weatherPanel.add(conditionLabel);
        weatherPanel.add(humidityLabel);

        add(weatherPanel, BorderLayout.EAST);
        add(statusLabel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addTask());
        editBtn.addActionListener(e -> editTask());
        deleteBtn.addActionListener(e -> deleteTask());
        weatherBtn.addActionListener(e -> loadWeather());

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                titleField.setText((String) model.getValueAt(row, 1));
            }
        });

        loadWeather();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void addTask() {
        Task task = new Task(
                UUID.randomUUID().toString(),
                titleField.getText(),
                LocalDateTime.now(),
                outdoorBox.isSelected()
        );

        manager.createTask(task)
                .flatMap(manager::getRecommendation)
                .subscribe(rec -> SwingUtilities.invokeLater(() -> {
                    model.addRow(new Object[]{task.getId(), task.getTitle()});
                    statusLabel.setText(rec.getMessage());
                    titleField.setText("");
                    outdoorBox.setSelected(false);
                }), error -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(error.getMessage());
                }));
    }

    private void editTask() {
        int row = table.getSelectedRow();

        if (row == -1) {
            statusLabel.setText("Select a task to edit");
            return;
        }

        String id = (String) model.getValueAt(row, 0);

        Task updatedTask = new Task(
                id,
                titleField.getText(),
                LocalDateTime.now(),
                outdoorBox.isSelected()
        );

        manager.updateTask(updatedTask)
                .flatMap(manager::getRecommendation)
                .subscribe(rec -> SwingUtilities.invokeLater(() -> {
                    model.setValueAt(updatedTask.getTitle(), row, 1);
                    statusLabel.setText("Updated: " + rec.getMessage());
                }), error -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(error.getMessage());
                }));
    }

    private void deleteTask() {
        int row = table.getSelectedRow();

        if (row == -1) {
            statusLabel.setText("Select a task first");
            return;
        }

        String id = (String) model.getValueAt(row, 0);

        manager.deleteTask(id)
                .subscribe(unused -> {
                }, error -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(error.getMessage());
                }), () -> SwingUtilities.invokeLater(() -> {
                    model.removeRow(row);
                    statusLabel.setText("Deleted");
                }));
    }

    private void loadWeather() {
        weatherService.getWeather()
                .subscribe(weather -> SwingUtilities.invokeLater(() -> {
                    cityLabel.setText("City: " + weather.getCityName());
                    tempLabel.setText("Temperature: " + weather.getTemperature() + " °C");
                    conditionLabel.setText("Condition: " + weather.getCondition());
                    humidityLabel.setText("Humidity: " + weather.getHumidity() + "%");
                }), error -> SwingUtilities.invokeLater(() -> {
                    cityLabel.setText("City: Unavailable");
                    tempLabel.setText("Temperature: —");
                    conditionLabel.setText("Condition: —");
                    humidityLabel.setText("Humidity: —");
                    statusLabel.setText("Weather data unavailable.");
                }));
    }
}
