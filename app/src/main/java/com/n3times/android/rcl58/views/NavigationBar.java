/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.n3times.android.rcl58.R;

/**
 * The navigation that appears at the top of every activity and allows to move between activities.
 *
 * <p>It's logically composed pf 3 parts: left, middle and right:
 * <p>
 *     <ul>
 *         <li>left and right are used to navigate</li>
 *         <li>the middle part contains a title, potentially clickable</li>
 *     </ul>
 */
public class NavigationBar extends View {
    private static final String LOG_TAG = "NavigationBar";

    private enum Destination {
        NONE, LEFT, RIGHT, HOME
    }

    private int bgHeightDp;

    private Destination leftDestination = Destination.NONE;
    private Destination rightDestination = Destination.NONE;

    private boolean isRightHighlighted;
    private boolean isTitleHighlighted;

    private String title;

    /**
     *  This is either this.title  or a trimmed version that fits.
     */
    private String displayedTitle;

    private float drawHeight;

    private final Paint bgPaint;
    private final Paint textPaint;
    private final Paint textPaintFocused;
    private final Paint textPaintDisabled;
    private final Paint textPaintHighlighted;
    private final Paint iconPaint;
    private final Paint iconPaintFocused;
    private final Paint iconPaintHighlighted;

    private Path leftPath;
    private Path rightPath;
    private float density;
    private float titleY;
    private OnItemClickedListener.Item focusedItem = null;
    private boolean enabled;

