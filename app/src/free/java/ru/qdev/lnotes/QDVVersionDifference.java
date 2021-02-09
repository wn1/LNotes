package ru.qdev.lnotes;

/**
 * Created by Vladimir Kudashov on 31.03.18.
 */

import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class QDVVersionDifference extends QDVVersion {
    static public boolean isFreeVersion() { return true; }

    static public boolean adsPresent () {
        return isFreeVersion();
    }

    static public void loadAd (AppCompatActivity activity) {
        MobileAds.initialize(activity, activity.getString(R.string.admob_app_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView = activity.findViewById(R.id.adView);
        adView.loadAd(adRequest);
    }
}
