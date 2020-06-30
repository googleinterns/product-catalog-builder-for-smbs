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

    // Firebase field names
    public static final String FIELD_PRODUCT_NAME = "product_name";
    public static final String FIELD_MRP = "MRP";
    public static final String FIELD_DISCOUNTED_PRICE = "discounted_price";
    public static final String FIELD_IMAGE_URL = "image_url";
    public static final String FIELD_EAN = "EAN";
    public static final String FIELD_OFFERS = "offers";

    @PropertyName(FIELD_PRODUCT_NAME)
    private String productName;
    @PropertyName(FIELD_MRP)
    private Double MRP = 0.0;
    @PropertyName(FIELD_DISCOUNTED_PRICE)
    private Double discountedPrice;
    @PropertyName(FIELD_IMAGE_URL)
    private String imageURL = DEFAULT_IMAGE_URL;
    @PropertyName(FIELD_EAN)
    private String EAN;
    @PropertyName(FIELD_OFFERS)
    private List<Offer> offers = new ArrayList<>();

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

    @PropertyName(FIELD_PRODUCT_NAME)
    public String getProductName() {
        return productName;
    }

    @PropertyName(FIELD_PRODUCT_NAME)
    public void setProductName(String productName) {
        this.productName = productName;
    }

    @PropertyName(FIELD_MRP)
    public Double getMRP() {
        return MRP;
    }

    @Exclude
    public String getMRPString() {
        return String.format(Locale.getDefault(), UIUtils.RUPEE + " %.2f", getMRP());
    }

    @PropertyName(FIELD_MRP)
    public void setMRP(Double MRP) {
        this.MRP = MRP;
    }

    @PropertyName(FIELD_DISCOUNTED_PRICE)
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

    @PropertyName(FIELD_DISCOUNTED_PRICE)
    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    @PropertyName(FIELD_IMAGE_URL)
    public String getImageURL() {
        if (imageURL == null) {
            return DEFAULT_IMAGE_URL;
        }
        return imageURL;
    }

    @PropertyName(FIELD_IMAGE_URL)
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @PropertyName(FIELD_EAN)
    public String getEAN() {
        return EAN;
    }

    @PropertyName(FIELD_EAN)
    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    @PropertyName(FIELD_OFFERS)
    public List<Offer> getOffers() {
        return offers;
    }

    @PropertyName(FIELD_OFFERS)
    public void setOffers(List<Offer> offers) {
        this.offers = new ArrayList<>();
        this.offers.addAll(offers);
    }
}
