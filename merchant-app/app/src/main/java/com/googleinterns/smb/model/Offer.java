package com.googleinterns.smb.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.UIUtils;

import java.io.Serializable;
import java.util.Locale;

/**
 * Product offer model
 */
public class Offer implements Serializable {

    // Offer types
    public enum OfferType {
        // Offer in discount percentage
        PERCENTAGE_OFFER,
        // Offer in flat discount in Rs
        FLAT_OFFER
    }

    /**
     * Empty constructor for firebase
     */
    public Offer() {

    }

    private static final String VALID = "Valid";
    private static final String EXPIRED = "Expired";

    @PropertyName("offer_type")
    private OfferType offerType;
    // Must be in [0, 100] for percentage type offer
    @PropertyName("offer_amount")
    private int offerAmount = 0;
    @PropertyName("validity")
    private Long validityEndDate;
    @PropertyName("is_valid_forever")
    private Boolean isValidForever;

    public Offer(OfferType offerType, int offerAmount, Long validityEndDate, Boolean isValidForever) {
        this.offerAmount = offerAmount;
        this.offerType = offerType;
        this.validityEndDate = validityEndDate;
        this.isValidForever = isValidForever;
    }

    @PropertyName("offer_type")
    public OfferType getOfferType() {
        return offerType;
    }

    @PropertyName("offer_type")
    public void setOfferType(OfferType offerType) {
        this.offerType = offerType;
    }

    @PropertyName("offer_amount")
    public int getOfferAmount() {
        return offerAmount;
    }

    @PropertyName("offer_amount")
    public void setOfferAmount(int offerAmount) {
        this.offerAmount = offerAmount;
    }

    @PropertyName("validity")
    public Long getValidityEndDate() {
        return validityEndDate;
    }

    @PropertyName("validity")
    public void setValidityEndDate(Long validityEndDate) {
        this.validityEndDate = validityEndDate;
    }

    @PropertyName("is_valid_forever")
    public Boolean getValidForever() {
        return isValidForever;
    }

    @PropertyName("is_valid_forever")
    public void setValidForever(Boolean validForever) {
        isValidForever = validForever;
    }

    /**
     * Apply offer on given price
     *
     * @param originalPrice Actual price
     * @return Discounted price
     */
    public double applyOffer(double originalPrice) {
        if (offerType == OfferType.FLAT_OFFER) {
            return (originalPrice - offerAmount);
        } else if (offerType == OfferType.PERCENTAGE_OFFER) {
            return (originalPrice * ((double) (100 - offerAmount) / 100));
        }
        return originalPrice;
    }

    @Exclude
    public String getOfferAmountString() {
        if (offerType == OfferType.PERCENTAGE_OFFER) {
            return String.format(Locale.getDefault(), "%d %s", offerAmount, UIUtils.PERCENT);
        } else {
            return String.format(Locale.getDefault(), "%s %d", UIUtils.RUPEE, offerAmount);
        }
    }

    @Exclude
    public String getValidity() {
        if (isValidForever) {
            return UIUtils.NIL_DATE;
        } else {
            return CommonUtils.getFormattedDate(validityEndDate);
        }
    }

    @Exclude
    public String getStatus() {
        if (isValidForever) {
            return VALID;
        } else {
            long today = CommonUtils.getTodayUTCInMillis();
            if (validityEndDate >= today) {
                return VALID;
            } else {
                return EXPIRED;
            }
        }
    }
}
