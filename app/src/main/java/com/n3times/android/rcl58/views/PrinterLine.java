/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.n3times.android.rcl58.vio.VioSupport;

/**
 * Displays a single line of the printer, that is 20 characters.
 *
 * <p>The characters are scaled so they fit as well as possible the screen of the device.
 * To keep the pixely look, the scaling is restricted to integers (instead of floats).
 */
public class PrinterLine extends View {
    /**
     * Type of line.
     *
     * <p>Only lines of type PAPER can be printed on.
     */
    public enum LineType {
        PAPER,
        START_ROLL,
    }

    private static final String LOG_TAG = "PrinterLine";

    private static final int TEXT_COLOR  = Color.BLACK;
    private static final int PAPER_COLOR = Colors.IVORY;
    private static final int BG_COLOR = Colors.DARK_GRAY;

    private static final int CHAR_COUNT = 20;

    // The following values are in pixels (The characters will be scaled appropriately
    // depending on the resolution of the device)
    private static final int CHAR_WIDTH     = 5;
    private static final int CHAR_HEIGHT    = 7;
    private static final int CHAR_X_PADDING = 1;
    private static final int CHAR_Y_PADDING = 2;
    private static final int TEXT_WIDTH     = CHAR_COUNT * (CHAR_WIDTH + 2 * CHAR_X_PADDING);
    private static final int TEXT_HEIGHT    = CHAR_HEIGHT + 2 * CHAR_Y_PADDING;

    private String text;
    private LineType lineType;

    private static Paint bgPaperPaint = new Paint();
    private static Paint bgNoPaperPaint = new Paint();

    static {
        bgPaperPaint.setColor(PAPER_COLOR);
        bgNoPaperPaint.setColor(BG_COLOR);
    }

    private int pixelSize;

    /**
     * We cache the bitmaps of the characters around for performance purposes.
     * The char that represents the character will be the index into the array.
     * Only some (at most 64) of the members of the array will be non null.
     * Note that the cost of storing all this bitmaps is relatively small:
     * bytes = 64 * CHAR_WIDTH * CHAR_HEIGHT * pixelSize * pixelSize * 4
     * That is < 40K for a pixelSize of 2.
     */
    private static Bitmap bitmaps[] = new Bitmap[256];

    /**
     * Creates a new printer line.
     *
     * <p>Note that we ignore the attributes and that clients should always call
     * setText(.) and setLineType(.) from a newly created line, or a recycled line.
     */
    public PrinterLine(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the text to be printed.
     */
    public void setText(String text) {
        this.text = text;
        this.invalidate();
    }

    /**
     * One of PAPER or START_ROLL.
     */
    public void setLineType(LineType lineType) {
        this.lineType = lineType;
        this.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (pixelSize != w / TEXT_WIDTH) {
            // Use the biggest pixel size that will fit the width of the view;
            pixelSize = w / TEXT_WIDTH;

            // Force to recompute the bitmaps
            for (int i = 0; i < bitmaps.length; i++) {
                bitmaps[i] = null;
            }
        }

        this.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Rect rect = ViewUtils.getMeasures(widthMeasureSpec, heightMeasureSpec);

        int w = rect.width();
        int h = rect.height();

        // We expect to be given the width and the height
        if (w == 0 || h != 0) {
            Log.w(LOG_TAG, "Unexpected measure value");
        } else {
            pixelSize = w / TEXT_WIDTH;
            h = TEXT_HEIGHT * pixelSize;
        }

        setMeasuredDimension(w, h);
    }

    /**
     * Creates a bitmap for a given character.
     * The bitmap will be based on a a character 5 pixels wide x 7 pixels high,
     * scale up by the given scale.
     */
    private Bitmap createBitmap(char c, int scale, int paperColor, int textColor) {
        int[] colors = new int[CHAR_WIDTH * scale * CHAR_HEIGHT * scale];

        for (int j = 0; j < colors.length; j++) {
            colors[j] = paperColor;
        }

        for (int row = 0; row < CHAR_HEIGHT; row++) {
            for (int col = 0; col < CHAR_WIDTH; col++) {
                if (VioSupport.hasPixel(c, row, col)) {
                    for (int j = 0; j < scale; j++) {
                        for (int k = 0; k < scale; k++) {
                            int row2 = row * scale + j;
                            int col2 = col * scale + k;
                            colors[row2 * scale * CHAR_WIDTH + col2] = textColor;
                        }
                    }
                }
            }
        }

        return Bitmap.createBitmap(
                colors, CHAR_WIDTH * pixelSize, CHAR_HEIGHT * pixelSize, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (lineType == LineType.START_ROLL) {
            canvas.drawPaint(bgNoPaperPaint);
            canvas.drawRect(0, getHeight() * 2 / 3, getWidth(), getHeight(), bgPaperPaint);
        } else if (lineType == LineType.PAPER) {
            canvas.drawPaint(bgPaperPaint);

            if (text != null && !text.equals("")) {
                // Center the text
                float startTextX = (getWidth()  - pixelSize *  TEXT_WIDTH) / 2;
                float startTextY = (getHeight() - pixelSize * TEXT_HEIGHT) / 2;

                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (c == ' ') continue;
                    if (bitmaps[c] == null) {
                        bitmaps[c] = createBitmap(c, pixelSize, PAPER_COLOR, TEXT_COLOR);
                    }
                    canvas.drawBitmap(bitmaps[c],
                            startTextX + (i * (CHAR_WIDTH + 2 * CHAR_X_PADDING) + CHAR_X_PADDING) * pixelSize,
                            startTextY + CHAR_Y_PADDING * pixelSize, null);
                }
            }
        }
    }
}