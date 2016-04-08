/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.n3times.android.rcl58.R;

/**
 * A bar with a single button that appears at the bottom of some activities.
 *
 * <p>The action bar can have a single listener.
 */
public class ActionBar extends View {
    private static final String LOG_TAG = "ActionBar";

    private static final int FONT_SIZE_DP = 18;

    private static final int TITLE_COLOR          = Color.WHITE;
    private static final int TITLE_COLOR_FOCUSED  = Color.LTGRAY;
    private static final int TITLE_COLOR_DISABLED = Colors.GRAY;

    private String title;

    private Paint backgroundPaint;
    private Paint titlePaint;
    private Paint titleFocusedPaint;
    private Paint titleDisabledPaint;

    private boolean focused;
    private boolean disabled;

    private Rect titleBounds = new Rect();

    private OnItemClickedListener listener;

    /**
     * Constructs an action bar.
     *
     * <p>Attributes:
     * <p>
     *     <ul>
     *         <li>"captiom": the caption of the button of the action bar</li>
     *         <li>"color": the background color</li>
     *         <li>"disabled": whether the action bar is clickable at start time</li>
     *     </ul>
     *
     */
    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ActionBar,
                0, 0);

        float density = ViewUtils.getDensity(context);

        title    = a.getString(R.styleable.ActionBar_caption);
        disabled = a.getBoolean(R.styleable.ActionBar_disabled, false);
        int bgColor = a.getColor(R.styleable.ActionBar_color, Colors.ONYX);

        backgroundPaint = ViewUtils.makeColorPaint(bgColor);

        Typeface bold = Typeface.DEFAULT_BOLD;
        float fontSize = FONT_SIZE_DP * density;

        titlePaint         = ViewUtils.makeTextPaint(TITLE_COLOR,          bold, fontSize);
        titleFocusedPaint  = ViewUtils.makeTextPaint(TITLE_COLOR_FOCUSED,  bold, fontSize);
        titleDisabledPaint = ViewUtils.makeTextPaint(TITLE_COLOR_DISABLED, bold, fontSize);

        a.recycle();
    }

    /**
     * Sets the title of the button of the action bar.
     */
    public void setTitle(String title) {
        this.title = title;
        this.invalidate();
    }

    /**
     * Enables or disables the button in the action bar.
     */
    public void setEnabled(boolean enabled) {
        this.disabled = !enabled;
        this.invalidate();
    }

    /**
     * The listener of the action.
     *
     * There is only one listener, typically the current (active) activity.
     *
     * @param listener the listener that will get the callback, or null to unregister
     */
    public void setListener(OnItemClickedListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (disabled) {
            return true;
        }

        invalidate();

        focused = false;
        float y = event.getY();
        boolean in = (y >= 0) && (y < getHeight());
        boolean up = event.getAction() == MotionEvent.ACTION_UP;
        focused = in;
        if (in && up) {
            performClick();
            if (listener != null) {
                listener.onItemClicked(OnItemClickedListener.Item.MIDDLE);
            }
        }

        return true;
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

        // Select text paint depending on the state of the action bar
        Paint textPaint = disabled ? titleDisabledPaint : focused ? titleFocusedPaint : titlePaint;

        // Center the text
        float textX = (getWidth() - textPaint.measureText(title)) / 2;
        textPaint.getTextBounds("A", 0, 1, titleBounds);
        float titleH = titleBounds.height();
        float textY = (getHeight() + titleH) / 2;

        // Draw text and background
        canvas.drawPaint(backgroundPaint);
        canvas.drawText(title, textX, textY, textPaint);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (this == changedView && visibility == VISIBLE) {
            focused = false;
        }
    }
}
