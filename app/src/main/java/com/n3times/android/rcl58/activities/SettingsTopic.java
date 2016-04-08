/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.app.Activity;

import com.n3times.android.rcl58.R;

/**
 * Displays text about a topic in the Settings section.
 */
public class SettingsTopic extends TopicActivity {
    @Override
    protected Class<? extends Activity> getPreviousActivity() {
        return SettingsHome.class;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_settings_topic;
    }
}
