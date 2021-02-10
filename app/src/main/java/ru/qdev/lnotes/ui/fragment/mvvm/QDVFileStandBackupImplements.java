package ru.qdev.lnotes.ui.fragment.mvvm;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.scottyab.aescrypt.AESCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.qdev.lnotes.R;
import ru.qdev.lnotes.ThisApp;
import ru.qdev.lnotes.db.QDVDbDatabase;
import ru.qdev.lnotes.ui.activity.QDVNotesHomeActivity;
import ru.qdev.lnotes.utils.QDVFileUtils;

import static android.app.Activity.RESULT_OK;

public class QDVFileStandBackupImplements {

    public interface EventListener {
        void startActivityForResult(Intent intent, int requestCode);
        void startActivity(Intent intent);
    }

    public static final int SELECTFILE_RESTORE_DB_RESULT_CODE = 51;
    public static final int SELECTFILE_SAVE_DB_RESULT_CODE = 52;
    public static final int SELECTFILE_RESTORE_DB_OLD_OS_RESULT_CODE = 53;

    private static String passwordForBackup;

    Context context;

    @NonNull
    EventListener listener;

    public QDVFileStandBackupImplements(EventListener listener, Context context) {
        this.context = context;
        this.listener = listener;
    }

    @UiThread
    void saveBackup() {
        final EditText editText = new EditText(context);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        new AlertDialog.Builder(context).
                setTitle(R.string.input_password_for_backup_db_title).
                setCancelable(true)
                .setView(editText).
                setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        InputMethodManager inputMethodManager = (InputMethodManager) ThisApp.getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                        String password = editText.getText().toString();
                        if (password.length()==0) {
                            new AlertDialog.Builder(context).
                                    setMessage(R.string.input_password)
                                    .setCancelable(true)
                                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            saveBackup();
                                        }
                                    }).show();
                            return;
                        }
                        saveBackup(password);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMethodManager = (InputMethodManager) ThisApp.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        }).setNeutralButton(R.string.action_without_password, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMethodManager = (InputMethodManager) ThisApp.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                saveBackup("");
            }
        }).show();
        editText.requestFocus();
        editText.requestFocusFromTouch();
        InputMethodManager inputMananger =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMananger != null) {
            inputMananger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @AnyThread
    private File getSaveBackupDirForOldOs() {
        String saveFileAppendPath = "/Backups/LNotes/";
        File storageFile = Environment.getExternalStorageDirectory();
        if (storageFile != null) {
            return new File(storageFile.getPath() + saveFileAppendPath);
        }
        return null;
    }

    @UiThread
    private void saveBackup(String password) {
        passwordForBackup = password;

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm-ss");
        String fileNameString = dateFormat.format(new Date())+(passwordForBackup.length()==0 ? ".db" :".dbcr");
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            //for old version
            File dbBackupFile = null;
            File storageBackupDir = getSaveBackupDirForOldOs();
            if (storageBackupDir != null) {
                dbBackupFile = new File(storageBackupDir.getAbsolutePath() + "/"  + fileNameString);
            }
            if (dbBackupFile==null) {
                new AlertDialog.Builder(context).
                        setMessage(context.getString(R.string.file_create_error) + dbBackupFile.getAbsolutePath())
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            }
            dbBackupFile.getParentFile().mkdirs();

            try {
                boolean result = saveBackup(new FileOutputStream(dbBackupFile), true);
                if (result) {
                    new AlertDialog.Builder(context).
                            setMessage(context.getString(R.string.backup_saved_to) + dbBackupFile.getAbsolutePath())
                            .setCancelable(true)
                            .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(context, QDVNotesHomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    context.startActivity(intent);
                                }
                            }).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                new AlertDialog.Builder(context).
                        setMessage(context.getString(R.string.file_create_error) + dbBackupFile.getAbsolutePath())
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
            }
        }
        else {
            try {
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                i.putExtra( "android.content.extra.SHOW_ADVANCED", true);
                i.putExtra(Intent.EXTRA_TITLE, fileNameString);
                listener.startActivityForResult(i, SELECTFILE_SAVE_DB_RESULT_CODE);
            } catch (Exception ignore) {
                new AlertDialog.Builder(context).setMessage(R.string.file_selector_not_found)
                        .setCancelable(true).setNegativeButton(R.string.cancel, null).show();
            }
        }
    }

    @UiThread
    private boolean saveBackup (OutputStream os, boolean withoutCloseActivity) {
        File dbFile = new QDVDbDatabase(context).getFileDB();

        InputStream is = null;
        boolean result = false;
        try {

            is = new FileInputStream(dbFile);
            if (passwordForBackup.length()==0) {
                result = QDVFileUtils.copyFile(is, os);
            }
            else {
                AESCrypt.encrypt(passwordForBackup, is, os);
                result = true;
            }
            Log.d("Save db","wrote data");
        } catch (Exception e) {
            Log.d("Save db","FAILED TO WRITE", e);
        } finally {
            try
            {
                is.close();
            }
            catch (Exception ignored)
            {}
            try
            {
                os.close();
            }
            catch (Exception ignored)
            {}
        }

        passwordForBackup = null;
        if (result){
            Toast.makeText(context, R.string.backup_saved, Toast.LENGTH_LONG).show();
        }
        else
        {
            new AlertDialog.Builder(context).
                    setMessage(String.format(context.getString(R.string.error_with_id), "300"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return false;
        }

        if (!withoutCloseActivity) {
            Intent intent = new Intent(context, QDVNotesHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            listener.startActivity(intent);
        }
        return true;
    }

    @UiThread
    void restoreBackup () {
        try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                //for old version
                File backupsDir = getSaveBackupDirForOldOs();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(Uri.parse(backupsDir.getAbsolutePath()), "*/*");
                listener.startActivityForResult(intent, SELECTFILE_RESTORE_DB_OLD_OS_RESULT_CODE);
            }
            else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.putExtra( "android.content.extra.SHOW_ADVANCED", true);
//              String[] mimeTypes = {"file/.db", "file/.dbcr"};
//    			intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                listener.startActivityForResult(intent, SELECTFILE_RESTORE_DB_RESULT_CODE);
            }
        }
        catch (Exception ignore) {
            new AlertDialog.Builder(context).setMessage(R.string.file_selector_not_found)
                    .setCancelable(true).setNegativeButton(R.string.cancel, null).show();
        }
    }

    @UiThread
    private void restoreBackup(InputStream inputStream, boolean isCrypted, String password) {
        if (isCrypted && password==null) {
            new AlertDialog.Builder(context).
                    setMessage(String.format(context.getString(R.string.error_with_id), "304"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.action_ok, null).show();
            return;
        }

        File dbFile = new QDVDbDatabase(context).getFileDB();
        OutputStream outputStream = null;
        boolean result = false;
        File backupFile = new File(dbFile.toString()+".bak");
        File decryptedFile = new File(dbFile.toString()+".tmp");
        try {
            backupFile.delete();
            decryptedFile.delete();
            outputStream = new FileOutputStream(decryptedFile);
            boolean flag = true;
            if (isCrypted) {
                AESCrypt.decrypt(password, inputStream, outputStream);
            }
            else
            {
                flag = QDVFileUtils.copyFile(inputStream, outputStream);
            }
            outputStream.close();
            outputStream = null;
            if (flag) {
                dbFile.renameTo(backupFile);
                result = QDVFileUtils.copyFile(decryptedFile, dbFile, false);
                if (!result) {
                    dbFile.delete();
                    backupFile.renameTo(dbFile);
                }
            }
            Log.d("Restore db","write data");
        } catch (Exception e) {
            Log.d("Restore db","FAILED TO WRITE", e);
        } finally {
            try
            {
                inputStream.close();
            }
            catch (Exception ignored)
            {

            }
        }
        passwordForBackup = null;
        Log.d("Restore db", "Restore db result:" + result + ", dbFile: " + dbFile.getAbsolutePath());

        if (result) {
            Toast.makeText(context, R.string.backup_restored, Toast.LENGTH_LONG).show();
        }
        else {
            new AlertDialog.Builder(context).
                    setMessage(R.string.password_error)
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        Intent intent = new Intent(context, QDVNotesHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("needReloadDb", true);
        listener.startActivity(intent);
    }

    @UiThread
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case SELECTFILE_RESTORE_DB_RESULT_CODE:
            case SELECTFILE_RESTORE_DB_OLD_OS_RESULT_CODE:
            if(resultCode == RESULT_OK){
                final ContentResolver cr = context.getContentResolver();
                Uri uri = data.getData();
                if (uri == null){
                    new AlertDialog.Builder(context).
                    setMessage(String.format(context.getString(R.string.error_with_id), "301"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }

                boolean isCrypted = true;
                InputStream inputStream = null;
                try {
                    inputStream = cr.openInputStream(uri);
                    byte[] sqliteFileDetector = new byte[13];
                    int detectorReaded = inputStream.read(sqliteFileDetector);
                    inputStream.close();
                    inputStream = cr.openInputStream(uri);
                    if (detectorReaded == 13) {
                        if ("SQLite format".equals(new String(sqliteFileDetector))){
                            isCrypted = false;
                        }
                    }
                } catch (Exception ignored) {}

                if (isCrypted) {
                    final EditText editText = new EditText(context);
                    final InputStream finalInputStream = inputStream;
                    final boolean finalIsCrypted = isCrypted;
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    new AlertDialog.Builder(context).setTitle(R.string.input_password_for_backup_db_title).setCancelable(true)
                            .setView(editText).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    InputMethodManager inputMethodManager = (InputMethodManager) ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                                    final String password = editText != null ? editText.getText().toString() : "";
                                    restoreBackup(finalInputStream, finalIsCrypted, password);
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    InputMethodManager inputMethodManager = (InputMethodManager) ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                                }
                            }).show();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            editText.requestFocus();
                            editText.requestFocusFromTouch();
                            InputMethodManager inputMananger = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMananger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }
                    });
                }
                else
                {
                    restoreBackup(inputStream, isCrypted, "");
                }
            }
            break;
            case SELECTFILE_SAVE_DB_RESULT_CODE:
            if(resultCode == RESULT_OK){
                if (passwordForBackup==null) {
                    new AlertDialog.Builder(context).
                    setMessage(String.format(context.getString(R.string.error_with_id), "304"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.action_ok, null).show();
                    return;
                }
                final ContentResolver cr = context.getContentResolver();
                Uri uri = data.getData();
                if (uri == null){
                    new AlertDialog.Builder(context).
                    setMessage(String.format(context.getString(R.string.error_with_id), "303"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }

                OutputStream os = null;
                try {
                    os = cr.openOutputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(context).
                    setMessage(String.format(context.getString(R.string.error_with_id), "305"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }

                saveBackup(os, false);
            }
            break;
        }
    }
}