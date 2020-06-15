package com.googleinterns.smb.common.preference;

import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import androidx.annotation.StringRes;

import com.googleinterns.smb.R;
import com.googleinterns.smb.common.CameraSource;
import com.googleinterns.smb.common.CameraSource.SizePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configures live preview demo settings.
 */
public class LivePreviewPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_live_preview);
        setUpCameraPreferences();
    }

    private void setUpCameraPreferences() {
        setUpCameraPreviewSizePreference(
                R.string.pref_key_rear_camera_preview_size,
                R.string.pref_key_rear_camera_picture_size,
                CameraSource.CAMERA_FACING_BACK);
    }

    private void setUpCameraPreviewSizePreference(
            @StringRes int previewSizePrefKeyId, @StringRes final int pictureSizePrefKeyId, int cameraId) {
        final ListPreference previewSizePreference =
                (ListPreference) findPreference(getString(previewSizePrefKeyId));

        Camera camera = null;
        try {
            camera = Camera.open(cameraId);

            List<SizePair> previewSizeList = CameraSource.generateValidPreviewSizeList(camera);
            String[] previewSizeStringValues = new String[previewSizeList.size()];
            final Map<String, String> previewToPictureSizeStringMap = new HashMap<>();
            for (int i = 0; i < previewSizeList.size(); i++) {
                SizePair sizePair = previewSizeList.get(i);
                previewSizeStringValues[i] = sizePair.preview.toString();
                if (sizePair.picture != null) {
                    previewToPictureSizeStringMap.put(
                            sizePair.preview.toString(), sizePair.picture.toString());
                }
            }
            previewSizePreference.setEntries(previewSizeStringValues);
            previewSizePreference.setEntryValues(previewSizeStringValues);

            if (previewSizePreference.getEntry() == null) {
                // First time of opening the Settings page.
                SizePair sizePair =
                        CameraSource.selectSizePair(
                                camera,
                                CameraSource.DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH,
                                CameraSource.DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT);
                String previewSizeString = sizePair.preview.toString();
                previewSizePreference.setValue(previewSizeString);
                previewSizePreference.setSummary(previewSizeString);
                PreferenceUtils.saveString(
                        getActivity(),
                        pictureSizePrefKeyId,
                        sizePair.picture != null ? sizePair.picture.toString() : null);
            } else {
                previewSizePreference.setSummary(previewSizePreference.getEntry());
            }

            previewSizePreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            String newPreviewSizeStringValue = (String) newValue;
                            previewSizePreference.setSummary(newPreviewSizeStringValue);
                            PreferenceUtils.saveString(
                                    LivePreviewPreferenceFragment.this.getActivity(),
                                    pictureSizePrefKeyId,
                                    previewToPictureSizeStringMap.get(newPreviewSizeStringValue));
                            return true;
                        }
                    });

        } catch (Exception e) {
            // If there's no camera for the given camera id, hide the corresponding preference.
            ((PreferenceCategory) findPreference(getString(R.string.pref_category_key_camera)))
                    .removePreference(previewSizePreference);
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
    }
}
