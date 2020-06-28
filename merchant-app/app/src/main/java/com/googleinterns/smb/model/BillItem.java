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
import java.util.Map;

/**
 * Bill item model
 */
public class BillItem extends Product {

    private static final String TAG = BillItem.class.getName();
    @PropertyName("quantity")
    private int qty = 1;

    // Empty constructor for firebase
    public BillItem() {

    }

    public BillItem(Product product) {
        super(product);
    }

    public BillItem(JSONObject orderJSONObject) {
        try {
            if (orderJSONObject.has("EAN")) {
                setEAN(orderJSONObject.getString("EAN"));
            }
            if (orderJSONObject.has("MRP")) {
                setMRP(orderJSONObject.getDouble("MRP"));
            } else {
                setMRP(0.0);
            }
            setQty(orderJSONObject.getInt("quantity"));
            if (orderJSONObject.has("discounted_price")) {
                setDiscountedPrice(orderJSONObject.getDouble("discounted_price"));
            } else {
                setDiscountedPrice(0.0);
            }
            setProductName(orderJSONObject.getString("product_name"));
            if (orderJSONObject.has("image_url")) {
                setImageURL(orderJSONObject.getString("image_url"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON", e);
        }
    }

    public BillItem(Map<String, Object> data) {
        if (data.get("EAN") != null) {
            setEAN((String) data.get("EAN"));
        }
        Double mrp = 0.0;
        Double discountedPrice = 0.0;
        if (data.get("MRP") != null) {
            if (data.get("MRP") instanceof Long) {
                mrp = ((Long) data.get("MRP")).doubleValue();
                discountedPrice = ((Long) data.get("discounted_price")).doubleValue();
            } else {
                mrp = (Double) data.get("MRP");
                discountedPrice = (Double) data.get("discounted_price");
            }
        }
        setMRP(mrp);
        setDiscountedPrice(discountedPrice);
        Long qty = (Long) data.get("quantity");
        setQty(qty.intValue());
        setProductName((String) data.get("product_name"));
        if (data.get("image_url") != null) {
            setImageURL((String) data.get("image_url"));
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

    @PropertyName("quantity")
    public int getQty() {
        return qty;
    }

    @PropertyName("quantity")
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
