package com.googleinterns.smb.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.googleinterns.smb.R;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Utility class to perform notification related functions
 */
public class NotificationUtils {

    /**
     * Utility class should'nt be instantiated
     */
    private NotificationUtils() {
    }

    private static final String TAG = NotificationUtils.class.getName();

    // Notification channel IDs
    // Channel for order notifications
    public static final int ORDER_CHANNEL = 0;
    // Channel for merchant engagement notifications
    public static final int UPDATE_CHANNEL = 1;

    // Channel IDs
    private static final List<String> channelIDs = Arrays.asList(
            "ORDER_CHANNEL",
            "UPDATE_CHANNEL");
    // Information about notification channel
    private static final List<String> channelNames = Arrays.asList(
            "Order Notifications",
            "Update Notifications");
    // Channel descriptions
    private static final List<String> channelDescriptions = Arrays.asList(
            "Customer order notifications",
            "Other updates notifications");

    public static void createNotificationChannel(Context context, int channel) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.e(TAG, "Unable to get notification manager");
            return;
        }
        // Create notification channel for android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelIDs.get(channel),
                    channelNames.get(channel),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription(channelDescriptions.get(channel));
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static void createNotification(Context context, int channel, String title, String text, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.e(TAG, "Unable to get notification manager");
            return;
        }
        Notification notification = new NotificationCompat.Builder(context, channelIDs.get(channel))
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .build();
        int uniqueID = (int) ((new Date().getTime() / 1000L)) % Integer.MAX_VALUE;
        notificationManager.notify(uniqueID, notification);
    }
}
