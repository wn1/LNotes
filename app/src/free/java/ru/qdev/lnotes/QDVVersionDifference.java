package ru.qdev.lnotes;

/**
 * Created by Vladimir Kudashov on 31.03.18.
 */

import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

public class QDVVersionDifference {
    static public boolean isFreeVersion() { return true; }

    @Nullable
    static public TextView getLabelBuyPlusVersion (ViewGroup rootLayout) {
        return null;
    }
}
