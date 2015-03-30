package com.mitechlt.tvportal.play.adapters;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * This {@link android.support.v4.app.FragmentStatePagerAdapter} is used to bind
 * the data to the {@link android.support.v4.view.ViewPager} that's used to
 * swipe through the various views
 *
 */
public class PagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    /**
     * Holds each Fragment added to this adapter
     */
    private final List<Fragment> mData = new ArrayList<Fragment>(3);

    /**
     * The page titles
     */
    private final String[] mTitles;

    /**
     * Constructor for <code>PagerAdapter</code>
     *
     * @param fragment The {@link android.support.v4.app.Fragment} to use
     */
    public PagerAdapter(Fragment fragment, int titles) {
        super(fragment.getChildFragmentManager());
        mTitles = fragment.getResources().getStringArray(titles);
    }

    /**
     * Constructor for <code>PagerAdapter</code>
     *
     * @param fragment The {@link android.support.v4.app.Fragment} to use
     */
    public PagerAdapter(Fragment fragment, String[] titles) {
        super(fragment.getChildFragmentManager());
        mTitles = titles;
    }

    /**
     * Constructor for <code>PagerAdapter</code>
     *
     * @param activity The {@link android.support.v7.app.ActionBarActivity} to use
     */
    public PagerAdapter(ActionBarActivity activity, int titles) {
        super(activity.getSupportFragmentManager());
        mTitles = activity.getResources().getStringArray(titles);
    }

    public PagerAdapter(ActionBarActivity activity, String title) {
        super(activity.getSupportFragmentManager());
        mTitles = new String[]{title};
    }

    @Override
    public Fragment getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    /**
     * Add a new {@link android.support.v4.app.Fragment} to the list
     *
     * @param fragment The {@link android.support.v4.app.Fragment} to add
     */
    public void add(Fragment fragment) {
        mData.add(fragment);
        notifyDataSetChanged();
    }

    /**
     * Removes all elements from this List, leaving it empty
     */
    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

}
