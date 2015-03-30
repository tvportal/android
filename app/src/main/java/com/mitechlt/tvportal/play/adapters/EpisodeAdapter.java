package com.mitechlt.tvportal.play.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.fragments.EpisodeFragment;
import com.mitechlt.tvportal.play.model.Episode;
import com.mitechlt.tvportal.play.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAdapter extends BaseAdapter {

    private static final String TAG = "EpisodeAdapter";

    /**
     * The data used in the Adapter
     */
    private final List<Episode> mData = new ArrayList<Episode>();

    /**
     * Instance of {@link android.view.LayoutInflater}
     */
    private final LayoutInflater mInflater;

    private final Context mContext;

    private final EpisodeFragment mEpisodeFragment;

    /**
     * Constructor for <code>EpisodeAdapter</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public EpisodeAdapter(Context context, EpisodeFragment fragment) {
        mEpisodeFragment = fragment;
        mContext = context;
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
        final ViewHolder holder;
        //Bind the data
        final Episode data = (Episode) getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_episode, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTitle.setText(data.title);
        holder.mSubtitle.setText(data.episode);
        holder.mTickFrame.setTag(position);
        holder.mTick.setImageDrawable(AppUtils.isWatched(mContext, data.episode, data.season)
                ? mContext.getResources().getDrawable(R.drawable.ic_tick_on)
                : mContext.getResources().getDrawable(R.drawable.ic_tick_off));

        holder.mTickFrame.setTag(position);

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
    public void buildData(List<Episode> data) {
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
    private final class ViewHolder {

        private final TextView mTitle;
        private final TextView mSubtitle;
        private final ImageView mTick;
        private final FrameLayout mTickFrame;

        private ViewHolder(View convertView) {
            mTitle = (TextView) convertView.findViewById(R.id.text1);
            mSubtitle = (TextView) convertView.findViewById(R.id.text2);
            mTick = (ImageView) convertView.findViewById(R.id.tick);
            mTickFrame = (FrameLayout) convertView.findViewById(R.id.tick_frame);
            mTickFrame.setOnClickListener(mEpisodeFragment);
        }
    }
}
