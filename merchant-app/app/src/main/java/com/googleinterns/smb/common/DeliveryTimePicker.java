package com.googleinterns.smb.common;

import android.util.Pair;

import com.google.android.material.chip.ChipGroup;
import com.googleinterns.smb.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Delivery time picker for merchant to select estimated delivery time. See {@link com.googleinterns.smb.NewOrderDisplayActivity}
 */
public class DeliveryTimePicker {

    // Choice constants, delivery time choices in seconds
    private Map<Integer, Integer> mChoiceChipToSeconds = new HashMap<Integer, Integer>() {
        {
            // Choice chip options
            put(R.id.choice_10mins, 10 * 60);
            put(R.id.choice_15mins, 15 * 60);
            put(R.id.choice_20mins, 20 * 60);
            put(R.id.choice_30mins, 30 * 60);
            put(R.id.choice_45mins, 45 * 60);
            put(R.id.choice_1hour, 60 * 60);
            put(R.id.choice_2hours_and_more, 2 * 60 * 60);
        }
    };

    private ChipGroup mDeliveryTimeChipGroup;
    private int mDeliveryTimeInSeconds;
    private boolean mIsDeliveryTimeSet = false;


    public DeliveryTimePicker(ChipGroup deliveryTimeChipGroup) {
        mDeliveryTimeChipGroup = deliveryTimeChipGroup;
        // Set default delivery time
        deliveryTimeChipGroup.check(R.id.choice_1hour);
        mDeliveryTimeInSeconds = mChoiceChipToSeconds.get(R.id.choice_1hour);
        deliveryTimeChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                mIsDeliveryTimeSet = true;
                mDeliveryTimeInSeconds = mChoiceChipToSeconds.get(checkedId);
            }
        });
    }

    public int getDeliveryTimeInSeconds() {
        return mDeliveryTimeInSeconds;
    }

    /**
     * Set default delivery time using travel time information from Directions API
     */
    public void setDefaultDeliveryTime(int estimatedDeliveryTimeInSeconds) {
        if (mIsDeliveryTimeSet) {
            return;
        }
        List<Pair<Integer, Integer>> chipIdDeliveryTimePairs = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : mChoiceChipToSeconds.entrySet()) {
            Integer deliveryTime = entry.getValue();
            Integer chipId = entry.getKey();
            chipIdDeliveryTimePairs.add(new Pair<>(chipId, deliveryTime));
        }
        Collections.sort(chipIdDeliveryTimePairs, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                return o1.second - o2.second;
            }
        });
        // Find first choice chip with time more than estimated delivery time
        for (int i = 0; i < chipIdDeliveryTimePairs.size(); i++) {
            int chipId = chipIdDeliveryTimePairs.get(i).first;
            int deliveryTime = chipIdDeliveryTimePairs.get(i).second;
            if (estimatedDeliveryTimeInSeconds <= deliveryTime) {
                mDeliveryTimeInSeconds = deliveryTime;
                mDeliveryTimeChipGroup.check(chipId);
                return;
            }
        }
        // Set largest value if no time is within estimated time
        Pair<Integer, Integer> lastPair = chipIdDeliveryTimePairs.get(chipIdDeliveryTimePairs.size() - 1);
        mDeliveryTimeChipGroup.check(lastPair.first);
        mDeliveryTimeInSeconds = lastPair.second;
    }
}
