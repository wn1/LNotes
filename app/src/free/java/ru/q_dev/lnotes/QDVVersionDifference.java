package ru.q_dev.lnotes;

/**
 * Created by user_vladimir on 31.03.18.
 */

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class QDVVersionDifference {
    static public boolean isFreeVersion() { return true; }

    static public boolean adsPresent () {
        return isFreeVersion();
    }

    static public void loadAd (Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView = (AdView) activity.findViewById(R.id.adView);
        adView.loadAd(adRequest);
    }
}