    private OnItemClickedListener listener;

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        ViewUtils.disableHardwareAcceleration(this);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.NavigationBar,
                0, 0);

        String value = a.getString(R.styleable.NavigationBar_left);

        if (value == null) {
            leftDestination = Destination.NONE;
        } else if (value.equals("arrow")) {
            leftDestination = Destination.LEFT;
        } else {
            leftDestination = Destination.NONE;
        }

        value = a.getString(R.styleable.NavigationBar_right);

        if (value == null) {
            rightDestination = Destination.NONE;
        } else if (value.equals("arrow")) {
            rightDestination = Destination.RIGHT;
        } else if (value.equals("home")) {
            rightDestination = Destination.HOME;
        } else {
            rightDestination = Destination.NONE;
        }

        bgHeightDp = a.getInt(R.styleable.NavigationBar_bg_height_dp, 44);
        boolean thin = bgHeightDp < 44;
        enabled = a.getBoolean(R.styleable.NavigationBar_enabled, false);
        title = a.getString(R.styleable.NavigationBar_title);

        density = ViewUtils.getDensity(context);

        int bgColor = a.getColor(R.styleable.NavigationBar_bg_color, Colors.ONYX);

        bgPaint = ViewUtils.makeColorPaint(bgColor);

        float font_size = bgHeightDp * (thin ? 0.65f : 0.45f);

        textPaint = ViewUtils.makeTextPaint(Colors.IVORY, Typeface.DEFAULT_BOLD, font_size * density);
        textPaintDisabled  = ViewUtils.makeTextPaint(Colors.IVORY, Typeface.DEFAULT_BOLD, font_size * density);
        textPaintHighlighted = ViewUtils.makeTextPaint(Colors.GOLD, Typeface.DEFAULT_BOLD, font_size * density);
        textPaintFocused = ViewUtils.makeTextPaint(Color.LTGRAY, Typeface.DEFAULT_BOLD, font_size * density);
        iconPaint = ViewUtils.makeStrokePaint(Colors.IVORY, Paint.Style.STROKE, 2.0f * density);
        iconPaintHighlighted = ViewUtils.makeStrokePaint(Colors.GOLD, Paint.Style.STROKE, 2.0f * density);
        iconPaintFocused = ViewUtils.makeStrokePaint(Colors.IVORY, Paint.Style.FILL_AND_STROKE, 2.0f * density);

        a.recycle();
    }

    /**
     * Makes the arrow on the right highlighted.
     */
    public void setHighlightedRightArrow(boolean isRightArrowHighlighted) {
        if (isRightArrowHighlighted != this.isRightHighlighted) {
            this.isRightHighlighted = isRightArrowHighlighted;
            this.invalidate();
        }
    }

    /**
     * Makes the title highlighted.
     */
    public void setHighlightedTitle(boolean isTitleHighlighted) {
        if (isTitleHighlighted != this.isTitleHighlighted) {
            this.isTitleHighlighted = isTitleHighlighted;
            this.invalidate();
        }
    }

    /**
     * The title in the middle of the navigation bar.
     */
    public void setTitle(String title) {
        this.title = title;

        // Let onDraw(.) determine the displayedTitle
        displayedTitle = null;

        this.invalidate();
    }

    /**
     * Sets the single listener that will get callbacks from the navigation bar.
     */
    public void setListener(OnItemClickedListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();
        boolean inside = x >= 0 && x < getWidth() && y >= 0 && y < getHeight();

        OnItemClickedListener.Item touchedItem = null;

        if (inside) {
            int i = (int) (x * 5 / getWidth());

            if (i == 0) {
                touchedItem = OnItemClickedListener.Item.LEFT;
            } else if (i == 4) {
                touchedItem = OnItemClickedListener.Item.RIGHT;
            } else if (enabled) {
                touchedItem = OnItemClickedListener.Item.MIDDLE;
            }
        }

        OnItemClickedListener.Item previousFocusedItem = focusedItem;

        if (inside) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (listener != null) {
                    listener.onItemClicked(touchedItem);
                    // For better feedback to the user, we want the clicked item to stay
                    // highlighted until we leave the activity
                } else {
                    focusedItem = null;
                }
            } else {
                focusedItem = touchedItem;
            }
        } else {
            focusedItem = null;
        }

        if (focusedItem != previousFocusedItem) {
            invalidate();
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        drawHeight = bgHeightDp * density;

        float margin = 0.3f * bgHeightDp * density;
        float side   = 0.4f * bgHeightDp * density;

        if (leftDestination != Destination.NONE) {
            leftPath = new Path();
            Matrix matrix = new Matrix();
            matrix.postScale(side, side);
            matrix.postTranslate(margin + side / 2, drawHeight / 2);
            Shapes.leftArrow.transform(matrix, leftPath);
        }

        if (rightDestination != Destination.NONE) {
            rightPath= new Path();
            Matrix matrix = new Matrix();
            matrix.postScale(side, side);
            matrix.postTranslate(getWidth() - (margin + side / 2), drawHeight / 2);

            if (rightDestination == Destination.RIGHT) {
                Shapes.rightArrow.transform(matrix, rightPath);
            } else if (rightDestination == Destination.HOME) {
                Shapes.circle.transform(matrix, rightPath);
            }
        }

        Rect nbounds = new Rect();

        textPaint.getTextBounds(title, 0, 1, nbounds);
        titleY = drawHeight / 2 + nbounds.height() / 2;

        Paint currentTextPaint = !enabled ? textPaintFocused
                : focusedItem == OnItemClickedListener.Item.MIDDLE ? textPaintFocused : textPaint;

        displayedTitle = ViewUtils.fitText(title, getWidth() * 0.666f, currentTextPaint);
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

        canvas.drawRect(0, 0, getWidth(), drawHeight, bgPaint);

        if (displayedTitle == null) {
            // Trim text
            displayedTitle = ViewUtils.fitText(title, getWidth() * 2.0f / 3.0f, textPaint);

            // Center vertically
            Rect nbounds = new Rect();
            textPaint.getTextBounds(title, 0, title.length(), nbounds);
            titleY = (drawHeight + nbounds.height()) / 2;
        }

        Paint currentTextPaint = !enabled ? textPaintDisabled
                : isTitleHighlighted ? textPaintHighlighted
                : focusedItem == OnItemClickedListener.Item.MIDDLE ? textPaintFocused : textPaint;

        canvas.drawText(displayedTitle, (getWidth() - currentTextPaint.measureText(displayedTitle)) / 2, titleY,
                currentTextPaint);

        if (leftDestination != Destination.NONE) {
            boolean focused = focusedItem == OnItemClickedListener.Item.LEFT;
            Paint paint = focused ? iconPaintFocused : iconPaint;
            canvas.drawPath(leftPath, paint);
        }

        if (rightDestination != Destination.NONE) {
            boolean focused = focusedItem == OnItemClickedListener.Item.RIGHT;
            Paint paint = focused ? iconPaintFocused
                    : isRightHighlighted ? iconPaintHighlighted : iconPaint;
            canvas.drawPath(rightPath, paint);
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        focusedItem = null;
        invalidate();
    }
}
