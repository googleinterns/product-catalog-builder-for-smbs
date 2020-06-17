package com.googleinterns.smb.common;

import android.os.AsyncTask;
import android.util.Log;

import com.google.common.base.Charsets;
import com.googleinterns.smb.MainActivity;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostRequestTask extends AsyncTask<String, String, String> {

    private static final String TAG = PostRequestTask.class.getName();

    @Override
    protected String doInBackground(String... strings) {
        String urlString = strings[0];
        String postData = strings[1];
        BufferedOutputStream out = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            out = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, Charsets.UTF_8));
            writer.write(postData);
            writer.flush();
            writer.close();
            out.close();

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "ERROR";
            } else {
                return "OK";
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return "ERROR";
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "Post request status: " + s);
        if (s.equals("OK")) {
            UIUtils.showToast(MainActivity.getContext(), "Accepted order, you will be notified once order is confirmed");
        } else {
            UIUtils.showToast(MainActivity.getContext(), "Error occurred while notifying customer");
        }
    }
}
