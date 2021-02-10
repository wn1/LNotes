package ru.qdev.lnotes.ui.fragment.mvvm;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

public class QDVFileStandViewModel
        extends AndroidViewModel implements QDVFileStandBackupImplements.EventListener {

    public interface QDVFileStandView {
        void startActivityForResult(Intent intent, int requestCode) ;
        void startActivity(Intent intent);
    }

    public QDVFileStandViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        QDVFileStandView view = weekView.get();
        if (view != null) {
            view.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        QDVFileStandView view = weekView.get();
        if (view != null) {
            view.startActivity(intent);
        }
    }

    private QDVFileStandBackupImplements fileStandBackupImplements = null;

    private QDVFileStandBackupImplements getFileStandBackupImplements() {
        if (fileStandBackupImplements == null) {
            fileStandBackupImplements =
                    new QDVFileStandBackupImplements(this, getApplication());
        }
        return fileStandBackupImplements;
    }

    @NonNull
    private WeakReference<QDVFileStandView> weekView;

    public void bind(QDVFileStandView view) {
        this.weekView = new WeakReference(view);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getFileStandBackupImplements().onActivityResult(requestCode, resultCode, data);
    }

    public void openFile() {
        getFileStandBackupImplements().restoreBackup();
    }

    public void saveBackup() {
        getFileStandBackupImplements().saveBackup();
    }
}