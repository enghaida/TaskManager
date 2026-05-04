package taskmanager.ui;

import taskmanager.impl.TaskManagerImpl;
import taskmanager.model.Task;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Main application window for the Smart Task Manager.
 * Provides a Swing-based UI for adding and deleting tasks, displaying
 * them in a table, and showing weather-based scheduling recommendations
 * in a status bar at the bottom of the window.
 * <p>
 * All UI updates triggered by reactive callbacks are dispatched via
 * {@link SwingUtilities#invokeLater} to ensure Swing thread safety.
 */
public class SmartTaskManagerFrame extends JFrame {
    private TaskManagerImpl manager = new TaskManagerImpl();
    private JTextField titleField = new JTextField(15);
    private JCheckBox outdoorBox = new JCheckBox("Outdoor");
    private DefaultTableModel model = new DefaultTableModel();
    private JTable table = new JTable(model);
    private JLabel statusLabel = new JLabel(" ");

    /**
     * Constructs and displays the Smart Task Manager window.
     * Initializes all UI components, wires up Add and Delete button
     * listeners, and makes the frame visible.
     */
    public SmartTaskManagerFrame() {
        setTitle("Smart Task Manager");
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        top.add(new JLabel("Title:"));
        top.add(titleField);
        top.add(outdoorBox);
        top.add(addBtn);
        top.add(editBtn);
        top.add(deleteBtn);

        add(top, BorderLayout.NORTH);

        model.addColumn("ID");
        model.addColumn("Title");
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            Task task = new Task(
                UUID.randomUUID().toString(),
                titleField.getText(),
                LocalDateTime.now(),
                outdoorBox.isSelected()
            );
            manager.createTask(task)
                .flatMap(manager::getRecommendation)
                .subscribe(rec -> {
                    SwingUtilities.invokeLater(() -> {
                        model.addRow(new Object[]{task.getId(), task.getTitle()});
                        statusLabel.setText(rec.getMessage());
                    });
                });
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                statusLabel.setText("Select a task to edit.");
                return;
            }
            String id = (String) model.getValueAt(row, 0);
            String newTitle = titleField.getText();
            if (newTitle.isEmpty()) {
                statusLabel.setText("Enter a new title in the field.");
                return;
            }
            Task updated = new Task(id, newTitle, LocalDateTime.now(), outdoorBox.isSelected());
            manager.updateTask(updated)
                .subscribe(
                    t -> SwingUtilities.invokeLater(() -> {
                        model.setValueAt(t.getTitle(), row, 1);
                        statusLabel.setText("Updated");
                    }),
                    error -> SwingUtilities.invokeLater(() -> statusLabel.setText(error.getMessage()))
                );
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) model.getValueAt(row, 0);
                manager.deleteTask(id)
                .subscribe(
                    null,
                    error -> SwingUtilities.invokeLater(() -> statusLabel.setText(error.getMessage())),
                    () -> SwingUtilities.invokeLater(() -> {
                        model.removeRow(row);
                        statusLabel.setText("Deleted");
                    })
                );
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
