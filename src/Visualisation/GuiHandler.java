package Visualisation;

import Storage.DataManager;
import Storage.Date;
import Storage.Features;
import Storage.FreshnessLvl;
import Storage.Product;
import Storage.Shelf;
import Storage.TreeGraph;
import DataCalculations.FreshCalculator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * The main GUI controller for the supermarket inventory visualization system.
 * Handles user interactions, image loading, and coordinates between visualization components.
 */
public class GuiHandler extends Application {
    private TreeGraph productGraph;
    private GraphVisualizer graphVisualizer;
    private Shelf currentShelf;
    private Product selectedProduct;
    
    private ImageView shelfImageView;
    private Label statusLabel;
    private Rectangle selectionRect;
    private double startX, startY;
    
    private Menu fileMenu;
	private Stage mainStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.productGraph = new TreeGraph(new HashMap<>());
        this.graphVisualizer = new GraphVisualizer(productGraph);
        this.mainStage = primaryStage;
        primaryStage.setTitle("Supermarket Inventory Visualizer");
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        //Menu Bar
        MenuBar menuBar = new MenuBar();
        fileMenu = new Menu("File");
        MenuItem loadImageItem = new MenuItem("Load Shelf Image");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(loadImageItem, new SeparatorMenuItem(), exitItem);
        
        Menu viewMenu = new Menu("View");
        MenuItem showGraphItem = new MenuItem("Show Product Graph");
        viewMenu.getItems().add(showGraphItem);
        
        menuBar.getMenus().addAll(fileMenu, viewMenu);
        root.setTop(menuBar);
        
        // Image View
        shelfImageView = new ImageView();
        shelfImageView.setPreserveRatio(true);
        shelfImageView.setSmooth(true);
        shelfImageView.setCache(true);
        
        ScrollPane imageScrollPane = new ScrollPane(shelfImageView);
        imageScrollPane.setFitToWidth(true);
        imageScrollPane.setFitToHeight(true);
        root.setCenter(imageScrollPane);
        
        // Status Bar
        statusLabel = new Label("Ready");
        statusLabel.setPadding(new Insets(5));
        root.setBottom(statusLabel);
        
        // Event Handlers
        loadImageItem.setOnAction(e -> loadShelfImage(primaryStage));
        
        showGraphItem.setOnAction(e -> {
            if (productGraph.getAllProducts().isEmpty()) {
                showAlert("No Products", "Please load data first", Alert.AlertType.WARNING);
            } else {
                graphVisualizer.displayGraph();
            }
        });
        exitItem.setOnAction(e -> Platform.exit());
        
