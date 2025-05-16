package Visualisation;

import Storage.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphVisualizer extends Pane {
    private final TreeGraph graph;
    private final Map<Product, Point> positions;
    private final Map<Product, Integer> productClusterMap;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    private final Canvas canvas;

    /**
     * Displays TreeGraph visually where Products are nodes and similar products are connected with lines
     * @param graph
     * @param clusterMap
     */
    public GraphVisualizer(TreeGraph graph,Map<Product,Integer> ClustMap) {
        this.graph = graph;
        this.positions = generateHierarchicalShelfLayout(graph,WIDTH,150,150); // calculating where each product will go
        this.productClusterMap = ClustMap;

        // Initialize canvas
        int TWidth = Math.max(WIDTH,graph.getAllProducts().size()*200);
        int THeight = Math.max(HEIGHT,graph.getAllProducts().size()*100);
        canvas = new Canvas(TWidth,THeight);
        this.setPrefSize(TWidth,THeight);
        this.getChildren().add(canvas);

        // Draw the graph
        drawGraph(canvas.getGraphicsContext2D());
    }

    /**
     * Draws everything
     * Edge thickness depends on similarity
     * Nodes are drawn at their (x, y) position
     * @param gc
     */
    private void drawGraph(GraphicsContext gc) {
        gc.setLineWidth(1); // setting the line width
        gc.setStroke(Color.GRAY); // setting color of line

        // Drawing edges (shelf → product, product → similar products)
        for (Product p : positions.keySet()) {
            Point pos1 = positions.get(p); // getting first position (shelf)

            // Shelf nodes connect to its products
            if (p.getFeatures() == null && p.getImg() == null) { // this is a dummy shelf node
                for (Product product : graph.getAllProducts()) {
                    if (product.getShelfID().equals(p.getName())) { // checking which products belong to this shelf
                        Point pos2 = positions.get(product);
                        gc.strokeLine(pos1.getX(),pos1.getY(),pos2.getX(),pos2.getY()); // drawing edge line
                    }
                }
            } else { // product nodes connect to similar products
                List<Product> simProducts = graph.findSimilarProducts(p);
                for (Product neighbor : simProducts) {
                    if (!positions.containsKey(neighbor)) {
                    	continue; // skip if not laid out
                    }
                    Point pos2 = positions.get(neighbor);
                    gc.strokeLine(pos1.getX(),pos1.getY(),pos2.getX(),pos2.getY()); // draw edge line
                }
            }
        }

        // Draw nodes (shelves, products, similar products)
        int radius = 30;
        Color[] ClustColors = {Color.CYAN,Color.MAGENTA,Color.ORANGE,Color.GREEN,Color.PINK,Color.LIGHTGRAY};

        for (Product p : positions.keySet()) {
            Point point = positions.get(p);
            int clusterId = productClusterMap.getOrDefault(p,0); // default to cluster 0
            Color nodeColor = ClustColors[clusterId%ClustColors.length];

            gc.setFill(nodeColor); // fill color based on cluster
            gc.fillOval(point.getX() - radius / 2.0, point.getY() - radius / 2.0, radius, radius);

            gc.setStroke(Color.BLACK); // border
            gc.strokeOval(point.getX() - radius / 2.0, point.getY() - radius / 2.0, radius, radius);

            gc.setFill(Color.BLACK); // text color
            gc.fillText(p.getName(), point.getX() - radius / 2.0, point.getY() - radius);
        }
    }

    /**
     * Generates a similarity-based layout using a simple force-directed algorithm.
     * @param graph
     * @param panelWidth
     * @param verticalSpacing
     * @param horizontalSpacing
     * @return Map<Product, Point>
     */
    private Map<Product, Point> generateHierarchicalShelfLayout(TreeGraph graph,int PanWidth,int VertSpacing,int HorizSpacing) {
        Map<Product, Point> layout = new HashMap<>();
        Map<String, List<Product>> shelves = new HashMap<>();

        //grouping products by shelf ID
        for (Product product : graph.getAllProducts()) {
            shelves.computeIfAbsent(product.getShelfID(), k -> new ArrayList<>()).add(product);
        }

        int x = 50; // start drawing from the left
        int yShelf = 50; // first row for shelves

        //layout each shelf and its tree
        for (String shelfID : shelves.keySet()) {
            List<Product> shelfProducts = shelves.get(shelfID);

            //creating a dummy shelf node (optional)
            Product shelfDummy = new Product(shelfID,null,null,null,null,null); // only used for display
            Point shelfPos = new Point(x,yShelf);
            layout.put(shelfDummy,shelfPos); //placed at the top of the diagram

            int xProduct = x;
            int yProduct = yShelf+VertSpacing; // position each product below the shelf node
            //going through each product in a shelf
            for (Product product : shelfProducts) {
                //storing the position for each product under the shelf
                Point productPos = new Point(xProduct,yProduct);
                layout.put(product, productPos);
                //layout similar products under each product
                List<Product> similars = graph.getNeighbors(product);
                int xSim = xProduct - ((similars.size()-1)*HorizSpacing/2); // calculating starting x-position for similar products
                int ySim = yProduct + VertSpacing; // go down a depth
                //placing similar products underneath each product
                for (Product sim : similars) {
                    if (!layout.containsKey(sim)) {
                        layout.put(sim, new Point(xSim,ySim));
                        xSim += HorizSpacing;
                    }
                }
                xProduct += HorizSpacing; // spacing to the next product
            }
            x += shelfProducts.size() * HorizSpacing + 100; // move to next shelf group
        }
        return layout;
    }
}