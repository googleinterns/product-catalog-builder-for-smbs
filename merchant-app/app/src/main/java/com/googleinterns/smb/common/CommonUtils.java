package com.googleinterns.smb.common;

import android.content.Intent;
import android.util.Log;

import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {

    // utility class shouldn't be instantiated
    private CommonUtils() {

    }

    public static final String DETECTED_BARCODES = "DETECTED_BARCODES";
    public static final String DETECTED_PRODUCTS = "DETECTED_PRODUCTS";
    private static final String TAG = "CommonUtils";

    /**
     * utility to get barcodes from serialised data in intent.
     */
    public static List<String> getBarcodes(Intent intent) {
        if ((!intent.hasExtra(DETECTED_BARCODES))) throw new AssertionError();
        List<String> barcodes;
        try {
            barcodes = (List<String>) intent.getSerializableExtra(DETECTED_BARCODES);
            if (barcodes == null) {
                barcodes = new ArrayList<>();
            }
        } catch (Exception e) {
            barcodes = new ArrayList<>();
            Log.e(TAG, "Error loading barcodes from intent");
        }
        return barcodes;
    }

    /**
     * utility to get products from serialised data in intent.
     */
    public static List<Product> getProducts(Intent intent) {
        if ((!intent.hasExtra(DETECTED_PRODUCTS))) throw new AssertionError();
        List<Product> products;
        try {
            products = (List<Product>) intent.getSerializableExtra(DETECTED_PRODUCTS);
            if (products == null) {
                products = new ArrayList<>();
            }
        } catch (Exception e) {
            products = new ArrayList<>();
            Log.e(TAG, "Error loading products from intent");
        }
        return products;
    }
}
