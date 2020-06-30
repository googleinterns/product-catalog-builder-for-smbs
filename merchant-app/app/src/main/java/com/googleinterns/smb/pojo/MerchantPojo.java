package com.googleinterns.smb.pojo;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class MerchantPojo {

    /**
     * Empty constructor for Firebase
     */
    public MerchantPojo() {

    }

    // Field constants
    public static final String FIELD_MID = "mid";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_TOKEN = "token";
    public static final String FIELD_NUM_PRODUCTS = "num_products";
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_STORE_NAME = "store_name";
    public static final String FIELD_DOMAIN_NAME = "domain_name";

    // Unique merchant UID given by firebase auth
    @PropertyName("mid")
    private String mid;
    // Merchant name
    @PropertyName("name")
    private String name;
    // Merchant email
    @PropertyName("email")
    private String email;
    // Merchant device token, used to send notifications by FCM
    @PropertyName("token")
    private String token;
    // Number of products in inventory
    @PropertyName("num_products")
    private int numProducts;
    // Merchant LatLng
    @PropertyName("location")
    private List<Double> location;
    // Merchant address
    @PropertyName("address")
    private String address;
    // Merchant store name
    @PropertyName("store_name")
    private String storeName;
    // Merchant domain name
    @PropertyName("domain_name")
    private String domainName;

    @PropertyName("mid")
    public String getMid() {
        return mid;
    }

    @PropertyName("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("token")
    public String getToken() {
        return token;
    }

    @PropertyName("token")
    public void setToken(String token) {
        this.token = token;
    }

    @PropertyName("num_products")
    public int getNumProducts() {
        return numProducts;
    }

    @PropertyName("num_products")
    public void setNumProducts(int numProducts) {
        this.numProducts = numProducts;
    }

    @PropertyName("location")
    public List<Double> getLocation() {
        return location;
    }

    @PropertyName("location")
    public void setLocation(List<Double> location) {
        this.location = location;
    }

    @PropertyName("address")
    public String getAddress() {
        return address;
    }

    @PropertyName("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @PropertyName("store_name")
    public String getStoreName() {
        return storeName;
    }

    @PropertyName("store_name")
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @PropertyName("domain_name")
    public String getDomainName() {
        return domainName;
    }

    @PropertyName("domain_name")
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}
