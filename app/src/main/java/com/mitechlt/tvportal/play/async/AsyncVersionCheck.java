package com.mitechlt.tvportal.play.async;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.mitechlt.tvportal.play.R;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * This is a Temporary method as amazon server is underway.
 * <p/>
 * Checks whether a new version exists on Slideme and offers options to update if that is the case.
 */
public class AsyncVersionCheck extends AsyncTask<String, Void, String> {

    public static final String TAG = "AsyncVersionCheck";

    private Context mContext;

    private String mVersion;
    private String mVersion_striped;

    private String mOperaVersion = null;
    private String mOperaVersion_striped;

    private int slideme = 0;
    private int tvp = 0;

    private File mOutputFile;

    private Document document;

    public AsyncVersionCheck(Context context, String version) {
        mContext = context;
        mVersion = version;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            Connection.Response response = Jsoup.connect("http://m.apps.opera.com/en_us/tv_portal_stream_tv_and_movies.html").userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").execute();
            int code = response.statusCode();
            if (code == 200) {

                document = Jsoup.connect("http://m.android-4-0-3-plus.apps.opera.com/en_us/tv_portal_stream_tv_and_movies.html").userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").get();
               Document deviceredirect = Jsoup.connect(document.baseUri()).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").get();
               Elements element = deviceredirect.select(".pd-block").get(1).select("p");

              if (element.toString().contains("Version number:")){
                int Index = element.toString().indexOf("number:");
                int Indexend = element.toString().indexOf("]");
                  mOperaVersion = element.toString().substring(Index + 8, Indexend).toString();
            }
            } else {
                return null;
            }

            //Todo: Billy, why are we stripping this crap? Why can't we check our versionCode against the versioncode stored on our server?
            //Todo: I'm asking because sometimes your 'stripped' strings are returning null, and this seems way more complicated than it needs to be.

            //just remove everything thats not a number
            mVersion_striped = mVersion.replaceAll("[^0-9]", "");
            mOperaVersion_striped = mOperaVersion.replaceAll("[^0-9]", "");

            //check if string is null before transitioning to int
            if(mOperaVersion_striped  != null || mOperaVersion_striped != null){
                slideme = Integer.parseInt(mOperaVersion_striped);
                tvp = Integer.parseInt(mVersion_striped);}



            //Todo: This code is really broken
            //Cleaned up a lot more efficient switched to opera
            if (tvp != 0 || slideme != 0){
            if(slideme > tvp) {  //  Update TV Portal from Slideme

               //Get apk url
                Document doc4 = Jsoup.connect(document.baseUri())
                        .userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                        .get();

                Elements dlbutton = doc4.select(".download_buttons a");
                String hreff = dlbutton.attr("href");


                //Download apk to sd card
                URL url = new URL(hreff);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();

                String PATH = Environment.getExternalStorageDirectory() + "/download/";
                File file = new File(PATH);
                file.mkdirs();
                mOutputFile = new File(file, "Tv-Portal.v"+mOperaVersion+".apk");
                if (mOutputFile.exists()) {
                  mOutputFile.delete();
                }

                FileOutputStream fos = new FileOutputStream(mOutputFile);

                InputStream is = httpURLConnection.getInputStream();

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                is.close();
            }
            }
        } catch (IOException ignored) {
        }
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Todo: Still getting null pointer exceptions here
        //Better way to check to make sure were not getting null and that slide me version is larger
       if (tvp != 0 || slideme != 0){
        if(slideme > tvp) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mContext.getString(R.string.update_available));
            builder.setIcon(R.drawable.ic_launcher);
            builder.setMessage(String.format(mContext.getString(R.string.new_version_available, mVersion, mOperaVersion)));
            builder.setPositiveButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                   //Install from sdcard
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "Tv-Portal-slideme.v"+mOperaVersion+".apk")), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });

            builder.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  //  mOutputFile.delete();
                }
            });
            builder.show();
            }
        }


    }
}

