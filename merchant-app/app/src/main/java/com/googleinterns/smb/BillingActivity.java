package com.googleinterns.smb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.BillAdapter;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.fragment.AddDiscountDialogFragment;
import com.googleinterns.smb.model.Product;

import java.io.Serializable;
import java.util.List;

public class BillingActivity extends AppCompatActivity implements FirebaseUtils.OnProductReceivedListener, AddDiscountDialogFragment.OptionSelectListener, BillAdapter.QtyChangeListener {

    private static final String TAG = BillingActivity.class.getName();

    private TextView mTextViewTotalPrice;
    private TextView mTextViewDiscountPrice;
    private TextView mTextViewFinalPrice;
    private Double mTotalPrice = 0.0;
    private int mDiscountPercent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        mTextViewTotalPrice = findViewById(R.id.total_price);
        mTextViewDiscountPrice = findViewById(R.id.discount);
        mTextViewFinalPrice = findViewById(R.id.final_price);
        Button mAddDiscount = findViewById(R.id.add_discount);
        mAddDiscount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDiscountDialogFragment addDiscountDialogFragment = new AddDiscountDialogFragment();
                addDiscountDialogFragment.show(getSupportFragmentManager(), "Add discount dialog");
            }
        });
        FirebaseUtils.queryProducts(this, CommonUtils.getBarcodes(getIntent()));
    }


    private void initRecyclerView(List<Product> products) {
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

    @Override
    public void onProductReceived(List<Product> products) {
        initRecyclerView(products);
    }

    @Override
    public void onDiscountSelect(int percent) {
        // replace add discount button with discount price layout
        View view = findViewById(R.id.add_discount_layout);
        view.setVisibility(View.GONE);
        view = findViewById(R.id.discount_layout);
        view.setVisibility(View.VISIBLE);
        mDiscountPercent = percent;
        updatePriceTextViews();
    }

    @SuppressLint("DefaultLocale")
    private void updatePriceTextViews() {
        Double mDiscountPrice = mTotalPrice * mDiscountPercent / 100;
        Double mFinalPrice = mTotalPrice - mDiscountPrice;
        mTextViewTotalPrice.setText(String.format("%.2f", mTotalPrice));
        mTextViewDiscountPrice.setText(String.format("- %.2f", mDiscountPrice));
        mTextViewFinalPrice.setText(String.format("%.2f", mFinalPrice));
    }

    @Override
    public void onQtyChange(Double newPrice) {
        mTotalPrice = newPrice;
        updatePriceTextViews();
    }

    public static Intent makeIntentFromBarcodes(Context context, List<String> barcodes) {
        return new Intent(context, BillingActivity.class)
                .putExtra(CommonUtils.DETECTED_BARCODES, (Serializable) barcodes);
    }
}
