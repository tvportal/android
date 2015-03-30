package com.mitechlt.tvportal.play.adapters;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.databases.RecentTable;
import com.squareup.picasso.Picasso;

public class RecentAdapter extends SimpleCursorAdapter {

    private static final String TAG = "RecentAdapter";

    /**
     * Instance of {@link android.view.LayoutInflater}
     */
    private final LayoutInflater mInflater;

    private DataHolder[] mData;

    private int mSeasonIdx;
    private int mEpisodeIdx;
    private int mTitleIdx;
    private int mImageIdx;
    private int mTypeIdx;
    private int mRatingIdx;
    SharedPreferences prefs;

    /**
     * Constructor for <code>RecentAdapter</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public RecentAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to, 0);

        mInflater = LayoutInflater.from(context);
        mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Recycle the ViewHolder
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.grid_item_recent, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mData != null && mData.length > 0 && position < mData.length) {
            final DataHolder dataHolder = mData[position];
            holder.mTitle.setText(dataHolder.title);
            if (dataHolder.type.equals("TV Show")) {
                holder.mSubtitle.setText(dataHolder.season + dataHolder.episode);
                holder.mSubtitle.setVisibility(View.VISIBLE);
                holder.mRatingBar.setVisibility(View.INVISIBLE);
            } else {
                holder.mSubtitle.setVisibility(View.INVISIBLE);
                holder.mRatingBar.setVisibility(View.VISIBLE);
                holder.mRatingBar.setRating((dataHolder.rating / 100f) * 5);
            }
            holder.mType.setText(dataHolder.type);
            holder.mTypeBackground.setBackgroundColor(dataHolder.typeBackgroundColor);

            if (dataHolder.imgUri != null && !dataHolder.imgUri.isEmpty()) {
                Picasso.with(mContext)
                        .load(dataHolder.imgUri)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.mImageView);

            } else {
                Picasso.with(mContext)
                        .load(R.drawable.ic_placeholder)
                        .into(holder.mImageView);
            }
        }
        return convertView;
    }

    public void processData() {

        Cursor mRecentCursor = getCursor();

        mData = new DataHolder[getCount()];
        getColumnIndices(mRecentCursor);

        Resources res = mContext.getResources();

        for (int i = 0; i < getCount(); i++) {

            mRecentCursor.moveToPosition(i);

            mData[i] = new DataHolder();
            mData[i].title = mRecentCursor.getString(mTitleIdx);
            mData[i].imgUri = mRecentCursor.getString(mImageIdx);
            mData[i].episode = mRecentCursor.getString(mEpisodeIdx);
            mData[i].season = mRecentCursor.getString(mSeasonIdx);
            mData[i].rating = mRecentCursor.getInt(mRatingIdx);

            int seasonNum = -1;
            int episodeNum = -1;
            if (mData[i].season != null && mData[i].episode != null) {
                seasonNum = Integer.valueOf(mData[i].season.substring(mData[i].season.length() - 1));
                episodeNum = Integer.valueOf(mData[i].episode.substring(mData[i].episode.length() - 1));
            }

            String type = mRecentCursor.getString(mTypeIdx);
            if (type != null && type.equals("movie")) {
                mData[i].typeBackgroundColor = res.getColor(R.color.holo_blue);
                type = "Movie";
            } else if (type != null && type.equals("tvshow")) {
                mData[i].typeBackgroundColor = res.getColor(R.color.holo_green);
                StringBuilder seasonString = new StringBuilder();
                seasonString.append("S");
                if (seasonNum < 10) {
                    seasonString.append("0");
                }
                seasonString.append(seasonNum);
                mData[i].season = seasonString.toString();
                StringBuilder episodeString = new StringBuilder();
                episodeString.append("E");
                if (episodeNum < 10) {
                    episodeString.append("0");
                }
                episodeString.append(episodeNum);
                mData[i].episode = episodeString.toString();
                type = "TV Show";

            }
            mData[i].type = type;
        }
    }

    private void getColumnIndices(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            mTitleIdx = cursor.getColumnIndexOrThrow(RecentTable.COLUMN_TITLE);
            mTypeIdx = cursor.getColumnIndexOrThrow(RecentTable.COLUMN_TYPE);
            mSeasonIdx = cursor.getColumnIndexOrThrow(RecentTable.COLUMN_SEASON);
            mEpisodeIdx = cursor.getColumnIndexOrThrow(RecentTable.COLUMN_EPISODE);
            mImageIdx = cursor.getColumnIndexOrThrow(RecentTable.COLUMN_IMAGE);
            mRatingIdx = cursor.getColumnIndexOrThrow(RecentTable.COLUMN_RATING);
        }
    }

    private class DataHolder {
        public DataHolder() {
            super();
        }

        String title;
        String type;
        String season;
        String episode;
        String imgUri;
        int rating;
        int typeBackgroundColor;
    }

    /**
     * ViewHolder implementation
     */
    private static final class ViewHolder {

        private final TextView mTitle;
        private final TextView mSubtitle;
        private final TextView mType;
        private final RelativeLayout mTypeBackground;
        private final ImageView mImageView;
        private final RatingBar mRatingBar;

        private ViewHolder(View convertView) {
            mTitle = (TextView) convertView.findViewById(R.id.text1);
            mSubtitle = (TextView) convertView.findViewById(R.id.text2);
            mType = (TextView) convertView.findViewById(R.id.text3);
            mTypeBackground = (RelativeLayout) convertView.findViewById(R.id.type_background);
            mImageView = (ImageView) convertView.findViewById(R.id.image1);
            mRatingBar = (RatingBar) convertView.findViewById(R.id.rating_bar1);

        }
    }
}
