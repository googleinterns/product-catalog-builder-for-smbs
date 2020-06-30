// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.googleinterns.smb.barcodescanning;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.googleinterns.smb.common.CameraImageGraphic;
import com.googleinterns.smb.common.FrameMetadata;
import com.googleinterns.smb.common.GraphicOverlay;
import com.googleinterns.smb.common.VisionProcessorBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Barcode Detector
 */
public class BarcodeScanningProcessor extends VisionProcessorBase<List<FirebaseVisionBarcode>> {

    private static final String TAG = BarcodeScanningProcessor.class.getName();

    private final FirebaseVisionBarcodeDetector mDetector;
    private Set<String> mDetectedBarcodes = new HashSet<>();
    // Number of iterations to check to confirm reliability of barcode detected
    private static final int DEBOUNCE = 3;
    private int mFrameNumber = 0;
    // Set of barcodes detected in previous frames
    private Set<String>[] mLast = new HashSet[DEBOUNCE];

    public BarcodeScanningProcessor() {
        // Using EAN_13 barcode format
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_EAN_13)
                        .build();
        // Remove 'options' parameter to detect all types of barcode formats. However, this leads to slower processing
        mDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
    }

    @Override
    public void stop() {
        try {
            mDetector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionBarcode>> detectInImage(FirebaseVisionImage image) {
        return mDetector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionBarcode> barcodes,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        Set<String> currentFrameBarcodes = new HashSet<>();
        int currentFrameIdx = mFrameNumber % DEBOUNCE;
        for (int i = 0; i < barcodes.size(); ++i) {
            FirebaseVisionBarcode barcode = barcodes.get(i);
            currentFrameBarcodes.add(barcode.getRawValue());
            boolean exists = true;
            // Check if barcode present in previous frames
            for (int j = 0; j < DEBOUNCE; j++) {
                // Present in current frame so ignore
                if (j == currentFrameIdx)
                    continue;
                if (!mLast[j].contains(barcode.getRawValue()))
                    exists = false;
            }
            // If present in all DEBOUNCE frames, then add to final set. This improves accuracy
            if (exists) {
                String ean = barcode.getRawValue();
                mDetectedBarcodes.add(ean);
                BarcodeGraphic barcodeGraphic = new BarcodeGraphic(graphicOverlay, barcode);
                graphicOverlay.add(barcodeGraphic);
            }
        }
        // Update current idx barcodes
        mLast[currentFrameIdx] = currentFrameBarcodes;
        // Update frame number
        mFrameNumber++;
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }

    public List<String> getDetectedBarCodes() {
        return new ArrayList<>(mDetectedBarcodes);
    }

    public void getFromBitmap(Bitmap bitmap, final BarcodeStatusListener listener) {
        detectInImage(FirebaseVisionImage.fromBitmap(bitmap))
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                List<String> mBarcodes = new ArrayList<>();
                                for (int i = 0; i < barcodes.size(); i++) {
                                    FirebaseVisionBarcode barcode = barcodes.get(i);
                                    mBarcodes.add(barcode.getRawValue());
                                }
                                listener.onSuccess(mBarcodes);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                listener.onFailure(e);
                            }
                        });
    }
}