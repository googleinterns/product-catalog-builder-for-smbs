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
import com.googleinterns.smb.pojo.MerchantPojo;

import java.util.ArrayList;
import java.util.Arrays;
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
     * Listener interface to notify newly detected products during billing. See {@link com.googleinterns.smb.BillingActivity}
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

    /**
     * Listener interface to receive retrieved brands from database
     */
    public interface OnBrandFetchedListener {
        void onBrandFetched(List<Brand> brands);
    }


    private final static String TAG = Merchant.class.getName();
    public final static String NUM_PRODUCTS = "NUM_PRODUCTS";


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
    // Merchant address
    private String address;
    // Merchant store name
    private String storeName;
    // Merchant domain name
    private String domainName;
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
                                // New merchant sign in
                                MerchantPojo merchantPojo = new MerchantPojo();
                                merchantPojo.setMid(mid);
                                merchantPojo.setName(name);
                                merchantPojo.setEmail(email);
                                merchantPojo.setNumProducts(numProducts);

                                FirebaseFirestore.getInstance().collection("merchants")
                                        .document(mid)
                                        .set(merchantPojo);
                            } else {
                                // Merchant already exists
                                MerchantPojo merchantPojo = document.toObject(MerchantPojo.class);
                                oldToken = merchantPojo.getToken();
                                numProducts = merchantPojo.getNumProducts();
                                storeName = merchantPojo.getStoreName();
                                address = merchantPojo.getAddress();
                                List<Double> location = merchantPojo.getLocation();
                                if (location != null) {
                                    latLng = new LatLng(location.get(0), location.get(1));
                                }
                                domainName = merchantPojo.getDomainName();
                                storeNumProducts();
                            }
                            final String finalOldToken = oldToken;
                            // Check if device token has changed. If yes, then update new token in database
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
     * Get merchant instance
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
     * Fetch inventory, store and return in callback. Overloaded function (with-callback version)
     */
    public void fetchProducts(final OnProductFetchedListener listener) {
        if (inventory != null) {
            listener.onProductFetched(getInventory());
            return;
        }
        Query query = FirebaseFirestore.getInstance().collection("merchants/" + mid + "/products");
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        inventory = new HashMap<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
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

    /**
     * Fetch inventory and store in memory.
     * Overloaded function (without-callback version)
     */
    public void fetchProducts() {
        fetchProducts(new OnProductFetchedListener() {
            @Override
            public void onProductFetched(List<Product> products) {
                // Ignore callback
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
            batch.set(documentReference, product);
            inventory.put(product.getEAN(), product);
        }
        numProducts = inventory.size();
        storeNumProducts();
        DocumentReference merchant = FirebaseFirestore.getInstance().collection("merchants").document(mid);
        batch.update(merchant, MerchantPojo.FIELD_NUM_PRODUCTS, numProducts);
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
                .update(MerchantPojo.FIELD_NUM_PRODUCTS, numProducts);
    }

    /**
     * Update product
     */
    public void updateProduct(final OnDataUpdatedListener listener, Product product) {
        inventory.put(product.getEAN(), product);
        FirebaseFirestore.getInstance()
                .collection("merchants/" + mid + "/products")
                .document(product.getEAN())
                .set(product)
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
    public void getUpdatedProducts(NewProductsFoundListener listener, List<Product> products) {
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

    public LatLng getLatLng() {
        return latLng;
    }

    public String getAddress() {
        return address;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setAddress(String address) {
        this.address = address;
        FirebaseFirestore.getInstance().collection("merchants")
                .document(mInstance.getMid())
                .update(MerchantPojo.FIELD_ADDRESS, address);
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
        FirebaseFirestore.getInstance().collection("merchants")
                .document(mInstance.getMid())
                .update(MerchantPojo.FIELD_STORE_NAME, storeName);
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
        List<Double> location = Arrays.asList(latLng.latitude, latLng.longitude);
        FirebaseFirestore.getInstance().collection("merchants")
                .document(mInstance.getMid())
                .update(MerchantPojo.FIELD_LOCATION, location);
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
        FirebaseFirestore.getInstance().collection("merchants")
                .document(mInstance.getMid())
                .update(MerchantPojo.FIELD_DOMAIN_NAME, domainName);
        Map<String, Object> data = new HashMap<>();

        data.put(MerchantPojo.FIELD_DOMAIN_NAME, domainName);
        data.put(MerchantPojo.FIELD_MID, getMid());
        FirebaseFirestore.getInstance()
                .collection("domains")
                .document(domainName)
                .set(data);
    }

    /**
     * Fetch all brands for the products in merchant's inventory
     */
    public void fetchBrands(final OnBrandFetchedListener listener) {
        Query query = FirebaseFirestore.getInstance().collection("merchants/" + mid + "/brands");
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Brand> brands = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Brand brand = document.toObject(Brand.class);
                            brands.add(brand);
                        }
                        listener.onBrandFetched(brands);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Firebase Error: ", e);
                    }
                });
    }
}
