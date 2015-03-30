package com.mitechlt.tvportal.play.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.mitechlt.tvportal.play.CastApplication;
import com.mitechlt.tvportal.play.R;
import com.mitechlt.tvportal.play.activities.LocalPlayerActivity;
import com.mitechlt.tvportal.play.activities.SettingsActivity;
import com.mitechlt.tvportal.play.databases.RecentContentProvider;
import com.mitechlt.tvportal.play.databases.RecentTable;
import com.mitechlt.tvportal.play.utils.AppUtils;
import com.mitechlt.tvportal.play.utils.Config;
import com.revmob.RevMob;
import com.revmob.ads.fullscreen.RevMobFullscreen;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;


public class AsyncLinkParser extends AsyncTask<String, Void, String> {

    public static final String TAG = "AsyncLinkParser";

    private Context mContext;

    public ProgressDialog mProgressDialog;

    VideoCastManager mCastManager;

    private RevMobFullscreen fullscreen;

//    private AdManager mManager;

    private String mSeason;
    private String mEpisode;
    private String mTitle;
    private String mImageUri;
    private String mLink;
    private int mNumSeasons;
    private int mNumEpisodes;
    private int mRating;


    SharedPreferences mPrefs;

    /**
     * @param context the context
     * @param title   the title of this movie/episode
     * @param season  the season this episode belongs to (or null if a movie)
     * @param link    the link to the mirrors page (not the mirror's link itself)
     */
    public AsyncLinkParser(Context context, String title, String episode, String season, String link, String imgUri, int numSeasons, int numEpisodes, int rating) {
        mContext = context;
        mTitle = title;
        mSeason = season;
        mEpisode = episode;
        mLink = link;
        mImageUri = imgUri;
        mNumSeasons = numSeasons;
        mNumEpisodes = numEpisodes;
        mRating = rating;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        mCastManager = CastApplication.getCastManager(mContext);
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setTitle(mContext.getResources().getString(R.string.loading));
        mProgressDialog.setMessage(mContext.getResources().getString(R.string.fetching_link));
        mProgressDialog.show();

        //Load interstitial ad
        if (AppUtils.canShowAds(mContext)) {

            RevMob revmob = RevMob.start((Activity) mContext, Config.REVMOB_AD_ID);
            fullscreen = revmob.createFullscreen((Activity) mContext, null); // pre-load it without showing it
        }
    }

