/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 *  The view in charge of rendering the keyboard as well as of translating
 *  motion events into key events.
 *
 *  <p>The key events, which indicate which key was pressed or released,
 *  are forwarded to the listener.
 */
public class Keyboard extends ImageView {
    private static final String LOG_TAG = "Keyboard";

    private OnKeyActionListener listener;

    // These values indicate the position of the keys normalized to floating
    // numbers between 0.0f and 1.0f
    // KEY_START_Y and KEY_HEIGHT were found by trial and error
    private static final float KEY_START_Y = 0.02f;
    private static final float KEY_HEIGHT  = (1 - KEY_START_Y) / 9 * 1.02f;
    private static final float KEY_START_X = 0.0f;
    private static final float KEY_WIDTH   = 0.2f;

    // The keyboard is composed of 9 * 5 = 45 keys
    private static final int ROW_COUNT = 9;
    private static final int COL_COUNT = 5;

    /**
     * Constructs a keyboard view.
     *
     * <p>The keyboard is non customizable (attrs are ignored).
     */
    public Keyboard(final Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (listener == null) {
                    return false;
                }

                // Determine which key the user touched
                float x = event.getX() / getWidth();
                float y = event.getY() / getHeight();
                int col = (int) ((x - KEY_START_X) / KEY_WIDTH) + 1;
                col = col < 1 ? 1 : col > COL_COUNT ? COL_COUNT : col;
                int row = (int) ((y - KEY_START_Y) / KEY_HEIGHT) + 1;
                row = row < 1 ? 1 : row > ROW_COUNT ? ROW_COUNT : row;

                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    ViewUtils.playSound(Keyboard.this, context);
                    listener.onKeyPressed(row, col);
                } else if (action == MotionEvent.ACTION_UP) {
                    listener.onKeyReleased(row, col);
                }
                return true;
            }
        });
    }

    @Override
    public boolean isHapticFeedbackEnabled () {
        return true;
    }

    /**
     * Sets the listener that will be called when the user presses the keys.
     */
    public void setListener(OnKeyActionListener listener) {
        this.listener = listener;
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