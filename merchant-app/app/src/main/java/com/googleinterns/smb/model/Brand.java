package com.googleinterns.smb.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class Brand {

    /**
     * Empty constructor for firebase
     */
    public Brand() {

    }

    public static final String FIELD_BRAND_NAME = "brand_name";
    public static final String FIELD_BRAND_IMAGE_URL = "brand_image_url";
    public static final String FIELD_OFFERS = Product.FIELD_OFFERS;

    @PropertyName(FIELD_BRAND_NAME)
    private String brandName;
    @PropertyName(FIELD_OFFERS)
    private List<Offer> offers = new ArrayList<>();
    @PropertyName(FIELD_BRAND_IMAGE_URL)
    private String brandImageUrl;

    @Exclude
    public int getNumOffers() {
        return offers.size();
    }

    @PropertyName(FIELD_BRAND_NAME)
    public String getBrandName() {
        return brandName;
    }

    @PropertyName(FIELD_BRAND_NAME)
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    @PropertyName(FIELD_OFFERS)
    public List<Offer> getOffers() {
        return offers;
    }

    @PropertyName(FIELD_OFFERS)
    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    @PropertyName(FIELD_BRAND_IMAGE_URL)
    public String getBrandImageUrl() {
        return brandImageUrl;
    }

    @PropertyName(FIELD_BRAND_IMAGE_URL)
    public void setBrandImageUrl(String brandImageUrl) {
        this.brandImageUrl = brandImageUrl;
    }
}
