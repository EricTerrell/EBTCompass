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
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.ericbt.ebtcompass.utils.LocaleUtils;

public class Speaker {
    public static void speakForTurn(Turn.Direction direction, TextToSpeech textToSpeech, Context context) {
        Log.i(StringLiterals.LOG_TAG,
                String.format(
                        LocaleUtils.getLocale(),
                        "speakForTurn %s",
                        direction));

        switch(direction) {
            case LEFT: {
                speakForTurn(R.string.speak_left, textToSpeech, context);
            }
            break;

            case RIGHT: {
                speakForTurn(R.string.speak_right, textToSpeech, context);
            }
            break;
        }
    }

    private static void speakForTurn(int resId, TextToSpeech textToSpeech, Context context) {
        textToSpeech.speak(context.getString(resId), TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public static void speakForArrival(TextToSpeech textToSpeech, Context context) {
        textToSpeech.speak(context.getString(R.string.you_have_arrived), TextToSpeech.QUEUE_FLUSH, null, null);
    }
}
