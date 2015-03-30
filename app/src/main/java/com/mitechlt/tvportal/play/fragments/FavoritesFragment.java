package com.mitechlt.tvportal.play.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoCheckBox;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.activities.MovieActivity;
import com.mitechlt.tvportal.play.activities.TVShowActivity;
import com.mitechlt.tvportal.play.adapters.FavoriteAdapter;
import com.mitechlt.tvportal.play.databases.FavoritesContentProvider;
import com.mitechlt.tvportal.play.databases.FavoritesTable;
import com.mitechlt.tvportal.play.utils.AppUtils;
import com.mitechlt.tvportal.play.utils.Config;

import java.util.ArrayList;

/**
 * A fragment containing a ListView and adapter to display a list of 'favorited' movies & TV shows
 */
@SuppressLint("NewApi")
public class FavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ViewStub.OnInflateListener {

    private FavoriteAdapter mAdapter;
    private ListView mListView;
    private Cursor mFavouriteCursor;
    private ViewStub mEmptyView;
    private static final String ARG_TITLE = "title";
    private static final String ARG_SERIES = "series";
    private static final String ARG_LINK = "link";
    private static final String ARG_IMAGE_URI = "img_uri";
    private static final String ARG_RATING = "rating";
    SharedPreferences mPrefs;

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static FavoritesFragment newInstance() {
        final FavoritesFragment fragment = new FavoritesFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FavoritesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(FavoritesFragment.this.getActivity());
        mAdapter = new FavoriteAdapter(this.getActivity(), R.layout.list_item_card, null, new String[]{}, new int[]{});

        setHasOptionsMenu(true);
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list_cards, container, false);

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setSmoothScrollbarEnabled(true);

        //CAB for API>11
        if (AppUtils.hasHoneycomb()) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @SuppressLint("NewApi")
                @Override
                public void onItemCheckedStateChanged(android.view.ActionMode actionMode, int position, long id, boolean b) {
                    int numCheckedItems = mListView.getCheckedItemCount();
                    String numSelected = "";
                    if (numCheckedItems > 0) {
                        numSelected = getActivity().getResources().getQuantityString(R.plurals.selected_items, numCheckedItems, numCheckedItems);
                    }
                    actionMode.setTitle(numSelected);
                }

                @Override
                public boolean onCreateActionMode(android.view.ActionMode actionMode, Menu menu) {
                    getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode actionMode, MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_remove) {

                        //The list of actual listItem position we're going to retrieve
                        ArrayList<Integer> checkedListPositions = new ArrayList<Integer>();

                        //The list of selected item positions
                        SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();

                        if (checkedItemPositions != null) {
                            int size = checkedItemPositions.size();
                            for (int i = 0; i < size; i++) {
                                //Get the actual list position for this item
                                int actualPos = checkedItemPositions.keyAt(i);
                                //Check if this item is actually selected
                                if (checkedItemPositions.valueAt(i)) {
                                    checkedListPositions.add(actualPos);
                                }
                            }
                        }

                        for (int position : checkedListPositions) {
                            mFavouriteCursor.moveToPosition(position);
                            String title = mFavouriteCursor.getString(mFavouriteCursor.getColumnIndex(FavoritesTable.COLUMN_TITLE));
                            if (title != null) {
                                title = title.replace("'", "''");
                                String where = FavoritesTable.COLUMN_TITLE + "='" + title + "'";
                                getActivity().getContentResolver().delete(FavoritesContentProvider.CONTENT_URI, where, null);
                            }
                        }
                    }

