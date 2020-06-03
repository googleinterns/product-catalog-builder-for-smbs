package com.googleinterns.smb.model;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Bill item model
 */
public class BillItem extends Product {

    private static final String TAG = BillItem.class.getName();
    private int qty = 1;

    // empty constructor for firebase
    public BillItem() {

    }

    public BillItem(Product product) {
        super(product);
    }

    public BillItem(JSONObject orderJSONObject) {
        try {
            setEAN(orderJSONObject.getString("EAN"));
            setMRP(orderJSONObject.getDouble("MRP"));
            setQty(orderJSONObject.getInt("quantity"));
            setDiscountedPrice(orderJSONObject.getDouble("discounted_price"));
            setProductName(orderJSONObject.getString("product_name"));
            setImageURL(orderJSONObject.getString("image_url"));
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON", e);
        }
    }

    /**
     * Utility to get bill item list from products
     */
    public static List<BillItem> getBillItems(List<Product> products) {
        List<BillItem> billItems = new ArrayList<>();
        for (Product product : products) {
            billItems.add(new BillItem(product));
        }
        return billItems;
    }

    public int getQty() {
        return qty;
    }

    @SuppressLint("DefaultLocale")
    public String getQtyString() {
        return String.format("%d", qty);
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Double getTotalPrice() {
        return qty * getDiscountedPrice();
    }

    @SuppressLint("DefaultLocale")
    public String getTotalPriceString() {
        return String.format(RUPEE + " %.2f", getTotalPrice());
    }
}
