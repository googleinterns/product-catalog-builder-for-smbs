package com.googleinterns.smb.pojo;

/**
 * POJO class for modelling OrderStatus request. See {@link com.googleinterns.smb.common.APIHandler}
 */
public class OrderStatus {

    private String userId;
    private String orderId;
    private String status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
