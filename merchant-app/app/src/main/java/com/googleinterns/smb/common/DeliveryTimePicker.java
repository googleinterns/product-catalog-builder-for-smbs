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

public class DeliveryTimePicker {

    // Choice constants, delivery time choices in seconds
    private Map<Integer, Integer> choiceChipToSeconds = new HashMap<Integer, Integer>() {
        {
            put(R.id.choice_10mins, 10 * 60);
            put(R.id.choice_15mins, 15 * 60);
            put(R.id.choice_20mins, 20 * 60);
            put(R.id.choice_30mins, 30 * 60);
            put(R.id.choice_45mins, 45 * 60);
            put(R.id.choice_1hour, 60 * 60);
            put(R.id.choice_2hours_and_more, 2 * 60 * 60);
        }
    };

    private ChipGroup deliveryTimeChipGroup;
    private int deliveryTimeInSeconds;
    private boolean isDeliveryTimeSet = false;


    public DeliveryTimePicker(ChipGroup deliveryTimeChipGroup) {
        this.deliveryTimeChipGroup = deliveryTimeChipGroup;
        // Set default delivery time
        deliveryTimeChipGroup.check(R.id.choice_1hour);
        deliveryTimeInSeconds = choiceChipToSeconds.get(R.id.choice_1hour);
        deliveryTimeChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                isDeliveryTimeSet = true;
                deliveryTimeInSeconds = choiceChipToSeconds.get(checkedId);
            }
        });
    }

    public int getDeliveryTimeInSeconds() {
        return deliveryTimeInSeconds;
    }

    /**
     * Set default delivery time using travel time information from Directions API
     */
    public void setDefaultDeliveryTime(int estimatedDeliveryTimeInSeconds) {
        if (isDeliveryTimeSet) {
            return;
        }
        List<Pair<Integer, Integer>> chipIdDeliveryTimePairs = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : choiceChipToSeconds.entrySet()) {
            Integer deliveryTime = entry.getValue();
            Integer chipId = entry.getKey();
            chipIdDeliveryTimePairs.add(new Pair<Integer, Integer>(chipId, deliveryTime));
        }
        Collections.sort(chipIdDeliveryTimePairs, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                return o1.second - o2.second;
            }
        });
        for (int i = 0; i < chipIdDeliveryTimePairs.size(); i++) {
            int chipId = chipIdDeliveryTimePairs.get(i).first;
            int deliveryTime = chipIdDeliveryTimePairs.get(i).second;
            if (estimatedDeliveryTimeInSeconds <= deliveryTime) {
                deliveryTimeInSeconds = deliveryTime;
                deliveryTimeChipGroup.check(chipId);
                return;
            }
        }
        // Set largest value if no time is within estimated time
        Pair<Integer, Integer> lastPair = chipIdDeliveryTimePairs.get(chipIdDeliveryTimePairs.size() - 1);
        deliveryTimeChipGroup.check(lastPair.first);
        deliveryTimeInSeconds = lastPair.second;
    }
}
