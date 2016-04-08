/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.n3times.android.rcl58.R;
import com.n3times.android.rcl58.views.CustomListAdapter;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;
import com.n3times.android.rcl58.vio.VioLibrary;
import com.n3times.android.rcl58.vio.VioVolume;

/**
 * Displays a list of all the programs in a given volume.
 */
public class LibVolume extends BaseActivity {
    private static final String INDEX_KEY     = "index";
    private static final String IS_MODULE_KEY = "is_module";

    private boolean isModule;
    private CustomListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lib_volume);

        Intent intent = getIntent();

        isModule = intent.getBooleanExtra(IS_MODULE_KEY, false);

        final VioVolume volume = isModule ? VioLibrary.instance(this).getMasterVolume()
                : VioLibrary.instance(this).getExamplesVolume();

        // Handle navigation bar
        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.navigationBar1);
        navigationBar.setTitle(volume.getName());
        navigationBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.LEFT) {
                    onBackPressed();
                } else if (item == Item.RIGHT) {
                    Intent intent = new Intent(LibVolume.this, Calculator.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        });

        // Handle list view
        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listAdapter = new CustomListAdapter(new CustomListAdapter.Data() {
            protected int getCount() {
                return volume.getProgramCount();
            }
            protected String getText(int position) {
                return (isModule ? (position < 9 ? "0" : "") + (position + 1) + " - ": "") + volume.getProgram(position).getName();
            }
        });
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                Intent intent = new Intent(LibVolume.this, LibProgram.class);
                intent.putExtra(IS_MODULE_KEY, isModule);
                intent.putExtra(INDEX_KEY, position);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LibHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        listAdapter.notifyDataSetChanged();
    }
}
