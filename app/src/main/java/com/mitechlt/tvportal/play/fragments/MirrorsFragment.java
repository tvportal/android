package com.mitechlt.tvportal.play.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.mitechlt.tvportal.play.CastApplication;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.adapters.MirrorAdapter;
import com.mitechlt.tvportal.play.async.AsyncLinkParser;
import com.mitechlt.tvportal.play.loaders.MirrorLoader;
import com.mitechlt.tvportal.play.model.Mirror;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MirrorsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Mirror>>, ListView.OnItemClickListener, ViewStub.OnInflateListener {

    private final String TAG = "MirrorsFragment";
    private MirrorAdapter mAdapter;
    private ListView mListView;
    private String mTitle;
    private String mLink;
    private String mEpisode;
    private String mSeason;
    private String mImageUri;
    private int mNumSeasons;
    private int mNumEpisodes;
    private int mRating;
    private ProgressBar mProgressBar;
    private ViewStub mEmptyView;
    private AsyncLinkParser mAsyncLinkParser;
    private static final String ARG_TITLE = "title";
    private static final String ARG_LINK = "link";
    private static final String ARG_EPISODE = "episode";
    private static final String ARG_SEASON = "season";
    private static final String ARG_IMAGE_URI = "img_uri";
    private static final String ARG_NUM_EPISODES = "num_episodes";
    private static final String ARG_NUM_SEASONS = "num_seasons";
    private static final String ARG_RATING = "rating";
    private VideoCastManager mCastManager;
    private VideoCastConsumerImpl mCastConsumer;

    /**
     * Returns a new instance of this fragment for the given Episode.
     *
     * @param title the mTitle of this Episode
     * @param link  the link to the Mirrors for this Episode
     */
    public static MirrorsFragment newInstance(String title, String link, String episode, String season, String imgUri, int numSeasons, int numEpisodes, int rating) {
        final MirrorsFragment fragment = new MirrorsFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_LINK, link);
        args.putString(ARG_EPISODE, episode);
        args.putString(ARG_SEASON, season);
        args.putString(ARG_IMAGE_URI, imgUri);
        args.putInt(ARG_NUM_SEASONS, numSeasons);
        args.putInt(ARG_NUM_EPISODES, numEpisodes);
        args.putInt(ARG_RATING, rating);

        fragment.setArguments(args);
        return fragment;
    }

    public MirrorsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VideoCastManager.checkGooglePlayServices(this.getActivity());

        setHasOptionsMenu(true);

        mAdapter = new MirrorAdapter(this.getActivity());

        mTitle = getArguments().getString(ARG_TITLE);
        mLink = getArguments().getString(ARG_LINK);
        mEpisode = getArguments().getString(ARG_EPISODE);
        mSeason = getArguments().getString(ARG_SEASON);
        mImageUri = getArguments().getString(ARG_IMAGE_URI);
        mNumSeasons = getArguments().getInt(ARG_NUM_SEASONS);
        mNumEpisodes = getArguments().getInt(ARG_NUM_EPISODES);
        mRating = getArguments().getInt(ARG_RATING);
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list_cards, container, false);
        mCastManager = CastApplication.getCastManager(this.getActivity());

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setSmoothScrollbarEnabled(true);

        mListView.setOnItemClickListener(this);

        mEmptyView = (ViewStub) rootView.findViewById(android.R.id.empty);
        mEmptyView.setOnInflateListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

        //Restart the loader when the chromecast is connected/disconnected, to refresh the list showing chromecast supported links (or not)
        if (mCastManager != null) {

            mCastConsumer = new VideoCastConsumerImpl() {
                @Override
                public void onDisconnected() {
                    super.onDisconnected();
                    getLoaderManager().restartLoader(0, null, MirrorsFragment.this);
                }

                @Override
                public void onConnected() {
                    super.onConnected();
                    getLoaderManager().restartLoader(0, null, MirrorsFragment.this);
                }
            };

            mCastManager.addVideoCastConsumer(mCastConsumer);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        // Release any references to avoid memory leaks
        mListView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        if (mAsyncLinkParser != null) {
            ProgressDialog dialog = mAsyncLinkParser.mProgressDialog;
            if (dialog != null) {
                dialog.cancel();
            }
        }

        if (mCastManager != null && mCastConsumer != null) {
            mCastManager.removeVideoCastConsumer(mCastConsumer);
        }

        super.onDestroy();
    }


    @Override
    public Loader<List<Mirror>> onCreateLoader(int id, Bundle args) {
        return new MirrorLoader(this, mLink);
    }

    @Override
    public void onLoadFinished(Loader<List<Mirror>> loader, List<Mirror> mirrors) {

        mProgressBar.setVisibility(View.GONE);
        mAdapter.clear();

        if (mirrors == null || mirrors.isEmpty()) {
            mListView.setEmptyView(mEmptyView);
        } else {
            // Add the data to the adapter
            mAdapter.buildData(mirrors);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Mirror>> loader) {
        // Clear the data back to null to prevent memory leaks
        mAdapter.clear();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Mirror mirror = (Mirror) mAdapter.getItem(position);
        mAsyncLinkParser = new AsyncLinkParser(getActivity(), mTitle, mEpisode, mSeason, mLink, mImageUri, mNumSeasons, mNumEpisodes, mRating);
        mAsyncLinkParser.execute(mirror.title, mirror.link);
    }

    @Override
    public void onInflate(ViewStub stub, View view) {
        TextView textView = (TextView) view.findViewById(R.id.text1);
        textView.setText(R.string.empty_mirrors);
    }
}
