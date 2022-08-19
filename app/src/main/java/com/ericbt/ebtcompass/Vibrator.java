/*
  EBT Compass
  (C) Copyright 2022, Eric Bergman-Terrell

  This file is part of EBT Compass.

    EBT Compass is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBT Compass is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBT Compass.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.ebtcompass;

import android.content.Context;
import android.util.Log;

import com.ericbt.ebtcompass.utils.LocaleUtils;

public class Vibrator {
    private final static long VIBRATE_PAUSE    = 500;
    private final static long VIBRATE_DURATION = 250;

    // One vibration for right
    private final static long[] RIGHT_PATTERN = { 0, VIBRATE_DURATION };

    // Two vibrations for left
    private final static long[] LEFT_PATTERN = { 0, VIBRATE_DURATION, VIBRATE_PAUSE, VIBRATE_DURATION };

    // Five vibrations for arrival
    private final static long[] ARRIVAL_PATTERN = { 0, VIBRATE_DURATION, VIBRATE_PAUSE, VIBRATE_DURATION, VIBRATE_PAUSE, VIBRATE_DURATION, VIBRATE_PAUSE, VIBRATE_DURATION, VIBRATE_PAUSE, VIBRATE_DURATION };

    public static void vibrateForTurn(Turn.Direction direction, Context context) {
        Log.i(StringLiterals.LOG_TAG,
                String.format(LocaleUtils.getLocale(),
                        "vibrateForTurn %s",
                        direction));

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

    public static void vibrateForArrival(Context context) {
        final android.os.Vibrator vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        vibrator.vibrate(ARRIVAL_PATTERN, -1);
    }
}
