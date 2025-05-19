package Visualisation;

import Storage.Product;
import Storage.TreeGraph;
import Storage.FreshnessLvl;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

/**
 * Visualizes product relationships as an interactive graph using JavaFX.
 * Nodes represent products with color-coding based on freshness levels,
 * and edges represent similarity relationships between products.
 */
public class GraphVisualizer {
    
    private static final double NODE_RADIUS = 20;
    private static final double SPACING = 100;
    
    private final TreeGraph productGraph;
    private final Map<Product, Circle> nodeMap;
    private Pane graphPane;
    private Stage stage;

    /**
     * Constructs a new GraphVisualizer for the specified product graph.
     * @param productGraph The tree graph containing product relationships to visualize
     */
    public GraphVisualizer(TreeGraph productGraph) {
        this.productGraph = productGraph;
        this.nodeMap = new HashMap<>();
    }

    /**
     * Displays the product relationship graph in a new window.
     * The graph layout follows a circular pattern with products of the same
     * type clustered together.
     */
    public void displayGraph() {
        initializeGraphWindow();
        drawGraph();
        stage.show();
    }

    /**
     * Highlights similar products when a node is clicked.
     * @param product The product to highlight and show details for
     */
    public void highlightSimilarProducts(Product product) {
        resetNodeStyles();
        
        Circle selectedNode = nodeMap.get(product);
        if (selectedNode != null) {
            selectedNode.setStroke(Color.BLUE);
            selectedNode.setStrokeWidth(3);
        }
        
        highlightSimilarNodes(product);
        showProductDetails(product);
    }

    private void initializeGraphWindow() {
        stage = new Stage();
        stage.setTitle("Product Similarity Graph");
        
        graphPane = new Pane();
        graphPane.setPrefSize(800, 600);
        
        Scene scene = new Scene(graphPane);
        stage.setScene(scene);
    }

    private void drawGraph() {
        graphPane.getChildren().clear();
        nodeMap.clear();
        
        int productCount = productGraph.getAllProducts().size();
        int index = 0;
        
        for (Product product : productGraph.getAllProducts()) {
            double angle = 2 * Math.PI * index / productCount;
            double x = 400 + 250 * Math.cos(angle);
            double y = 300 + 250 * Math.sin(angle);
            
            createProductNode(product, x, y);
            createSimilarityEdges(product);
            index++;
        }
    }

    private void createProductNode(Product product, double x, double y) {
        Circle node = new Circle(x, y, NODE_RADIUS);
        node.setFill(getFreshnessColor(product.getFresh()));
        node.setStroke(Color.BLACK);
        
        node.setOnMouseEntered(e -> node.setStroke(Color.BLUE));
        node.setOnMouseExited(e -> node.setStroke(Color.BLACK));
        node.setOnMouseClicked(e -> highlightSimilarProducts(product));
        
        Text label = new Text(x - NODE_RADIUS, y - NODE_RADIUS - 5, product.getName());
        
        graphPane.getChildren().addAll(node, label);
        nodeMap.put(product, node);
    }

    private void createSimilarityEdges(Product product) {
        for (Product neighbor : productGraph.getNeighbors(product)) {
            if (nodeMap.containsKey(neighbor)) {
                Line edge = createEdge(nodeMap.get(product), nodeMap.get(neighbor));
                graphPane.getChildren().add(edge);
                edge.toBack();
            }
        }
    }

    private Line createEdge(Circle source, Circle target) {
        Line edge = new Line(
            source.getCenterX(), source.getCenterY(),
            target.getCenterX(), target.getCenterY()
        );
        edge.setStroke(Color.LIGHTGRAY);
        return edge;
    }

    private void resetNodeStyles() {
        nodeMap.values().forEach(node -> {
            node.setStroke(Color.BLACK);
            node.setStrokeWidth(1);
        });
    }

    private void highlightSimilarNodes(Product product) {
        for (Product similar : productGraph.findSimilarProducts(product, 3)) {
            Circle node = nodeMap.get(similar);
            if (node != null) {
                node.setStroke(Color.PURPLE);
                node.setStrokeWidth(2);
            }
        }
    }

    private void showProductDetails(Product product) {
        ProductDetailsDialog.show(product, 
            productGraph.findSimilarProducts(product, 3));
    }

    private Color getFreshnessColor(FreshnessLvl freshness) {
        return switch (freshness) {
            case FRESH -> Color.GREEN;
            case ROTATE -> Color.ORANGE;
            case EXPIRED -> Color.RED;
        };
    }
}