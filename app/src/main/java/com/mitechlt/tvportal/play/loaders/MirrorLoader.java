package com.mitechlt.tvportal.play.loaders;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mitechlt.tvportal.play.activities.MovieActivity;
import com.mitechlt.tvportal.play.activities.TVShowActivity;
import com.mitechlt.tvportal.play.fragments.MirrorsFragment;
import com.mitechlt.tvportal.play.model.Mirror;
import com.mitechlt.tvportal.play.utils.Config;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link android.support.v4.content.AsyncTaskLoader} that processes the {@link com.mitechlt.tvportal.play.model.Mirror}s for the app
 */
public class MirrorLoader extends WrappedAsyncTaskLoader<List<Mirror>> {

    private final String TAG = getClass().getSimpleName();

    private final MirrorsFragment mMirrorsFragment;

    /**
     * The result of the operation
     */
    private final List<Mirror> mResult = new ArrayList<Mirror>();

    private String PRIMEWIRE_BASE_URL = "http://www.primewire.ag/";

    private String urlSuffix;

    SharedPreferences prefs;

    String url;

    /**
     * Constructor for <code>MirrorLoader</code>
     *
     * @param mirrorsFragment The {@link com.mitechlt.tvportal.play.fragments.MirrorsFragment} calling this loader
     * @param link            The link to the episode's mirrors
     */
    public MirrorLoader(MirrorsFragment mirrorsFragment, String link) {
        super(mirrorsFragment.getActivity());
        mMirrorsFragment = mirrorsFragment;
        urlSuffix = link;
    }

    @Override
    public List<Mirror> loadInBackground() {

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean(Config.ARG_USE_PROXY, false)) {
            Document doc;
            try {
                doc = Jsoup.connect("http://secureable.secure-tv-p.appspot.com/").data("url", "http:/" + urlSuffix).followRedirects(true).timeout(0).post();
                url = doc.baseUri();
            } catch (IOException ignored) {
            }
        } else {
            url = PRIMEWIRE_BASE_URL + urlSuffix;
        }

        ArrayList<String> mirrors = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();

        try {
            //  if(!url.equals(null)) {
            Document TVShowPage = Jsoup.connect(url).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(0).get();

            Elements elements = TVShowPage.select("table[class=movie_version],table[class=movie_version movie_version_alt");
            for (Element element : elements) {

                //1. Get the links
                Elements elements1 = element.select(".movie_version_link a");
                for (Element element1 : elements1) {
                    String mirrorUrl = element1.absUrl("href");
                    //   Pattern hdMoviePattern = Pattern.compile("gd=0", Pattern.LITERAL);
                    //  Matcher matcher = hdMoviePattern.matcher(mirrorUrl);
                    /**
                     * Todo:
                     * Find a better way to handle links for 'Part 2'. Currently, if the 'Part 2' link is added,
                     * because it falls under one 'mirror title', we end up with one extra link than there are
                     * mirrors. This means all mirror/link combinations below this will be off by one
                     *
                     * Todo:
                     * Find a better way to handle & disregard trailer links.
                     */
                    if (!element1.text().equals("Part 2") && !element1.text().equals("Watch Trailer") && !element1.text().equals("Trailer Page")) {
                        //We only want results that don't contain HD/Sponsored Links
                        links.add(mirrorUrl);
                    }
                }

                // 2. Get the mirror names
                Elements hosts = element.getElementsByClass("version_host");
                for (Element host : hosts) {
                    Elements scriptElements = host.getElementsByTag("script");
                    for (Element element1 : scriptElements) {
                        for (DataNode node : element1.dataNodes()) {
                            String data = node.getWholeData();
                            // if (!data.contains("HD Sponsor")) {
                            mirrors.add(data.substring(data.indexOf("'") + 1, data.lastIndexOf("'")));
                            //  }
                        }
                    }
                }
            }
            //}
        } catch (IOException ignored) {
        }
        ArrayList<String> allowedMirrors = new ArrayList<String>();
        if (prefs.getBoolean(Config.ARG_USE_PROXY, false)) {
            allowedMirrors.add("gorillavid.in");
            allowedMirrors.add("promptfile.com");
            allowedMirrors.add("thefile.me");
            //allowedMirrors.add("sockshare.com");
            //allowedMirrors.add("putlocker.com");
            //allowedMirrors.add("firedrive.com");
            allowedMirrors.add("nowvideo.eu");
            allowedMirrors.add("daclips.com");
            allowedMirrors.add("daclips.in");
            allowedMirrors.add("nowvideo.sx");
            allowedMirrors.add("divxstage.eu");
            allowedMirrors.add("movshare.net");
            allowedMirrors.add("videoweed.es");
            //allowedMirrors.add("novamov.com");
            //allowedMirrors.add("movpod.net");
            allowedMirrors.add("bestreams.net");
        } else {
            allowedMirrors.add("gorillavid.in");
            allowedMirrors.add("promptfile.com");
            allowedMirrors.add("thefile.me");
            allowedMirrors.add("sockshare.com");
            allowedMirrors.add("putlocker.com");
            allowedMirrors.add("firedrive.com");
            allowedMirrors.add("nowvideo.eu");
            allowedMirrors.add("daclips.com");
            allowedMirrors.add("daclips.in");
            allowedMirrors.add("nowvideo.sx");
            allowedMirrors.add("divxstage.eu");
            allowedMirrors.add("movshare.net");
            allowedMirrors.add("videoweed.es");
            //allowedMirrors.add("novamov.com");
            allowedMirrors.add("movpod.net");
            allowedMirrors.add("bestreams.net");
        }
        // Add a new Mirror for each title
        int size = mirrors.size();
        for (int i = 0; i < size; i++) {
            String mirror = mirrors.get(i);
            boolean ccmirror;
            ccmirror = mirror.equals("bestreams.net") ||
                    mirror.equals("gorillavid.in") ||
                    mirror.equals("thefile.me") ||
                    mirror.equals("promptfile.com") ||
                    mirror.equals("daclips.in");
            mirror = mirror.toLowerCase();
            if (allowedMirrors.contains(mirror)) {
                //Only add chromecast compatible mirrors if our chromecast is connected
                Activity parent = mMirrorsFragment.getActivity();
                if (parent instanceof MovieActivity) {
                    if (((MovieActivity) parent).isCastConnected()) {
                        if (ccmirror) {
                            mResult.add(new Mirror(mirror, links.get(i), true));
                        }
                    } else {
                        mResult.add(new Mirror(mirror, links.get(i), ccmirror));
                    }

                } else if (parent instanceof TVShowActivity) {
                    if (((TVShowActivity) parent).isCastConnected()) {
                        if (ccmirror) {
                            mResult.add(new Mirror(mirror, links.get(i), true));
                        }
                    } else {
                        mResult.add(new Mirror(mirror, links.get(i), ccmirror));
                    }
                }
            }
        }
        return mResult;
    }

}