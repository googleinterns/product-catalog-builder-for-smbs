package com.googleinterns.smb.common;

import android.os.AsyncTask;

import com.google.common.base.Charsets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchRequestTask extends AsyncTask<String, String, String> {

    public interface OnFetchCompleteListener {
        void onFetchComplete(String response);
    }

    ;

    private OnFetchCompleteListener listener;

    public FetchRequestTask(OnFetchCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... uri) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(uri[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                return sb.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        listener.onFetchComplete(response);
    }
}
