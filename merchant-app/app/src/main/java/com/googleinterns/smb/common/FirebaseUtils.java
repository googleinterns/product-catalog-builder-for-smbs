package com.googleinterns.smb.common;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.googleinterns.smb.R;
import com.googleinterns.smb.model.BillItem;
import com.googleinterns.smb.model.Brand;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for performing common Firebase Firestore queries
 */
public class FirebaseUtils {

    private static final String TAG = FirebaseUtils.class.getName();

    public interface BarcodeProductQueryListener {
        void onQueryComplete(List<Product> products);
    }

    public interface OnOrderReceivedListener {
        void onOrderReceived(List<Order> orders);
    }

    public interface DomainAvaliabilityCheckListener {
        void onCheckComplete(boolean isAvailable);
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
            throw new ClassCastException(context + " must implement " + BarcodeProductQueryListener.class.getName());
        }
        if (barcodes.isEmpty()) {
            listener.onQueryComplete(new ArrayList<Product>());
            return;
        }
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("products");
        final int[] numTasks = {barcodes.size()};
        List<Product> products = new ArrayList<>();
        for (String barcode : barcodes) {
            DocumentReference document = collectionReference.document(barcode);
            document.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot productDocument = task.getResult();
                                if (productDocument != null && productDocument.exists()) {
                                    products.add(productDocument.toObject(Product.class));
                                }
                            }
                            numTasks[0]--;
                            if (numTasks[0] == 0) {
                                listener.onQueryComplete(products);
                            }
                        }
                    });
        }
    }

    /**
     * Query current new orders from the database
     */
    public static void getNewOrders(final Context context) {
        final OnOrderReceivedListener listener;
        try {
            listener = (OnOrderReceivedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement " + OnOrderReceivedListener.class.getName());
        }

        Merchant merchant = Merchant.getInstance();
        Query query = FirebaseFirestore.getInstance().collection("merchants/" + merchant.getMid() + "/orders");
        long currentTime = System.currentTimeMillis();
        long boundTime = currentTime - ORDER_MIN * 60 * 1000;

        // Get all orders after "boundTime" i.e. not older than ORDER_MIN
        query = query.whereGreaterThan(Order.FIELD_TIMESTAMP, boundTime);
        query = query.whereEqualTo(Order.FIELD_STATUS, Order.NEW_ORDER);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Order> orders = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                Order order = documentSnapshot.toObject(Order.class);
                                orders.add(order);
                            }
                            listener.onOrderReceived(orders);
                        } else {
                            Log.e(TAG, "Firebase Error: ", task.getException());
                            UIUtils.showToast(context, context.getString(R.string.fetch_error));
                        }
                    }
                });
    }

    /**
     * Query ongoing orders from the database
     */
    public static void getOngoingOrders(final Context context) {
        final OnOrderReceivedListener listener;
        try {
            listener = (OnOrderReceivedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement " + OnOrderReceivedListener.class.getName());
        }

        Merchant merchant = Merchant.getInstance();
        Query query = FirebaseFirestore.getInstance().collection("merchants/" + merchant.getMid() + "/orders");
        query = query.whereIn("status", Arrays.asList(Order.ONGOING, Order.DISPATCHED, Order.DELIVERED));
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Order> orders = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                Order order = documentSnapshot.toObject(Order.class);
                                orders.add(order);
                            }
                            listener.onOrderReceived(orders);
                        } else {
                            Log.e(TAG, "Firebase Error: ", task.getException());
                            UIUtils.showToast(context, context.getString(R.string.fetch_error));
                        }
                    }
                });
    }

    /**
     * Accept customer order. Update order details in database
     *
     * @param order     Originally placed order
     * @param billItems Available items
     */
    public static void acceptOrder(Order order, List<BillItem> billItems) {
        String oid = order.getOid();
        Merchant merchant = Merchant.getInstance();
        DocumentReference orderRef = FirebaseFirestore.getInstance().collection("merchants/" + merchant.getMid() + "/orders").document(oid);
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        batch.update(orderRef, Order.FIELD_STATUS, Order.ACCEPTED);
        batch.update(orderRef, Order.FIELD_ITEMS, billItems);
        batch.commit();
    }

    /**
     * Check if given domain is already taken or not
     *
     * @param domain Queried domain
     */
    public static void isDomainAvailable(String domain, final DomainAvaliabilityCheckListener listener) {
        final DocumentReference domainRef = FirebaseFirestore.getInstance().collection("domains/").document(domain);
        domainRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    boolean isDomainUsed = Objects.requireNonNull(task.getResult()).exists();
                    listener.onCheckComplete(!isDomainUsed);
                } else {
                    listener.onCheckComplete(false);
                }
            }
        });
    }

    /**
     * Update order status in firestore
     *
     * @param oid       Order ID
     * @param newStatus Updated status
     */
    public static void updateOrderStatus(String oid, String newStatus) {
        Merchant merchant = Merchant.getInstance();
        DocumentReference orderRef = FirebaseFirestore.getInstance()
                .collection("merchants/" + merchant.getMid() + "/orders")
                .document(oid);
        orderRef.update(Order.FIELD_STATUS, newStatus);
    }

    /**
     * Update product with new offers
     *
     * @param product Product object with updated offers
     */
    public static void updateProductOffers(Product product) {
        String ean = product.getEAN();
        String mid = Merchant.getInstance().getMid();
        FirebaseFirestore.getInstance().collection("merchants/" + mid + "/products")
                .document(ean)
                .update(Product.FIELD_OFFERS, product.getOffers());
    }

    /**
     * Update brand with new offers
     *
     * @param brand Brand object with updated offers
     */
    public static void updateBrandOffers(Brand brand) {
        String brandName = brand.getBrandName();
        String mid = Merchant.getInstance().getMid();
        FirebaseFirestore.getInstance().collection("merchants/" + mid + "/brands")
                .document(brandName)
                .update(Brand.FIELD_OFFERS, brand.getOffers());
    }
}
