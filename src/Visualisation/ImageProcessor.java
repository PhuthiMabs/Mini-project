package Visualisation;

import Storage.Features;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class ImageProcessor {
	/**
     * Extracts visual features from a product image.
     * @param image The product image to analyze
     * @return Features object containing color histogram, average gray value, and dimensions
     * @throws IllegalArgumentException if the image is null
     */
    public static Features extractFeatures(Image image) {
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int pixelCount = width * height;
        
        double totalGray = 0;
        double[] rgbHistogram = new double[12]; // 4 bins for each R, G, B
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                
                // Grayscale
                double gray = 0.299 * color.getRed() * 255 + 
                             0.587 * color.getGreen() * 255 + 
                             0.114 * color.getBlue() * 255;
                totalGray += gray;
                
                // Histogram
                int rBin = (int) (color.getRed() * 3);     // 0-3
                int gBin = (int) (color.getGreen() * 3);   // 0-3
                int bBin = (int) (color.getBlue() * 3);    // 0-3
                rgbHistogram[rBin]++;
                rgbHistogram[4 + gBin]++;
                rgbHistogram[8 + bBin]++;
            }
        }
        
        // Normalize histogram
        for (int i = 0; i < rgbHistogram.length; i++) {
            rgbHistogram[i] /= pixelCount;
        }
        
        double avgGray = totalGray / pixelCount;
        return new Features(avgGray, rgbHistogram, width, height);
    }
    
    /**
     * Crops a rectangular region from an image.
     * @param original The source image to crop from
     * @param x The x-coordinate of the top-left corner
     * @param y The y-coordinate of the top-left corner
     * @param width The width of the crop region
     * @param height The height of the crop region
     * @return The cropped image
     * @throws IllegalArgumentException if crop dimensions are invalid
     */
    public static WritableImage cropImage(Image original, int x, int y, int width, int height) {
        validateCropDimensions(original, x, y, width, height);
        
        WritableImage cropped = new WritableImage(width, height);
        PixelReader reader = original.getPixelReader();
        PixelWriter writer = cropped.getPixelWriter();
        
        for (int cropY = 0; cropY < height; cropY++) {
            for (int cropX = 0; cropX < width; cropX++) {
                writer.setColor(cropX, cropY, 
                    reader.getColor(x + cropX, y + cropY));
            }
        }
        return cropped;
    }
    
    
    private static void validateCropDimensions(Image image, int x, int y, int w, int h) {
        if (x < 0 || y < 0 || w <= 0 || h <= 0 ||
            x + w > image.getWidth() || y + h > image.getHeight()) {
            throw new IllegalArgumentException(
                String.format("Invalid crop dimensions: x=%d, y=%d, w=%d, h=%d", x, y, w, h));
        }
    }
    
    
    private static void updateHistogram(double[] histogram, Color color) {
        int rBin = (int) (color.getRed() * 3);
        int gBin = (int) (color.getGreen() * 3) + 4;
        int bBin = (int) (color.getBlue() * 3) + 8;
        
        histogram[rBin]++;
        histogram[gBin]++;
        histogram[bBin]++;
    }

    private static double calculateLuminance(Color color) {
        return 0.299 * color.getRed() * 255 + 
               0.587 * color.getGreen() * 255 + 
               0.114 * color.getBlue() * 255;
    }

    private static void normalizeHistogram(double[] histogram, int pixelCount) {
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] /= pixelCount;
        }
    }

    
    
}