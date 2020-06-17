package com.googleinterns.smb.model;

import android.util.Log;

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
    private int qty = 1;

    // Empty constructor for firebase
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

    public BillItem(Map<String, Object> data) {
        setEAN((String) data.get("EAN"));
        Double mrp;
        Double discountedPrice;
        if (data.get("MRP") instanceof Long) {
            mrp = ((Long) data.get("MRP")).doubleValue();
            discountedPrice = ((Long) data.get("discounted_price")).doubleValue();
        } else {
            mrp = (Double) data.get("MRP");
            discountedPrice = (Double) data.get("discounted_price");
        }
        setMRP(mrp);
        setDiscountedPrice(discountedPrice);
        Long qty = (Long) data.get("quantity");
        setQty(qty.intValue());
        setProductName((String) data.get("product_name"));
        setImageURL((String) data.get("image_url"));
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

    public String getQtyString() {
        return String.format(Locale.getDefault(), "%d", qty);
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Double getTotalPrice() {
        return qty * getDiscountedPrice();
    }

    public String getTotalPriceString() {
        return String.format(Locale.getDefault(), UIUtils.RUPEE + " %.2f", getTotalPrice());
    }
}
