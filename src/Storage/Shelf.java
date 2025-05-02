package Storage;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents one shelf image containing a lot of products
 */
public class Shelf {
	private String shelfId;
	private Image shelfImage;
	private List<Product> productList;
	
	public Shelf(String shelfId, Image shelfImage) {
		super();
		this.shelfId = shelfId;
		this.shelfImage = shelfImage;
		this.productList = new ArrayList<>();
	}
	public List<Product> getProductList() {
		return productList;
	}
	//add all the products found on image or shelf
	public void addProductList(Product product) {
		this.productList.add(product);
	}
	public String getShelfId() {
		return shelfId;
	}
	public Image getShelfImage() {
		return shelfImage;
	}

}
