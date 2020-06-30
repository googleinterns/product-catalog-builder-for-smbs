package com.googleinterns.smb.common.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.googleinterns.smb.R;

/**
 * Hosts the preference fragment to configure settings for a demo activity that specified by the
 * {@link LaunchSource}.
 */
public class CameraSettingsActivity extends AppCompatActivity {

    public static final String EXTRA_LAUNCH_SOURCE = "extra_launch_source";

    /**
     * Specifies where this activity is launched from.
     */
    public enum LaunchSource {
        LIVE_PREVIEW(R.string.pref_screen_title_live_preview, LivePreviewPreferenceFragment.class);

        private final int titleResId;
        private final Class<? extends PreferenceFragment> prefFragmentClass;

        LaunchSource(int titleResId, Class<? extends PreferenceFragment> prefFragmentClass) {
            this.titleResId = titleResId;
            this.prefFragmentClass = prefFragmentClass;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_settings);

        LaunchSource launchSource =
                (LaunchSource) getIntent().getSerializableExtra(EXTRA_LAUNCH_SOURCE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(launchSource.titleResId);
        }

        try {
            getFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.settings_container,
                            launchSource.prefFragmentClass.getDeclaredConstructor().newInstance())
                    .commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
