/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Allows to navigate from the Calculator to other activities
 *
 * <p>Appears at the top of the Calculator Activity when clicking on "menu".
 */
public class CalculatorMenu extends View {
    private static final String LOG_TAG = "Calculator Menu";

    private static final int ITEM_COUNT = 5;

    private static String text[] = {"Program", "Help", "Library", "More", "Printer"};

    private static final int BG_COLOR   = Colors.ONYX;
    private static final int TEXT_COLOR = Colors.IVORY;

    private final Paint bgPaint;
    private final Paint textPaint;
    private final Paint iconPaint;
    private final Paint iconFocusedPaint;

    private final float density;

    private OnItemClickedListener.Item focusedItem = null;

    private Path shapes[] = new Path[ITEM_COUNT];

    private OnItemClickedListener listener;

    /**
     * Constructs a calculator menu.
     *
     * <p>This view has a unique non customizable look and feel (attrs are ignored).
     */
    public CalculatorMenu(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Work around Android bug
        ViewUtils.disableHardwareAcceleration(this);

        density = ViewUtils.getDensity(context);

        // Initialize the different paints
        bgPaint = ViewUtils.makeColorPaint(BG_COLOR);
        textPaint = ViewUtils.makeTextPaint(TEXT_COLOR, Typeface.DEFAULT_BOLD, 14 * density);
        iconPaint = ViewUtils.makeStrokePaint(TEXT_COLOR, Paint.Style.STROKE, 2.0f * density);
        iconFocusedPaint =
                ViewUtils.makeStrokePaint(TEXT_COLOR, Paint.Style.FILL_AND_STROKE, 2.0f * density);
    }

    /**
     * Allows one single listener to register for events from the calculator menu.
     */
    public void setListener(OnItemClickedListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        boolean preventRedraw = false;

        float x = event.getX();
        float y = event.getY();
        boolean inside = x >= 0 && x < getWidth() && y >= 0 && y < getHeight();

        int index = inside ? (int) (x * ITEM_COUNT / getWidth()) : -1;
        OnItemClickedListener.Item touchedItem =
                index != -1 ? OnItemClickedListener.Item.values()[index] : null;
        OnItemClickedListener.Item previousFocusedItem = focusedItem;

        if (inside) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (listener != null) {
                    listener.onItemClicked(touchedItem);

                    // For better feedback to the user, we want the clicked item to stay
                    // highlighted until we leave the activity
                    preventRedraw = true;
                }
                focusedItem = null;
            } else {
                focusedItem = touchedItem;
            }
        } else {
            focusedItem = null;
        }

        if (!preventRedraw && focusedItem != previousFocusedItem) {
            invalidate();
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        Path shapesFrom[] = {
                Shapes.leftArrow, Shapes.info, Shapes.roundedSquare, Shapes.more, Shapes.rightArrow};

        for (int i = 0; i < shapes.length; i++) {
            float side = 17.6f * density;
            Matrix matrix = new Matrix();
            matrix.postScale(side, side);
            matrix.postTranslate(w * (i + 0.5f) / ITEM_COUNT, 20 * density);
            shapes[i] = new Path();
            shapesFrom[i].transform(matrix, shapes[i]);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        for (int i = 0; i < ITEM_COUNT; i++) {
            canvas.drawPath(shapes[i], focusedItem == OnItemClickedListener.Item.values()[i] ? iconFocusedPaint : iconPaint);
            canvas.drawText(text[i], getWidth() * (i + 0.5f) / ITEM_COUNT - textPaint.measureText(text[i]) / 2,
                    57.5f * density, textPaint);
        }
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
}
