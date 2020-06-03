package com.googleinterns.smb.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.googleinterns.smb.OrderDisplayActivity;
import com.googleinterns.smb.R;
import com.googleinterns.smb.model.Merchant;

import org.json.JSONException;
import org.json.JSONObject;


public class FirebaseCloudMessagingService extends FirebaseMessagingService {

    private static final String TAG = FirebaseCloudMessagingService.class.getName();
    private static final String PLACE_ORDER = "PLACE_ORDER";

    // Notification channel IDs
    // Channel for order notifications
    private static final String ORDER_CHANNEL = "ORDER_CHANNEL";
    // Channel for merchant engagement notifications
    private static final String UPDATE_CHANNEL = "UPDATE_CHANNEL";

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
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            JSONObject data = new JSONObject(remoteMessage.getData());
            try {
                if (PLACE_ORDER.equals(data.getString("type"))) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager == null) {
                        Log.e(TAG, "Unable to get notification manager");
                        return;
                    }
                    // Create notification channel for android Oreo and above
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(ORDER_CHANNEL, "Order Notifications", NotificationManager.IMPORTANCE_HIGH);
                        notificationChannel.enableLights(true);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setDescription("Customer order notifications");
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                    Intent intent = new Intent(this, OrderDisplayActivity.class);
                    intent.putExtra("items", remoteMessage.getData().get("items"));
                    intent.putExtra("data", data.toString());
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, START_ORDER_DISPLAY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notification = new NotificationCompat.Builder(this, ORDER_CHANNEL)
                            .setContentTitle("New order is available")
                            .setContentText("Tap to view")
                            .setSmallIcon(R.drawable.ic_android_black_24dp)
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setAutoCancel(true)
                            .build();
                    notificationManager.notify(1, notification);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parse exception", e);
            }
        }
    }
}
