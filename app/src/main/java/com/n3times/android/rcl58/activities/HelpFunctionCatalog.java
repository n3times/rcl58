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

/**
 * Displays the list of functions available in the calculator.
 */
public class HelpFunctionCatalog extends BaseActivity {
    /**
     * Function names and their codes
     */
    private static final String[][] data = {
            {"0 - 9", "00-09"},
            {"A - E'", "10-19"},
            {"+", "85"},
            {"\u2212", "75"},
            {"\u00d7", "65"},
            {"\u00f7", "55"},
            {"+/-", "94"},
            {".", "93"},
            {"\u03c0", "89"},
            {"(", "53"},
            {")", "54"},
            {"=", "95"},
            {"\u2211+", "78"},
            {"\u221ax", "34"},
            {"|x|", "50"},
            {"1/x", "35"},
            {"2nd", "21"},
            {"Adv", "98"},
            {"BST", "51"},
            {"CE", "24"},
            {"CLR", "25"},
            {"CMs", "47"},
            {"cos", "39"},
            {"CP", "29"},
            {"D.MS", "88"},
            {"Deg", "60"},
            {"Del", "56"},
            {"Dsz", "97"},
            {"EE", "52"},
            {"Eng", "57"},
            {"Exc", "48"},
            {"Fix", "58"},
            {"Grad", "80"},
            {"GTO", "61"},
            {"HIR", "82"},
            {"If flg", "87"},
            {"Ind", "40"},
            {"Ins", "46"},
            {"Int", "59"},
            {"INV", "22"},
            {"Lbl", "76"},
            {"List", "90"},
            {"lnx", "23"},
            {"log", "28"},
            {"LRN", "31"},
            {"Nop", "68"},
            {"Op", "69"},
            {"Pause", "66"},
            {"Pgm", "36"},
            {"Prd", "49"},
            {"Prt", "99"},
            {"P\u2192R", "37"},
            {"R/S", "91"},
            {"Rad", "70"},
            {"RCL", "43"},
            {"RST", "81"},
            {"SBR", "71"},
            {"sin", "38"},
            {"SST", "41"},
            {"St flg", "86"},
            {"STO", "42"},
            {"SUM", "44"},
            {"tan", "30"},
            /* {"Write", "96"}, */ // Only used in RCL-59
            {"x:t", "32"},
            {"x=t", "67"},
            {"x\u00b2", "33"},
            {"x\u0303" /*"x\u0304"*/, "79"},
            {"x\u2265t", "77"},
            {"yx" /*"y\u02e3"*/, "45"},
    };

    private CustomListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help_function_catalog);

        // Set the look and feel of the list view as well as its model
        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listAdapter = new CustomListAdapter(new CustomListAdapter.Data() {
            @Override
            protected String getText(int position) {
                return data[position][0];
            }

            @Override
            protected int getCount() {
                return data.length;
            }

            @Override
            protected boolean isCentered() {
                return true;
            }

            @Override
            protected int getPressedBgColor(int position) {
                return Colors.NICE_GREEN;
            }
        });
        listView.setAdapter(listAdapter);

        // Determine where to navigate from the navigation bar
        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.navigationBar1);
        navigationBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.LEFT) {
                    onBackPressed();
                } else if (item == Item.RIGHT) {
                    Intent intent = new Intent(HelpFunctionCatalog.this, Calculator.class);
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
                Intent intent = new Intent(HelpFunctionCatalog.this, HelpTopic.class);

                String title = data[position][0];
                intent.putExtra("title", title);

                String help = data[position][1];
                String helpUri = "help/catalog/" + help + ".hlp";
                intent.putExtra("help_uri", helpUri);

                intent.putExtra("from_catalog", true);

                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HelpHome.class);
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
