package ru.qdev.lnotes.ui.fragment.mvvm;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Intent;
import android.support.annotation.NonNull;

import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class QDVFileStandViewModel
        extends AndroidViewModel implements QDVFileStandBackupImplements.EventListener {

    public interface QDVFileStandView extends QDVFileStandBackupImplements.EventListener {

    }

    public QDVFileStandViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode) {
        viewEventsDispatcher.dispatchEvent(new Function1<QDVFileStandView, Unit>() {
            @Override
            public Unit invoke(QDVFileStandView qdvFileStandView) {
                qdvFileStandView.startActivityForResult(intent, requestCode);
                return null;
            }
        });
    }

    @Override
    public void startActivity(final Intent intent) {
        viewEventsDispatcher.dispatchEvent(new Function1<QDVFileStandView, Unit>() {
            @Override
            public Unit invoke(QDVFileStandView qdvFileStandView) {
                qdvFileStandView.startActivity(intent);
                return null;
            }
        });
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
    private EventsDispatcher<QDVFileStandView> viewEventsDispatcher;

    public void bind(QDVFileStandView view, LifecycleOwner lifecycleOwner) {
        this.viewEventsDispatcher = new EventsDispatcher();
        viewEventsDispatcher.bind(lifecycleOwner, view);
    }

    public void unbind() {

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