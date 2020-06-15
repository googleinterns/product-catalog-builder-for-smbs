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
package com.googleinterns.smb.textrecognition;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.googleinterns.smb.common.CameraImageGraphic;
import com.googleinterns.smb.common.FrameMetadata;
import com.googleinterns.smb.common.GraphicOverlay;
import com.googleinterns.smb.common.ProductDatabase;
import com.googleinterns.smb.common.VisionProcessorBase;
import com.googleinterns.smb.model.Product;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Processor for the text recognition demo.
 */
public class TextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "TextRecProc";

    public interface OnProductFoundListener {
        void onProductFound(List<Product> products);
    }

    private final FirebaseVisionTextRecognizer detector;
    private ProductDatabase productDatabase;
    private OnProductFoundListener mListener;

    public TextRecognitionProcessor(OnProductFoundListener listener) {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        productDatabase = new ProductDatabase(FirebaseFirestore.getInstance().collection("products"));
        mListener = listener;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay,
                    originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            String query = blocks.get(i).getText();
            queries.add(query);
            graphicOverlay.add(new TextGraphic(graphicOverlay, blocks.get(i)));
        }
        // has to be done in an async task, call UI thread listener to update bottom sheet. will not affect preview
        List<Product> matchedProducts = productDatabase.fuzzySearch(queries);

        if (matchedProducts.size() > 0) {
            mListener.onProductFound(matchedProducts);
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }
}
