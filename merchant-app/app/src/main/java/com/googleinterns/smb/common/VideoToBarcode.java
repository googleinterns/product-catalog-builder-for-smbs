package com.googleinterns.smb.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.googleinterns.smb.barcodescanning.BarcodeScanningProcessor;
import com.googleinterns.smb.barcodescanning.BarcodeStatusListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class acts as a cumulative barcode converter task. Reads the video frame by frame and detects from barcode in each frame.
 * For extracting barcode from a bitmap (image frame) it makes use of the BarcodeScanningProcessor.
 * The frames chosen for processing is determined by the FRAMES_PER_SECOND attribute.
 * It determines the number of frames which are processed per second. It implements BarcodeStatusListener as it makes
 * asynchronous requests to BarcodeScanningProcessor.
 */
public class VideoToBarcode extends AsyncTask<Object, Void, List<String>> implements BarcodeStatusListener {

    private static final String TAG = "VideoToBarcode";
    private MediaMetadataRetriever mRetriever;
    private BarcodeStatusListener mListener;
    private BarcodeScanningProcessor mImageProcessor;
    // number of frames to be scanned in one second interval
    private static final int FRAMES_PER_SECOND = 4;
    private Set<String> mBarcodes;
    private ProgressDialog mProgressDialog;

    public VideoToBarcode(Context context, BarcodeStatusListener listener, Uri videoUri, @Nullable ProgressDialog progressDialog) {
        super();
        mListener = listener;
        mImageProcessor = new BarcodeScanningProcessor();
        // initialization for retrieving frames from video
        mRetriever = new MediaMetadataRetriever();
        mRetriever.setDataSource(context, videoUri);
        mBarcodes = new HashSet<>();
        mProgressDialog = progressDialog;
    }

    @Override
    protected List<String> doInBackground(Object... params) {
        String duration = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int durationInSeconds = Integer.parseInt(duration) / 1000;
        int intervalInMicroSeconds = 1000000 / FRAMES_PER_SECOND;
        int numFrames = durationInSeconds * FRAMES_PER_SECOND;
        if (mProgressDialog != null) {
            mProgressDialog.setMax(numFrames);
        }
        for (int i = 0; i < numFrames; i++) {
            Bitmap bitmap = mRetriever.getFrameAtTime(intervalInMicroSeconds * i, MediaMetadataRetriever.OPTION_CLOSEST);
            if (bitmap != null) {
                // async call to image processor. receives callback onSuccess() / onFailure()
                mImageProcessor.getFromBitmap(bitmap, this);
            }
            // update progress dialog if available
            if (mProgressDialog != null) {
                mProgressDialog.setProgress(i + 1);
            }
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        return new ArrayList<>(mBarcodes);
    }

    @Override
    protected void onPostExecute(List<String> barcodes) {
        mListener.onSuccess(barcodes);
    }

    @Override
    public void onSuccess(List<String> barcodes) {
        // add barcodes from the current frame
        mBarcodes.addAll(barcodes);
    }

    @Override
    public void onFailure(Exception e) {
        // ignore frame
    }
}
