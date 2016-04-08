/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * View that represents a single line of the listing of the program
 */
public class ListingLine extends View {
    private static final String LOG_TAG = "ListingLine";

    private static final int FONT_SIZE_DP = 22;
    private static final int TEXT_COLOR = Color.BLACK;
    private static final int BG_COLOR = Color.WHITE;
    private static final int INACTIVE_BG_COLOR = Colors.LIGHT_GRAY;
    private static final int SEPARATOR_COLOR = Color.LTGRAY;

    private String text;

    private RectF bounds;
    private RectF bgBounds;
    private Paint bgPaint;
    private Paint blackPaint;
    private Paint inactiveBgPaint;
    private Paint textPaint;
    private Paint separatorPaint;
    private float density;
    private float textY;
    private boolean active = true;
    private int separatorHeight;

    /**
     * Constructs a line of the listing.
     */
    public ListingLine(Context context, AttributeSet attrs) {
        super(context, attrs);

        density = ViewUtils.getDensity(context);

        bgPaint         = ViewUtils.makeColorPaint(BG_COLOR);
        blackPaint      = ViewUtils.makeColorPaint(Color.BLACK);
        inactiveBgPaint = ViewUtils.makeColorPaint(INACTIVE_BG_COLOR);
        separatorPaint  = ViewUtils.makeColorPaint(SEPARATOR_COLOR);
        textPaint       = ViewUtils.makeTextPaint(TEXT_COLOR, Typeface.MONOSPACE, FONT_SIZE_DP * density);

        separatorHeight = (int) (density + 0.5f);
    }

    /**
     * Sets the text of the line.
     */
    public void setText(String text) {
        this.text = text;
        this.invalidate();
    }

    /**
     * Indicates whether a line is part of the program.
     *
     * <p>If a program is L lines long, lines 0 to L - 1 are active, and the rest are inactive.
     */
    public void setActive(boolean active) {
        this.active = active;
        this.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Rect rect = ViewUtils.getMeasures(widthMeasureSpec, heightMeasureSpec);

        int w = rect.width();
        int h = rect.height();

        // We expect to be given the width and the width only
        if (w == 0 || h != 0) {
            Log.w(LOG_TAG, "Unexpected measure value");
        } else {
            h = (int) (34 * density);
        }

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        bounds   = new RectF(0, 0, w, h);
        bgBounds = new RectF(0.06f * w, 0, 0.94f * w, h);///

        Rect nbounds0 = new Rect();

        textPaint.getTextBounds("000000000000000000000000000000000000000000", 0, 40, nbounds0);
        bgBounds = new RectF(w / 2 - nbounds0.width() / 2, 0, w / 2 + nbounds0.width() / 2, h);///
        bgBounds = new RectF(0, 0, w, h);///

        Rect nbounds = new Rect();

        textPaint.getTextBounds("0", 0, 1, nbounds);
        textY = bgBounds.height() / 2 + nbounds.height() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(bounds, blackPaint);
        canvas.drawRect(bgBounds, active ? bgPaint : inactiveBgPaint);
        canvas.drawRect(bgBounds.left, bounds.height() - separatorHeight, bgBounds.right, bounds.height(),
                separatorPaint);
        if (text != null) {
            float textX = bounds.width() / 2 - textPaint.measureText(text) / 2;
            canvas.drawText(text, textX, textY, textPaint);
        }
    }
}