    @Override
    protected String doInBackground(String... params) {

        String mirrorName = params[0].toLowerCase();
        String mirrorLink = params[1];

        if (mirrorLink == null || mirrorLink.length() == 0) {
            return null;
        }

        String urlToReturn = null;

        try {

            if (mirrorName.equals("gorillavid.in")) {
//                String frameUrl;
//                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
//                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
//
//                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
//                    Element frameproxy = doc.select("noframes").last();
//                    if (frameproxy == null) {
//                        return null;
//                    }
//                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
//                } else {
//                    Element frame = doc.select("frame").last();
//                    if (frame == null) {
//                        return null;
//                    }
//                    frameUrl = frame.attr("src");
//                }
                // Get the actual video mMirrorLink from the GorillaVid mobile site
                Document doc2 = Jsoup.connect(mirrorLink).timeout(0).get();
                Elements scripts = doc2.getElementsByTag("script");
                for (Element script : scripts) {
                    for (DataNode dataNode : script.dataNodes()) {
                        String nodeText = dataNode.toString();
                        if (nodeText.contains("mp4")) {
                            urlToReturn = nodeText.substring(88, 181);
                        }
                    }
                }

            } else if (mirrorName.equals("promptfile.com")) {

//                String frameUrl;
//                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
//                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
//                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
//                    Element frameproxy = doc.select("noframes").last();
//                    if (frameproxy == null) {
//                        return null;
//                    }
//                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
//                } else {
//                    Element frame = doc.select("frame").last();
//                    if (frame == null) {
//                        return null;
//                    }
//                    frameUrl = frame.attr("src");
//                }

                // Get the actual video mMirrorLink from the PromptFile mobile site
                Document doc2 = Jsoup.connect(mirrorLink).timeout(300000).get();
                Elements links = doc2.select("input");
                String url2 = links.attr("value");

                HttpConnection.Response doc3 = (HttpConnection.Response) Jsoup.connect(mirrorLink)
                        .data("action", "")
                        .data("chash", url2).method(Connection.Method.POST)
                        .execute();

                Map<String, String> loginCookies = doc3.cookies();

                Document doc4 = Jsoup.connect(mirrorLink)
                        .cookies(loginCookies)
                        .get();

                Elements elements = doc4.getElementById("view_cont").select("a");
                urlToReturn = elements.attr("href");


            } else if (mirrorName.equals("thefile.me")) {

             /*   String frameUrl;
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                    Element frameproxy = doc.select("noframes").last();
                    if (frameproxy == null) {
                        return null;
                    }
                    frameUrl = frameproxy.toString().replace("<noframes>", "").replaceAll("\\s+", "").replace("</noframes>", "");
                } else {
                    Element frame = doc.select("frame").last();
                    if (frame == null) {
                        return null;
                    }
                    frameUrl = frame.attr("src");
                }*/

                //get the actual mMirrorLink to the hosting site
                Document doc2 = Jsoup.connect(mirrorLink).timeout(10000).get();
                Element element = doc2.getElementsByAttribute("name").get(6);
                String endUrl = element.attr("value");

                // Open up the second page to find the remainder of the mMirrorLink
                mirrorLink = doc2.baseUri().substring(18).replaceAll("\\s+", "") + "-910x405.html";
                String embededurl = "http://thefile.me/embed-" + mirrorLink;
                Document doc3 = Jsoup.connect(embededurl).timeout(10000).get();
                Element element1 = doc3.getElementsByTag("script").last();
                String startUrl = null;
                if (element1 == null) {
                    return null;
                }
                for (DataNode Node : element1.dataNodes()) {
                    if (Node.getWholeData().startsWith("eval")) {
                        int Index = Node.getWholeData().indexOf("|video|");
                        int Indexend = Node.getWholeData().indexOf("|file|");

                        startUrl = Node.getWholeData().substring(Index + 7, Indexend);
                    }
                }

                if (startUrl != null && endUrl != null) {
                    urlToReturn = "http://dd.thefile.me/d/" + startUrl + "/" + endUrl;
                }
            } else if (mirrorName.equals("sockshare.com")) {

                Document doc = Jsoup.connect(mirrorLink).timeout(0).get();

                //We don't use the standard primewire frame here. Perform a meta refresh
                Elements meta = doc.select("html head meta");
                if (meta.attr("http-equiv").contains("REFRESH"))
                    doc = Jsoup.connect(meta.attr("content").split("=")[1]).get();

                //Get input value for post
                Document doc2 = Jsoup.connect(doc.baseUri()).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();
                Elements links = doc2.select("input");
                String url = links.attr("value");

                // Post that value
                Connection.Response doc3 = Jsoup.connect(doc.baseUri())
                        .userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                        .data("hash", url)
                        .data("confirm", "Click Here to Continue ")
                        .method(Connection.Method.POST)
                        .execute();

                // Get cookies from post for redirect
                Map<String, String> loginCookies = doc3.cookies();

                // Do that refresh with a meta refresh
                Elements meta2 = doc2.select("html head meta");
                if (meta2.attr("http-equiv").contains("REFRESH"))
                    doc = Jsoup.connect(meta2.attr("content").split("=")[1]).get();

                // Get the mMirrorLink to mMirrorLink page
                Document doc4 = Jsoup.connect(doc.baseUri()).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                        .timeout(15000)
                        .cookies(loginCookies)
                        .get();
                links = doc4.select(".download_buttton a");
                for (Element Element_Href_Name : links) {
                    String linktolink = Element_Href_Name.absUrl("href");

                    // Connect to mMirrorLink to do refresh ignore content type or will crash
                    Document doc5 = Jsoup.connect(linktolink).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                            .ignoreContentType(true)
                            .timeout(15000)
                            .cookies(loginCookies)
                            .get();

                    // Do another meta refresh for redirect. It's what gives us the mMirrorLink (there is no text on this page)
                    // The redirect url is the mMirrorLink
                    if (meta2.attr("http-equiv").contains("REFRESH"))
                        doc5 = Jsoup.connect(meta2.attr("content").split("=")[1]).get();

                    urlToReturn = doc5.baseUri();
                }
              //added firedrive same as pulocker
            } else if (mirrorName.equals("putlocker.com") || mirrorName.equals("firedrive.com")) {

                Document doc = Jsoup.connect(mirrorLink).timeout(15000).get();

                // Perform a meta refresh
                Elements meta = doc.select("html head meta");
                if (meta.attr("http-equiv").contains("REFRESH"))
                    doc = Jsoup.connect(meta.attr("content").split("=")[1]).get();

                Document doc2 = Jsoup.connect(doc.baseUri()).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();
                Elements links = doc2.select("div#download_menu a");
                urlToReturn = links.attr("href");

            } else if (mirrorName.equals("videoweed.es")) {

              /*  String frameUrl;
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                    Element frameproxy = doc.select("noframes").last();
                    if (frameproxy == null) {
                        return null;
                    }
                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
                } else {
                    Element frame = doc.select("frame").last();
                    if (frame == null) {
                        return null;
                    }
                    frameUrl = frame.attr("src");
                }*/

                //seems stupid but has to be done for the redirect to give us proper url
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();

                int startindex = doc.baseUri().indexOf("/videos/") + 8;
                String number = doc.baseUri().substring(startindex);

                Document doc2 = Jsoup.connect("http://www.videoweed.es/mobile/video.php?id=" + number).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();

                Element element1 = doc2.select("video#player source").last();

                if (element1 != null) {
                    urlToReturn = element1.attr("src");
                }

            } else if (mirrorName.equals("nowvideo.eu")) {

               /* String frameUrl;
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                    Element frameproxy = doc.select("noframes").last();
                    if (frameproxy == null) {
                        return null;
                    }
                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
                } else {
                    Element frame = doc.select("frame").last();
                    if (frame == null) {
                        return null;
                    }
                    frameUrl = frame.attr("src");
                }*/
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                int startindex = doc.baseUri().indexOf("/videos/") + 8;
                String number = doc.baseUri().substring(startindex);

                Document doc2 = Jsoup.connect("http://www.nowvideo.eu/mobile/video.php?id=" + number).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();
                Element element1 = doc2.select("video#player source").last();

                if (element1 != null) {
                    urlToReturn = element1.attr("src");
                }

            } else if (mirrorName.equals("nowvideo.sx")) {

               /* String frameUrl;
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                    Element frameproxy = doc.select("noframes").last();
                    if (frameproxy == null) {
                        return null;
                    }
                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
                } else {
                    Element frame = doc.select("frame").last();
                    if (frame == null) {
                        return null;
                    }
                    frameUrl = frame.attr("src");
                }*/
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                int startindex = doc.baseUri().indexOf("/videos/") + 8;
                String number = doc.baseUri().substring(startindex);

                Document doc2 = Jsoup.connect("http://www.nowvideo.sx/mobile/video.php?id=" + number).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();
                Element element1 = doc2.select("video#player source").last();

                if (element1 != null) {
                    urlToReturn = element1.attr("src");
                }


            } else if (mirrorName.equals("divxstage.eu")) {

                /*String frameUrl;
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                    Element frameproxy = doc.select("noframes").last();
                    if (frameproxy == null) {
                        return null;
                    }
                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
                } else {
                    Element frame = doc.select("frame").last();
                    if (frame == null) {
                        return null;
                    }
                    frameUrl = frame.attr("src");
                }*/
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                int startindex = doc.baseUri().indexOf("/videos/") + 8;
                String number = doc.baseUri().substring(startindex);

                Document doc2 = Jsoup.connect("http://www.divxstage.eu/mobile/video.php?id=" + number).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();
                Element element1 = doc2.select("video#player source").last();

                if (element1 != null) {
                    urlToReturn = element1.attr("src");
                }

            } else if (mirrorName.equals("movshare.net")) {

                /*String frameUrl;
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                    Element frameproxy = doc.select("noframes").last();
                    if (frameproxy == null) {
                        return null;
                    }
                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
                } else {
                    Element frame = doc.select("frame").last();
                    if (frame == null) {
                        return null;
                    }
                    frameUrl = frame.attr("src");
                }*/

                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                //were getting the index of the start and end of url
                int startindex = doc.baseUri().indexOf("/videos/") + 8;
                String number = doc.baseUri().substring(startindex);

                Document doc2 = Jsoup.connect("http://www.movshare.net/mobile/video.php?id=" + number).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();
                Element element1 = doc2.select("video#player source").last();

                if (element1 != null) {
                    urlToReturn = element1.attr("src");
                }
            } else if (mirrorName.equals("bestreams.net")) {

               /* String frameUrl;
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                    Element frameproxy = doc.select("noframes").last();
                    if (frameproxy == null) {
                        return null;
                    }
                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
                } else {
                    Element frame = doc.select("frame").last();
                    if (frame == null) {
                        return null;
                    }
                    frameUrl = frame.attr("src");
                }*/

                // getting video id
                Document doc5 = Jsoup.connect(mirrorLink).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();

                if (doc5.toString().contains("The file you were looking for could not be found")) {
                    return null;
                }
                Element links2 = doc5.select("input").get(2);
                String id = links2.attr("value");

                //connect to embeded page with id
                Document doc6 = Jsoup.connect("http://bestreams.net/embed-" + id + "-950x562.html").referrer(doc5.baseUri()).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Chrome").get();

                //use your code to pull mMirrorLink from page
                Elements scripts = doc6.getElementsByTag("script");
                for (Element script : scripts) {
                    for (DataNode Node : script.dataNodes()) {
                        if (Node.getWholeData().contains("mp4")) {
                            //were getting the index of the start and end of url
                            int Index = Node.getWholeData().indexOf("file:");
                            int Indexend = Node.getWholeData().indexOf("\",");
                            urlToReturn = Node.getWholeData().substring(Index + 7, Indexend);

                        }
                    }
                }


            } else if (mirrorName.equals("daclips.in") || mirrorName.equals("daclips.com")) {

                /*String frameUrl;
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                if (mPrefs.getBoolean(Config.ARG_USE_PROXY, false)) {
                    Element frameproxy = doc.select("noframes").last();
                    if (frameproxy == null) {
                        return null;
                    }
                    frameUrl = frameproxy.toString().replace("<noframes>", "").replace(" ", "").replace("</noframes>", "");
                } else {
                    Element frame = doc.select("frame").last();
                    if (frame == null) {
                        return null;
                    }
                    frameUrl = frame.attr("src");
                }*/

                Document doc2 = Jsoup.connect(mirrorLink).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();
                Elements elements = doc2.select("div#player_code script");

                for (Element element1 : elements) {
                    for (DataNode dataNode : element1.dataNodes()) {
                        String nodeText = dataNode.toString();
                        if (nodeText.contains("mp4")) {
                            //bypass the first http
                            int first_http = dataNode.toString().indexOf("http") + 10;
                            String url_link = dataNode.toString().substring(first_http);
                            //were getting the index of the start and end of url
                            int indexstart = url_link.indexOf("http");
                            int indexend = url_link.indexOf("mp4") + 3;
                            urlToReturn = url_link.substring(indexstart, indexend);
                        }
                    }
                }
            } /*else if (mirrorName.equals("novamov.com")) {

                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
                Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                Element frame = doc.select("frame").last();
                if (frame == null) {
                    return null;
                }
                String frameUrl = frame.attr("src");

                //2. Get class search_item get tag a with href value
                Document doc2 = Jsoup.connect(mirrorLink).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").timeout(15000).get();
                System.out.println(doc2.baseUri().toString());

                Elements elements = doc2.select(".search_item a");
                System.out.println(elements.toString());
                urlToReturn = elements.attr("href");
                System.out.println(urlToReturn);

            }*/ else if (mirrorName.equals("movpod.net")) {
                // 1. Parse the PrimeWire frame, to get the actual mMirrorLink to the hosting site
               /* Document doc = Jsoup.connect(mirrorLink).timeout(10000).get();
                Element frame = doc.select("frame").last();
                if (frame == null) {
                    return null;
                }
                String frameUrl = frame.attr("src");*/
                //2. Do some fancy shit to get the mMirrorLink
                Document doc2 = Jsoup.connect(mirrorLink).timeout(10000).get();
                Elements elements = doc2.getElementsByTag("script");
                for (Element element : elements) {
                    for (DataNode dataNode : element.dataNodes()) {
                        if (dataNode.toString().contains("file:")) {
                            //bypass the first http
                            int first_http = dataNode.toString().indexOf("http") + 10;
                            String url_link = dataNode.toString().substring(first_http);
                            //were getting the index of the start and end of url
                            int indexstart = url_link.indexOf("http");
                            int indexend = url_link.indexOf("mp4") + 3;
                            urlToReturn = url_link.substring(indexstart, indexend);
                        }
                    }
                }
            }

        } catch (IOException ignored) {

        }
        return urlToReturn;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        if (result == null || TextUtils.isEmpty(result)) {
            Toast.makeText(mContext, R.string.mirror_null, Toast.LENGTH_LONG).show();

        } else {

            if (mSeason != null && mTitle != null) {
                if (!AppUtils.canWatchVideo(mContext, mSeason, mTitle)) {
                    return;
                }
            }

            //Show our fullscreen ad
            if (fullscreen != null) {
                fullscreen.show();
            }

            String description = "";
            String author = "";

            MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            metadata.addImage(new WebImage(Uri.parse(mImageUri)));
            metadata.putString(MediaMetadata.KEY_TITLE, mTitle);
            metadata.putString(MediaMetadata.KEY_SUBTITLE, description);
            metadata.putString(MediaMetadata.KEY_STUDIO, author);
            MediaInfo mediaInfo = new MediaInfo.Builder(result)
                    .setMetadata(metadata)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("video/*")
                    .build();


            //Mark this episode as watched (if we have an episode, not a movie)
            if (mSeason != null) {
                AppUtils.toggleWatched(mContext, mEpisode, mSeason, mLink, true);
            }

            ContentResolver resolver = mContext.getApplicationContext().getContentResolver();
            String[] cols = new String[]{RecentTable.COLUMN_TITLE};
            Uri recentUri = RecentContentProvider.CONTENT_URI;
            Cursor cursor = resolver.query(recentUri, cols, null, null, null);

            if (cursor != null && cursor.getCount() >= 20) {
                cursor.moveToPosition(19);
                String titlelast = cursor.getString(cursor.getColumnIndex(RecentTable.COLUMN_TITLE));
                if (titlelast != null) {
                    titlelast = titlelast.replace("'", "''");
                    String where = RecentTable.COLUMN_TITLE + "='" + titlelast + "'";
                    mContext.getContentResolver().delete(RecentContentProvider.CONTENT_URI, where, null);
                }
                cursor.close();
            }

            if (AppUtils.isRecent(mContext, mTitle)) {
                if (mTitle != null) {
                    mTitle = mTitle.replace("'", "''");
                    String where = RecentTable.COLUMN_TITLE + "='" + mTitle + "'";
                    mContext.getContentResolver().delete(RecentContentProvider.CONTENT_URI, where, null);
                }
            }

            ContentValues values = new ContentValues();
            values.put(RecentTable.COLUMN_TYPE, mSeason == null ? AppUtils.MOVIE : AppUtils.TVSHOW);
            values.put(RecentTable.COLUMN_TITLE, mTitle);
            values.put(RecentTable.COLUMN_LINK, mLink);
            values.put(RecentTable.COLUMN_IMAGE, mImageUri);
            values.put(RecentTable.COLUMN_SEASON, mSeason);
            values.put(RecentTable.COLUMN_EPISODE, mEpisode);
            values.put(RecentTable.COLUMN_NUM_SEASONS, mNumSeasons);
            values.put(RecentTable.COLUMN_NUM_EPISODES, mNumEpisodes);
            values.put(RecentTable.COLUMN_RATING, mRating);

            mContext.getContentResolver().insert(RecentContentProvider.CONTENT_URI, values);

            //If chromecast is connected, bypass the chooser and go straight to the LocalPlayerActivity
            //which will start casting
            if (mCastManager != null && mCastManager.isConnected()) {
                Intent intent = new Intent(mContext, LocalPlayerActivity.class);
                intent.putExtra("media", Utils.fromMediaInfo(mediaInfo));
                intent.putExtra("shouldStart", true);
                mContext.startActivity(intent);
                return;
            }

            //Otherwise, see if we need to show the chooser dialog
            switch (mPrefs.getInt(SettingsActivity.ARG_DEFAULT_PLAYER, -1)) {
                case -1:
                    //No default action stored
                    AppUtils.showChooserDialog(mContext, mediaInfo);
                    break;

                case AppUtils.DefaultPlayer.LOCAL:
                    //Local player chosen
                    Intent intent = new Intent(mContext, LocalPlayerActivity.class);
                    intent.putExtra("media", Utils.fromMediaInfo(mediaInfo));
                    intent.putExtra("shouldStart", true);
                    mContext.startActivity(intent);
                    break;

                case AppUtils.DefaultPlayer.EXTERNAL:
                    //External player chosen
                    AppUtils.startIntentChooserForUrl(mContext, result);
                    break;
            }
        }
    }
}