                    actionMode.finish();
                    return true;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode actionMode) {

                }
            });
        } else {
            //Todo: Just use the context menus
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListView.setItemChecked(position, false);

                if (AppUtils.getFavoriteType(getActivity(), position).equals(AppUtils.MOVIE)) {
                    //User clicked on a movie
                    String mLinkmov = AppUtils.getFavoriteLink(getActivity(), position);
                    if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {//proxyon change url to proxy
                        System.out.println(mPrefs.getBoolean(Config.ARG_USE_PROXY, false));
                        if (!mLinkmov.contains("primewire.ag")) {
                            System.out.println(mLinkmov);
                            mLinkmov = "/www.primewire.ag" + mLinkmov;
                            System.out.println(mLinkmov);
                        }
                    }
                    //proxyoff change url to noproxy
                    else {
                        if (mLinkmov.contains("primewire.ag")) {
                            mLinkmov = mLinkmov.replace("/www.primewire.ag", "");
                        }
                    }
                    Intent intent = new Intent(getActivity(), MovieActivity.class);
                    intent.putExtra(ARG_TITLE, AppUtils.getFavoriteTitle(FavoritesFragment.this.getActivity(), position));
                    intent.putExtra(ARG_IMAGE_URI, AppUtils.getFavoriteImageUri(getActivity(), position));//
                    intent.putExtra(ARG_LINK, mLinkmov);
                    intent.putExtra(ARG_RATING, AppUtils.getFavoriteRating(getActivity(), position));
                    startActivity(intent);
                } else if (AppUtils.getFavoriteType(getActivity(), position).equals(AppUtils.TVSHOW)) {
                    //User clicked on a TV show
                    String link = AppUtils.getFavoriteLink(getActivity(), position);
                    if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                        //Proxy is on. Update url accordingly
                        System.out.println(mPrefs.getBoolean(Config.ARG_USE_PROXY, false));
                        if (!link.contains("primewire.ag")) {
                            link = "/www.primewire.ag" + link;
                        }
                    }
                    //Proxy is off. Update url accordingly
                    else {
                        if (link.contains("primewire.ag")) {
                            link = link.replace("/www.primewire.ag", "");
                        }
                    }
                    Intent intent = new Intent(getActivity(), TVShowActivity.class);
                    intent.putExtra(ARG_SERIES, AppUtils.getFavoriteTitle(getActivity(), position));
                    intent.putExtra(ARG_LINK, link);
                    intent.putExtra(ARG_IMAGE_URI, AppUtils.getFavoriteImageUri(getActivity(), position));
                    intent.putExtra(ARG_RATING, AppUtils.getFavoriteRating(getActivity(), position));
                    startActivity(intent);
                }
            }
        });

        mEmptyView = (ViewStub) rootView.findViewById(android.R.id.empty);
        mEmptyView.setOnInflateListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getUserVisibleHint()) {
            inflater.inflate(R.menu.favorites_menu, menu);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            String sortOrder = prefs.getString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_DEFAULT);
            if (sortOrder.equals(Config.SORT_FAVORITE_DEFAULT)) {
                MenuItem item = menu.findItem(R.id.sort_default);
                if (item != null) {
                    item.setChecked(true);
                }
            } else if (sortOrder.equals(Config.SORT_FAVORITE_MOVIES_AZ)) {
                MenuItem item = menu.findItem(R.id.sort_movies_az);
                if (item != null) {
                    item.setChecked(true);
                }
            } else if (sortOrder.equals(Config.SORT_FAVORITE_TV_SHOWS_AZ)) {
                MenuItem item = menu.findItem(R.id.sort_tvshows_az);
                if (item != null) {
                    item.setChecked(true);
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

            switch (item.getItemId()) {

                case R.id.sort_default: {
                    prefs.edit().putString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_DEFAULT).apply();
                    break;
                }

                case R.id.sort_movies_az: {
                    prefs.edit().putString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_MOVIES_AZ).apply();
                    break;
                }

                case R.id.sort_tvshows_az: {
                    prefs.edit().putString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_TV_SHOWS_AZ).apply();
                    break;
                }
            }

            item.setChecked(true);

            getLoaderManager().restartLoader(0, null, this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        // Release any references to avoid memory leaks
        mListView = null;
        super.onDestroyView();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String sortOrder = prefs.getString(Config.SORT_ORDER_FAVORITE, Config.SORT_FAVORITE_DEFAULT);

        String[] projection = {
                FavoritesTable.COLUMN_ID,
                FavoritesTable.COLUMN_TITLE,
                FavoritesTable.COLUMN_TYPE,
                FavoritesTable.COLUMN_IMAGE};

        return new CursorLoader(this.getActivity(),
                FavoritesContentProvider.CONTENT_URI, projection, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.getCount() == 0) {
            mListView.setEmptyView(mEmptyView);
        }
        mAdapter.swapCursor(cursor);
        mFavouriteCursor = mAdapter.getCursor();
        mAdapter.processData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onInflate(ViewStub viewStub, View view) {
        TextView textView = (TextView) view.findViewById(R.id.text1);
        textView.setText(R.string.empty_favourites);

        //Don't show the proxy option in an empty favorites fragment
        RobotoCheckBox checkBox = (RobotoCheckBox) view.findViewById(R.id.checkBox1);
        checkBox.setVisibility(View.GONE);
    }
}
