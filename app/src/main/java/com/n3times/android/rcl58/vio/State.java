/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

/**
 * The internal state of the calculator.
 *
 * <p>These methods are provided through JNI by the native engine.
 */
public class State {
    /**
     * Pointer to the native implementation.
     */
    long nativeImpl;

    /**
     * Called by the native implementation.
     */
    State(long nativeImpl) {
        this.nativeImpl = nativeImpl;
    }

    /**
     * Gets one of the digits currently on the display.
     *
     * @param index a number between 0 and 12
     * @return the digit at position index
     */
    public native char displayGetChar(int index);

    /**
     * Gets the position of the dot (the decimal point) of the number on the screen.
     *
     * @return the position of the dot (between 0 and 12), or -1 if there is no such dot
     */
    public native int displayGetDotPosition();

    /**
     * Gets the capacity of the calculator in steps (typically 1,000).
     */
    public native int programGetMaxStepCount();

    /**
     * Gets the number of steps actually used.
     */
    public native int programGetEffectiveStepCount();

    /**
     * Gets the step at a given index.
     *
     * @param index the step index (0 to the max step count - 1)
     * @return the step as an integer
     */
    public native int programGetStep(int index);

    /**
     * Gets the step as a human readable string.
     *
     * @param index the step index (0 to the max step count - 1)
     * @return the step as a String
     */
    public native String programGetStepAsString(int index);

    /**
     * Tells the engine to prepare itself to provide the steps
     * in human readable form.
     */
    public native void programSynchronizeSteps();

    /**
     * Gets the number of lines printed by the printer so far.
     *
     * <p>Note that this number can be bigger that the capacity of
     * the printer's circular buffer.
     */
    public native int printerGetAlltimePrintedCount();

    /**
     * Gets the number of lines that the printer remembers.
     *
     * @return the number of lines the printer remembers
     */
    public native int printerGetCircularBufferCapacity();

    /**
     * Gets a character of the printer output.
     *
     * @param charIndex the index of the character within the line (0 to 19)
     * @param lineIndex the index of the line
     * @return the character at the given indices
     */
    public native char printerGetCharAtLine(int charIndex, int lineIndex);

    /**
     * Gets the index of the module program currently selected.
     *
     * @return the program index selected, or 0 if no program selected
     */
    public native int moduleGetSelectedProgram();

    /**
     * Gets the program id that was passed when loading the program.
     *
     * @return the program id used, or 0 if no such program
     */
    public native long programGetId();

    /**
     * Tells whether the user has pressed the "2nd" key (equivalent to a "shift" key).
     *
     * <p>This can be used to give the user appropriate feedback.
     */
    public native boolean isSecondModifier();
}
