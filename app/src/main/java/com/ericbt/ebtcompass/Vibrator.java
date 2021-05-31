package com.ericbt.ebtcompass;

import android.content.Context;
import android.util.Log;

import com.ericbt.ebtcompass.utils.LocaleUtils;

public class Vibrator {
    private final static long VIBRATE_PAUSE    = 500;
    private final static long VIBRATE_DURATION = 250;

    // One vibration for right
    private final static long RIGHT_PATTERN[] = { 0, VIBRATE_DURATION };

    // Two vibrations for left
    private final static long LEFT_PATTERN[] = { 0, VIBRATE_DURATION, VIBRATE_PAUSE, VIBRATE_DURATION };

    public static void vibrateForTurn(Turn.Direction direction, Context context) {
        Log.i(StringLiterals.LOG_TAG, String.format(LocaleUtils.getDefaultLocale(), "vibrateForTurn %s", direction));

        final android.os.Vibrator vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        switch (direction) {
            case RIGHT: {
                vibrator.vibrate(RIGHT_PATTERN, -1);
            }
            break;

            case LEFT: {
                vibrator.vibrate(LEFT_PATTERN, -1);
            }
            break;
        }
    }
}