        // Image Click Handler
        shelfImageView.setOnMouseClicked(e -> {
            if (currentShelf != null && !currentShelf.getProductList().isEmpty()) {
                int randomIndex = (int)(Math.random() * currentShelf.getProductList().size());
                selectedProduct = currentShelf.getProductList().get(randomIndex);
                statusLabel.setText("Selected: " + selectedProduct.getName());
                
                List<Product> similarProducts = productGraph.findSimilarProducts(selectedProduct, 3);
                ProductDetailsDialog.show(selectedProduct, similarProducts);
            }
        });
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        setupDataPersistence();
        setupImageCropping();
    }
    /**
     * functionality for adding shelf image 
     * @param primaryStage - the stage used for showing all layout elements
     */
    private void loadShelfImage(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Shelf Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                Image shelfImage = new Image(selectedFile.toURI().toString());
                shelfImageView.setImage(shelfImage);
                
                currentShelf = new Shelf("Shelf-" + selectedFile.getName(), shelfImage);
                statusLabel.setText("Loaded: " + selectedFile.getName());
                
                addProductsToGraph(currentShelf.getProductList());
                
            } catch (Exception ex) {
                statusLabel.setText("Error loading image: " + ex.getMessage());
                showAlert("Error", "Error loading image", Alert.AlertType.ERROR);
            }
        }
    }
    
    
    /**
     * helper method for setting up image cropping
     */
    private void setupImageCropping() {
        shelfImageView.setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();
            selectionRect = new Rectangle(startX, startY, 0, 0);
            selectionRect.setStroke(Color.BLUE);
            selectionRect.setStrokeWidth(2);
            selectionRect.setFill(Color.color(0, 0, 1, 0.1));
            Pane imagePane = (Pane) shelfImageView.getParent();
            imagePane.getChildren().add(selectionRect);
        });

        shelfImageView.setOnMouseDragged(e -> {
            if (selectionRect != null) {
                double width = e.getX() - startX;
                double height = e.getY() - startY;
                selectionRect.setWidth(Math.abs(width));
                selectionRect.setHeight(Math.abs(height));
                selectionRect.setX(width < 0 ? e.getX() : startX);
                selectionRect.setY(height < 0 ? e.getY() : startY);
            }
        });

        shelfImageView.setOnMouseReleased(e -> {
            if (selectionRect != null) {
                // Get cropped image
                Image cropped = ImageProcessor.cropImage(
                    shelfImageView.getImage(),
                    (int) selectionRect.getX(),
                    (int) selectionRect.getY(),
                    (int) selectionRect.getWidth(),
                    (int) selectionRect.getHeight()
                );
                
                // Create new product from cropped region
                createProductFromCrop(cropped);
                
                // Remove selection rectangle
                Pane imagePane = (Pane) shelfImageView.getParent();
                imagePane.getChildren().remove(selectionRect);
            }
        });
    }
    
    /**
     * helper method fr updating product fresshnes
     */
    private void updateProductFreshness() {
        if (currentShelf == null) return;
        
        LocalDate today = LocalDate.now();
        for (Product product : currentShelf.getProductList()) {
            Date expiry = product.getExpiryDate();
            LocalDate expiryDate = LocalDate.of(
                expiry.getYear(), 
                expiry.getMonth(), 
                expiry.getDay()
            );
            
            FreshnessLvl freshness = FreshCalculator.calculate(today, expiryDate);
            product.setFresh(freshness);
        }
        
        if (graphVisualizer != null) {
            graphVisualizer.displayGraph();
        }
    }
    
    /**
     * method for creating product from crop
     * @param cropped - the cropped image
     */

    private void createProductFromCrop(Image cropped) {
        // Show dialog to enter product details
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Product");
        dialog.setHeaderText("Enter product details");
        dialog.setContentText("Product Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            DatePicker expiryPicker = new DatePicker();
            expiryPicker.setPromptText("Expiry Date");
            
            GridPane grid = new GridPane();
            grid.add(new Label("Expiry Date:"), 0, 0);
            grid.add(expiryPicker, 1, 0);
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Product");
            alert.setHeaderText("Set expiry date for " + name);
            alert.getDialogPane().setContent(grid);
            
            Optional<ButtonType> expiryResult = alert.showAndWait();
            expiryResult.ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Features features = ImageProcessor.extractFeatures(cropped);
                    Date expiryDate = new Date(
                        expiryPicker.getValue().getYear(),
                        expiryPicker.getValue().getMonthValue(),
                        expiryPicker.getValue().getDayOfMonth()
                    );
                    
                    Product newProduct = new Product(
                        name, 
                        features, 
                        currentShelf, 
                        cropped, 
                        expiryDate, 
                        FreshnessLvl.FRESH
                    );
                    
                    currentShelf.addProductList(newProduct);
                    productGraph.addProduct(newProduct);
                }
            });
        });
    }
    
    
    /**
     * method for setting up data persistence
     */
    private void setupDataPersistence() {
        // Add to menu
        MenuItem saveItem = new MenuItem("Save Data");
        MenuItem loadItem = new MenuItem("Load Data");
        fileMenu.getItems().addAll(new SeparatorMenuItem(), saveItem, loadItem);
        
        saveItem.setOnAction(e -> saveData());
        loadItem.setOnAction(e -> loadData());
    }

    private void saveData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Shelf Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        File file = fileChooser.showSaveDialog(mainStage);
        
        if (file != null) {
            try {
                DataManager.saveShelf(currentShelf, file.getAbsolutePath());
                statusLabel.setText("Data saved to " + file.getName());
            } catch (Exception ex) {
                showAlert("Save Error", "Failed to save data: " + ex.getMessage(), 
                         Alert.AlertType.ERROR);
            }
        }
    }

    private void loadData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Shelf Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        File file = fileChooser.showOpenDialog(mainStage);
        
        if (file != null) {
            try {
                currentShelf = DataManager.loadShelf(file.getAbsolutePath());
                shelfImageView.setImage(currentShelf.getShelfImage());
                addProductsToGraph(currentShelf.getProductList());
                statusLabel.setText("Data loaded from " + file.getName());
            } catch (Exception ex) {
                showAlert("Load Error", "Failed to load data: " + ex.getMessage(), 
                         Alert.AlertType.ERROR);
            }
        }
    }

    // Update DataManager.java
    public static void saveShelf(Shelf shelf, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(filePath))) {
            oos.writeObject(shelf);
        }
    }

    public static Shelf loadShelf(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(filePath))) {
            return (Shelf) ois.readObject();
        }
    }
    
    private void addProductsToGraph(List<Product> products) {
        productGraph = new TreeGraph(new HashMap<>());
        graphVisualizer = new GraphVisualizer(productGraph);
        
        for (Product product : products) {
            productGraph.addProduct(product);
        }
        
        for (int i = 0; i < products.size(); i++) {
            for (int j = i + 1; j < products.size(); j++) {
                Product p1 = products.get(i);
                Product p2 = products.get(j);
                
                if (p1.getName().equals(p2.getName())) {
                    productGraph.addEdge(p1, p2);
                }
            }
        }
        
        statusLabel.setText("Added " + products.size() + " products to graph");
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void launchApp() {
        launch();
    }
}