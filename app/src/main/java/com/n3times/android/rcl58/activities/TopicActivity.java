/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.n3times.android.rcl58.R;
import com.n3times.android.rcl58.Rcl58App;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;
import com.n3times.android.rcl58.vio.Hlp2Html;

/**
 * Displays text from a URI pointing to a file in "hlp" format.
 */
public abstract class TopicActivity extends BaseActivity {
    private WebView webView;

    /**
     * Returns the activity we want to navigate on back press.
     */
    protected abstract Class<? extends  Activity> getPreviousActivity();

    /**
     * Gets the resource for the layout that this activity uses.
     */
    protected abstract int getLayoutResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResource());

        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.navigationBar1);
        webView = (WebView) findViewById(R.id.webView1);

        // Use the intent to get the data for the navigation bar and the view
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String helpUri = intent.getStringExtra("help_uri");
        navigationBar.setTitle(title);
        String html = Hlp2Html.getHtml(
                this, Rcl58App.ASSET_BASE + "/" + Rcl58App.HELP_DIR + "/" + Rcl58App.HELP_CSS_FILENAME, helpUri);
        webView.loadDataWithBaseURL(
                Rcl58App.ASSET_BASE + "/" + Rcl58App.HELP_DIR, html, Rcl58App.HTML_MIME_TYPE, null, null);

        // Determine where to navigate from the navigation bar
        navigationBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.LEFT) {
                    onBackPressed();
                } else if (item == Item.RIGHT) {
                    Intent intent = new Intent(TopicActivity.this, Calculator.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Not clearing the webView may result in the previous page being displayed
        // for a fraction of a second next time we use the webView
        webView.loadUrl("about:blank");

        // We need to get back to the appropriate screen
        Class backClass = getPreviousActivity();
        Intent intent = new Intent(TopicActivity.this, backClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
