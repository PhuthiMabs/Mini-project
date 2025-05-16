package Storage;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

/**
 * Represents one shelf image
 */
public class Shelf {
	private String shelfId;
	private Image shelfImg;
	private List<Product> productList;
	
	public Shelf(String shelfId,Image shelfImage) {
		super();
		this.shelfId = shelfId;
		this.shelfImg = shelfImage;
		this.productList = new ArrayList<>();
	}
	/**
	 * Gives the list of products in this shelf
	 * @return
	 */
	public List<Product> getProductList() {
		
		return productList;
	}
	/**
	 * Adds all the products found on image or shelf
	 * @param product
	 */
	public void addProductList(Product product) {
		this.productList.add(product);
	}
	
	//Getters and setters
	public String getShelfId() {
		return shelfId;
	}
	public Image getShelfImage() {
		return shelfImg;
	}

}
