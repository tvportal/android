package com.mitechlt.tvportal.play.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
/**
 * Created by billykrystal245 on 4/15/2014.
 */
public class NetStatus {
    private static NetStatus mInstance = new NetStatus();
    static Context mContext;
    ConnectivityManager connectivityManager;
    boolean connected = false;

    public static NetStatus getInstance(Context ctx) {
        mContext = ctx;
        return mInstance;
    }

    public boolean isOnline(Context con) {
        try {
            connectivityManager = (ConnectivityManager) con
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("Connection Error" + e.getMessage());
        }
        return connected;
    }
}