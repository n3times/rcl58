/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * The Display view, in charge of rendering the LED display of the calculator.
 */
public class Display extends View {
    private static final String LOG_TAG = "Display";

    private static final int TEXT_COLOR = Color.RED;
    private static final int BG_COLOR   = Color.BLACK;
    private static final float STROKE_WIDTH_DP  = 2.25f;
    private static final float DIGIT_INTERSPACE = 0.8f;
    private static final float TEXT_MARGIN      = 1.0f;

    private static final int DIGIT_COUNT = 12;

    private String digits;
    private int dotPosition;

    private Paint bgPaint;
    private Paint textPaint;

    private Path[] digitPaths;

    /**
     * Constructs a Display view with the display set to "0".
     */
    public Display(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Work around an Android bug
        ViewUtils.disableHardwareAcceleration(this);

        float density = ViewUtils.getDensity(context);

        bgPaint = ViewUtils.makeColorPaint(BG_COLOR);
        textPaint = ViewUtils.makeStrokePaint(TEXT_COLOR, Paint.Style.FILL_AND_STROKE, STROKE_WIDTH_DP * density);

        setDisplay("          0 ", -1);
    }

    /**
     * For every digit on the display we compute its path, appropriately scaled to the
     * screen of the device.
     *
     * <p>The path is a description of all the lines that make the digit within the display:
     * it's not just the shape, and dimensions of the digit but also its position within
     * the display.
     *
     * <p>From the class Digits we retrieve a canonical path for a given digit, that is
     * a path that hasn't been scaled or translated yet. We transform that canonical path
     * so the digit is scaled and placed at the appropriate location of the display.
     */
    private Path[] getDigitPaths() {
        if (digitPaths != null) {
            return digitPaths;
        }

        digitPaths = new Path[DIGIT_COUNT];

        // Determine all the canonical dimensions, where the unit is the height of a digit
        final float digitCanonicalH = 1.0f;
        final float digitCanonicalW = Digits.getMaxWidth();
        final float textCanonicalW =
                DIGIT_COUNT * digitCanonicalW + (DIGIT_COUNT - 1) * DIGIT_INTERSPACE;
        final float displayCanonicalW = textCanonicalW + 2 * TEXT_MARGIN;
        final float scale = getWidth() / displayCanonicalW;
        final float displayCanonicalH = getHeight() / scale;

        // Translate and scale
        for (int i = 0; i < digits.length(); i++) {
            Matrix matrix = new Matrix();

            // The digit will be moved horizontally by an amount that depends on its position (i)
            // Center the digit vertically
            matrix.postTranslate(
                    TEXT_MARGIN + i * (digitCanonicalW + DIGIT_INTERSPACE),
                    (displayCanonicalH - digitCanonicalH) / 2);

            // Scale up the digit by the multiplier
            matrix.postScale(scale, scale);

            // digitPaths[i] is the path of the the ith digit, computed from a canonical
            // digit path that is transformed by the appropriate matrix
            digitPaths[i] = new Path();
            Digits.getDigit(digits.charAt(i), dotPosition == i).transform(matrix, digitPaths[i]);
        }

        return digitPaths;
    }

    /**
     * Sets the content of the display
     *
     * @param digits a String of 12 characters. Values are '0'-'9', '-', '[' or 'E'
     * @param dotPosition either -1 (no dot) or a number between 1 and 12
     */
    public void setDisplay(String digits, int dotPosition) {
        if (dotPosition != this.dotPosition || !digits.equals(this.digits)) {
            this.digits = digits;
            this.dotPosition = dotPosition;
            digitPaths = null;
            this.invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        digitPaths = null;
        this.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Rect rect = ViewUtils.getMeasures(widthMeasureSpec, heightMeasureSpec);

        int w = rect.width();
        int h = rect.height();

        // We expect to be given the width and the height
        if (w == 0 || h == 0) {
            Log.w(LOG_TAG, "Unexpected measure value");
        }

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPaint(bgPaint);

        digitPaths = getDigitPaths();
        for (Path path : digitPaths) {
            canvas.drawPath(path, textPaint);
        }
    }
}
