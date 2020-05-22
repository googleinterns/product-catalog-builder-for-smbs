package com.googleinterns.smb.model;

import android.annotation.SuppressLint;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;

public class Product implements Serializable {

    private String productName;
    private Double MRP;
    private Double discountedPrice;
    private String imageURL;
    public static final String RUPEE = "\u20b9";

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getMRP() {
        return MRP;
    }

    @SuppressLint("DefaultLocale")
    public String getMRPString() {
        return String.format(RUPEE + " %.2f", getMRP());
    }

    public void setMRP(Double MRP) {
        this.MRP = MRP;
    }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }

    @SuppressLint("DefaultLocale")
    public String getDiscountedPriceString() {
        return String.format(RUPEE + " %.2f", getDiscountedPrice());
    }

    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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
        MRP = documentSnapshot.getDouble("MRP");
        // Initialise discounted price to be same as MRP
        discountedPrice = MRP;
        imageURL = documentSnapshot.getString("image_url");
    }

    /**
     * Copy constructor
     */
    public Product(Product product) {
        setProductName(product.getProductName());
        setDiscountedPrice(product.getDiscountedPrice());
        setImageURL(product.getImageURL());
        setMRP(product.getMRP());
    }

}
