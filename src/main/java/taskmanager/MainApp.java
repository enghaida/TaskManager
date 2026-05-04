package taskmanager;

import taskmanager.ui.swing.SmartTaskManagerFrame;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartTaskManagerFrame::new);
    }
}