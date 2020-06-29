package com.googleinterns.smb.pojo;

import com.google.gson.annotations.SerializedName;
import com.googleinterns.smb.adapter.OrderDisplayAdapter;
import com.googleinterns.smb.model.BillItem;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO class to send bid request to consumer side API.
 */
public class SendBidRequest {

    private String merchantId;
    private String merchantName;
    private String merchantAddress;
    private Double totalPrice;
    private Double offersAvailed;
    @SerializedName("userId")
    private String userID;
    @SerializedName("orderId")
    private String orderID;
    @SerializedName("itemDetails")
    private List<OrderItem> items;
    private GeoLocation geoLocation;
    private Long deliveryTime;

    public static SendBidRequest createSendBidRequest(Order order, OrderDisplayAdapter adapter) {
        Merchant merchant = Merchant.getInstance();
        SendBidRequest request = new SendBidRequest();
        request.setMerchantId(merchant.getMid());
        request.setMerchantName(merchant.getName());
        // TODO get merchant address
        request.setMerchantAddress("N.A.");
        request.setTotalPrice(adapter.getTotalPrice());
        request.setOffersAvailed(0.0);

        List<OrderItem> items = new ArrayList<>();
        List<BillItem> billItems = adapter.getBillItems();
        List<Boolean> itemAvailabilities = adapter.getItemAvailabilities();
        for (int i = 0; i < billItems.size(); i++) {
            BillItem billItem = billItems.get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setItemName(billItem.getProductName());
            orderItem.setQuantity(billItem.getQty());
            orderItem.setUnitPrice(billItem.getDiscountedPrice());
            orderItem.setAvailable(itemAvailabilities.get(i));
            orderItem.setImageURL(billItem.getImageURL());
            items.add(orderItem);
        }
        request.setItems(items);
        request.setUserID(order.getCustomerUserId());
        request.setOrderID(order.getOid());

        GeoLocation geoLocation = new GeoLocation();
        geoLocation.setLatitude(merchant.getLatLng().latitude);
        geoLocation.setLongitude(merchant.getLatLng().longitude);

        request.setGeoLocation(geoLocation);
        request.setDeliveryTime((long) 60 * 60);
        return request;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantAddress() {
        return merchantAddress;
    }

    public void setMerchantAddress(String merchantAddress) {
        this.merchantAddress = merchantAddress;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getOffersAvailed() {
        return offersAvailed;
    }

    public void setOffersAvailed(Double offersAvailed) {
        this.offersAvailed = offersAvailed;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public Long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public static class OrderItem {

        @SerializedName("merchantItemName")
        private String itemName;
        private int quantity;
        private Double unitPrice;
        private Boolean isAvailable;
        private String imageURL;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Boolean getAvailable() {
            return isAvailable;
        }

        public void setAvailable(Boolean available) {
            isAvailable = available;
        }

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }
    }

    public static class GeoLocation {

        @SerializedName("lat")
        private Double latitude;
        @SerializedName("lng")
        private Double longitude;

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
