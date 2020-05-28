package com.googleinterns.smb.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.googleinterns.smb.model.Merchant;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseCloudMessagingService extends FirebaseMessagingService {

    private final static String TAG = "FirebaseService";

    @Override
    public void onNewToken(@NonNull String token) {
        Merchant merchant = Merchant.getInstance();
        merchant.updateToken(token);
        Log.d(TAG, "Service token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
