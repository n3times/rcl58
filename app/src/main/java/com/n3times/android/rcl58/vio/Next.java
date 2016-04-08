/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

/**
 * Indicates when to call the engine next() method.
 */
public class Next {
    /**
     * The type of Next:
     * <p>
     * <ul>
     *     <li>NEXT_IDLE: don't call next()</li>
     *     <li>NEXT_NOW: call next() as soon as possible</li>
     *     <li>NEXT_NO_OP: a non operation</li>
     *     <li>NEXT_DELAY: call next later</li>
     * </ul>
     */
    public enum Type {
        NEXT_IDLE,
        NEXT_NOW,
        NEXT_NO_OP,
        NEXT_DELAY,
    }

    /**
     * Compute the array of types statically for caching reasons.
     */
    private static Type[] nextTypes = Type.values();

    private Type type;
    private int ms;

    /**
     *  Gets the type of Next.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the number of milliseconds after which to call next().
     *
     * <p>Note: this only meaningful for NEXT_DELAY
     */
    public int getMs() {
        return ms;
    }

    /**
     * Constructs a Next object.
     */
    public Next(int type, int ms) {
        this.type = nextTypes[type];
        this.ms = ms;
    }
}
