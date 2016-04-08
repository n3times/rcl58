/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.n3times.android.rcl58.R;
import com.n3times.android.rcl58.views.ActionBar;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;
import com.n3times.android.rcl58.views.PrinterLine;
import com.n3times.android.rcl58.vio.State;
import com.n3times.android.rcl58.vio.Vio;
import com.n3times.android.rcl58.vio.VioListener;

/**
 * Displays the output of the printer.
 *
 * <p>The printer uses a circular buffer: only the last 1,000 lines are displayed.
 */
public class Printer extends BaseActivity implements  VioListener {
    /**
     * We pause the calculator when the user scrolls. This ensure the calculator
     * won't be printing new lines. Once the user is done scrolling, we resume soon after.
     */
    private static final int RESUME_AFTER_SCROLL_MS = 300;

    private static final int LINE_CHAR_COUNT = 20;

    private static final String CAPTION_OK     = "OK";
    private static final String CAPTION_CANCEL = "Cancel";
    private static final String CAPTION_CLEAR  = "Clear Paper";

    private ActionBar actionBar;
    private ListView listView;
    private PrinterListAdapter listAdapter;

    private static int lastPrinterCount;

    /**
     * Holds the view and model of the printer
     */
    static private class PrinterListAdapter extends BaseAdapter implements ListAdapter {
        // These characters represent the printable characters
        private static final String PRINT_CODES =
                  " 012345678"
                + "789ABCDE78"
                + "-FGHIJKLMN"
                + "MNOPQRSTMN"
                + ".UVWXYZ+x*"
                + "x*vpe(),x*"
                + "^%:/='`a\"?"
                + "\"?|!#_ns\"?"
                + " 012345678"
                + "789ABCDE78";

        @Override
        public int getCount() {
            State state = Vio.instance().getState();

            int max_count = state.printerGetCircularBufferCapacity();
            int count = Math.min(max_count, state.printerGetAlltimePrintedCount());
            if (count < max_count) {
                // The extra line is the top margin of the paper, which is left blank
                count++;
            }

            return count;
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
         *  Each view is an instance of PrinterLine
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = new PrinterLine(parent.getContext(), null);

            PrinterLine view = (PrinterLine) convertView;
            char[] text = new char[LINE_CHAR_COUNT];
            State state = Vio.instance().getState();

            // Note that the last line of the list (distance to bottom == 0) gets the last
            // printed line (lineIndex == state.printerGetAlltimePrintedCount())
            int distanceToBottom = this.getCount() - 1 - position;
            int lineIndex = state.printerGetAlltimePrintedCount() - distanceToBottom;
            if (lineIndex > 0) {
                for (int i = 0; i < LINE_CHAR_COUNT; i++) {
                    int c = state.printerGetCharAtLine(i, lineIndex);
                    text[i] = PRINT_CODES.charAt(c);
                }
            }
            view.setText(new String(text));
            view.setLineType(lineIndex <= 0 ? PrinterLine.LineType.START_ROLL
                    : PrinterLine.LineType.PAPER);

            return convertView;
        }
    }

    @Override
    public void stateChanged() {
        int newPrinterCount = Vio.instance().getState().printerGetAlltimePrintedCount();

        if (newPrinterCount != lastPrinterCount) {
            // Inform the adapter that it needs to query again the model
            listAdapter.notifyDataSetChanged();

            // Scroll to the end
            listView.setSelection(listAdapter.getCount() - 1);

            // Allow the user to clear the paper
            actionBar.setEnabled(true);
            lastPrinterCount = newPrinterCount;
        }
    }

    /**
     * Returns the last printer count seen by the user.
     */
    public static int getLastPrinterCount() {
        return lastPrinterCount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_printer);

        // Have the navigation handle going back to the Calculator activity
        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.navigationBar1);
        navigationBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.LEFT) {
                    onBackPressed();
                }
            }
        });

        // Set the look and feel of the list view as well as its model
        listView = (ListView) findViewById(R.id.listView1);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listAdapter = new PrinterListAdapter();
        listView.setAdapter(listAdapter);

        // Go to the end of the print out
        listView.setSelection(listAdapter.getCount() - 1);

        // Pause the calculator whenever the user is scrolling in order to
        // avoid overwhelming the user with new printed lines
        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    Vio.instance().resume(RESUME_AFTER_SCROLL_MS);
                } else {
                    Vio.instance().pause();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // We don't need to track this, just the scroll state
            }
        });

        // Handle the action bar to clear the paper
        actionBar = (ActionBar) findViewById(R.id.actionBar1);
        actionBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                if (item == Item.MIDDLE) {
                    new AlertDialog.Builder(Printer.this)
                            .setTitle(CAPTION_CLEAR)
                            .setNegativeButton(CAPTION_CANCEL, null)
                            .setPositiveButton(CAPTION_OK, new Dialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Vio.instance().resetPrinter();
                                    lastPrinterCount = 0;

                                    listAdapter.notifyDataSetChanged();
                                    listView.setSelection(listAdapter.getCount() - 1);

                                    actionBar.setEnabled(false);
                                }
                            })
                            .create().show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onResume() {
        super.onResume();

        actionBar.setEnabled(Vio.instance().getState().printerGetAlltimePrintedCount() > 0);
        Vio.instance().addListener(this);
        lastPrinterCount = Vio.instance().getState().printerGetAlltimePrintedCount();
    }

    @Override
    public void onPause() {
        super.onPause();

        Vio.instance().removeListener(this);
    }
}
