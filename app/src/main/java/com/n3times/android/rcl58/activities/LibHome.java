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
import com.n3times.android.rcl58.views.Colors;
import com.n3times.android.rcl58.views.CustomListAdapter;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;
import com.n3times.android.rcl58.vio.VioLibrary;
import com.n3times.android.rcl58.vio.VioVolume;

/**
 * Starting point of the Library section.
 */
public class LibHome extends BaseActivity {
    private CustomListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lib_home);

        // Set the look and feel of the list view as well as its model
        ListView listView = (ListView) findViewById(R.id.listView1);

        listView.setDivider(null);
        listView.setDividerHeight(0);

        final VioVolume[] modules = {
                VioLibrary.instance(this).getMasterVolume(),
                VioLibrary.instance(this).getExamplesVolume()
        };

        listAdapter = new CustomListAdapter(new CustomListAdapter.Data() {
            protected int getCount() {
                return modules.length;
            }

            protected String getText(int position) {
                return modules[position].getName();
            }

            protected int getIconColor(int position) {
                return position == 0 ? Colors.NICE_BLUE_LT : Colors.NICE_BLUE_DK;
            }
        });

        listView.setAdapter(listAdapter);

        // Determine where to navigate from the navigation bar
        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.navigationBar1);
        navigationBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.RIGHT) {
                    Intent intent = new Intent(LibHome.this, Calculator.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        });

        // Determine where to navigate from the list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                String title = modules[position].getName();
                Intent intent = new Intent(LibHome.this, LibVolume.class);
                intent.putExtra("is_module", position == 0);
                intent.putExtra("title", title);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Calculator.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        listAdapter.notifyDataSetChanged();
    }
}
