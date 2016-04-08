/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Path.Direction;

/**
 * Collection of vector-based paths, centered at the origin and inscribed
 * within a square of side 1 unit.
 *
 * <p>There are used by the NavigationBar.
 */
class Shapes {
    static Path leftArrow;
    static Path rightArrow;
    static Path circle;
    static Path roundedSquare;
    static Path info;
    static Path more;
    static Path square;

    static {
        leftArrow = new Path();
        leftArrow.moveTo( -0.5f, 0);
        leftArrow.rLineTo(    1, -0.5f);
        leftArrow.rLineTo(    0, 1);
        leftArrow.rLineTo(   -1, -0.5f);
        leftArrow.close();

        rightArrow = new Path();
        rightArrow.moveTo( 0.5f,  0);
        rightArrow.rLineTo(  -1,  0.5f);
        rightArrow.rLineTo(   0, -1);
        rightArrow.rLineTo(   1,  0.5f);
        rightArrow.close();

        circle = new Path();
        circle.addCircle(0, 0, 0.5f, Direction.CW);

        RectF rect = new RectF(-0.5f, -0.5f, 0.5f, 0.5f);

        roundedSquare = new Path();
        roundedSquare.addRoundRect(rect, 0.2f, 0.2f, Path.Direction.CW);

        square = new Path();
        square.addRect(rect, Path.Direction.CW);

        // 2 squares, vertically: ":"
        info = new Path();
        info.addRect(-1f/6, -1f/2, 1f/6, -1f/6, Direction.CW);
        info.addRect(-1f/6, 1f/6, 1f/6, 1f/2, Direction.CW);

        // 2 squares, horizontally: ".."
        more = new Path();
        more.addRect(-1f/2, -1f/6, -1f/6, 1f/6, Direction.CW);
        more.addRect(1f/6,  -1f/6,  1f/2, 1f/6, Direction.CW);
    }
}