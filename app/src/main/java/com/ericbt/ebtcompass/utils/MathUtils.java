package com.ericbt.ebtcompass.utils;

public class MathUtils {
    final static float ANGLE_360 = 360.0f;

    public static float normalizeAngle(float degrees) {
        float result = degrees;

        while (result >= ANGLE_360) {
            result -= ANGLE_360;
        }

        while (result < 0.0) {
            result += ANGLE_360;
        }

        return result;
    }
}
