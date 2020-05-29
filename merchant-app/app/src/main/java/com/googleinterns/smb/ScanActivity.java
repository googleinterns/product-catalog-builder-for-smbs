package com.googleinterns.smb;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.googleinterns.smb.common.CameraSource;
import com.googleinterns.smb.common.CameraSourcePreview;
import com.googleinterns.smb.common.GraphicOverlay;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.common.preference.SettingsActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract class for implementing basic scan functionality. Extend this class to implement specific scan type.
 */
public abstract class ScanActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    protected static final int PERMISSION_REQUESTS = 1;
    protected static final String TAG = ScanActivity.class.getName();
    protected CameraSource cameraSource = null;
    protected GraphicOverlay fireFaceOverlay;
    protected CameraSourcePreview firePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        // set content view and init child views
        initViews();

        // initialise camera preview views
        fireFaceOverlay = findViewById(R.id.fireFaceOverlay);
        firePreview = findViewById(R.id.firePreview);

        // get camera and read/write permissions
        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    abstract protected void initViews();

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, fireFaceOverlay);
        }
        Log.i(TAG, "Using Image Label Detector Processor");
        try {
            setDetector();
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor", e);
            UIUtils.showToast(this, "Can not create image processor");
        }
    }

    abstract protected void setDetector();

    /**
     * Add settings option in the menu bar
     *
     * @param menu setting menu
     * @return added status
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.live_preview_menu, menu);
        return true;
    }


    /**
     * Called when settings option is selected from menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.help) {
            View view = findViewById(R.id.help_layout);
            view.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (firePreview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (fireFaceOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                firePreview.start(cameraSource, fireFaceOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        firePreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    /**
     * Finish scanning process
     */
    public void finishScan(View view) {
        firePreview.stop();
        if (cameraSource != null) {
            cameraSource.release();
        }
        // retrieve data and transition to next activity
        createIntent();
    }

    /**
     * Create intent for next activity after scanning is finished
     */
    abstract protected void createIntent();

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    public void onHelpDismiss(View view) {
        view.setVisibility(View.GONE);
    }
}
