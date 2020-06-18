package com.googleinterns.smb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.googleinterns.smb.adapter.BillAdapter;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.fragment.AddDiscountDialogFragment;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Product;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * Billing activity to display scanned bill items.
 */
public class BillingActivity extends AppCompatActivity implements
        FirebaseUtils.BarcodeProductQueryListener,
        AddDiscountDialogFragment.DiscountDialogInterface,
        BillAdapter.QtyChangeListener,
        Merchant.OnDataUpdatedListener,
        Merchant.NewProductsFoundListener {

    private static final String TAG = BillingActivity.class.getName();

    private TextView mTextViewTotalPrice;
    private TextView mTextViewDiscountPrice;
    private TextView mTextViewFinalPrice;
    private double mTotalPrice = 0.0;
    private double mDiscount = 0.0;
    private Merchant merchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        setTitle("Bill");
        // Get current merchant instance
        merchant = Merchant.getInstance();

        // Initialise views
        mTextViewTotalPrice = findViewById(R.id.total_price);
        mTextViewDiscountPrice = findViewById(R.id.discount);
        mTextViewFinalPrice = findViewById(R.id.final_price);
        Button addDiscount = findViewById(R.id.add_discount);
        Button finish = findViewById(R.id.finish);

        // Set onclick listener for discount update
        addDiscount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDiscountDialogFragment addDiscountDialogFragment = new AddDiscountDialogFragment();
                addDiscountDialogFragment.show(getSupportFragmentManager(), "Add discount dialog");
            }
        });
        mTextViewDiscountPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDiscountDialogFragment addDiscountDialogFragment = new AddDiscountDialogFragment();
                addDiscountDialogFragment.show(getSupportFragmentManager(), "Add discount dialog");
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(OngoingOrdersActivity.makeIntent(BillingActivity.this));
            }
        });

        // Retrieve products from scanned barcode EANs
        FirebaseUtils.queryProducts(this, CommonUtils.getBarcodes(getIntent()));
    }


    private void initRecyclerView(List<Product> products) {
        // Initialise recycler view to display bill items
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        BillAdapter billAdapter = new BillAdapter(this, products, getSupportFragmentManager());
        recyclerView.setAdapter(billAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    /**
     * Callback from FirebaseUtils.queryProducts() convert from barcodes to products
     *
     * @param products products corresponding to EANs
     */
    @Override
    public void onQueryComplete(List<Product> products) {
        // Products with updated merchant price, and detect new products if any
        List<Product> updatedProduct = merchant.getUpdatedProducts(this, products);
        initRecyclerView(products);
        View view = findViewById(R.id.progressBar);
        view.setVisibility(View.GONE);
    }

    /**
     * Callback from discount dialog
     *
     * @param discount discount set by user
     */
    @Override
    public void onDiscountSelect(double discount) {
        // Replace add discount button with discount price layout
        View view = findViewById(R.id.add_discount_layout);
        view.setVisibility(View.GONE);
        view = findViewById(R.id.discount_layout);
        view.setVisibility(View.VISIBLE);
        mDiscount = discount;
        updatePriceTextViews();
    }

    private void updatePriceTextViews() {
        Double mFinalPrice = mTotalPrice - mDiscount;
        mTextViewTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", mTotalPrice));
        mTextViewDiscountPrice.setText(String.format(Locale.getDefault(), "- %.2f", mDiscount));
        mTextViewFinalPrice.setText(String.format(Locale.getDefault(), "%.2f", mFinalPrice));
    }

    /**
     * Callback from bill adapter (on change in quantity and hence change in total price)
     *
     * @param newPrice new total price of all items
     */
    @Override
    public void onQtyChange(double newPrice) {
        mTotalPrice = newPrice;
        updatePriceTextViews();
    }

    /**
     * Create intent for starting billing activity
     */
    public static Intent makeIntentFromBarcodes(Context context, List<String> barcodes) {
        return new Intent(context, BillingActivity.class)
                .putExtra(CommonUtils.DETECTED_BARCODES, (Serializable) barcodes);
    }

    /**
     * Callback from merchant.addProducts(), on successful addition of products
     */
    @Override
    public void onDataUpdateSuccess() {
        Snackbar.make(findViewById(R.id.billing_layout), R.string.quick_add_message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Callback from merchant.addProducts(), on database update failure
     */
    @Override
    public void onDataUpdateFailure() {
        Log.e(TAG, "Adding products failed");
    }

    @Override
    public double getTotalPrice() {
        return mTotalPrice;
    }

    @Override
    public void onNewProductsFound(List<Product> newProducts) {
        merchant.addProducts(this, newProducts);
    }
}
