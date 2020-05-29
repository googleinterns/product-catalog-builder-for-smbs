package com.googleinterns.smb;

import android.widget.TextView;

import com.googleinterns.smb.barcodescanning.BarcodeScanningProcessor;

public class ScanBarcodeActivity extends ScanActivity {

    private BarcodeScanningProcessor mDetector;

    @Override
    protected void initViews() {
        setTitle("Scan Barcode");
        setContentView(R.layout.activity_scan);
        TextView helpText = findViewById(R.id.help_text);
        helpText.setText(R.string.point_at_a_barcode_to_scan);
    }

    @Override
    protected void setDetector() {
        // attach barcode detector to camera source for live preview
        mDetector = new BarcodeScanningProcessor();
        cameraSource.setMachineLearningFrameProcessor(mDetector);
    }

    @Override
    protected void createIntent() {
        startActivity(ConfirmationActivity.makeIntent(this, mDetector.getDetectedBarCodes()));
    }
}