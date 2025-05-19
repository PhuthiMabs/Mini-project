import Storage.TreeGraph;
import Visualisation.GuiHandler;

/**
 * Entry point of application
 */
public class Main {
	/**
	 * main method
	 * @param args - command line arguments 
	 */
    public static void main(String[] args) {
        // Initialize with some sample data
        TreeGraph productGraph = new TreeGraph(new java.util.HashMap<>());
        
        // Launch the JavaFX application
        GuiHandler.launchApp();
    }
}