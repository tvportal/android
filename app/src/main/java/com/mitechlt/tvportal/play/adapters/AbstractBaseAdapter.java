package com.mitechlt.tvportal.play.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link android.widget.BaseAdapter} that exposes data from a
 * {@link java.util.List} to a {@link android.widget.AbsListView}
 *
 * @param <D> The data to expose
 */
public abstract class AbstractBaseAdapter<D> extends BaseAdapter {

    /**
     * The data used in this {@link android.widget.Adapter}
     */
    private final List<D> mData = new ArrayList<D>();

    /**
     * The id of the layout to use
     */
    protected final int mLayoutId;

    /**
     * Gets a new {@linkLayoutInflater} instance
     */
    protected final LayoutInflater mInflater;

    /**
     * Constructor for <code>AbstractBaseAdapter</code>
     *
     * @param context  The {@link android.content.Context} to use
     * @param layoutId The id of the layout to use
     */
    public AbstractBaseAdapter(Context context, int layoutId) {
        mLayoutId = layoutId;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) {
            return null;
        }
        return mData.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        // There's no need to return the actual id here
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Fake header
//        if (position == 0) {
//            return mInflater.inflate(R.layout.view_fake_header, parent, false);
//        }
        // Normal rows
        View v;
        if (convertView == null) {
            v = mInflater.inflate(mLayoutId, parent, false);
            v.setTag(new ViewHolder(v));
        } else {
            v = convertView;
        }
        bindView(position, v);
        return v;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    /**
     * Builds the data for this {@link android.widget.Adapter}
     *
     * @param data The data to add to this {@link android.widget.Adapter}
     */
    public void buildData(List<D> data) {
        // Clear the old data first to ensure new items are properly added
        mData.clear();
        // Build the data set
        mData.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * Clears the data in this {@link android.widget.Adapter}
     */
    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    /**
     * Bind an existing {@link android.view.View} to the data
     *
     * @param position The position in {@link #getView(int, android.view.View, android.view.ViewGroup)}
     * @param view     The recycled {@link android.view.View} bound to this {@link android.widget.Adapter}
     */
    protected abstract void bindView(int position, View view);

    /**
     * ViewHolder implementation
     */
    protected static final class ViewHolder {
        public TextView mTitle;
        public ImageView mImage;

        protected ViewHolder(View convertView) {
            mTitle = (TextView) convertView.findViewById(android.R.id.text1);
            mImage = (ImageView) convertView.findViewById(android.R.id.icon);
        }
    }

}
