package ru.qdev.lnotes;

public class QDVVersion {
    static public boolean isFileForStandVersion() {
        return BuildConfig.FLAVOR.equalsIgnoreCase("forFileStand");
    }
}
