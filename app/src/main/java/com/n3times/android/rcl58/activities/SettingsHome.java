/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.n3times.android.rcl58.R;
import com.n3times.android.rcl58.utilities.Prefs;
import com.n3times.android.rcl58.views.Colors;
import com.n3times.android.rcl58.views.CustomListAdapter;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;
import com.n3times.android.rcl58.vio.Vio;

/**
 * Allows to set user preferences, and gives users information about the application.
 */
public class SettingsHome extends BaseActivity {
    private static final String TITLE_CONTACT  = "Contact";
    private static final String TITLE_NEW      = "What's New";
    private static final String TITLE_RESET    = "Reset";
    private static final String TITLE_KEYBOARD = "Keyboard Feedback";

    private static final String MESSAGE_RESET  = "The state of the calculator, "
            + "including registers, program steps and printer, will be cleared";

    private static final String CAPTION_OK     = "OK";
    private static final String CAPTION_CANCEL = "CANCEL";

    private String[] soundOptions = {"None", "Sound", "Haptic"};

    private CustomListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_home);

        // Set the list view, its look and feel as well as its model
        final ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        final String[] data = {TITLE_NEW, TITLE_CONTACT, null, TITLE_RESET};
        listAdapter = new CustomListAdapter(new CustomListAdapter.Data() {
            @Override
            protected String getText(int position) {
                String text = data[position];

                if (position == 2) {
                    text = TITLE_KEYBOARD + ": "
                            + soundOptions[Prefs.instance(SettingsHome.this).getKeyFeedback().ordinal()];
                }

                return text;
            }

            @Override
            protected int getCount() {
                return data.length;
            }

            @Override
            protected int getPressedBgColor(int position) {
                return Colors.NICE_ORANGE;
            }

            protected int getIconColor(int position) {
                int n = getCount();
                int firstGreen = 255;
                int lastGreen = 180;
                int green = firstGreen + (lastGreen - firstGreen) * position / (n - 1);

                return Color.rgb(green, green / 2, green / 7);
            }
        });

        listView.setAdapter(listAdapter);

        // Handle user selection:
        // - What's new
        // - Contact
        // - Keyboard Feedback
        // - Reset
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                if (position <= 1) {
                    String title = position == 0 ? TITLE_NEW : TITLE_CONTACT;
                    String help = position == 0 ? "new_android" : "contact";
                    Intent intent = new Intent(SettingsHome.this, SettingsTopic.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("title", title);
                    intent.putExtra("help_uri", "help/primer/" + help + ".hlp");
                    startActivity(intent);
                } else if (position == 2) {
                    new AlertDialog.Builder(SettingsHome.this)
                    .setTitle(TITLE_KEYBOARD)
                    .setItems(soundOptions, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Prefs.instance(SettingsHome.this).setKeyFeedback(Prefs.KeyFeedback.values()[which]);
                            listAdapter.notifyDataSetChanged();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            listAdapter.notifyDataSetChanged();}
                    })
                    .create().show();
                } else if (position == 3) {
                    new AlertDialog.Builder(SettingsHome.this)
                    .setTitle(TITLE_RESET)
                    .setMessage(MESSAGE_RESET)
                    .setNegativeButton(CAPTION_CANCEL, new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            listAdapter.notifyDataSetChanged();
                        }
                    })
                    .setPositiveButton(CAPTION_OK, new Dialog.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Vio.instance().reset();
                            Intent intent = new Intent(SettingsHome.this, Calculator.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            listAdapter.notifyDataSetChanged();
                        }
                    })
                    .create().show();
                }
            }
        });

        // Handle the navigation bar
        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.navigationBar1);
        navigationBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.RIGHT) {
                    Intent intent = new Intent(SettingsHome.this, Calculator.class);
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

        listAdapter.notifyDataSetChanged();
    }
}
