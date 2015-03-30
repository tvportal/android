package com.mitechlt.tvportal.play.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.robototextview.widget.RobotoCheckBox;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.activities.TVShowActivity;
import com.mitechlt.tvportal.play.adapters.TVShowAdapter;
import com.mitechlt.tvportal.play.databases.FavoritesContentProvider;
import com.mitechlt.tvportal.play.databases.FavoritesTable;
import com.mitechlt.tvportal.play.loaders.TVShowLoader;
import com.mitechlt.tvportal.play.model.TVShow;
import com.mitechlt.tvportal.play.utils.AppUtils;
import com.mitechlt.tvportal.play.utils.Config;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class TVShowFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<TVShow>>, AbsListView.OnScrollListener, AppUtils.OPTIONS, ViewStub.OnInflateListener {

    private TVShowAdapter mAdapter;
    private ListView mListView;
    private String mFilter;
    private int FRAGMENT_ID = AppUtils.TVShowFragmentId;
    private int mStartPage = 1;
    private View mFooterView;
    private ProgressBar mProgressBar;
    private ViewStub mEmptyView;
    private View mRootView;
    private static final String ARG_SERIES = "series";
    private static final String ARG_LINK = "link";
    private static final String ARG_IMAGE_URI = "img_uri";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static TVShowFragment newInstance() {
        final TVShowFragment fragment = new TVShowFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TVShowFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new TVShowAdapter(this.getActivity());
    }

    @SuppressWarnings("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_list_cards, container, false);

        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mFooterView = inflater.inflate(R.layout.progress_footer, null);

        mListView = (ListView) mRootView.findViewById(android.R.id.list);

        mListView.addFooterView(mFooterView);
        mListView.setAdapter(mAdapter);
        mListView.removeFooterView(mFooterView);
        mListView.setFastScrollEnabled(true);
        mListView.setSmoothScrollbarEnabled(true);
        if (AppUtils.hasKitKat()) {
            mListView.setFastScrollAlwaysVisible(true);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //If the user clicked on the footer view, do nothing
                if (position == mAdapter.getCount()) {
                    return;
                }
                mListView.setItemChecked(position, false);
                Intent intent = new Intent(TVShowFragment.this.getActivity(), TVShowActivity.class);
                TVShow tvShow = (TVShow) mAdapter.getItem(position);
                intent.putExtra(ARG_SERIES, tvShow.title);
                intent.putExtra(ARG_LINK, tvShow.link);
                intent.putExtra(ARG_IMAGE_URI, tvShow.imageUri);
                startActivity(intent);
            }
        });
        mListView.setOnScrollListener(this);

        registerForContextMenu(mListView);

        mEmptyView = (ViewStub) mRootView.findViewById(android.R.id.empty);
        mEmptyView.setOnInflateListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(2, null, this);
    }

    @Override
    public void onDestroyView() {
        // Release any references to avoid memory leaks
        mListView = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        menu.setHeaderTitle(((TVShow) mAdapter.getItem(position)).title);
        menu.add(FRAGMENT_ID, FAVORITES, 0, R.string.add_to_favourites);

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == FRAGMENT_ID && getUserVisibleHint()) {
            int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;

            switch (item.getItemId()) {
                case FAVORITES: {
                    TVShow tvShow = (TVShow) mAdapter.getItem(position);
                    if (!AppUtils.isFavorite(getActivity(), tvShow.title)) {
                        ContentValues values = new ContentValues();
                        values.put(FavoritesTable.COLUMN_TITLE, tvShow.title);
                        values.put(FavoritesTable.COLUMN_LINK, tvShow.link);
                        values.put(FavoritesTable.COLUMN_TYPE, AppUtils.TVSHOW);
                        values.put(FavoritesTable.COLUMN_IMAGE, tvShow.imageUri);
                        values.put(FavoritesTable.COLUMN_RATING, tvShow.rating);
                        getActivity().getContentResolver().insert(FavoritesContentProvider.CONTENT_URI, values);
                        Toast.makeText(this.getActivity(), tvShow.title + getString(R.string.favourites_added), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this.getActivity(), tvShow.title + getString(R.string.favourites_already_added), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<List<TVShow>> onCreateLoader(int id, Bundle args) {
        return new TVShowLoader(getActivity(), mStartPage, mFilter);
    }

    @Override
    public void onLoadFinished(Loader<List<TVShow>> loader, List<TVShow> tvShows) {

        mProgressBar.setVisibility(View.GONE);

        if (tvShows == null || tvShows.isEmpty()) {
            mListView.setEmptyView(mEmptyView);
            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getLoaderManager().restartLoader(2, null, TVShowFragment.this);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            });
        } else {

            if (mListView != null && mListView.getFooterViewsCount() == 0 && tvShows.size() == 24) {
                mListView.addFooterView(mFooterView);
            }

            if (mStartPage == 1 || mAdapter.getCount() < 24) {
                mAdapter.clear();
            }

            mAdapter.buildData(tvShows);
            mStartPage++;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<TVShow>> loader) {

    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mListView.getLastVisiblePosition() == totalItemCount - 10) {
            getLoaderManager().restartLoader(2, null, this);
        }
    }

    @Override
    public void onInflate(ViewStub stub, View view) {
        TextView textView = (TextView) view.findViewById(R.id.text1);
        if (mFilter == null) {
            textView.setText(R.string.empty_tv_shows);
        } else {
            textView.setText(String.format(getActivity().getString(R.string.empty_tv_shows_query), mFilter));
        }

        RobotoCheckBox checkBox = (RobotoCheckBox) view.findViewById(R.id.checkBox1);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        checkBox.setChecked(preferences.getBoolean(Config.ARG_USE_PROXY, false));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                preferences.edit().putBoolean(Config.ARG_USE_PROXY, checked).apply();
            }
        });
    }

    /**
     * Causes the MovieLoader to restart, passing in the searchQuery
     * Also clears the adapter of it's current movies, removes the footer progressbar,
     * displays the centered progressbar, and sets the loader's 'starting page' back to 1
     *
     * @param searchQuery string
     */
    public void restartWithSearchQuery(String searchQuery) {

        mFilter = searchQuery;

        if (mAdapter != null) {
            mAdapter.clear();
        }

        if (mListView != null && mListView.getFooterViewsCount() != 0) {
            mListView.removeFooterView(mFooterView);
        }

        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        mStartPage = 1;

        getLoaderManager().restartLoader(2, null, TVShowFragment.this);
    }
}