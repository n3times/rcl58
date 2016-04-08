/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Allows to read/write user preferences as well as keeping track of whether the state is synced.
 *
 * <p>Preferences are saved across restarts of the application.
 */
public class Prefs {
    public enum KeyFeedback {NONE, SOUND, HAPTIC}
	
	private static final String PREFERENCE_FILE_KEY = "com.n3times.android.rcl59.PREFERENCE_FILE_KEY";

	private static final String KEYBOARD_FEEDBACK   = "keyboard_feedback";
	private static final String STATE_SYNCED        = "use_state_file";
	private static final String VERSION             = "version";
	
	private SharedPreferences prefs;
    private static Prefs singleton;

    /**
     * Returns the singleton instance.
     */
    public static synchronized Prefs instance(Context context) {
        if (singleton == null) {
            singleton = new Prefs(context);
        }

        return singleton;
    }

    private Prefs(Context context) {
        prefs = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
    }

    /**
     * Gets the current version as previously recorded.
     *
     * <p>This version may be smaller that the current version in which case it means
     * that either we have just upgraded, or that we have a fresh install.
     */
	public int getVersion() {
		return prefs.getInt(VERSION, 0);
	}

    /**
     * Sets and saves the current version.
     */
	public void setVersion(int version) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(VERSION, version);
		editor.apply();
	}

    /**
     * Sets the new key feedback.
     */
	public void setKeyFeedback(Prefs.KeyFeedback newKeyFeedback) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(KEYBOARD_FEEDBACK, newKeyFeedback.ordinal());
		editor.apply();
	}

    /**
     * Gets the current key feedback (sound, haptic or none).
     */
	public Prefs.KeyFeedback getKeyFeedback() {
        int ret = prefs.getInt(KEYBOARD_FEEDBACK, KeyFeedback.SOUND.ordinal());

        return KeyFeedback.values()[ret];
	}

    /**
     * Notes whether the saved state matches the current state of the calculator.
     */
	public void setStateSynced(boolean stateSynced) {
		SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(STATE_SYNCED, stateSynced);
		editor.apply();
	}

    /**
     * Indicates whether the latest state has been synced, that is saved.
     *
     * <p>Used after a restart, to see if the saved state can be safely used.
     */
	public boolean isStateSynced() {
		return prefs.getBoolean(STATE_SYNCED, false);
	}
}
