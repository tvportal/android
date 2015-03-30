package com.mitechlt.tvportal.play.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.databases.FavoritesTable;
import com.squareup.picasso.Picasso;

public class FavoriteAdapter extends SimpleCursorAdapter {

    /**
     * Instance of {@link android.view.LayoutInflater}
     */
    private final LayoutInflater mInflater;

    private DataHolder[] mData;

    private int mTitleIdx;
    private int mImageIdx;
    private int mTypeIdx;

    /**
     * Constructor for <code>FavoriteAdapter</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public FavoriteAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to, 0);

        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Recycle the ViewHolder
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_favorite, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mData != null && mData.length > 0 && position < mData.length) {
            final DataHolder dataHolder = mData[position];
            holder.mTitle.setText(dataHolder.title);
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

        Cursor mFavouriteCursor = getCursor();

        mData = new DataHolder[getCount()];
        getColumnIndices(mFavouriteCursor);

        Resources res = mContext.getResources();

        for (int i = 0; i < getCount(); i++) {

            mFavouriteCursor.moveToPosition(i);

            mData[i] = new DataHolder();
            mData[i].title = mFavouriteCursor.getString(mTitleIdx);

            String type = mFavouriteCursor.getString(mTypeIdx);
            if (type != null && type.equals("movie")) {
                mData[i].typeBackgroundColor = res.getColor(R.color.holo_blue);
                type = "Movie";
            }
            if (type != null && type.equals("tvshow")) {
                mData[i].typeBackgroundColor = res.getColor(R.color.holo_green);
                type = "TV Show";

            }
            mData[i].type = type;
            mData[i].imgUri = mFavouriteCursor.getString(mImageIdx);

        }
    }

    private void getColumnIndices(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            mTitleIdx = cursor.getColumnIndexOrThrow(FavoritesTable.COLUMN_TITLE);
            mImageIdx = cursor.getColumnIndexOrThrow(FavoritesTable.COLUMN_IMAGE);
            mTypeIdx = cursor.getColumnIndexOrThrow(FavoritesTable.COLUMN_TYPE);
        }
    }

    private class DataHolder {

        public DataHolder() {
            super();
        }

        String title;
        String type;
        String imgUri;
        int typeBackgroundColor;
    }

    /**
     * ViewHolder implementation
     */
    private static final class ViewHolder {

        private final TextView mTitle;
        private final TextView mType;
        private final RelativeLayout mTypeBackground;
        private final ImageView mImageView;

        private ViewHolder(View convertView) {
            mTitle = (TextView) convertView.findViewById(R.id.text1);
            mType = (TextView) convertView.findViewById(R.id.text2);
            mTypeBackground = (RelativeLayout) convertView.findViewById(R.id.type_background);
            mImageView = (ImageView) convertView.findViewById(R.id.image1);
        }
    }
}
