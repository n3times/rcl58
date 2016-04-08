/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

/**
 * Interface that listeners interested in getting key
 * events from the Keyboard need to implement.
 */
public interface OnKeyActionListener {
    /**
     * One of the calculator keys have been pressed.
     *
     * @param row between 1 and 9
     * @param col between 1 and 5
     */
    void onKeyPressed(int row, int col);

    /**
     * One of the calculator keys have been released.
     *
     * @param row between 1 and 9
     * @param col between 1 and 5
     */
    void onKeyReleased(int row, int col);
}
