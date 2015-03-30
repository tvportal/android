package com.mitechlt.tvportal.play.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.adapters.EpisodeAdapter;
import com.mitechlt.tvportal.play.loaders.EpisodeLoader;
import com.mitechlt.tvportal.play.model.Episode;
import com.mitechlt.tvportal.play.utils.AppUtils;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class EpisodeFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Episode>>, View.OnClickListener {

    private EpisodeAdapter mAdapter;
    private ListView mListView;
    private String mSeason;
    private String mSeries;
    private String mLink;
    private String mImageUri;
    private int mNumSeasons;
    private int mRating;
    private ProgressBar mProgressBar;
    private static final String ARG_SERIES = "series";
    private static final String ARG_SEASON = "season";
    private static final String ARG_LINK = "link";
    private static final String ARG_IMAGE_URI = "img_uri";
    private static final String ARG_NUM_SEASONS = "num_seasons";
    private static final String ARG_RATING = "rating";

    /**
     * @param series the mSeries of this TV Show
     * @param season the current mSeason number for this TV Show
     * @param link   the link to the episodes for this season
     * @return a new instance of this fragment.
     */
    public static EpisodeFragment newInstance(String series, String season, String link, String imageUri, int numSeasons, int rating) {

        final EpisodeFragment fragment = new EpisodeFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_SERIES, series);
        args.putString(ARG_SEASON, season);
        args.putString(ARG_LINK, link);
        args.putString(ARG_IMAGE_URI, imageUri);
        args.putInt(ARG_NUM_SEASONS, numSeasons);
        args.putInt(ARG_RATING, rating);
        fragment.setArguments(args);
        return fragment;
    }

    public EpisodeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAdapter = new EpisodeAdapter(this.getActivity(), this);

        mSeries = getArguments().getString(ARG_SERIES);
        mSeason = getArguments().getString(ARG_SEASON);
        mLink = getArguments().getString(ARG_LINK);
        mImageUri = getArguments().getString(ARG_IMAGE_URI);
        mNumSeasons = getArguments().getInt(ARG_NUM_SEASONS);
        mRating = getArguments().getInt(ARG_RATING);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(mSeason);
        }

    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                int length = mAdapter.getCount();
                String[] titles = new String[length];
                String[] links = new String[length];
                String[] episodes = new String[length];
                String[] seasons = new String[length];
                String series = "";
                for (int i = 0; i < length; i++) {
                    Episode episode = (Episode) mAdapter.getItem(i);
                    titles[i] = episode.title;
                    links[i] = episode.link;
                    episodes[i] = episode.episode;
                    seasons[i] = episode.season;
                    series = episode.series;
                }

                ft.replace(R.id.fragment_container, TVMirrorsFragment.newInstance(position, series, titles, links, episodes, seasons, mImageUri, mNumSeasons, mAdapter.getCount(), mRating));
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setSmoothScrollbarEnabled(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Set the subtitle again, to remove any episode number that might have been introduced in before returning to this fragment
        getActionBar().setSubtitle(mSeason);
    }

    @Override
    public void onDestroyView() {
        // Release any references to avoid memory leaks
        mListView = null;
        super.onDestroyView();
    }

    @Override
    public Loader<List<Episode>> onCreateLoader(int id, Bundle args) {
        return new EpisodeLoader(getActivity(), mSeries, mLink, mSeason);
    }

    @Override
    public void onLoadFinished(Loader<List<Episode>> loader, List<Episode> episodes) {

        mProgressBar.setVisibility(View.GONE);

        mAdapter.clear();

        // Add the data to the adapter
        mAdapter.buildData(episodes);
    }

    @Override
    public void onLoaderReset(Loader<List<Episode>> loader) {
        // Clear the data back to null to prevent memory leaks
        mAdapter.clear();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onClick(View view) {
        Episode episode = (Episode) mAdapter.getItem((Integer) view.getTag());
        AppUtils.toggleWatched(getActivity(), episode.episode, episode.season, episode.link, false);
        mAdapter.notifyDataSetChanged();
    }
}
