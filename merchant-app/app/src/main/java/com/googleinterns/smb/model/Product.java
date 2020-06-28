package com.googleinterns.smb.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.googleinterns.smb.common.UIUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Model to implement product information
 */
public class Product implements Serializable {

    private static String DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/mlkitsample-8fffb.appspot.com/o/product_images%2Fno_product_image.jpeg?alt=media&token=93edbbdf-00fc-481a-b5d0-5a3489c9f873";
    private static String TAG = Product.class.getName();

    @PropertyName("product_name")
    private String productName;
    @PropertyName("MRP")
    private Double MRP;
    @PropertyName("discounted_price")
    private Double discountedPrice;
    @PropertyName("image_url")
    private String imageURL = DEFAULT_IMAGE_URL;
    @PropertyName("EAN")
    private String EAN;
    @PropertyName("offers")
    private List<Offer> offers = new ArrayList<>();

    @PropertyName("product_name")
    public String getProductName() {
        return productName;
    }

    @PropertyName("product_name")
    public void setProductName(String productName) {
        this.productName = productName;
    }

    @PropertyName("MRP")
    public Double getMRP() {
        return MRP;
    }

    @Exclude
    public String getMRPString() {
        return String.format(Locale.getDefault(), UIUtils.RUPEE + " %.2f", getMRP());
    }

    @PropertyName("MRP")
    public void setMRP(Double MRP) {
        this.MRP = MRP;
    }

    @PropertyName("discounted_price")
    public Double getDiscountedPrice() {
        if (discountedPrice == null) {
            discountedPrice = MRP;
        }
        return discountedPrice;
    }

    @Exclude
    public String getDiscountedPriceString() {
        return String.format(Locale.getDefault(), UIUtils.RUPEE + " %.2f", getDiscountedPrice());
    }

    @PropertyName("discounted_price")
    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    @PropertyName("image_url")
    public String getImageURL() {
        if (imageURL == null) {
            return DEFAULT_IMAGE_URL;
        }
        return imageURL;
    }

    @PropertyName("image_url")
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @PropertyName("EAN")
    public String getEAN() {
        return EAN;
    }

    @PropertyName("EAN")
    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    /**
     * Empty constructor required by Firebase
     */
    public Product() {

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
        setOffers(product.getOffers());
    }

    @Exclude
    public int getNumOffers() {
        return offers.size();
    }

    @Exclude
    public String getOfferCountString() {
        int numOffers = getNumOffers();
        switch (numOffers) {
            case 0:
                return "No offers added";
            case 1:
                return "1 offer added";
            default:
                return String.format(Locale.getDefault(), "%d offers added", numOffers);
        }
    }

    @PropertyName("offers")
    public List<Offer> getOffers() {
        return offers;
    }

    @PropertyName("offers")
    public void setOffers(List<Offer> offers) {
        this.offers = new ArrayList<>();
        this.offers.addAll(offers);
    }
}
