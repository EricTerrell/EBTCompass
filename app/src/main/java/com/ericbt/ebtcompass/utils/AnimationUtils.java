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

package com.ericbt.ebtcompass.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class AnimationUtils {
    private static final float MAX_ALPHA = 1.0f;
    private static final float MIN_ALPHA = 0.25f;
    private static final int ANIMATION_MS = 500;

    public static Animation getFadeOutAnimation() {
        final Animation animation = new AlphaAnimation(MAX_ALPHA, MIN_ALPHA);
        animation.setDuration(ANIMATION_MS);
        animation.setFillAfter(true);

        return animation;
    }

    public static Animation getFadeInAnimation() {
        final Animation animation = new AlphaAnimation(MIN_ALPHA, MAX_ALPHA);
        animation.setDuration(ANIMATION_MS);
        animation.setFillAfter(true);

        return animation;
    }
}
