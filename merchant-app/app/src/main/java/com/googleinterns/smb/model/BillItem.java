package com.googleinterns.smb.model;

import android.annotation.SuppressLint;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BillItem extends Product {

    private int qty = 1;

    // empty constructor for firebase
    public BillItem() {

    }

    public BillItem(DocumentSnapshot documentSnapshot) {
        super(documentSnapshot);
    }

    public BillItem(Product product) {
        super(product);
    }

    public static List<BillItem> getBillItems(List<Product> products) {
        List<BillItem> billItems = new ArrayList<>();
        for (Product product : products) {
            billItems.add(new BillItem(product));
        }
        return billItems;
    }

    public int getQty() {
        return qty;
    }

    @SuppressLint("DefaultLocale")
    public String getQtyString() {
        return String.format("%d", qty);
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Double getTotalPrice() {
        return qty * getDiscountedPrice();
    }

    @SuppressLint("DefaultLocale")
    public String getTotalPriceString() {
        return String.format(RUPEE + " %.2f", getTotalPrice());
    }
}
