package com.googleinterns.smb.model;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.googleinterns.smb.common.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bill item model
 */
public class BillItem extends Product {

    private static final String TAG = BillItem.class.getName();

    // Field constants
    public static final String FIELD_QUANTITY = "quantity";

    @PropertyName(FIELD_QUANTITY)
    private int qty = 1;

    // Empty constructor for firebase
    public BillItem() {

    }

    public BillItem(Product product) {
        super(product);
    }

    /**
     * Get billitem from JSON object. See {@link com.googleinterns.smb.service.FirebaseCloudMessagingService}
     */
    public BillItem(JSONObject orderJSONObject) {
        try {
            // Required fields
            setQty(orderJSONObject.getInt(BillItem.FIELD_QUANTITY));
            setProductName(orderJSONObject.getString(Product.FIELD_PRODUCT_NAME));

            // Optional fields
            if (orderJSONObject.has(Product.FIELD_EAN)) {
                setEAN(orderJSONObject.getString(Product.FIELD_EAN));
            }
            if (orderJSONObject.has(Product.FIELD_MRP)) {
                setMRP(orderJSONObject.getDouble(Product.FIELD_MRP));
            }
            if (orderJSONObject.has(Product.FIELD_DISCOUNTED_PRICE)) {
                setDiscountedPrice(orderJSONObject.getDouble(Product.FIELD_DISCOUNTED_PRICE));
            }
            if (orderJSONObject.has(Product.FIELD_IMAGE_URL)) {
                setImageURL(orderJSONObject.getString(Product.FIELD_IMAGE_URL));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON", e);
        }
    }

    /**
     * Utility to get bill item list from products
     */
    @Exclude
    public static List<BillItem> getBillItems(List<Product> products) {
        List<BillItem> billItems = new ArrayList<>();
        for (Product product : products) {
            billItems.add(new BillItem(product));
        }
        return billItems;
    }

    @PropertyName(FIELD_QUANTITY)
    public int getQty() {
        return qty;
    }

    @PropertyName(FIELD_QUANTITY)
    public void setQty(int qty) {
        this.qty = qty;
    }

    @Exclude
    public String getQtyString() {
        return String.format(Locale.getDefault(), "%d", qty);
    }

    @Exclude
    public Double getTotalPrice() {
        return qty * getDiscountedPrice();
    }

    @Exclude
    public String getTotalPriceString() {
        return String.format(Locale.getDefault(), UIUtils.RUPEE + " %.2f", getTotalPrice());
    }
}
