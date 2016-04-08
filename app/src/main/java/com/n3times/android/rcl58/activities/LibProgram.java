/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.n3times.android.rcl58.R;
import com.n3times.android.rcl58.Rcl58App;
import com.n3times.android.rcl58.views.ActionBar;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;
import com.n3times.android.rcl58.vio.Hlp2Html;
import com.n3times.android.rcl58.vio.State;
import com.n3times.android.rcl58.vio.Vio;
import com.n3times.android.rcl58.vio.VioLibrary;
import com.n3times.android.rcl58.vio.VioProgram;
import com.n3times.android.rcl58.vio.VioVolume;

/**
 * Displays the title and the description of a given program.
 */
public class LibProgram extends BaseActivity {
    private boolean isModule;
    private int programIndex;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lib_program);

        Intent intent = getIntent();

        // Get from the intent which program we are dealing with.
        // The program is identified, uniquely, by whether it's in the module (Master Library)
        // or not (Examples Library) and by its index withing the volume.
        programIndex = intent.getIntExtra("index", 0);
        isModule = intent.getBooleanExtra("is_module", false);
        VioVolume volume = isModule ? VioLibrary.instance(this).getMasterVolume()
                : VioLibrary.instance(this).getExamplesVolume();
        final VioProgram program = volume.getProgram(programIndex);

        // Display the help for this program
        webView = (WebView) findViewById(R.id.webView1);
        String html = Hlp2Html.getHtml(this, Rcl58App.ASSET_BASE + "/" + Rcl58App.HELP_DIR + "/"
                + Rcl58App.HELP_CSS_FILENAME, program.getHelpUri());
        webView.loadDataWithBaseURL(Rcl58App.ASSET_BASE + Rcl58App.HELP_DIR, html, Rcl58App.HTML_MIME_TYPE, null, null);

        // Handle navigation bar
        final NavigationBar bar = (NavigationBar) findViewById(R.id.navigationBar1);
        bar.setTitle(program.getName());
        bar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.LEFT) {
                    onBackPressed();
                } else if (item == Item.RIGHT) {
                    Intent intent = new Intent(LibProgram.this, Calculator.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        });

        // Handle action bar
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionBar1);
        setActionBar();
        actionBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.MIDDLE) {
                    if (isModule) {
                        Vio.instance().moduleSelectProgram(programIndex + 1);
                    } else {
                        Vio.instance().loadProgram(program.getSrcUri(), programIndex + 1);
                    }
                    Intent intent = new Intent(LibProgram.this, Calculator.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     *  Sets the action bar that appears at the bottom of the screen
     */
    private void setActionBar() {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionBar1);
        String title;
        boolean enabled;
        State state = Vio.instance().getState();

        if (isModule) { // Master Library
            boolean selected = state.moduleGetSelectedProgram() == programIndex + 1;
            title = selected ? "Selected" : "Select";
            enabled = !selected;
        } else { // Examples Library
            boolean loaded = state.moduleGetSelectedProgram() == 0 && state.programGetId() == programIndex + 1;
            title = loaded ? "Reload" : "Load";
            enabled = true;
        }

        actionBar.setTitle(title);
        actionBar.setEnabled(enabled);
    }

    @Override
    public void onBackPressed() {
        // Not clearing the webView may result in the previous page being displayed
        // for a fraction of a second next time we use the webView
        webView.loadUrl("about:blank");

        Intent intent = new Intent(LibProgram.this, LibVolume.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setActionBar();
    }
}