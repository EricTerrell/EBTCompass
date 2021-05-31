package com.ericbt.ebtcompass.utils;

import java.util.HashMap;
import java.util.Map;

import static android.hardware.SensorManager.*;

public class SensorUtils {
    private static final Map<Integer, String> dictionary = new HashMap<>();

    static {
        dictionary.put(SENSOR_STATUS_ACCURACY_HIGH, "High");
        dictionary.put(SENSOR_STATUS_ACCURACY_MEDIUM, "Medium");
        dictionary.put(SENSOR_STATUS_ACCURACY_LOW, "Low");
        dictionary.put(SENSOR_STATUS_NO_CONTACT, "No Contact");
        dictionary.put(SENSOR_STATUS_UNRELIABLE, "Unreliable");
    }

    public static String getAccuracyText(int accuracy) {
        return dictionary.get(accuracy);
    }

}
