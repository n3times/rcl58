/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

/**
 * Interface for objects interested in changes to the internal state of the calculator.
 */
public interface VioListener {
    /**
     * Callback when the state of the calculator has changed.
     *
     * <p>Typically the callee will query the state to see what has changed.
     */
     void stateChanged();
}
