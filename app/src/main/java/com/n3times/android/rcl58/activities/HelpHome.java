/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.n3times.android.rcl58.R;
import com.n3times.android.rcl58.views.Colors;
import com.n3times.android.rcl58.views.CustomListAdapter;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;

/**
 * Starting point of the Help section.
 */
public class HelpHome extends BaseActivity {
    /**
     * The help caption and the name of help file for each section.
     */
    private static final String[][] data = {
            {"About", "about"},
            {"Scientific Calculator", "basics"},
            {"Hi-Lo Game", "hilo"},
            {"Hello World", "hello"},
            {"Flow Control", "flow"},
            {"Writing Help Files", "help"},
            {"Function Catalog", null}
    };

    private CustomListAdapter customListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help_home);

        // Set the look and feel of the list view as well as its model
        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setDivider(null);
        listView.setDividerHeight(0);

        customListAdapter = new CustomListAdapter(new CustomListAdapter.Data() {
            @Override
            protected String getText(int position) {
                return data[position][0];
            }

            @Override
            protected int getCount() {
                return data.length;
            }

            @Override
            protected int getPressedBgColor(int position) {
                return Colors.NICE_GREEN;
            }

            protected int getIconColor(int position) {
                int n = getCount();
                int firstGreen = 230;
                int lastGreen = 100;
                int green = firstGreen + (lastGreen - firstGreen) * position / (n - 1);

                return Color.rgb(green * 2 / 3, green, green * 2 / 3);
            }
        });
        listView.setAdapter(customListAdapter);

        // Determine where to navigate from the list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                String title = data[position][0];
                String help = data[position][1];
                boolean isFunctionCatalog = help == null;

                Intent intent = new Intent(HelpHome.this,
                        isFunctionCatalog ? HelpFunctionCatalog.class : HelpTopic.class);

                intent.putExtra("title", title);

                if (!isFunctionCatalog)
                    intent.putExtra("help_uri", "help/primer/" + help + ".hlp");

                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        // Determine where to navigate from the navigation bar
        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.navigationBar1);
        navigationBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.RIGHT) {
                    Intent intent = new Intent(HelpHome.this, Calculator.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                    startActivity(intent);
                }
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

        customListAdapter.notifyDataSetChanged();
    }
}
