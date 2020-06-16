package com.googleinterns.smb.model;

import com.google.firebase.firestore.DocumentSnapshot;
import com.googleinterns.smb.common.UIUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Model to implement product information
 */
public class Product implements Serializable {

    private String productName;
    private Double MRP;
    private Double discountedPrice;
    private String imageURL;
    private String EAN;

    private static String DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/mlkitsample-8fffb.appspot.com/o/product_images%2Fno_product_image.jpeg?alt=media&token=93edbbdf-00fc-481a-b5d0-5a3489c9f873";

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getMRP() {
        return MRP;
    }

    public String getMRPString() {
        return String.format(Locale.getDefault(), UIUtils.RUPEE + " %.2f", getMRP());
    }

    public void setMRP(Double MRP) {
        this.MRP = MRP;
    }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }

    public String getDiscountedPriceString() {
        return String.format(Locale.getDefault(), UIUtils.RUPEE + " %.2f", getDiscountedPrice());
    }

    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public String getImageURL() {
        if (imageURL == null) {
            return DEFAULT_IMAGE_URL;
        }
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    /**
     * Empty constructor required by Firebase
     */
    public Product() {

    }

    /**
     * Initialise product using firebase documentSnapshot
     *
     * @param documentSnapshot document snapshot from firebase
     */
    public Product(DocumentSnapshot documentSnapshot) {
        productName = documentSnapshot.getString("product_name");
        if (documentSnapshot.contains("MRP")) {
            MRP = documentSnapshot.getDouble("MRP");
        }
        if (documentSnapshot.contains("discounted_price")) {
            // Initialise discounted price to be same as MRP if not discounted_price is present
            Double d = documentSnapshot.getDouble("discounted_price");
            if (d != null) {
                discountedPrice = d;
            } else {
                discountedPrice = MRP;
            }
        }
        if (documentSnapshot.contains("image_url")) {
            imageURL = documentSnapshot.getString("image_url");
        }
        if (documentSnapshot.contains(("EAN"))) {
            EAN = documentSnapshot.getString("EAN");
        }
    }

    /**
     * Copy constructor
     */
    public Product(Product product) {
        setProductName(product.getProductName());
        setDiscountedPrice(product.getDiscountedPrice());
        setImageURL(product.getImageURL());
        setMRP(product.getMRP());
        setEAN(product.getEAN());
    }

    public Map<String, Object> createFirebaseDocument() {
        Map<String, Object> data = new HashMap<>();
        data.put("EAN", EAN);
        data.put("product_name", productName);
        data.put("MRP", MRP);
        data.put("discounted_price", discountedPrice);
        data.put("image_url", imageURL);
        return data;
    }
}
