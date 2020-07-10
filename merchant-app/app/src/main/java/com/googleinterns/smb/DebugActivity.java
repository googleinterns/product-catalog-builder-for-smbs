package com.googleinterns.smb;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Merchant;

/**
 * Debug Activity for internal use only. Displays merchant mid
 */
public class DebugActivity extends MainActivity {

    private String mMid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.debug_tools));
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_debug, null, false);
        mContainer.addView(contentView, 0);
        Merchant merchant = Merchant.getInstance();
        mMid = merchant.getMid();
        TextView midTextView = contentView.findViewById(R.id.text_view_mid);
        midTextView.setText(mMid);
    }

    /**
     * onClick for copy to clipboard button
     */
    public void copyToClipBoard(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("mid", mMid);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        UIUtils.showToast(this, "Copied to clipboard");
    }
}
