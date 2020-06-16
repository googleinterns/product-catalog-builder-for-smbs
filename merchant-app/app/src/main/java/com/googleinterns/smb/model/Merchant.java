package com.googleinterns.smb.model;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.googleinterns.smb.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Merchant class using singleton pattern which represents the current merchant.
 */
public class Merchant {

    /**
     * Listener interface for database update status
     */
    public interface OnDataUpdatedListener {
        void onDataUpdateSuccess();

        void onDataUpdateFailure();
    }

    /**
     * Listener interface to notify newly detected products during billing
     */
    public interface NewProductsFoundListener {
        void onNewProductsFound(List<Product> newProducts);
    }

    /**
     * Listener interface to receive retrieved products from database
     */
    public interface OnProductFetchedListener {
        void onProductFetched(List<Product> products);
    }

    private final static String TAG = "Merchant";
    private final static String NUM_PRODUCTS = "NUM_PRODUCTS";

    // Unique merchant UID given by firebase auth
    private String mid;
    // Merchant name
    private String name;
    // Merchant email
    private String email;
    // Merchant device token, used to send notifications by FCM
    private String token;
    // Merchant photo URI
    private Uri photoUri;
    // Number of products in inventory
    private int numProducts;
    // Merchant LatLng
    private LatLng latLng;
    // Merchant inventory
    private Map<String, Product> inventory;

    // Merchant singleton instance
    private static Merchant mInstance;

    /**
     * Initialise merchant and update in database, reinitialise information if already present
     */
    private Merchant() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // User must sign in before use
        assert user != null;
        // Unique ID given by FirebaseAuth
        mid = user.getUid();
        name = user.getDisplayName();
        email = user.getEmail();
        photoUri = user.getPhotoUrl();
        numProducts = getStoredNumProducts();
        latLng = new LatLng(23.012265, 72.587970);
        final Map<String, Object> data = new HashMap<>();
        data.put("mid", mid);
        data.put("name", name);
        data.put("email", email);
        data.put("num_products", 0);
        FirebaseFirestore.getInstance().collection("merchants")
                .document(mid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String oldToken = null;
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (!document.exists()) {
                                FirebaseFirestore.getInstance().collection("merchants")
                                        .document(mid)
                                        .set(data);
                            } else {
                                oldToken = document.getString("token");
                                numProducts = document.getLong("num_products").intValue();
                                storeNumProducts();
                            }
                            final String finalOldToken = oldToken;
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w(TAG, "getInstanceId failed", task.getException());
                                                return;
                                            }
                                            String newToken = task.getResult().getToken();
                                            if (!newToken.equals(finalOldToken)) {
                                                updateToken(Objects.requireNonNull(task.getResult()).getToken());
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Get singleton merchant instance
     *
     * @return merchant instance
     */
    public static synchronized Merchant getInstance() {
        if (mInstance != null) {
            return mInstance;
        }
        mInstance = new Merchant();
        return mInstance;
    }

    /**
     * Fetch inventory and store. Overloaded function (without-callback version)
     */
    public void fetchProducts() {
        fetchProducts(new OnProductFetchedListener() {
            @Override
            public void onProductFetched(List<Product> products) {
                // Ignore callback
            }
        });
    }

    /**
     * Fetch inventory, store and return in callback. Overloaded function (with-callback version)
     */
    public void fetchProducts(final OnProductFetchedListener listener) {
        if (inventory != null) {
            listener.onProductFetched(getInventory());
            return;
        }
        inventory = new HashMap<>();
        Query query = FirebaseFirestore.getInstance().collection("merchants/" + mid + "/products");
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = new Product(document);
                            inventory.put(product.getEAN(), product);
                        }
                        numProducts = inventory.size();
                        listener.onProductFetched(getInventory());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Firebase Error: ", e);
                    }
                });
    }

    private int getStoredNumProducts() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        return preferences.getInt(NUM_PRODUCTS, 0);
    }

    private void storeNumProducts() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(NUM_PRODUCTS, numProducts);
        editor.apply();
    }

    /**
     * Remove merchant instance
     */
    public static synchronized void removeInstance() {
        if (mInstance != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(NUM_PRODUCTS);
            editor.apply();
            FirebaseFirestore.getInstance().collection("merchants")
                    .document(mInstance.getMid())
                    .update("token", FieldValue.delete());
        }
        mInstance = null;
    }

    /**
     * Add all products in list to merchant's inventory
     */
    public void addProducts(final OnDataUpdatedListener listener, List<Product> products) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for (Product product : products) {
            String collectionPath = "merchants/" + mid + "/products";
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection(collectionPath).document(product.getEAN());
            Map<String, Object> data = product.createFirebaseDocument();
            batch.set(documentReference, data);
            inventory.put(product.getEAN(), product);
        }
        numProducts = inventory.size();
        storeNumProducts();
        DocumentReference merchant = FirebaseFirestore.getInstance().collection("merchants").document(mid);
        batch.update(merchant, "num_products", numProducts);
        batch.commit()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onDataUpdateSuccess();
                        } else {
                            listener.onDataUpdateFailure();
                        }
                    }
                });
    }

    /**
     * Delete all products present in the list
     */
    public void deleteProduct(final OnDataUpdatedListener listener, Product product) {
        inventory.remove(product.getEAN());
        numProducts = inventory.size();
        storeNumProducts();
        FirebaseFirestore.getInstance()
                .collection("merchants/" + mid + "/products")
                .document(product.getEAN())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onDataUpdateSuccess();
                        } else {
                            listener.onDataUpdateFailure();
                        }
                    }
                });
        FirebaseFirestore.getInstance()
                .collection("merchants")
                .document(mid)
                .update("num_products", numProducts);
    }

    /**
     * Update product
     */
    public void updateProduct(final OnDataUpdatedListener listener, Product product) {
        inventory.put(product.getEAN(), product);
        FirebaseFirestore.getInstance()
                .collection("merchants/" + mid + "/products")
                .document(product.getEAN())
                .set(product.createFirebaseDocument())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onDataUpdateSuccess();
                        } else {
                            listener.onDataUpdateFailure();
                        }
                    }
                });
    }

    /**
     * Given a list of products, update merchant price for those which are present in inventory.
     * Also notify about products which are not present in the inventory
     */
    public List<Product> getUpdatedProducts(NewProductsFoundListener listener, List<Product> products) {
        List<Product> newProducts = new ArrayList<>();
        for (Product product : products) {
            Product merchantProduct = inventory.get(product.getEAN());
            if (merchantProduct != null) {
                product.setDiscountedPrice(merchantProduct.getDiscountedPrice());
            } else {
                newProducts.add(product);
            }
        }
        if (newProducts.size() > 0) {
            listener.onNewProductsFound(newProducts);
        }
        return products;
    }

    private List<Product> getInventory() {
        return new ArrayList<>(inventory.values());
    }

    /**
     * Update merchant device token
     */
    public void updateToken(String token) {
        this.token = token;
        FirebaseFirestore.getInstance().collection("merchants")
                .document(mid)
                .update("token", token);
    }

    public String getMid() {
        return mid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public int getNumProducts() {
        return numProducts;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
