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

    @PropertyName("customer_name")
    private String customerName;
    @PropertyName("customer_address")
    private String customerAddress;
    @PropertyName("user_id")
    private String customerUserId;
    @PropertyName("oid")
    private String oid;
    @PropertyName("items")
    private List<BillItem> billItems = new ArrayList<>();
    @PropertyName("status")
    private String status;
    @PropertyName("timestamp")
    private long timestamp;
    @PropertyName("location")
    private List<Double> customerLatLng;
    @PropertyName("customer_contact")
    private String customerContact;

    /**
     * Empty constructor for Firebase
     */
    public Order() {

    }

    public Order(Map<String, String> data) {
        customerUserId = data.get("user_id");
        customerName = data.get("customer_name");
        customerAddress = data.get("customer_address");
        oid = data.get("oid");
        status = data.get("status");
        timestamp = Long.parseLong(Objects.requireNonNull(data.get("timestamp")));
        if (data.containsKey("customer_contact")) {
            customerContact = data.get("customer_contact");
        }
        try {
            JSONArray items = new JSONArray(data.get("items"));
            for (int i = 0; i < items.length(); i++) {
                BillItem billItem = new BillItem(items.getJSONObject(i));
                billItems.add(billItem);
            }
            JSONArray location = new JSONArray(data.get("location"));
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

    public LatLng getCustomerLocation() {
        return new LatLng(customerLatLng.get(0), customerLatLng.get(1));
    }

    @PropertyName("customer_name")
    public String getCustomerName() {
        return customerName;
    }

    @PropertyName("customer_name")
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @PropertyName("customer_address")
    public String getCustomerAddress() {
        return customerAddress;
    }

    @PropertyName("customer_address")
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    @PropertyName("user_id")
    public String getCustomerUserId() {
        return customerUserId;
    }

    @PropertyName("user_id")
    public void setCustomerUserId(String customerUserId) {
        this.customerUserId = customerUserId;
    }

    @PropertyName("oid")
    public String getOid() {
        return oid;
    }

    @PropertyName("oid")
    public void setOid(String oid) {
        this.oid = oid;
    }

    @PropertyName("items")
    public List<BillItem> getBillItems() {
        return billItems;
    }

    @PropertyName("items")
    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("location")
    public void setCustomerLatLng(List<Double> customerLatLng) {
        this.customerLatLng = customerLatLng;
    }

    @PropertyName("location")
    public List<Double> getCustomerLatLng() {
        return customerLatLng;
    }

    @PropertyName("customer_contact")
    public String getCustomerContact() {
        return customerContact;
    }

    @PropertyName("customer_contact")
    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }
}
