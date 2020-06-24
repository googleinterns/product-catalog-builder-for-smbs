package com.googleinterns.smb.common;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommonUtils {

    // Utility class shouldn't be instantiated
    private CommonUtils() {

    }

    public static final String DETECTED_BARCODES = "DETECTED_BARCODES";
    public static final String DETECTED_PRODUCTS = "DETECTED_PRODUCTS";
    private static final String TAG = "CommonUtils";

    /**
     * Utility to get barcodes from serialised data in intent.
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
     * Utility to get products from serialised data in intent.
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

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo networkInfo : info) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getFormattedTime(long diffInSec) {
        if (diffInSec < 60) {
            return String.format(Locale.getDefault(), "%d seconds", diffInSec);
        }
        long diffInMin = diffInSec / 60;
        if (diffInMin < 60) {
            String minutes = diffInMin == 1 ? "minute" : "minutes";
            return String.format(Locale.getDefault(), "%d %s", diffInMin, minutes);
        }
        long diffInHour = diffInMin / 60;
        if (diffInHour < 24) {
            String hours = diffInHour == 1 ? "hour" : "hours";
            return String.format(Locale.getDefault(), "%d %s", diffInHour, hours);
        }
        return "1 day";
    }

    public static String getStringFromLatLng(LatLng latLng) {
        return String.format(Locale.getDefault(), "%f,%f", latLng.latitude, latLng.longitude);
    }

}
