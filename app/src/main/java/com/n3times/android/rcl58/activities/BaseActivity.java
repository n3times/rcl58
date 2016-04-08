/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.n3times.android.rcl58.vio.Vio;

/**
 * Parent of all activities.
 *
 * <p>Handles pausing and resuming engine between transitions. Maintains bookmarks for
 * Help, Library and Settings.
 */
public abstract class BaseActivity extends Activity {
    public static boolean DEBUG_BASE = false;

    /**
     * Used, for debugging purposes, to ensure that there is at most one activity
     * of each type (A constraint that "should" be true in the system).
     */
    private static Set<Class> classes = new HashSet<>();

    private static Class lastHelpClass = HelpHome.class;
    private static Class lastLibClass = LibHome.class;
    private static Class lastSettingsClass = SettingsHome.class;

    /**
     * Bookmark for the last visited Help Activity.
     */
    public static Class lastHelpClass() {
        return lastHelpClass;
    }

    /**
     * Bookmark for the last visited Library Activity.
     */
    public static Class lastLibClass() {
        return lastLibClass;
    }

    /**
     * Bookmark for the last visited Settings Activity.
     */
    public static Class lastSettingsClass() {
        return lastSettingsClass;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG_BASE) {
            // Sanity check
            if (classes.contains(getClass())) {
                throw new AssertionError();
            }

            classes.add(getClass());
        }

        // Incantation to make our app fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (DEBUG_BASE) {
            // Sanity check
            if (!classes.contains(getClass())) {
                throw new AssertionError();
            }

            classes.remove(getClass());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Pause the engine. Saves the state (if it has changed), so it can be
        // restored later if necessary.
        Vio.instance().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Resume the engine.
        Vio.instance().resume();

        // Update bookmarks
        Class thisClass = this.getClass();
        if (thisClass == HelpTopic.class || thisClass == HelpFunctionCatalog.class
                || thisClass == HelpHome.class) {
            lastHelpClass = thisClass;
        } else if (thisClass == LibVolume.class || thisClass == LibProgram.class
                || thisClass == LibHome.class) {
            lastLibClass = thisClass;
        } else if (thisClass == SettingsTopic.class || thisClass == SettingsHome.class) {
            lastSettingsClass = thisClass;
        }
    }

    @Override
    public void onBackPressed() {
        // Do not call super.
        // Activities should handle back presses.
    }
}
