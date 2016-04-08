/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.utilities;

import android.content.Context;
import android.widget.Toast;

/**
 * Central point to log and debug in a consistent way throughout the application
 */
public class Debug {
    private enum Type {
        NONE, LIGHT, ALL
    }

    public static Type type = Type.LIGHT;

    /**
     * "Toast" a message.
     *
     * <p>The debug level will determine whether the message will arrive to the user.
     */
    public static void toast(Context context, String message, boolean isEssential) {
        if (type == Type.NONE || (type == Type.LIGHT && !isEssential)) {
            return;
        }

        Toast.makeText(context, message, isEssential ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    /**
     * "Toast" a non essential message.
     */
    public static void toast(Context context, String message) {
        Debug.toast(context, message, false);
    }
}
