package com.googleinterns.smb.common;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.googleinterns.smb.MainActivity;
import com.googleinterns.smb.model.BillItem;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for performing common firestore queries
 */
public class FirebaseUtils {

    private static final String TAG = FirebaseUtils.class.getName();

    public interface BarcodeProductQueryListener {
        void onQueryComplete(List<Product> products);
    }

    public interface OnOrderReceivedListener {
        void onOrderReceived(List<Order> orders);
    }

    // Utility class shouldn't be instantiated
    private FirebaseUtils() {

    }

    // Time interval to get orders within last ORDER_MIN minutes
    private static final int ORDER_MIN = 30;

    /**
     * Get products from barcodes
     */
    public static void queryProducts(final Context context, List<String> barcodes) {
        final BarcodeProductQueryListener listener;
        try {
            listener = (BarcodeProductQueryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement OnProductReceivedListener");
        }
        if (barcodes.isEmpty()) {
            listener.onQueryComplete(new ArrayList<Product>());
            return;
        }
        Query query = FirebaseFirestore.getInstance().collection("products");
        // Filter products containing these barcodes
        query = query.whereIn("EAN", barcodes);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Product> products = new ArrayList<>();
                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            Product product = new Product(documentSnapshot);
                            products.add(product);
                        }
                        listener.onQueryComplete(products);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Firebase Error: ", e);
                        UIUtils.showToast(context, "Could'nt fetch products from database");
                    }
                });
    }

    public static void getNewOrders(final Context context) {
        final OnOrderReceivedListener listener;
        try {
            listener = (OnOrderReceivedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement OnOrderReceivedListener");
        }
        Merchant merchant = Merchant.getInstance();
        Query query = FirebaseFirestore.getInstance().collection("merchants/" + merchant.getMid() + "/orders");
        long currentTime = System.currentTimeMillis();
        long boundTime = currentTime - ORDER_MIN * 60 * 1000;
        // Get all orders after "boundTime" i.e. not older than ORDER_MIN
        query = query.whereGreaterThan("timestamp", boundTime);
        query = query.whereEqualTo("status", Order.NEW_ORDER);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Order> orders = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                Map<String, Object> data = documentSnapshot.getData();
                                Order order = new Order(data);
                                orders.add(order);
                            }
                            Log.e(TAG, "Orders " + orders.toString());
                            listener.onOrderReceived(orders);
                        } else {
                            Log.e(TAG, "Firebase Error: ", task.getException());
                            UIUtils.showToast(context, "Could'nt fetch orders from database");
                        }
                    }
                });

    }

    public static void getOngoingOrders(final Context context) {
        final OnOrderReceivedListener listener;
        try {
            listener = (OnOrderReceivedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement OnOrderReceivedListener");
        }
        Merchant merchant = Merchant.getInstance();
        Query query = FirebaseFirestore.getInstance().collection("merchants/" + merchant.getMid() + "/orders");
        query = query.whereEqualTo("status", Order.ONGOING);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Order> orders = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                Map<String, Object> data = documentSnapshot.getData();
                                Order order = new Order(data);
                                orders.add(order);
                            }
                            listener.onOrderReceived(orders);
                        } else {
                            Log.e(TAG, "Firebase Error: ", task.getException());
                            UIUtils.showToast(context, "Could'nt fetch orders from database");
                        }
                    }
                });
    }

    public static void acceptOrder(Order order, List<BillItem> billItems) {
        String oid = order.getOid();
        Merchant merchant = Merchant.getInstance();
        DocumentReference order_ref = FirebaseFirestore.getInstance().collection("merchants/" + merchant.getMid() + "/orders").document(oid);
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        batch.update(order_ref, "status", Order.ACCEPTED);
        List<Map<String, Object>> items = new ArrayList<>();
        for (BillItem billItem : billItems) {
            items.add(billItem.createFirebaseDocument());
        }
        batch.update(order_ref, "items", items);
        batch.commit();
    }

    public static void declineOrder(Order order) {
        String oid = order.getOid();
        Merchant merchant = Merchant.getInstance();
        DocumentReference order_ref = FirebaseFirestore.getInstance().collection("merchants/" + merchant.getMid() + "/orders").document(oid);
        order_ref.update("status", Order.DECLINED)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        UIUtils.showToast(MainActivity.getContext(), "Order declined");
                    }
                });
    }

}
