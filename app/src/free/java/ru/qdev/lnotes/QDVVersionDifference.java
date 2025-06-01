package ru.qdev.lnotes;

/**
 * Created by Vladimir Kudashov on 31.03.18.
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import src.R;

public class QDVVersionDifference {
    static public boolean isFreeVersion() { return true; }

    @Nullable
    static public TextView getLabelBuyPlusVersion (ViewGroup rootLayout) {
        return rootLayout.findViewById(R.id.buyPlusVersion);
    }
}
