package com.googleinterns.smb.scan;

import android.content.Intent;
import android.widget.TextView;

import com.google.android.gms.common.internal.service.Common;
import com.googleinterns.smb.BillingActivity;
import com.googleinterns.smb.ConfirmationActivity;
import com.googleinterns.smb.R;
import com.googleinterns.smb.barcodescanning.BarcodeScanningProcessor;
import com.googleinterns.smb.common.CommonUtils;

import java.io.Serializable;

/**
 * Specialisation of {@link ScanActivity} to implement Barcode scanning functionality.
 */
public class ScanBarcodeActivity extends ScanActivity {

    private BarcodeScanningProcessor mDetector;

    @Override
    protected void initViews() {
        setTitle("Scan Barcode");
        setContentView(R.layout.activity_scan_barcode);
        TextView helpText = findViewById(R.id.text_view_help);
        helpText.setText(R.string.point_at_a_barcode_to_scan);
    }

    @Override
    protected void setDetector() {
        // Attach barcode detector to camera source for live preview
        mDetector = new BarcodeScanningProcessor();
        mCameraSource.setMachineLearningFrameProcessor(mDetector);
    }

    @Override
    protected boolean isProductDetected() {
        return !mDetector.getDetectedBarCodes().isEmpty();
    }

    @Override
    protected void transition() {
        if (getIntent().hasExtra(CREATE_BILL)) {
            startActivity(BillingActivity.makeIntentFromBarcodes(this, mDetector.getDetectedBarCodes()));
        } else if (getIntent().hasExtra(SCAN)) {
            Intent data = new Intent();
            data.putExtra(CommonUtils.DETECTED_BARCODES, (Serializable) mDetector.getDetectedBarCodes());
            setResult(RESULT_OK, data);
            finish();
        } else {
            startActivity(ConfirmationActivity.makeIntentFromBarcodes(this, mDetector.getDetectedBarCodes()));
        }
    }
}
