/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.util;

import static android.os.VibrationEffect.createPredefined;
import static com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.MainThreadInitializedObject;

/**
 * Wrapper around {@link Vibrator} to easily perform haptic feedback where necessary.
 */
@TargetApi(Build.VERSION_CODES.Q)
public class VibratorWrapper {

    public static final MainThreadInitializedObject<VibratorWrapper> INSTANCE =
            new MainThreadInitializedObject<>(VibratorWrapper::new);

    public static final AudioAttributes VIBRATION_ATTRS = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

    public static final VibrationEffect EFFECT_TEXTURE_TICK =
            createPredefined(VibrationEffect.EFFECT_TEXTURE_TICK);

    public static final VibrationEffect EFFECT_TICK =
            createPredefined(VibrationEffect.EFFECT_TICK);

    public static final VibrationEffect EFFECT_CLICK =
            createPredefined(VibrationEffect.EFFECT_CLICK);

    public static final VibrationEffect EFFECT_HEAVY_CLICK =
            createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK);

    private final Vibrator mVibrator;
    private final boolean mHasVibrator;

    private boolean mIsHapticFeedbackEnabled;

    public VibratorWrapper(Context context) {
        mVibrator = context.getSystemService(Vibrator.class);
        mHasVibrator = mVibrator.hasVibrator();
        int vibIntensity = Utilities.getVibrationIntensity(context);
        mIsHapticFeedbackEnabled = vibIntensity != 0;
    }

    public static VibrationEffect getVibrationIntensity(Context context) {
        VibrationEffect effect;
        int vibIntensity = Utilities.getVibrationIntensity(context);
        switch (vibIntensity) {
            case 1:
                effect = EFFECT_TEXTURE_TICK;
                break;
            case 2: 
                effect = EFFECT_TICK;
                break;
            case 3:
                effect = EFFECT_CLICK;
                break;
            case 4:
                effect = EFFECT_HEAVY_CLICK;
                break;
            default:
                effect = EFFECT_TICK;
                break;
        }
        
        return effect;
    }

    /** Vibrates with the given effect if haptic feedback is available and enabled. */
    public void vibrate(VibrationEffect vibrationEffect) {
        if (mHasVibrator && mIsHapticFeedbackEnabled) {
            UI_HELPER_EXECUTOR.execute(() -> mVibrator.vibrate(vibrationEffect, VIBRATION_ATTRS));
        }
    }

    /**
     * Vibrates with a single primitive, if supported, or use a fallback effect instead. This only
     * vibrates if haptic feedback is available and enabled.
     */
    @SuppressLint("NewApi")
    public void vibrate(int primitiveId, float primitiveScale, VibrationEffect fallbackEffect) {
        if (mHasVibrator && mIsHapticFeedbackEnabled) {
            UI_HELPER_EXECUTOR.execute(() -> {
                if (Utilities.ATLEAST_R && primitiveId >= 0
                        && mVibrator.areAllPrimitivesSupported(primitiveId)) {
                    mVibrator.vibrate(VibrationEffect.startComposition()
                            .addPrimitive(primitiveId, primitiveScale)
                            .compose(), VIBRATION_ATTRS);
                } else {
                    mVibrator.vibrate(fallbackEffect, VIBRATION_ATTRS);
                }
            });
        }
    }
}
