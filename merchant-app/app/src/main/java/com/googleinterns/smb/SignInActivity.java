package com.googleinterns.smb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.SignInButton;
import com.googleinterns.smb.common.UIUtils;

import java.util.Collections;

public class SignInActivity extends AppCompatActivity {

    private static final int START_SIGN_IN = 1;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        progressBar = findViewById(R.id.progress_bar);
        SignInButton signInButton = findViewById(R.id.sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSignIn();
            }
        });
        // Customize google sign in button
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View view = signInButton.getChildAt(i);
            if (view instanceof TextView) {
                TextView signInTextView = (TextView) view;
                signInTextView.setText(R.string.sign_in_with_google);
            }
        }
    }

    public void onSignIn() {
        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();
        startActivityForResult(intent, START_SIGN_IN);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_SIGN_IN) {
            progressBar.setVisibility(View.INVISIBLE);
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, OngoingOrdersActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
