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
