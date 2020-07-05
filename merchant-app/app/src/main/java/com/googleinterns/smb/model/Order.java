package com.googleinterns.smb.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.googleinterns.smb.common.APIHandler;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.pojo.OrderStatus;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class to model the customer order
 */
public class Order implements Serializable {

    private static final String TAG = Order.class.getName();

    // Status constants
    public static final String NEW_ORDER = "NEW_ORDER";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String DECLINED = "DECLINED";
    public static final String ONGOING = "ONGOING";
    public static final String DISPATCHED = "DISPATCHED";
    public static final String DELIVERED = "DELIVERED";

    // Field constants
    public static final String FIELD_CUSTOMER_NAME = "customer_name";
    public static final String FIELD_CUSTOMER_ADDRESS = "customer_address";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_OID = "oid";
    public static final String FIELD_ITEMS = "items";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_CUSTOMER_CONTACT = "customer_contact";

    @PropertyName(FIELD_CUSTOMER_NAME)
    private String customerName;
    @PropertyName(FIELD_CUSTOMER_ADDRESS)
    private String customerAddress;
    @PropertyName(FIELD_USER_ID)
    private String customerUserId;
    @PropertyName(FIELD_OID)
    private String oid;
    @PropertyName(FIELD_ITEMS)
    private List<BillItem> billItems = new ArrayList<>();
    @PropertyName(FIELD_STATUS)
    private String status;
    @PropertyName(FIELD_TIMESTAMP)
    private long timestamp;
    @PropertyName(FIELD_LOCATION)
    private List<Double> customerLatLng;
    @PropertyName(FIELD_CUSTOMER_CONTACT)
    private String customerContact;

    /**
     * Empty constructor for Firebase
     */
    public Order() {

    }

    public Order(Map<String, String> data) {
        customerUserId = data.get(FIELD_USER_ID);
        customerName = data.get(FIELD_CUSTOMER_NAME);
        customerAddress = data.get(FIELD_CUSTOMER_ADDRESS);
        oid = data.get(FIELD_OID);
        status = data.get(FIELD_STATUS);
        timestamp = Long.parseLong(Objects.requireNonNull(data.get(FIELD_TIMESTAMP)));
        if (data.containsKey(FIELD_CUSTOMER_CONTACT)) {
            customerContact = data.get(FIELD_CUSTOMER_CONTACT);
        }
        try {
            JSONArray items = new JSONArray(data.get(FIELD_ITEMS));
            for (int i = 0; i < items.length(); i++) {
                BillItem billItem = new BillItem(items.getJSONObject(i));
                billItems.add(billItem);
            }
            JSONArray location = new JSONArray(data.get(FIELD_LOCATION));
            customerLatLng = Arrays.asList(location.getDouble(0), location.getDouble(1));
        } catch (JSONException e) {
            Log.e(TAG, "Error while initialising order", e);
        }
    }

    @Exclude
    public Double getOrderTotal() {
        Double total = 0.0;
        for (BillItem billItem : billItems) {
            total += billItem.getTotalPrice();
        }
        return total;
    }

    @Exclude
    public String getTimeElapsedString(long currentTime) {
        long diffInSec = (currentTime - timestamp) / 1000;
        if (diffInSec < 60) {
            return String.format(Locale.getDefault(), "%d seconds ago", diffInSec);
        }
        long diffInMin = diffInSec / 60;
        if (diffInMin < 60) {
            String minutes = diffInMin == 1 ? "minute" : "minutes";
            return String.format(Locale.getDefault(), "%d %s ago", diffInMin, minutes);
        }
        long diffInHour = diffInMin / 60;
        if (diffInHour < 24) {
            String hours = diffInHour == 1 ? "hour" : "hours";
            return String.format(Locale.getDefault(), "%d %s ago", diffInHour, hours);
        }
        return "1 day ago";
    }

