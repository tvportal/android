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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoCheckBox;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.activities.MovieActivity;
import com.mitechlt.tvportal.play.activities.TVShowActivity;
import com.mitechlt.tvportal.play.adapters.RecentAdapter;
import com.mitechlt.tvportal.play.databases.RecentContentProvider;
import com.mitechlt.tvportal.play.databases.RecentTable;
import com.mitechlt.tvportal.play.utils.AppUtils;
import com.mitechlt.tvportal.play.utils.Config;

import java.util.ArrayList;

/**
 * A fragment containing a GridView and adapter to display a list of recently watched Movies & Shows
 */
public class RecentFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ViewStub.OnInflateListener {

    private RecentAdapter mAdapter;
    private GridView mGridView;
    private Cursor mRecentCursor;
    private ViewStub mEmptyView;
    private static final String ARG_SERIES = "series";
    private static final String ARG_TITLE = "title";
    private static final String ARG_IMAGE_URI = "img_uri";
    private static final String ARG_LINK = "link";
    private static final String ARG_RATING = "rating";
    private String mLink;


    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static RecentFragment newInstance() {
        final RecentFragment fragment = new RecentFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public RecentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RecentAdapter(this.getActivity(), R.layout.list_item_card, null, new String[]{}, new int[]{});

        setHasOptionsMenu(true);
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_grid_cards, container, false);

        mGridView = (GridView) rootView.findViewById(android.R.id.list);
        mGridView.setAdapter(mAdapter);
        mGridView.setFastScrollEnabled(true);
        mGridView.setSmoothScrollbarEnabled(true);

        //CAB for API>11
        if (AppUtils.hasHoneycomb()) {
            mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mGridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @SuppressLint("NewApi")
                @Override
                public void onItemCheckedStateChanged(android.view.ActionMode actionMode, int position, long id, boolean b) {
                    int numCheckedItems = mGridView.getCheckedItemCount();
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
                        SparseBooleanArray checkedItemPositions = mGridView.getCheckedItemPositions();

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
                            mRecentCursor.moveToPosition(position);
                            String title = mRecentCursor.getString(mRecentCursor.getColumnIndex(RecentTable.COLUMN_TITLE));
                            if (title != null) {
                                title = title.replace("'", "''");
                                String where = RecentTable.COLUMN_TITLE + "='" + title + "'";
                                getActivity().getContentResolver().delete(RecentContentProvider.CONTENT_URI, where, null);
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

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (AppUtils.getRecentType(RecentFragment.this.getActivity(), position).equals(AppUtils.MOVIE)) {
                    //User clicked on a movie
                    mLink = AppUtils.getRecentLink(RecentFragment.this.getActivity(), position);

                    Intent intent = new Intent(RecentFragment.this.getActivity(), MovieActivity.class);
                    intent.putExtra(ARG_TITLE, AppUtils.getRecentTitle(RecentFragment.this.getActivity(), position));
                    intent.putExtra(ARG_LINK, mLink);
                    intent.putExtra(ARG_IMAGE_URI, AppUtils.getRecentImage(getActivity(), position));
                    intent.putExtra(ARG_RATING, AppUtils.getRecentRating(getActivity(), position));
                    startActivity(intent);

                } else if (AppUtils.getRecentType(RecentFragment.this.getActivity(), position).equals(AppUtils.TVSHOW)) {
                    //User clicked on a TV show
                    mLink = AppUtils.getRecentLink(RecentFragment.this.getActivity(), position);
                    mLink = mLink.substring(0, mLink.lastIndexOf("/"));

                    Intent intent = new Intent(RecentFragment.this.getActivity(), TVShowActivity.class);
                    intent.putExtra(ARG_SERIES, AppUtils.getRecentTitle(RecentFragment.this.getActivity(), position));
                    intent.putExtra(ARG_LINK, mLink);
                    intent.putExtra(ARG_IMAGE_URI, AppUtils.getRecentImage(getActivity(), position));
                    intent.putExtra(ARG_RATING, AppUtils.getRecentRating(getActivity(), position));
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
        menu.removeItem(R.id.action_sort);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        // Release any references to avoid memory leaks
        mGridView = null;
        super.onDestroyView();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String sortOrder = prefs.getString(Config.SORT_ORDER_RECENT, Config.SORT_RECENT_DEFAULT);

        String[] projection = {
                RecentTable.COLUMN_TITLE,
                RecentTable.COLUMN_LINK,
                RecentTable.COLUMN_IMAGE,
                RecentTable.COLUMN_ID,
                RecentTable.COLUMN_TYPE,
                RecentTable.COLUMN_NUM_SEASONS,
                RecentTable.COLUMN_SEASON,
                RecentTable.COLUMN_EPISODE,
                RecentTable.COLUMN_NUM_EPISODES,
                RecentTable.COLUMN_RATING};

        return new CursorLoader(this.getActivity(),
                RecentContentProvider.CONTENT_URI, projection, null, null, sortOrder);
    }

    private int getScreenOrientation() {
        return getResources().getConfiguration().orientation;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.getCount() == 0) {
            mGridView.setEmptyView(mEmptyView);
        }
        mAdapter.swapCursor(cursor);
        mRecentCursor = mAdapter.getCursor();
        mAdapter.processData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onInflate(ViewStub viewStub, View view) {
        TextView textView = (TextView) view.findViewById(R.id.text1);
        textView.setText(R.string.empty_recents);

        //Don't show the proxy option in an empty recent fragment
        RobotoCheckBox checkBox = (RobotoCheckBox) view.findViewById(R.id.checkBox1);
        checkBox.setVisibility(View.GONE);
    }
}
