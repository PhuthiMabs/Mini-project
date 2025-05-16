package Visualisation;

import DataCalculations.FreshCalculator;
import DataCalculations.GroupClust;
import Memory.ManageData;
import Storage.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GuiHandler extends Application {
    private ImgPanel imagePanel;
    private ManageData dataManager;
    private TextField nameField, shelfField, EyearField, EmonthField, EdayField, TyearField, TmonthField, TdayField;
    private Image loadedImage,originalImage;
    private String ImgPath;
    private Date today;
    private GroupClust Gclust;

    @Override
    public void start(Stage primaryStage) {
        dataManager = new ManageData();
        primaryStage.setTitle("Supermarket Product Manager");
        primaryStage.setScene(new Scene(createContent(),1000,500));
        primaryStage.show();
    }

    private BorderPane createContent() {
        BorderPane root = new BorderPane();
        root.setLeft(createInputPanel());
        root.setCenter(imagePanel = new ImgPanel());
        return root;
    }

    private GridPane createInputPanel() {
    	//creating panel 
        GridPane inputPanel = new GridPane();
        inputPanel.setPadding(new Insets(10));
        inputPanel.setHgap(10);
        inputPanel.setVgap(10);
        //fields for input
        nameField = new TextField();
        shelfField = new TextField();
        TyearField = new TextField();
        TmonthField = new TextField();
        TdayField = new TextField();
        EyearField = new TextField();
        EmonthField = new TextField();
        EdayField = new TextField();
        //buttons to press for action
        Button loadImageBtn = new Button("Load Image");
        Button cropBtn = new Button("Crop & Analyze");
        Button addBtn = new Button("Add Product");
        Button showGraphBtn = new Button("Show Graph");
        Button saveBtn = new Button("Save Data");
        Button loadBtn = new Button("Load Data");
        Button viewFreshnessGridBtn = new Button("View Freshness");
        Button rotationBtn = new Button("Rotation"); 
        Button resetImageBtn = new Button("Reset Image");
        //actions of each button
        loadImageBtn.setOnAction(e -> loadImage());
        cropBtn.setOnAction(e -> cropImage());
        addBtn.setOnAction(e -> addProduct());
        showGraphBtn.setOnAction(e -> showGraph());
        saveBtn.setOnAction(e -> saveData());
        loadBtn.setOnAction(e -> loadData());
        viewFreshnessGridBtn.setOnAction(e -> showFreshnessGrid());
        resetImageBtn.setOnAction(e -> resetImage());
        rotationBtn.setOnAction(e -> showRotationRecommendations());
        //input being taken in
        inputPanel.add(new Label("Product Name:"),0,0);
        inputPanel.add(nameField,1,0);
        inputPanel.add(new Label("Shelf ID:"),0, 1);
        inputPanel.add(shelfField,1,1);
        inputPanel.add(new Label("Today Date(YYYY MM DD):"),0,2);
        inputPanel.add(TyearField, 1, 2);
        inputPanel.add(TmonthField, 2, 2);
        inputPanel.add(TdayField, 3, 2);
        inputPanel.add(new Label("Expiry Date(YYYY MM DD):"), 0, 3);
        inputPanel.add(EyearField, 1,3);
        inputPanel.add(EmonthField,2,3);
        inputPanel.add(EdayField, 3, 3);
        inputPanel.add(loadImageBtn,0,4);
        inputPanel.add(cropBtn,1,4);
        inputPanel.add(addBtn, 0,5);
        inputPanel.add(showGraphBtn,1, 5);
        inputPanel.add(saveBtn,0,6);
        inputPanel.add(loadBtn,1,6);
        inputPanel.add(viewFreshnessGridBtn, 0, 7);
        inputPanel.add(rotationBtn,1,7);
        inputPanel.add(resetImageBtn, 0, 8);

        return inputPanel;
    }

    /**
     * Resets image after zooming
     */
    private void resetImage() {
        if (originalImage != null) {
            loadedImage = new WritableImage(originalImage.getPixelReader(),
            		(int)originalImage.getWidth(),(int)originalImage.getHeight());
            imagePanel.setImage(loadedImage);
        } else {
            showError("No image to reset.");
        }
    }

    /**
     * Loads an image from user's files
     */
    private void loadImage() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            ImgPath = file.getAbsolutePath();
            try {
                originalImage = new Image(file.toURI().toString());
                loadedImage = new WritableImage(originalImage.getPixelReader(),(int)originalImage.getWidth(),(int)originalImage.getHeight());
                imagePanel.setImage(loadedImage);
            } catch (Exception ex) {
                showError("Failed to load image.");
            }
        }
    }

    /**
     * Crops image and captures dimensions/size of image
     */
    private void cropImage() {
        try {
            Rectangle r = imagePanel.getSelection();
            if (r == null || r.getWidth() == 0 || r.getHeight() == 0) {
                showError("Please draw a region to crop.");
                return;
            }
            Image cropped = ImageProcessor.cropImage(loadedImage,(int)r.getX(),(int)r.getY(),(int)r.getWidth(),(int)r.getHeight());
            loadedImage = cropped;
            imagePanel.setImage(cropped);
        } catch (Exception ex) {
            showError("Invalid crop parameters.");
        }
    }

    /**
     * adds a product to list of products in shelves
     */
    private void addProduct() {
        try {
        	if (nameField.getText().isEmpty()||shelfField.getText().isEmpty()) {
                showError("Name and Shelf ID are required.");
                return;
            }
            String name = nameField.getText();
            String shelf = shelfField.getText();
            int tyear = Integer.parseInt(TyearField.getText());
            int tmonth = Integer.parseInt(TmonthField.getText());
            int tday = Integer.parseInt(TdayField.getText());
            int eyear = Integer.parseInt(EyearField.getText());
            int emonth = Integer.parseInt(EmonthField.getText());
            int eday = Integer.parseInt(EdayField.getText());
            Date expiry = new Date(eyear, emonth, eday);
            today = new Date(tyear, tmonth, tday);
            Features feats = ImageProcessor.extractFeatures(loadedImage);
            List<Product> similar = dataManager.findSimilarTo(new Product(name,feats,new Shelf(shelf,null),null,null,null));
            if (!similar.isEmpty()) {
                Product suggested = similar.get(0);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Suggested match: " + suggested.getName() + ". Accept?");
                if (alert.showAndWait().get() == ButtonType.OK) {
                    // Create a temporary product for similarity comparison
                    Product tempProduct = new Product(name, feats, new Shelf(shelf,loadedImage),loadedImage,expiry, 
                                                     FreshCalculator.getFreshnessLevel(today,expiry));
                    tempProduct.setImagePath(ImgPath);
                    // Add similarity edge
                    dataManager.addSimilarity(tempProduct, suggested);
                    // Update suggested product’s metadata
                    suggested.setFresh(FreshCalculator.getFreshnessLevel(today,expiry));
                    showMessage("Product matched and similarity updated: " + suggested.getName());
                    return;
                }
            }
            Product product = new Product(name,feats,new Shelf(shelf,loadedImage),loadedImage,expiry,FreshCalculator.getFreshnessLevel(today,expiry));
            product.setImagePath(ImgPath);
            dataManager.addProduct(product);
            showMessage("Product added.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to add product.");
        }
    }

    /**
     * Shows node/edge graph
     */
    private void showGraph() {
    	Gclust = new GroupClust(5,dataManager.getAllProducts());
        Stage graphStage = new Stage();
        graphStage.setTitle("Graph");
        Map<Product, Integer> clusterMap = new HashMap<>();
        for (Map.Entry<Integer, List<Product>> entry : Gclust.getClusters().entrySet()) {
            for (Product p : entry.getValue()) {
                clusterMap.put(p,entry.getKey());
            }
        }
        GraphVisualizer visual = new GraphVisualizer(dataManager.getGraph(),new HashMap<>());
        ScrollPane scrollPane = new ScrollPane(visual);
        Scene scene = new Scene(scrollPane);
        graphStage.setScene(scene);
        graphStage.show();
    }

    /**
     * Saves the list of products and shelves if needed for use at a later stage
     */
    private void saveData() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            try {
                dataManager.saveToCSV(file.getPath());
                showMessage("Data saved.");
            } catch (IOException ex) {
                showError("Failed to save.");
            }
        }
    }

    /**
     * loads saved data that can be reused
     */
    private void loadData() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            try {
                dataManager.loadFromCSV(file.getPath());
                showMessage("Data loaded.");
            } catch (IOException ex) {
                showError("Failed to load.");
            }
        }
    }

    /**
     * Utility that displays message given
     * @param msg
     */
    private void showMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Utility shows error given
     */
    private void showError(String err) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(err);
        alert.showAndWait();
    }

    /**
     * Shows which products need to be changed 
     */
    private void showFreshnessGrid() {
        if (today == null) {
            showError("Please add at least one product so the system can track today's date.");
            return;
        }

        Map<FreshnessLvl, List<Product>> groups = dataManager.groupByFreshness();
        for (FreshnessLvl lvl : groups.keySet()) {
            groups.get(lvl).sort((p1, p2) -> {
                int d1 = Date.ExpiryDate(today,p1.getExpiryDate());
                int d2 = Date.ExpiryDate(today,p2.getExpiryDate());
                return Integer.compare(d1,d2);
            });
        }

        GridPane gridPanel = new GridPane();
        gridPanel.setHgap(10);
        gridPanel.setVgap(5);
        gridPanel.setPadding(new Insets(10));

        int col = 0;
        for (FreshnessLvl lvl : FreshnessLvl.values()) {
            Label header = new Label(lvl.name());
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-background-color: lightgray; -fx-padding: 5;");
            header.setAlignment(Pos.CENTER);
            gridPanel.add(header,col++, 0);
        }

        for (int row = 1; row <= 5; row++) {
            col = 0;
            for (FreshnessLvl lvl : FreshnessLvl.values()) {
                List<Product> list = groups.getOrDefault(lvl,new ArrayList<>());
                if (row - 1 < list.size()) {
                    Product p = list.get(row - 1);
                    Label productLabel = new Label(p.getName() + " (" + Date.ExpiryDate(today,p.getExpiryDate()) + " days)");
                    productLabel.setAlignment(Pos.CENTER);
                    switch (lvl) {
                        case FRESH: productLabel.setTextFill(Color.GREEN); break;
                        case ROTATE: productLabel.setTextFill(Color.ORANGE); break;
                        case SHELF_STABLE: productLabel.setTextFill(Color.BLUE); break;
                        case EXPIRED: productLabel.setTextFill(Color.RED); break;
                    }
                    gridPanel.add(productLabel,col++,row);
                } else {
                    gridPanel.add(new Label(""),col++,row);
                }
            }
        }

        for (int row = 1; row<=5; row++) {
            int c = 0;//column
            for (FreshnessLvl lvl : FreshnessLvl.values()) {
                List<Product> list = groups.getOrDefault(lvl, new ArrayList<>());
                if (row - 1 < list.size()) {
                    Product p = list.get(row-1);
                    HBox cell = new HBox(5);
                    ImageView imgView = new ImageView(p.getImg());
                    imgView.setFitWidth(50);
                    imgView.setFitHeight(50);
                    Label label = new Label(p.getName() + " (" +Date.ExpiryDate(today,p.getExpiryDate())+ " days)");
                    cell.getChildren().addAll(imgView,label);
                    gridPanel.add(cell,c++,row);
                } else {
                    gridPanel.add(new Label(""),c++,row);
                }
            }
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Top 5 Products by Freshness Level");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(gridPanel);
        alert.showAndWait();
    }
    
    /**
     * Recommends which products to change
     */
    private void showRotationRecommendations() {
        List<Product> rotatables = FreshCalculator.getRotatableProducts(dataManager.getAllProducts());
        List<Product> expired = FreshCalculator.getExpiredProducts(dataManager.getAllProducts());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rotation Recommendations");
        StringBuilder sb = new StringBuilder();
        sb.append("Rotate Soon:\n");
        rotatables.forEach(p -> sb.append(p.getName()).append(" (").append(Date.ExpiryDate(today, p.getExpiryDate())).append(" days)\n"));
        sb.append("\nExpired:\n");
        expired.forEach(p -> sb.append(p.getName()).append("\n"));
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
    
}