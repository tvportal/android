package com.mitechlt.tvportal.play.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.model.Season;

import java.util.ArrayList;
import java.util.List;

public class SeasonAdapter extends BaseAdapter {

    /**
     * The data used in the Adapter
     */
    private final List<Season> mData = new ArrayList<Season>();

    /**
     * Instance of {@link android.view.LayoutInflater}
     */
    private final LayoutInflater mInflater;

    /**
     * Constructor for <code>SeasonAdapter</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public SeasonAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Recycle the ViewHolder
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Bind the data
        final Season data = (Season) getItem(position);
        holder.mTitle.setText(data.title);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Builds the data for this {@link android.widget.Adapter}
     *
     * @param data The data to add to this {@link android.widget.Adapter}
     */
    public void buildData(List<Season> data) {
        // Clear the data first to ensure new items are added
        mData.clear();
        // Loop through and build the data set
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
     * ViewHolder implementation
     */
    private static final class ViewHolder {

        private final TextView mTitle;

        private ViewHolder(View convertView) {
            mTitle = (TextView) convertView.findViewById(R.id.text1);
        }
    }
}
