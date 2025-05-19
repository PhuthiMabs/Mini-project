package Storage;
/**
 * Just products being taken in storing its data
 */

import java.io.Serializable;	
import java.util.Objects;

import javafx.scene.image.Image;

public class Product implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6224236929731330003L;
	private String name;//Name of product being stored
	private Features features;//feature of each image of product ,e.g shape etc..
	private Shelf shelf;//shelf ,product is found in 
	private transient Image img;//image of product
	private Date expiryDate;//how many days before a product expires
	private FreshnessLvl fresh;//how fresh products are
	private String imagePath;

	/**
	 * Constructor for class
	 * @param name
	 * @param features
	 * @param shelfID
	 * @param img
	 * @param expiryDate
	 * @param fresh
	 */
	public Product(String name, Features features, Shelf shelf, Image img, Date expiryDate, FreshnessLvl fresh) {
		super();
		this.name = name;
		this.features = features;
		this.shelf = shelf;
		this.img = img;
		this.expiryDate = expiryDate;
		this.fresh = fresh;
	}

	@Override
	public String toString() {
		return "Product - [name=" + name + "] [features =" + features.toString() + "] [shelfID =" + shelf.getShelfId() + "] [expiryDate ="
				+ expiryDate + "] [fresh =" + fresh + "]";
	}

	//Getters and setters for class variables
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	public Date getExpiryDate() {
		return expiryDate;
	}

	public FreshnessLvl getFresh() {
		return fresh;
	}

	public void setFresh(FreshnessLvl fresh) {
		this.fresh = fresh;
	}

	public String getName() {
		return name;
	}

	public Features getFeatures() {
		return features;
	}

	public String getShelfID() {
		return shelf.getShelfId();
	}

	public Image getImg() {
		return img;
	}
	
	/**
	 * Method checks if products are the same using its name,shelf ID as well as its features
	 * @param other
	 * @return true/false
	 */
	@Override
	public boolean equals(Object other)throws NullPointerException,IllegalArgumentException{
		if(other == null || !(other instanceof Product)) {
			return false;
		}
		Product sim = (Product)other;
		
		if (this == other) return true;
        if(name.equals(sim.getName()) && 
        shelf.getShelfId().equals(sim.getShelfID()) &&
        features.equals(sim.getFeatures())) {return true;}
        return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name,shelf.getShelfId(),features); /* based on name and shelfID */ }
}
