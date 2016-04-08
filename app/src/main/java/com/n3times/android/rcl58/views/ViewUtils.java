/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.util.DisplayMetrics;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;

import com.n3times.android.rcl58.utilities.Prefs;

/**
 * Utility methods used across the views.
 */
public class ViewUtils {
    static float getDensity(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        windowManager.getDefaultDisplay().getMetrics(metrics);

        return metrics.density;
    }

    /**
     * Makes paint of given color.
     */
    static Paint makeColorPaint(int color) {
        Paint paint = new Paint();

        paint.setColor(color);

        return paint;
    }

    /**
     * Makes paint for text.
     */
    static Paint makeTextPaint(int color, Typeface typeface, float fontSize) {
        Paint paint = new Paint();

        paint.setColor(color);

        if (typeface != null) {
            paint.setTypeface(typeface);
        }

        if (fontSize > 0) {
            paint.setTextSize(fontSize);
        }

        paint.setAntiAlias(true);

        return paint;
    }

    /**
     * Makes paint for painting objects of type Path.
     */
    static Paint makeStrokePaint(int color, Paint.Style style, float width) {
        Paint paint = new Paint();

        paint.setColor(color);
        paint.setStyle(style);
        paint.setStrokeWidth(width);
        paint.setAntiAlias(true);

        return paint;
    }

    /**
     * Plays the sound preferred by the user.
     *
     * @param view necessary for haptic feedback
     * @param context an activity context
     */
    public static void playSound(View view, Context context) {
        Prefs.KeyFeedback keyFeedback = Prefs.instance(context).getKeyFeedback();

        if (keyFeedback == Prefs.KeyFeedback.SOUND) {
            AudioManager audioManager =
                    (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1.0f);
        } else if (keyFeedback == Prefs.KeyFeedback.HAPTIC) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    /**
     * Trims text if necessary to fit within a given space.
     *
     * @param text the text to fit
     * @param width the amount of space we have to fit the text
     * @param paint the paint to use to render the text
     * @return the original text, possibly trimmed
     */
    static String fitText(String text, float width, Paint paint) {
        if (paint.measureText(text) <= width) {
            return text;
        }

        int lo = 0;
        int hi = text.length();
        String newText = "";

        while (hi - lo > 1) {
            int mid = (hi + lo) / 2;
            newText = text.substring(0, mid + 1) + "...";
            if (paint.measureText(newText) <= width) {
                lo = mid;
            } else {
                hi = mid;
            }
        }

        return newText;
    }

    /**
     * Work around for an Android bug (at least for version 3.x) when rendering vector
     * graphics in accelerated mode.
     *
     * <p>This doesn't really affect the performance of our app, because the 2 widgets that
     * are the most graphically intensive (printer at listing) don't use vector graphics
     * and therefore don't call this method.
     */
    static void disableHardwareAcceleration(View view) {
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * Gets the exact width and heights, or upper bounds, or 0 if unspecified.
     *
     * @return Rect rectangle of the form (0, 0, w, h)
     */
    public static Rect getMeasures(int widthMeasureSpec, int heightMeasureSpec) {
        int w = 0;
        int h = 0;

        int wMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int wSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int hMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int hSize = View.MeasureSpec.getSize(heightMeasureSpec);

        if (wMode == View.MeasureSpec.EXACTLY || wMode == View.MeasureSpec.AT_MOST) {
            w = wSize;
        }

        if (hMode == View.MeasureSpec.EXACTLY || hMode == View.MeasureSpec.AT_MOST) {
            h = hSize;
        }

        return new Rect(0, 0, w, h);
    }
}
