package ru.q_dev.lnotes;

import android.support.annotation.Nullable;

import com.j256.ormlite.dao.CloseableIterator;

import java.util.ArrayList;

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */
abstract class QDVDbIteratorListViewAdapterExt<T extends QDVDbEntity>
        extends QDVDbIteratorListViewAdapter<T> {

    private ArrayList<T> itemsAddingToTop;
    private int topItemsCount = 0;

    public QDVDbIteratorListViewAdapterExt() {
    }

    QDVDbIteratorListViewAdapterExt(ArrayList<T> itemsAddingToTop) {
        setItemsAddingToTop(itemsAddingToTop);
    }

    public void setItemsAddingToTop(ArrayList<T> itemsAddingToTop) {
        this.itemsAddingToTop = itemsAddingToTop;
        topItemsCount = itemsAddingToTop!=null ? itemsAddingToTop.size() : 0;
    }

    public void loadData(ArrayList<T> itemsAddingToTop, CloseableIterator<T> newDbIterator) {
        setItemsAddingToTop(itemsAddingToTop);
        loadDbIterator(newDbIterator);
    }

    @Nullable
    @Override
    public T getItem(int p0) {
        if (p0 < topItemsCount) {
            return itemsAddingToTop.get(p0);
        }
        return super.getItem(p0 - topItemsCount);
    }

    @Override
    public long getItemId(int p0) {
        return 0;
    }

    @Override
    public int getCount() {
        return super.getCount() + topItemsCount;
    }
}

