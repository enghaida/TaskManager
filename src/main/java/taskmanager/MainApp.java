package taskmanager;

import javax.swing.SwingUtilities;
import taskmanager.ui.SmartTaskManagerFrame;

/**
 * Entry point for the Smart Task Manager application.
 * Launches the Swing UI on the Event Dispatch Thread.
 */
public class MainApp {

    /**
     * Application entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartTaskManagerFrame::new);
    }
}
