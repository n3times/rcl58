/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.graphics.Matrix;
import android.graphics.Path;

/**
 * The digits used by the Display as vector-based paths.
 *
 * <p>A digit is any character that can be on the display.
 * These are the actual digits ('0' .. '9') as well as ' ', '-', '[', 'E'.
 * '[' is the "busy" or "running" character on the left of the display
 * and 'E' is a combination of '[' and '-'
 */
class Digits {
    private static final Path digit[][];
    private static final Path digitBlank[];
    private static final Path digitE[];
    private static final Path digitC[];
    private static final Path digitMinus[];

    private static final float maxWidth;

    // Customization, assuming that the digit is horizontally centered at
    // the origin and that the middle horizontal segment is at y = 0
    private static final float xMin = -0.23f;
    private static final float xMax = +0.23f;
    private static final float yMin = -0.45f;
    private static final float yMax = +0.55f;
    private static final float dpX  = +0.5f;
    private static final float strokeWidth = 0.03f;
    private static final float skew = 0.1f;

    // Each digit is represented by a 7-bit integer telling with segments are on, based on
    // the following diagram:
    //    0
    //  5   1
    //    6
    //  4   2
    //    3
    // For example '1' is represented by (1 << 1) + (1 << 2) = 0x06
    // '-' would be 1 << 6 = 0x40, but we render it using a different logic (see below)
    private static final int[] digitSegments =
            {0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F};
    private static final int cSegments     = 0x39;
    private static final int eSegments     = 0x79;
    private static final int blankSegments = 0x00;

    /**
     * Retrieves the path of given digit.
     * Path starts at (0, 0), left-bottom corner, and is of height 1
     * (except for ' ' and '-')
     */
    public static Path getDigit(char c, boolean dot) {
        Path[] d = null;

        if (c >= '0' && c <= '9') d = digit[c - '0'];
        else if (c == ' ')        d = digitBlank;
        else if (c == '-')        d = digitMinus;
        else if (c == '[')        d = digitC;
        else if (c == 'E')        d = digitE;

        return d != null ? d[dot ? 1 : 0] : null;
    }

    /**
     * Retrieves the maximum width among all the characters.
     *
     * <p>The width includes the slant as well as the dot. It's for example the width of "0."
     */
    public static float getMaxWidth() {
        return maxWidth;
    }

    private static void drawVerticalLine(float x, float yMin, float yMax, float w, Path path) {
        path.addRect(x - w / 2, yMin - w / 2, x + w / 2, yMax + w / 2, Path.Direction.CW);
    }

    private static void drawHorizontalLine(float y, float xMin, float xMax, float w, Path path) {
        path.addRect(xMin - w / 2, y - w / 2, xMax + w / 2, y + w / 2, Path.Direction.CW);
    }

    private static void drawPoint(float x, float y, float w, Path path) {
        path.addRect(x - w / 2, y - 3 * w, x + 3 * w, y + w / 2, Path.Direction.CW);
    }

    private static void skewDigit(Path path) {
        Matrix matrix = new Matrix();

        matrix.setSkew(-skew, 0.0f);
        matrix.postTranslate(-xMin + strokeWidth / 2, -yMin + strokeWidth / 2);
        path.transform(matrix);
    }

    private static Path getDigit(int segments, boolean dp) {
        Path path = new Path();

        if ((segments & 0x01) != 0) drawHorizontalLine(yMin, xMin, xMax, strokeWidth, path);
        if ((segments & 0x02) != 0) drawVerticalLine(  xMax, yMin,    0, strokeWidth, path);
        if ((segments & 0x04) != 0) drawVerticalLine(  xMax,    0, yMax, strokeWidth, path);
        if ((segments & 0x08) != 0) drawHorizontalLine(yMax, xMin, xMax, strokeWidth, path);
        if ((segments & 0x10) != 0) drawVerticalLine(  xMin,    0, yMax, strokeWidth, path);
        if ((segments & 0x20) != 0) drawVerticalLine(  xMin, yMin,    0, strokeWidth, path);
        if ((segments & 0x40) != 0) drawHorizontalLine(   0, xMin, xMax, strokeWidth, path);

        if (dp) {
            drawPoint(dpX, yMax, strokeWidth, path);
        }
        skewDigit(path);

        return path;
    }

    static {
        float maxWidthToDot   = dpX  - xMin + strokeWidth + (strokeWidth * 7 / 2)       * skew;
        float maxWidthToDigit = xMax - xMin + strokeWidth + (yMax - yMin + strokeWidth) * skew;

        maxWidth = Math.max(maxWidthToDot, maxWidthToDigit);
        digit      = new Path[10][2];
        digitBlank = new Path[2];
        digitC     = new Path[2];
        digitE     = new Path[2];
        digitMinus = new Path[2];

        for (int d = 0; d < 10; d++) {
            for (int dp = 0; dp < 2; dp++) {
                digit[d][dp] = getDigit(digitSegments[d], dp != 0);
            }
        }

        for (int i = 0; i <= 1; i++) {
            digitBlank[i] = getDigit(blankSegments, i == 1);
            digitC[i]     = getDigit(cSegments,     i == 1);
            digitE[i]     = getDigit(eSegments,     i == 1);
        }

        // The "minus" case is a little different because we want the segment to be shorter
        digitMinus[0] = new Path();
        digitMinus[1] = new Path();
        drawHorizontalLine(0, xMin + strokeWidth, xMax, strokeWidth, digitMinus[0]);
        drawHorizontalLine(0, xMin + strokeWidth, xMax, strokeWidth, digitMinus[1]);
        drawPoint(dpX, yMax, strokeWidth, digitMinus[1]);
        skewDigit(digitMinus[0]);
        skewDigit(digitMinus[1]);
    }
}
