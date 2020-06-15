package com.googleinterns.smb.barcodescanning;

import java.util.List;

/**
 * Listener interface to return barcodes after async detection is complete
 */
public interface BarcodeStatusListener {
    void onSuccess(List<String> barcodes);

    void onFailure(Exception e);
}
