/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.views;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

/**
 * Holds the view and model used by many of the list views.
 *
 * <p>Each item of the list view is one the custom ListItem views.
 */
public class CustomListAdapter extends BaseAdapter implements ListAdapter {
    /**
     * Data used to customize a CustomListAdapter
     */
    public static abstract class Data {
        protected abstract int getCount();

        protected abstract String getText(int position);

        protected int getIconColor(int position) {
            return -1;
        }

        protected int getPressedBgColor(int position) {
            return Colors.NICE_BLUE;
        }

        protected float getItemHeight() {
            return 44.0f;
        }

        protected boolean isCentered() {
            return false;
        }
    }

    private CustomListAdapter.Data data;

    /**
     * Constructor initialized with some data that describes the look and feel as well as the model.
     */
    public CustomListAdapter(CustomListAdapter.Data data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.getCount();
    }

    /**
     * We don't need to return anything meaningful as we will
     * be using the position to determine which item has been clicked.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * We don't need to return anything meaningful as we will
     * be using the position to determine which item has been clicked.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = new CustomListItem(parent.getContext(), null);

        CustomListItem view = (CustomListItem) convertView;
        String text = data.getText(position);

        view.setText(text);
        view.setHeight(data.getItemHeight());
        view.setPressedBgColor(data.getPressedBgColor(position));
        view.setIconColor(data.getIconColor(position));
        view.setCentered(data.isCentered());
        view.setPosition(position == getCount() - 1 ? -1 : position);

        return convertView;
    }
}