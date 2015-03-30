package com.mitechlt.tvportal.play.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.model.TVShow;
import com.mitechlt.tvportal.play.utils.Config;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link android.support.v4.content.AsyncTaskLoader} that processes the {@link com.mitechlt.tvportal.play.model.TVShow}s for the app
 */
public class TVShowLoader extends WrappedAsyncTaskLoader<List<TVShow>> {

    private final String TAG = getClass().getSimpleName();

    /**
     * The result of the operation
     */
    private static String BASE_URL = null;

    private final List<TVShow> mResult = new ArrayList<TVShow>();

    private static final String PROXY_PRIMEWIRE_BASE_URL = "http://tv-ppy.appspot.com/www.primewire.ag/";

    private static final String PRIMEWIRE_BASE_URL = "http://www.primewire.ag/";

    private static final String PRIMEWIRE_POPULAR_SUFFIX = "?tv&sort=views&page=";

    private static final String PRIMEWIRE_SEARCH_SUFFIX = "?search_keywords=";

    private static final String PRIMEWIRE_KEY_SUFFIX = "&key=";

    private static final String PRIMEWIRE_PAGE_SUFFIX = "&page=";

    private static final String PRIMEWIRE_SORT_SUFFIX = "&sort=views";

    private static final String PRIMEWIRE_SECTION_SUFFIX = "&search_section=2";

    private int mStartPage = 0;

    private String mSearchQuery;

    SharedPreferences prefs;

    /**
     * Constructor for <code>TVShowLoader</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public TVShowLoader(Context context, int startPage, String searchQuery) {
        super(context);
        mStartPage = startPage;
        mSearchQuery = searchQuery;
    }

    @Override
    public List<TVShow> loadInBackground() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean(Config.ARG_USE_PROXY, false)) {
            BASE_URL = PROXY_PRIMEWIRE_BASE_URL.toString();
        } else {
            BASE_URL = PRIMEWIRE_BASE_URL.toString();
        }

        //We've been passed a search query, so handle that
        if (mSearchQuery != null) {
            try {
                //1. Get the key used for searching
                Document doc = Jsoup.connect(BASE_URL).get();
                Element element = doc.select("input[name=key]").first();
                if (element == null) {
                    return null;
                }
                String key = element.attr("value");

                //2. Perform the search
                String searchUrl = BASE_URL + PRIMEWIRE_SEARCH_SUFFIX + mSearchQuery
                        + PRIMEWIRE_KEY_SUFFIX + key +
                        PRIMEWIRE_SECTION_SUFFIX + PRIMEWIRE_SORT_SUFFIX;
                Document searchPage = Jsoup.connect(searchUrl).userAgent("Mozilla").get();

                fillLists(searchPage);

            } catch (IOException ignored) {
            }

        } else {

            try {
                String url = BASE_URL + PRIMEWIRE_POPULAR_SUFFIX + mStartPage;
                Document popularTVshowPage = Jsoup.connect(url).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(0).get();

                fillLists(popularTVshowPage);

            } catch (IOException ignored) {
            }
        }

        return mResult;
    }

    /**
     * Parses an {@link org.jsoup.nodes.Document} and fills the ArrayLists necessary to return
     * an {@link java.util.ArrayList<com.mitechlt.tvportal.play.model.TVShow>}
     * @param document the page to process
     */
    private void fillLists(Document document) {

        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<String> imageUris = new ArrayList<String>();
        ArrayList<Integer> ratings = new ArrayList<Integer>();
        ArrayList<String> years = new ArrayList<String>();
        ArrayList<String> genres = new ArrayList<String>();

        Elements popularMovieElements = document.select(".index_item_ie a , .item_categories , .current-rating");

        for (Element element : popularMovieElements) {

            //Get the title
            String title = element.attr("title");
            if (!title.equals("") && title.length() > 12) {
                titles.add(title.substring(6, title.length() - 7));

                //Get the year (substring of the title)
                years.add(title.substring(title.length() - 5, title.length() - 1));

                //Get the link to the movie
                links.add(element.attr("href"));

                //Get image url
                Elements imageElements = element.getElementsByTag("img");
                if (imageElements != null) {
                    if (prefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                        imageUris.add("http://tv-ppy.appspot.com"+imageElements.attr("src"));
                    }else{
                    imageUris.add(imageElements.attr("src"));}
                } else {
                    imageUris.add(null);
                }

                //Get the rating
                Element nextElement = element.nextElementSibling();
                if (nextElement != null) {
                    Element ratingElement = nextElement.getElementsByClass("current-rating").first();
                    if (ratingElement != null && ratingElement.attr("style") != null && !TextUtils.isEmpty(ratingElement.attr("style"))) {
                        ratingElement.getElementsByClass("current-rating");
                        String style = ratingElement.attr("style");
                        String width = style.substring(7, style.length() - 3);
                        ratings.add(Integer.valueOf(width));
                    } else {
                        ratings.add(0);
                    }
                } else {
                    ratings.add(0);
                }

                //Get the genres
                if (nextElement != null) {
                    Element genreElement = nextElement.nextElementSibling();
                    if (genreElement != null && genreElement.text() != null && !TextUtils.isEmpty(genreElement.text())) {
                        genres.add(genreElement.text().replaceAll(" ", "|"));
                    } else {
                        genres.add(getContext().getString(R.string.unknown));
                    }
                }

            }
        }

        // Add a new Movie for each title.
        int size = titles.size();
        for (int i = 0; i < size; i++) {
            String title = titles.get(i);
            mResult.add(new TVShow(title, links.get(i), imageUris.get(i), ratings.get(i), years.get(i), genres.get(i)));
        }
    }
}
