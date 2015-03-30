package com.mitechlt.tvportal.play.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.mitechlt.tvportal.play.R;

public class SocialAdapter extends BaseAdapter {


    /**
     * Instance of {@link android.view.LayoutInflater}
     */
    private final LayoutInflater mInflater;

    private final Context mContext;

    private final TypedArray mIcons;

    private final String[] mTitles;

    /**
     * Constructor for <code>SeasonAdapter</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public SocialAdapter(Context context, String[] titles, TypedArray icons) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mTitles = titles;
        mIcons = icons;
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Recycle the ViewHolder
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_social, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Bind the data
        holder.mTitle.setText(mTitles[position]);
        holder.mIcon.setImageResource(mIcons.getResourceId(position, -1));

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * ViewHolder implementation
     */
    private static final class ViewHolder {

        private final RobotoTextView mTitle;
        private final ImageView mIcon;

        private ViewHolder(View convertView) {
            mTitle = (RobotoTextView) convertView.findViewById(R.id.text1);
            mIcon = (ImageView) convertView.findViewById(R.id.icon1);
        }
    }
}
