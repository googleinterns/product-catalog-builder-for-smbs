package com.googleinterns.smb.common;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.googleinterns.smb.model.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for common utility functions
 */
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

    public static String getFormattedElapsedTime(long timeElapsedInSec) {
        if (timeElapsedInSec < 60) {
            return String.format(Locale.getDefault(), "%d seconds", timeElapsedInSec);
        }
        long diffInMin = timeElapsedInSec / 60;
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

    public static String getCommaFormattedLatLng(LatLng latLng) {
        return String.format(Locale.getDefault(), "%f,%f", latLng.latitude, latLng.longitude);
    }

    public static String getFormattedDistance(long distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format(Locale.getDefault(), "%d m", (int) distanceInMeters);
        }
        double distanceInKms = (double) distanceInMeters / 1000;
        return String.format(Locale.getDefault(), "%.2f km", distanceInKms);
    }

    public static String getFormattedDate(Long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    public static long getTodayUTCInMillis() {
        Calendar now = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.clear();
        today.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        return today.getTimeInMillis();
    }
}
