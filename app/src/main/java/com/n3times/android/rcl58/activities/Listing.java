/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.n3times.android.rcl58.R;
import com.n3times.android.rcl58.views.ListingLine;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;
import com.n3times.android.rcl58.vio.State;
import com.n3times.android.rcl58.vio.Vio;

/**
 * Displays the steps of the program in memory.
 */
public class Listing extends BaseActivity {
    /**
     * Holds the view and model of the program listing
     */
    static private class ProgramListAdapter extends BaseAdapter implements ListAdapter {
        /**
         * Display all the 1,000 steps, even if some are empty. The UI make it easy to
         * differentiate empty from non empty steps
         */
        @Override
        public int getCount() {
            return Vio.instance().getState().programGetMaxStepCount();
        }

        @Override
        public Object getItem(int position) {
            // Irrelevant value since we will be using the position in getView(.)
            return null;
        }

        @Override
        public long getItemId(int position) {
            // Irrelevant value since we will be using the position in getView(.)
            return 0;
        }

        /**
         * Each view is an instance of ListingLine
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            State state = Vio.instance().getState();

            if (convertView == null) {
                convertView = new ListingLine(parent.getContext(), null);
            }

            ((ListingLine) convertView).setActive(position < state.programGetEffectiveStepCount());

            int step = state.programGetStep(position);
            String stepAsString = state.programGetStepAsString(position);

            int len = stepAsString.length();
            if (len == 1) {
                stepAsString = " " + stepAsString + " ";
            } else if (len == 2) {
                int c = stepAsString.charAt(0);
                if (c >= 0 && c <= 9) {
                    stepAsString = " " + stepAsString;
                } else {
                    stepAsString = stepAsString + " ";
                }
            }

            String text = String.format("   %03d %02d   %s     ", position, step, stepAsString);
            ((ListingLine) convertView).setText(text);

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_listing);

        // Set the look and feel of the list view as well as its model
        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        ProgramListAdapter listAdapter = new ProgramListAdapter();
        listView.setAdapter(listAdapter);

        // Determine where to navigate from the navigation bar
        final NavigationBar bar = (NavigationBar) findViewById(R.id.navigationBar1);
        bar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.RIGHT) {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Let know the engine we are about to request the program steps
        Vio.instance().getState().programSynchronizeSteps();
    }
}
