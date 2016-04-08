/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.n3times.android.rcl58.R;

/**
 * Displays text about a topic in the Help section.
 */
public class HelpTopic extends TopicActivity {
    private boolean from_catalog;

    @Override
    protected Class<? extends Activity> getPreviousActivity() {
        // Go back to the activity we came from
        return from_catalog ? HelpFunctionCatalog.class : HelpHome.class;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_help_topic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note whether we came from the catalog, to handle back presses appropriately
        from_catalog = getIntent().getBooleanExtra("from_catalog", false);
    }
}
