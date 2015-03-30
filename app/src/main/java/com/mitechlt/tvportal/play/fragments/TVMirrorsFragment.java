package com.mitechlt.tvportal.play.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.adapters.PagerAdapter;
import com.mitechlt.tvportal.play.views.SlidingTabLayout;

/**
 * A placeholder fragment containing a simple view.
 */
public class TVMirrorsFragment extends Fragment {

    private PagerAdapter mAdapter;

    private final String TAG = "MirrorsFragment";

    private int mPosition;

    private static final String ARG_POSITION = "position";
    private static final String ARG_TITLES = "titles";
    private static final String ARG_SERIES = "series";
    private static final String ARG_EPISODES = "episodes";
    private static final String ARG_SEASONS = "seasons";
    private static final String ARG_LINKS = "links";
    private static final String ARG_IMAGE_URI = "img_uri";
    private static final String ARG_NUM_EPISODES = "num_episodes";
    private static final String ARG_NUM_SEASONS = "num_seasons";
    private static final String ARG_RATING = "rating";

    /**
     * Returns a new instance of this fragment for the given Episode.
     *
     * @param position the current position (chosen episode)
     * @param titles   the list of episode titles
     * @param links    the list of episode links
     * @param seasons  the list of episode season
     * @return a new instance of this fragment for the given list of Episodes.
     */
    public static TVMirrorsFragment newInstance(int position, String series, String[] titles, String[] links, String[] episodes, String[] seasons, String imgUri, int numSeasons, int numEpisodes, int rating) {
        final TVMirrorsFragment fragment = new TVMirrorsFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putStringArray(ARG_TITLES, titles);
        args.putString(ARG_SERIES, series);
        args.putStringArray(ARG_LINKS, links);
        args.putStringArray(ARG_EPISODES, episodes);
        args.putStringArray(ARG_SEASONS, seasons);
        args.putString(ARG_IMAGE_URI, imgUri);
        args.putInt(ARG_NUM_SEASONS, numSeasons);
        args.putInt(ARG_NUM_EPISODES, numEpisodes);
        args.putInt(ARG_RATING, rating);
        fragment.setArguments(args);
        return fragment;
    }

    public TVMirrorsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mPosition = getArguments().getInt(ARG_POSITION);
        String[] titles = getArguments().getStringArray(ARG_TITLES);
        String series = getArguments().getString(ARG_SERIES);
        String[] links = getArguments().getStringArray(ARG_LINKS);
        String[] episodes = getArguments().getStringArray(ARG_EPISODES);
        String[] seasons = getArguments().getStringArray(ARG_SEASONS);
        String imageUri = getArguments().getString(ARG_IMAGE_URI);
        int numSeasons = getArguments().getInt(ARG_NUM_SEASONS);
        int numEpisodes = getArguments().getInt(ARG_NUM_EPISODES);
        int rating = getArguments().getInt(ARG_RATING);

        ActionBar actionBar = getActionBar();
        if (actionBar != null && seasons != null) {
            actionBar.setSubtitle(seasons[mPosition]);
        }

        int length = titles.length;

        mAdapter = new PagerAdapter(this, episodes);
        for (int i = 0; i < length; i++) {
            mAdapter.add(MirrorsFragment.newInstance(series, links[i], episodes[i], seasons[i], imageUri, numSeasons, numEpisodes, rating));
        }
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tv_mirrors_fragment, container, false);

        final ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(mAdapter);
        pager.setCurrentItem(mPosition);

        final SlidingTabLayout slidingTabs = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        slidingTabs.setShouldExpand(false);
        slidingTabs.setViewPager(pager);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }
}
