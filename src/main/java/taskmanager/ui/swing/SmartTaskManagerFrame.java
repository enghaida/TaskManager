package taskmanager.ui;

import taskmanager.api.TaskManager;
import taskmanager.impl.TaskManagerImpl;
import taskmanager.model.Task;
import reactor.core.scheduler.Schedulers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class SmartTaskManagerFrame extends JFrame {

    private final TaskManager taskManager;
    private final DefaultTableModel tableModel;
    private final JTable taskTable;

    public SmartTaskManagerFrame() {
        this.taskManager = new TaskManagerImpl();

        setTitle("Manager (Swing)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);

        String[] columns = {"ID", "Title", "Due Time", "Weather Sensitive", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        taskTable = new JTable(tableModel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editBtn = new JButton("Edit Task");
        JButton deleteBtn = new JButton("Delete Task");
        JButton updateWeatherBtn = new JButton("Update Weather");
        JButton suggestBtn = new JButton("Suggest Schedule");
        JButton addBtn = new JButton("Add Task");

        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(updateWeatherBtn);
        buttonPanel.add(suggestBtn);
        buttonPanel.add(addBtn);

        setLayout(new BorderLayout());
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddTaskDialog());
        deleteBtn.addActionListener(e -> deleteSelectedTask());
        updateWeatherBtn.addActionListener(e -> updateRecommendation());
        suggestBtn.addActionListener(e -> updateRecommendation());

        setupTaskListener();
        refreshTasks();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupTaskListener() {
        taskManager.addChangeListener(new TaskManager.ChangeListener() {
            @Override
            public void onTaskCreated(Task t) { SwingUtilities.invokeLater(() -> refreshTasks()); }
            @Override
            public void onTaskDeleted(String id) { SwingUtilities.invokeLater(() -> refreshTasks()); }
            @Override
            public void onTaskUpdated(Task t) { SwingUtilities.invokeLater(() -> refreshTasks()); }
        });
    }

    private void showAddTaskDialog() {
        JDialog dialog = new JDialog(this, "Add Task", true);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setSize(450, 300);

        JTextField idField = new JTextField(UUID.randomUUID().toString().substring(0, 8));
        idField.setEditable(false);
        JTextField titleField = new JTextField();
        JTextField dueField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        JCheckBox weatherBox = new JCheckBox("Weather Sensitive");

        dialog.add(new JLabel(" ID:")); dialog.add(idField);
        dialog.add(new JLabel(" Title:")); dialog.add(titleField);
        dialog.add(new JLabel(" Due (yyyy-MM-dd HH:mm):")); dialog.add(dueField);
        dialog.add(new JLabel("")); dialog.add(weatherBox);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        dialog.add(okBtn);
        dialog.add(cancelBtn);

        okBtn.addActionListener(e -> {
            try {
                LocalDateTime ldt = LocalDateTime.parse(dueField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Task newTask = new Task(idField.getText(), titleField.getText(), ldt, weatherBox.isSelected());
                taskManager.createTask(newTask).subscribeOn(Schedulers.boundedElastic()).subscribe();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Incorrect Date format! Use: yyyy-MM-dd HH:mm");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshTasks() {
        taskManager.getTasks()
                .subscribeOn(Schedulers.boundedElastic())
                .collectList()
                .subscribe(tasks -> SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (Task t : tasks) {
                        tableModel.addRow(new Object[]{t.id(), t.title(), t.dateTime(), t.outdoor(), "N/A"});
                    }
                }));
    }

    private void deleteSelectedTask() {
        int row = taskTable.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            taskManager.deleteTask(id).subscribeOn(Schedulers.boundedElastic()).subscribe();
        }
    }

    private void updateRecommendation() {
        int row = taskTable.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            taskManager.getTasks()
                    .filter(t -> t.id().equals(id))
                    .next()
                    .flatMap(taskManager::getRecommendation)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(rec -> SwingUtilities.invokeLater(() -> {
                        tableModel.setValueAt(rec.message(), row, 4);
                    }));
        }
    }
}
