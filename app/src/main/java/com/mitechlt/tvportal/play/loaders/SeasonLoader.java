package com.mitechlt.tvportal.play.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mitechlt.tvportal.play.model.Season;
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
 * An {@link android.support.v4.content.AsyncTaskLoader} that processes the {@link com.mitechlt.tvportal.play.model.Season}s for the app
 */
public class SeasonLoader extends WrappedAsyncTaskLoader<List<Season>> {

    private final String TAG = getClass().getSimpleName();

    /**
     * The result of the operation
     */
    private final List<Season> mResult = new ArrayList<Season>();

    private static final String PROXY_PRIMEWIRE_BASE_URL = "http://tv-ppy.appspot.com";

    private String PRIMEWIRE_BASE_URL = "http://www.primewire.ag/";

    private String urlSuffix;

    SharedPreferences prefs;

    /**
     * Constructor for <code>SeasonLoader</code>
     *
     * @param context The {@link android.content.Context} to use
     * @param link    The link to the season
     */
    public SeasonLoader(Context context, String link) {
        super(context);

        urlSuffix = link;
    }

    @Override
    public List<Season> loadInBackground() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean(Config.ARG_USE_PROXY, false)) {
            PRIMEWIRE_BASE_URL=PROXY_PRIMEWIRE_BASE_URL.toString();
        }

        ArrayList<String> seasons = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();

        try {
            String url = PRIMEWIRE_BASE_URL + urlSuffix;
            Document TVShowPage = Jsoup.connect(url).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(0).get();

            Elements element = TVShowPage.select("div.actual_tab");
            if (element == null) {
                return null;
            }
            Elements headings = element.select("h2");



            for (Element heading : headings) {
                Element seasonLink = heading.select("a").first();

                        if (heading.nextElementSibling().toString().contains("tv_episode_name")) {
                            if (!heading.nextElementSibling().select("span.tv_episode_name").toString().toLowerCase().contains("do not add links")) {
                                seasons.add(seasonLink.text());
                            }
                        }else if (heading.nextElementSibling().select("a").toString().contains("episode-0")) {
                            seasons.add(seasonLink.text());
                    }
                links.add(seasonLink.attr("href"));
        }

        } catch (IOException ignored) {
        }

        // Add a new Season for each title
        int size = seasons.size();
        for (int i = 0; i < size; i++) {
            String season = seasons.get(i);
            mResult.add(new Season(season, links.get(i)));
        }
        return mResult;
    }

}
