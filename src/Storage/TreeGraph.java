package Storage;
import  DataCalculations.KNN;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class stores graph of products using Maps to link them
 */
public class TreeGraph {
	private Map<Product, List<Product>> adjacencyList; // based on similarity
	
	public TreeGraph(Map<Product, List<Product>> adjacencyList) {
		super();
		this.adjacencyList = adjacencyList;
		
	}
	/**
	 * Method adds a product node to the graph
	 * @param product
	 */
    public void addProduct(Product product) {
    	//uses the equals() and hashCode() methods of the Product class
    	//to determine if a product already exists in the Map
        adjacencyList.putIfAbsent(product, new ArrayList<>());
    }

    /**
     * Adds a connection between two products if they are similar(eg. Milk1 --> Milk2)
     * @param p1
     * @param p2
     */
    public void addEdge(Product p1, Product p2) {
    	//Adds products if they are not already on the Map
        if (!adjacencyList.containsKey(p1)) {
        	addProduct(p1);
        }
        if (!adjacencyList.containsKey(p2)) {
        	addProduct(p2);
        }
        //Make an edge for each node to each other to show they are similar
        adjacencyList.get(p1).add(p2);
        adjacencyList.get(p2).add(p1); // undirected edge
    }

    /**
     * Get all connected products/neighbors of given product
     * @param product
     * @return List<Product> 
     */
    public List<Product> getNeighbors(Product product) {
    	//get me the products connected to the given product 
    	//if they exist otherwise give me an empty list
    	List<Product> products =  new ArrayList<>();
    	if(adjacencyList.containsKey(product)) {
    		products = adjacencyList.get(product);
    	}
        return products ;
    }

    /**
     * Get all products in an unordered set 
     * @return Set<Product>
     */
    public Set<Product> getAllProducts() {
    	Set<Product> setProd = adjacencyList.keySet();
    	//if we want to use a list instead for simplicity instead
    	//List<Product> listProd = new ArrayList<>(setProd); 
        return setProd;
    }

    /**
     * Find similar products based on freshness or features using KNN
     * @param product
     * @param k
     * @return List<Product>
     */
    public List<Product> findSimilarProducts(Product product, int k) {
    	List<Product> neighbors = getNeighbors(product);//retrieving all directly related neighbors of product
    	if (neighbors.isEmpty()) {
    		 return new ArrayList<>();
    	}
    	// Find best k using neighbors instead of allProducts
        int optimalK = KNN.findOptimalK(neighbors, Math.min(10, neighbors.size()));
        neighbors.sort(Comparator.comparingDouble(p -> KNN.computeDistance(product, p)));//arranging them in  in ascending order of distance from the given product by difference
        List<Product> output = neighbors.subList(0, Math.min(optimalK, neighbors.size()));
        return output;
    }

    // Check if the graph contains a product
    public boolean contains(Product product) {
        return adjacencyList.containsKey(product);
    }

    // Print graph for visualization/debug
    public void printGraph() {
        for (Map.Entry<Product, List<Product>> entry : adjacencyList.entrySet()) {
        	Product key = entry.getKey();
        	List<Product> neighbors = entry.getValue();//neighbors of product
        	
        	System.out.print(key.getName() + " --> [");
        	for (int i = 0; i < neighbors.size(); i++) {
        	    System.out.print(neighbors.get(i).getName());
        	    //adding a comma after each product excluding the last one
        	    if (i < neighbors.size() - 1) {
        	        System.out.print(", ");
        	    }
        	}
        	System.out.println("]");
        }
    }
}
