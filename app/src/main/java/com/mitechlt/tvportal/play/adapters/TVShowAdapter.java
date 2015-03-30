package com.mitechlt.tvportal.play.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.model.TVShow;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TVShowAdapter extends BaseAdapter {

    /**
     * The data used in the Adapter
     */
    private final List<TVShow> mData = new ArrayList<TVShow>();

    /**
     * Instance of {@link android.view.LayoutInflater}
     */
    private final LayoutInflater mInflater;

    private final Context mContext;

    /**
     * Map of characters to sections for {@link android.widget.SectionIndexer}
     */
    HashMap<String, Integer> mIndexer;

    /**
     * Sections for {@link android.widget.SectionIndexer}
     */
    String[] mSections;

    /**
     * Constructor for <code>TvshowAdapter</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public TVShowAdapter(Context context) {
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
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_card, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Bind the data
        final TVShow data = (TVShow) getItem(position);
        holder.mTitle.setText(data.title);
        holder.mGenres.setText(data.genres);
        holder.mYear.setText(data.year);
        holder.mRating.setRating((data.rating / 100f) * 5);
        if (data.imageUri != null && !data.imageUri.isEmpty()) {
            Picasso.with(mContext)
                    .load(data.imageUri)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.mImageView);
        } else {
            Picasso.with(mContext)
                    .load(R.drawable.ic_placeholder)
                    .into(holder.mImageView);
        }
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
    public void buildData(List<TVShow> data) {
        // Clear the data first to ensure new items are added
        // mData.clear();
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
        private final TextView mYear;
        private final TextView mGenres;
        private final RatingBar mRating;
        private final ImageView mImageView;

        private ViewHolder(View convertView) {
            mTitle = (TextView) convertView.findViewById(R.id.text1);
            mGenres = (TextView) convertView.findViewById(R.id.text2);
            mYear = (TextView) convertView.findViewById(R.id.text3);
            mRating = (RatingBar) convertView.findViewById(R.id.rating_bar1);
            mImageView = (ImageView) convertView.findViewById(R.id.image1);
        }
    }
}
