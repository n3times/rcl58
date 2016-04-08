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
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * The view used by CustomListAdapter for each one of the list items.
 *
 * <p>Clients customize this view by calling the appropriate setters.
 *
 * <p>These list items are typically used/created ny a CustomListAdapter.
 */
public class CustomListItem extends View {
    private static final int FONT_SIZE_DP = 20;
    private static final int TEXT_COLOR      = Color.DKGRAY;
    private static final int BG_COLOR        = Color.WHITE;
    private static final int SEPARATOR_COLOR = Color.LTGRAY;

    private String text;

    private RectF bounds;
    private Paint bgPaint;
    private Paint pressedBgPaint;
    private Paint pressedTextPaint;
    private Paint textPaint;
    private Paint separatorPaint;
    private Paint darkGrayPaint;
    private float density;
    private float textY;
    private int separatorHeight;
    private boolean isCentered;
    private float height;
    private Paint iconPaint;
    private Paint pressedIconPaint;
    private Path iconPath;
    private int position;

    /**
     * Constructs a CustomListItem.
     *
     * <p>To customize, use the setters (attrs is ignored).
     */
    public CustomListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        ViewUtils.disableHardwareAcceleration(this);

        density = ViewUtils.getDensity(context);

        bgPaint          = ViewUtils.makeColorPaint(BG_COLOR);
        separatorPaint   = ViewUtils.makeColorPaint(SEPARATOR_COLOR);
        pressedBgPaint   = ViewUtils.makeColorPaint(Colors.NICE_BLUE);
        textPaint        = ViewUtils.makeTextPaint(TEXT_COLOR, Typeface.DEFAULT_BOLD, FONT_SIZE_DP * density);
        pressedTextPaint = ViewUtils.makeTextPaint(Colors.IVORY, Typeface.DEFAULT_BOLD, FONT_SIZE_DP * density);
        darkGrayPaint    = ViewUtils.makeColorPaint(Color.DKGRAY);

        separatorHeight = (int) (density + 0.5f);
    }

    /**
     * Sets the caption/text of the item.
     */
    public void setText(String text) {
        this.text = text;
        this.invalidate();
    }

    /**
     * Determines whether the text should be centered.
     */
    public void setCentered(boolean isCentered) {
        this.isCentered = isCentered;
    }

    /**
     * Determines the height of the item.
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Position of the item within the list.
     *
     * @param position the position of the item within the list, except -1 for the last item
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Determines the color of the icon.
     */
    public void setIconColor(int iconColor) {
        iconPaint = null;
        iconPath = null;

        if (iconColor != -1) {
            iconPaint = new Paint();
            iconPaint.setColor(iconColor);
            iconPaint.setAntiAlias(true);
            pressedIconPaint = new Paint(iconPaint);
            pressedIconPaint.setColor(Colors.IVORY);

            iconPath = new Path();
            Matrix matrix = new Matrix();
            int side = 15;
            int margin = 15;
            matrix.postScale(side * density, side * density);
            matrix.postTranslate((margin + side / 2) * density, 22 * density);
            Shapes.square.transform(matrix, iconPath);
        }
    }

    /**
     * Sets the color that should be used as background when item is pressed.
     */
    public void setPressedBgColor(int pressedBgColor) {
        pressedBgPaint.setColor(pressedBgColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (int) (height * density));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        bounds = new RectF(0, 0, w, h);

        Rect nbounds = new Rect();

        textPaint.getTextBounds("0", 0, 1, nbounds);
        textY = bounds.height() / 2 + nbounds.height() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(bounds, isPressed() ? pressedBgPaint : bgPaint);

        // Draw a separator at the bottom of the item except fpr the last item
        if (!(position == -1 && isPressed())) {{
            canvas.drawRect(bounds.left, bounds.height() - separatorHeight, bounds.right, bounds.height(),
                    separatorPaint);
        }}

        // For the first item, if pressed add a separator so uts color does not merge with
        // the navigation bar
        if (position == 0 && isPressed()) {
            canvas.drawRect(bounds.left, 0, bounds.right, separatorHeight,
                    darkGrayPaint);
        }

        float textX;

        if (isCentered) {
            textX = bounds.width() / 2 - textPaint.measureText(text) / 2;
        } else {
            if (iconPaint != null) {
                textX = 45 * density;
                canvas.drawPath(iconPath, isPressed()  ? pressedIconPaint : iconPaint);
            } else {
                textX = 20 * density;
            }
        }

        canvas.drawText(text, textX, textY, isPressed()  ? pressedTextPaint : textPaint);
    }
}