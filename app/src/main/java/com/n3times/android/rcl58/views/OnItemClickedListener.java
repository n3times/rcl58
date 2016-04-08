/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

/**
 * Simple interface that allows listeners to know when an item in
 * the CalculatorMenu, an ActionBar or a NavigationBar has been clicked.
 */
public interface OnItemClickedListener {
    /**
     * The buttons that can be clicked.
     *
     * <p>Note that some widgets may only use a strict subset of these buttons.
     */
    enum Item {
        LEFT, LEFT_MIDDLE, MIDDLE, MIDDLE_RIGHT, RIGHT
    }

    /**
     * The given item has been clicked.
     */
    void onItemClicked(Item item);
}
