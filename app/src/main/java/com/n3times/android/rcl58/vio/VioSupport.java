/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

/**
 * Utility methods that the engine provides to clients.
 */
public class VioSupport {
    /**
     * Describes a given printable character.
     *
     * <p>A character can be seen as a matrix of 5 columns by 7 rows.
     * Typically a client will get the whole description of the character
     * (by calling this method 5x7 times) and cache the result.
     *
     * @param c a character that can be printed by the printer
     * @param row the row of the pixel we are inquiring (0 to 6)
     * @param col the column of the pixel we are inquiring (0 to 4)
     * @return true if there is a pixel set at location (row, col)
     */
    public static native boolean hasPixel(char c, int row, int col);
}
