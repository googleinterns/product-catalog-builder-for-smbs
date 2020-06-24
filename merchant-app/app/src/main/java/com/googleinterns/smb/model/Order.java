package com.googleinterns.smb.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
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

    private String customerName;
    private String customerAddress;
    private String customerUserId;
    private String oid;
    private List<BillItem> billItems = new ArrayList<>();
    private String status;
    private long timestamp;
    private List<Double> customerLatLng;

    public Order() {

    }

    public Order(Map<String, Object> data) {
        customerUserId = (String) data.get("user_id");
        customerName = (String) data.get("customer_name");
        customerAddress = (String) data.get("customer_address");
        oid = (String) data.get("oid");
        status = (String) data.get("status");
        timestamp = (long) data.get("timestamp");
        customerLatLng = (List<Double>) data.get("location");
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        for (Map<String, Object> item : items) {
            BillItem billItem = new BillItem(item);
            billItems.add(billItem);
        }
    }

    public Order(Map<String, String> data, boolean remoteMessage) {
        customerUserId = data.get("user_id");
        customerName = data.get("customer_name");
        customerAddress = data.get("customer_address");
        oid = data.get("oid");
        status = data.get("status");
        timestamp = Long.parseLong(data.get("timestamp"));
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

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Double getOrderTotal() {
        Double total = 0.0;
        for (BillItem billItem : billItems) {
            total += billItem.getTotalPrice();
        }
        return total;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

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

    public int getItemCount() {
        int numItems = 0;
        for (BillItem billItem : billItems) {
            numItems += billItem.getQty();
        }
        return numItems;
    }

    public LatLng getCustomerLatLng() {
        LatLng latLng = new LatLng(customerLatLng.get(0), customerLatLng.get(1));
        return latLng;
    }

    public String getCustomerUserId() {
        return customerUserId;
    }

    public String getOid() {
        return oid;
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

    public String getStatus() {
        return status;
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
                // TODO retry
            }
        });
    }
}
