package com.googleinterns.smb.common;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.googleinterns.smb.R;

/**
 * Utility class for common UI operations
 */
public class UIUtils {

    private static final String TAG = "UIUtils";

    // Constants
    public static final String RUPEE = "\u20b9";

    // Utility class should not be instantiated
    private UIUtils() {

    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showKeyboard(Context context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error: showKeyboard", e);
        }
    }

    public static void closeKeyboard(Context context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error: closeKeyboard", e);
        }
    }

    public static void showNoConnectionMessage(Context context, View view) {
        Snackbar.make(view, R.string.no_internet_connection, Snackbar.LENGTH_SHORT)
                .show();
    }
}