    @Exclude
    public String getTimeOfOrder() {
        Date date = new Date(timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        String period = "am";
        if (hours >= 12) {
            period = "pm";
        }
        if (hours == 0)
            hours = 12;
        if (hours > 12)
            hours -= 12;
        int minutes = calendar.get(Calendar.MINUTE);
        return String.format(Locale.getDefault(), "%d:%02d %s", hours, minutes, period);
    }

    @Exclude
    public int getItemCount() {
        int numItems = 0;
        for (BillItem billItem : billItems) {
            numItems += billItem.getQty();
        }
        return numItems;
    }

    public void notifyOrderDispatch() {
        if (!status.equals(Order.ONGOING))
            return;
        status = DISPATCHED;
        FirebaseUtils.updateOrderStatus(oid, DISPATCHED);
        // Consumer side API call to notify order dispatch
        notifyNewOrderStatus(getCustomerUserId(), oid, APIHandler.ConsumerService.DISPATCHED_STATUS_MESSAGE);
    }

    public void notifyOrderDelivered() {
        if (status.equals(Order.DELIVERED))
            return;
        if (status.equals(ONGOING)) {
            notifyOrderDispatch();
        }
        status = DELIVERED;
        FirebaseUtils.updateOrderStatus(oid, DELIVERED);
        // Consumer side API call to notify order delivered
        notifyNewOrderStatus(getCustomerUserId(), oid, APIHandler.ConsumerService.DELIVERED_STATUS_MESSAGE);
    }

    public void decline() {
        FirebaseUtils.updateOrderStatus(oid, DECLINED);
    }

    private void notifyNewOrderStatus(String userID, String orderID, String newStatus) {
        // Notify new order status to consumer side
        APIHandler.ConsumerService service = APIHandler.getConsumerService();
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setUserId(getCustomerUserId());
        orderStatus.setOrderId(oid);
        orderStatus.setStatus(newStatus);
        Call<Void> request = service.notifyOrderStatus(orderStatus);
        request.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    @Exclude
    public LatLng getCustomerLocation() {
        return new LatLng(customerLatLng.get(0), customerLatLng.get(1));
    }

    @PropertyName(FIELD_CUSTOMER_NAME)
    public String getCustomerName() {
        return customerName;
    }

    @PropertyName(FIELD_CUSTOMER_NAME)
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @PropertyName(FIELD_CUSTOMER_ADDRESS)
    public String getCustomerAddress() {
        return customerAddress;
    }

    @PropertyName(FIELD_CUSTOMER_ADDRESS)
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    @PropertyName(FIELD_USER_ID)
    public String getCustomerUserId() {
        return customerUserId;
    }

    @PropertyName(FIELD_USER_ID)
    public void setCustomerUserId(String customerUserId) {
        this.customerUserId = customerUserId;
    }

    @PropertyName(FIELD_OID)
    public String getOid() {
        return oid;
    }

    @PropertyName(FIELD_OID)
    public void setOid(String oid) {
        this.oid = oid;
    }

    @PropertyName(FIELD_ITEMS)
    public List<BillItem> getBillItems() {
        return billItems;
    }

    @PropertyName(FIELD_ITEMS)
    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    @PropertyName(FIELD_STATUS)
    public String getStatus() {
        return status;
    }

    @PropertyName(FIELD_STATUS)
    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName(FIELD_TIMESTAMP)
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName(FIELD_TIMESTAMP)
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName(FIELD_LOCATION)
    public void setCustomerLatLng(List<Double> customerLatLng) {
        this.customerLatLng = customerLatLng;
    }

    @PropertyName(FIELD_LOCATION)
    public List<Double> getCustomerLatLng() {
        return customerLatLng;
    }

    @PropertyName(FIELD_CUSTOMER_CONTACT)
    public String getCustomerContact() {
        return customerContact;
    }

    @PropertyName(FIELD_CUSTOMER_CONTACT)
    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }
}
