package com.googleinterns.smb.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.googleinterns.smb.NewOrderDisplayActivity;
import com.googleinterns.smb.NewOrdersActivity;
import com.googleinterns.smb.OngoingOrderDisplayActivity;
import com.googleinterns.smb.OngoingOrdersActivity;
import com.googleinterns.smb.common.NotificationUtils;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;

import org.json.JSONException;
import org.json.JSONObject;


public class FirebaseCloudMessagingService extends FirebaseMessagingService {

    private static final String TAG = FirebaseCloudMessagingService.class.getName();

    // Order display request code
    private static final int START_ORDER_DISPLAY = 1;

    @Override
    public void onNewToken(@NonNull String token) {
        Merchant merchant = Merchant.getInstance();
        // Update database with new token
        merchant.updateToken(token);
        Log.d(TAG, "Service token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            JSONObject data = new JSONObject(remoteMessage.getData());
            try {
                if (Order.NEW_ORDER.equals(data.getString("status"))) {
                    Order order = new Order(remoteMessage.getData());
                    NotificationUtils.createNotificationChannel(this, NotificationUtils.ORDER_CHANNEL);
                    Intent parentIntent = new Intent(this, NewOrdersActivity.class);
                    Intent intent = new Intent(this, NewOrderDisplayActivity.class);
                    intent.putExtra("card_new_order", order);
                    Intent[] intents = new Intent[]{parentIntent, intent};
                    PendingIntent pendingIntent = PendingIntent.getActivities(this, START_ORDER_DISPLAY, intents, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationUtils.createNotification(this, NotificationUtils.ORDER_CHANNEL, "New card_new_order received", "Tap to view", pendingIntent);
                } else if (Order.ONGOING.equals(data.getString("status"))) {
                    Order order = new Order(remoteMessage.getData());
                    NotificationUtils.createNotificationChannel(this, NotificationUtils.ORDER_CHANNEL);
                    Intent parentIntent = new Intent(this, OngoingOrdersActivity.class);
                    Intent intent = new Intent(this, OngoingOrderDisplayActivity.class);
                    intent.putExtra("card_new_order", order);
                    Intent[] intents = new Intent[]{parentIntent, intent};
                    PendingIntent pendingIntent = PendingIntent.getActivities(this, START_ORDER_DISPLAY, intents, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationUtils.createNotification(this, NotificationUtils.ORDER_CHANNEL, "Order confirmed", "Customer has confirmed card_new_order, you can start delivery", pendingIntent);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parse exception", e);
            }
        }
    }
}
