package com.googleinterns.smb.common;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.googleinterns.smb.R;
import com.googleinterns.smb.model.BillItem;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;

import java.util.List;

public class ConsumerAPIUtils {

    private ConsumerAPIUtils() {

    }

    public static JsonObject getSendBidPostJson(Order order, List<BillItem> billItems, List<Boolean> itemAvailabilities, double totalPrice, int deliveryTime) {
        JsonObject data = new JsonObject();
        Merchant merchant = Merchant.getInstance();
        data.addProperty("merchantId", merchant.getMid());
        data.addProperty("merchantName", merchant.getStoreName());
        data.addProperty("merchantAddress", merchant.getAddress());
        data.addProperty("totalPrice", totalPrice);
        data.addProperty("offersAvailed", 0.0);
        JsonArray items = new JsonArray();
        for (int i = 0; i < billItems.size(); i++) {
            BillItem billItem = billItems.get(i);
            JsonObject item = new JsonObject();
            item.addProperty("merchantItemName", billItem.getProductName());
            item.addProperty("quantity", billItem.getQty());
            item.addProperty("unitPrice", billItem.getDiscountedPrice());
            item.addProperty("isAvailable", itemAvailabilities.get(i));
            item.addProperty("imageURL", billItem.getImageURL());
            items.add(item);
        }
        data.add("itemDetails", items);
        data.addProperty("userId", order.getCustomerUserId());
        data.addProperty("orderId", order.getOid());
        JsonObject geoLocation = new JsonObject();
        geoLocation.addProperty("lat", merchant.getLatLng().latitude);
        geoLocation.addProperty("lng", merchant.getLatLng().longitude);
        data.add("geoLocation", geoLocation);
        data.addProperty("deliveryTime", deliveryTime);
        return data;
    }

    public static String getSendBidApiEndpoint(Context context) {
        return String.format("%s%s",
                context.getString(R.string.consumer_side_api),
                context.getString(R.string.send_bid));
    }
}
