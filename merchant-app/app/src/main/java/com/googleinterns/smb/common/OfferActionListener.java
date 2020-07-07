package com.googleinterns.smb.common;

public interface OfferActionListener {

    int PRODUCT_OFFER = 1;
    int BRAND_OFFER = 2;

    void onAddOfferSelect(int type, int itemIdx);

    void onEditOfferSelect(int type, int itemIdx, int offerIdx);

    void onDeleteOfferSelect(int type, int itemIdx, int offerIdx);
}
