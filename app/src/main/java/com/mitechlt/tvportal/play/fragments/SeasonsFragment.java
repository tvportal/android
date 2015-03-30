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
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.adapters.SeasonAdapter;
import com.mitechlt.tvportal.play.loaders.SeasonLoader;
import com.mitechlt.tvportal.play.model.Season;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SeasonsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Season>> {

    private SearchView mSearchView;
    private SeasonAdapter mAdapter;
    private ListView mListView;
    private String mSeries;
    private String mLink;
    private String mImageUri;
    private int mRating;
    private ProgressBar mProgressBar;
    private static final String ARG_SERIES = "series";
    private static final String ARG_LINK = "link";
    private static final String ARG_IMAGE_URI = "img_uri";
    private static final String ARG_RATING = "rating";

    /**
     * @param series the title of this TV Show
     * @return a new instance of this fragment
     */
    public static SeasonsFragment newInstance(String series, String link, String imageUri, int rating) {
        final SeasonsFragment fragment = new SeasonsFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_SERIES, series);
        args.putString(ARG_LINK, link);
        args.putString(ARG_IMAGE_URI, imageUri);
        args.putInt(ARG_RATING, rating);
        fragment.setArguments(args);
        return fragment;
    }

    public SeasonsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAdapter = new SeasonAdapter(this.getActivity());

        mSeries = getArguments().getString(ARG_SERIES);
        mLink = getArguments().getString(ARG_LINK);
        mImageUri = getArguments().getString(ARG_IMAGE_URI);
        mRating = getArguments().getInt(ARG_RATING);

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
                mListView.setItemChecked(position, false);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Season season = (Season) mAdapter.getItem(position);
                ft.replace(R.id.fragment_container, EpisodeFragment.newInstance(mSeries, season.title, season.link, mImageUri, mAdapter.getCount(), mRating));
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        mListView.setAdapter(mAdapter);

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

        //Remove any subtitle that might have been applied in a future transaction
        getActionBar().setSubtitle(null);
    }

    @Override
    public void onDestroyView() {
        // Release any references to avoid memory leaks
        mListView = null;
        super.onDestroyView();
    }

    @Override
    public Loader<List<Season>> onCreateLoader(int id, Bundle args) {
        return new SeasonLoader(getActivity(), mLink);
    }

    @Override
    public void onLoadFinished(Loader<List<Season>> loader, List<Season> seasons) {

        mProgressBar.setVisibility(View.GONE);

        mAdapter.clear();

        if (seasons != null && !seasons.isEmpty()) {
            // Add the data to the adapter
            mAdapter.buildData(seasons);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Season>> loader) {
        // Clear the data back to null to prevent memory leaks
        mAdapter.clear();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }
}
