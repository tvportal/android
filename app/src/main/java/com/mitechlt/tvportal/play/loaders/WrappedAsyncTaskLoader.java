package com.mitechlt.tvportal.play.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Loader which extends AsyncTaskLoaders and handles caveats as pointed out in
 * http://code.google.com/p/android/issues/detail?id=14944.
 * <p/>
 * Based on CursorLoader.java in the Fragment compatibility package
 *
 * @param <D> data type
 * @author Alexander Blom (me@alexanderblom.se)
 */
public abstract class WrappedAsyncTaskLoader<D> extends AsyncTaskLoader<D> {

    private D mData;

    /**
     * Constructor for <code>WrappedAsyncTaskLoader</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public WrappedAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(D data) {
        if (!isReset()) {
            mData = data;
            super.deliverResult(data);
        } else {
            // An asynchronous query came in while the loader is stopped
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        mData = null;
    }

}
