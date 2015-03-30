package com.mitechlt.tvportal.play.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mitechlt.tvportal.play.model.Episode;
import com.mitechlt.tvportal.play.utils.AppUtils;
import com.mitechlt.tvportal.play.utils.Config;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link android.support.v4.content.AsyncTaskLoader} that processes the {@link com.mitechlt.tvportal.play.model.Episode}s for the app
 */
public class EpisodeLoader extends WrappedAsyncTaskLoader<List<Episode>> {

    private final String TAG = getClass().getSimpleName();

    /**
     * The result of the operation
     */
    private final List<Episode> mResult = new ArrayList<Episode>();

    private static final String PROXY_PRIMEWIRE_BASE_URL = "http://tv-ppy.appspot.com";

    private String PRIMEWIRE_BASE_URL = "http://www.primewire.ag/";

    private String urlSuffix;

    private String mSeason;

    private String mSeries;

    SharedPreferences prefs;

    /**
     * Constructor for <code>EpisodeLoader</code>
     *
     * @param context The {@link android.content.Context} to use
     * @param link    The link to this season
     */
    public EpisodeLoader(Context context, String series, String link, String season) {
        super(context);

        urlSuffix = link;
        mSeason = season;
        mSeries = series;

    }

    @Override
    public List<Episode> loadInBackground() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean(Config.ARG_USE_PROXY, false)) {
            PRIMEWIRE_BASE_URL = PROXY_PRIMEWIRE_BASE_URL.toString();
        }
        ArrayList<String> episodes = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();

        try {
            String url = PRIMEWIRE_BASE_URL + urlSuffix;

            Document TVShowPage = Jsoup.connect(url).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(0).get();

            Element element = TVShowPage.select("div.actual_tab").first();
            if (element == null) {
                return null;
            }
            Elements headings = element.select("div.tv_episode_item");

            for (Element heading : headings) {
                Element seasonLink = heading.select("a").first();
                episodes.add(seasonLink.text());
                links.add(seasonLink.attr("href"));
            }

        } catch (IOException ignored) {
        }

        // Add a new Episode for each title
        int size = episodes.size();
        for (int i = 0; i < size; i++) {

            String title;
            String series;
            String episode;

            // Remove text after and including "-".
            // For example 'Episode 1 - The fuckwit continuum' becomes 'Episode 1'
            // I hate Big Bang Theory
            String text = episodes.get(i);
            if (text.contains("-")) {
                try {
                    title = text.substring(text.indexOf("-") + 2, text.length());
                    episode = text.substring(0, text.indexOf("-") - 1);
                } catch (StringIndexOutOfBoundsException e) {
                    Log.e("Episode Adapter", e.toString());
                    title = "Unknown";
                    episode = text;
                }
            } else {
                title = "Unknown";
                episode = text;
            }

            mResult.add(new Episode(title, mSeries, links.get(i), episode, mSeason, AppUtils.isWatched(getContext(), episode, mSeason)));
        }
        return mResult;
    }

}
