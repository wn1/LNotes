package ru.qdev.lnotes.ui.activity.backup;

import static ru.qdev.lnotes.core.events.DbManager.NOTES_DATABASE_NAME;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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

import ru.qdev.lnotes.ThisApp;
import ru.qdev.lnotes.ui.activity.notes.QDVNotesHomeActivity;
import ru.qdev.lnotes.utils.QDVFileUtils;
import ru.qdev.lnotes.utils.QDVTempFileSendUtils;
import src.R;

/**
 * Created by Vladimir Kudashov on 27.04.17.
 */

public class QDVBackupActivity extends AppCompatActivity {
	private static final String LOG_TAG = "QDVBackupActivity";
	private static final int SELECTFILE_RESTORE_DB_RESULT_CODE = 1;
	private static final int SELECTFILE_SAVE_DB_RESULT_CODE = 2;
    private static final int SELECTFILE_RESTORE_DB_OLD_OS_RESULT_CODE = 3;
	private static String passwordForBackup;

	//TODO To MVP architect migration needed

    @Override
    @UiThread
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(R.string.backup_title);
        }
        if (savedInstanceState!=null) {
            passwordForBackup =
                    savedInstanceState.getString("passwordForBackup", null);
        }
        setContentView(R.layout.backup_activity);
		findViewById(R.id.buttonRestoreDB).setOnClickListener(new OnClickListener () {
			public void onClick(View v) {
				restoreBackup();
			}
		});
		findViewById(R.id.buttonSaveDB).setOnClickListener(new OnClickListener () {
			public void onClick(View v) {
				saveBackup();
			}
		});
    }

    @Override
    @UiThread
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("passwordForBackup", passwordForBackup);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @UiThread
    private void saveBackup() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        new AlertDialog.Builder(this).
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
                    new AlertDialog.Builder(QDVBackupActivity.this).
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
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

        try {
            QDVTempFileSendUtils fileSend = new QDVTempFileSendUtils(this);
            File dbBackupFile = fileSend.getTempFile(fileNameString);

            boolean result = saveBackup(
                    new FileOutputStream(dbBackupFile),
                    true,
                    false
            );

            if (result) {
                Intent sendFileIntent = new Intent(Intent.ACTION_SEND);
                sendFileIntent.setType("application/octet-stream");

                sendFileIntent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.backup_send_title)
                );

                sendFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Uri sendFileURI = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        dbBackupFile
                );
                sendFileIntent.putExtra(Intent.EXTRA_STREAM, sendFileURI);

                startActivity(Intent.createChooser(
                        sendFileIntent,
                        getString(R.string.backup_send_title))
                );
            }
            else {
                new AlertDialog.Builder(QDVBackupActivity.this).
                        setMessage(getString(R.string.db_backup_error, "file send error"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
            }
        }
        catch (Throwable e) {
            Log.e(LOG_TAG, "saveBackup file error: " + e, e);
            new AlertDialog.Builder(QDVBackupActivity.this).
                        setMessage(getString(R.string.db_backup_error, e.getLocalizedMessage()))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
        }
    }

    private File getFileDB() {
        return getDatabasePath(NOTES_DATABASE_NAME);
    }

    @UiThread
    private boolean saveBackup (OutputStream os, boolean withoutCloseActivity, boolean withMessage) {
        File dbFile = getFileDB();

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

        if (withMessage) {
            if (result) {
                Toast.makeText(this, R.string.backup_saved, Toast.LENGTH_LONG).show();
            } else {
                new AlertDialog.Builder(QDVBackupActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "300"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return false;
            }
        }

        if (!withoutCloseActivity) {
            Intent intent = new Intent(this, QDVNotesHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    @UiThread
    private void restoreBackup () {
		try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                //for old version
                File backupsDir = getSaveBackupDirForOldOs();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(Uri.parse(backupsDir.getAbsolutePath()), "*/*");
                startActivityForResult(intent, SELECTFILE_RESTORE_DB_OLD_OS_RESULT_CODE);
            }
            else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.putExtra( "android.content.extra.SHOW_ADVANCED", true);
//              String[] mimeTypes = {"file/.db", "file/.dbcr"};
//    			intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, SELECTFILE_RESTORE_DB_RESULT_CODE);
            }
		}
		catch (Exception ignore) {
			new AlertDialog.Builder(this).setMessage(R.string.file_selector_not_found)
				.setCancelable(true).setNegativeButton(R.string.cancel, null).show();
		}		
    }

    @UiThread
    private void restoreBackup(InputStream inputStream, boolean isCrypted, String password) {
        if (isCrypted && password==null) {
            new AlertDialog.Builder(QDVBackupActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "304"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.action_ok, null).show();
            return;
        }

        File dbFile = getFileDB();
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
            Toast.makeText(this, R.string.backup_restored, Toast.LENGTH_LONG).show();
        }
        else {
            new AlertDialog.Builder(QDVBackupActivity.this).
                    setMessage(R.string.password_error)
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        Intent intent = new Intent(this, QDVNotesHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("needReloadDb", true);
        startActivity(intent);
    }
	
	@Override
    @UiThread
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode){
			case SELECTFILE_RESTORE_DB_RESULT_CODE:
            case SELECTFILE_RESTORE_DB_OLD_OS_RESULT_CODE:
				if(resultCode == RESULT_OK){
					final ContentResolver cr = getContentResolver();
					Uri uri = data.getData();
					if (uri == null){
						new AlertDialog.Builder(QDVBackupActivity.this).
							setMessage(String.format(getString(R.string.error_with_id), "301"))
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
                        final EditText editText = new EditText(this);
                        final InputStream finalInputStream = inputStream;
                        final boolean finalIsCrypted = isCrypted;
                        editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        new AlertDialog.Builder(this).setTitle(R.string.input_password_for_backup_db_title).setCancelable(true)
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
                                InputMethodManager inputMananger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
                        new AlertDialog.Builder(QDVBackupActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "304"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.action_ok, null).show();
                        return;
                    }
					final ContentResolver cr = getContentResolver();
					Uri uri = data.getData();
					if (uri == null){
						new AlertDialog.Builder(QDVBackupActivity.this).
							setMessage(String.format(getString(R.string.error_with_id), "303"))
							.setCancelable(true)
							.setPositiveButton(R.string.cancel, null).show();
						return;
					}

                    OutputStream os = null;
                    try {
                        os = cr.openOutputStream(uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(QDVBackupActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "305"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.cancel, null).show();
                        return;
                    }

                    saveBackup(os, false, true);
                }
				break;
		}
	}
}
