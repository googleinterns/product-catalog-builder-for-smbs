package com.googleinterns.smb.barcodescanning;

import java.util.List;

public interface BarcodeStatusListener {
    void onSuccess(List<String> barcodes);

    void onFailure(Exception e);
}
