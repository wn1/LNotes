package ru.q_dev.lnotes;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import java.text.*;
import android.provider.*;
import android.content.*;

import com.scottyab.aescrypt.AESCrypt;

import java.io.*;

/**
 * Created by Vladimir Kudashov on 27.04.17.
 */

public class QDVBackupActivity extends AppCompatActivity {
	private static final int SELECTFILE_RESTORE_DB_RESULT_CODE = 1;
	private static final int SELECTFILE_SAVE_DB_RESULT_CODE = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
	
	private void saveBackup(){
    	if (((EditText)findViewById(R.id.editPassword)).toString().length()==0) {
            new AlertDialog.Builder(QDVBackupActivity.this).
						setMessage("Введите пароль")
						.setCancelable(true)
						.setPositiveButton(R.string.action_ok, null).show();
            return;
		}

		//TODO TEMP
//		File dbFile = new QDVMyBaseOpenHelper(this,  new DatabaseErrorHandler() {
//			@Override
//			public void onCorruption(SQLiteDatabase sqLiteDatabase) {
//					new AlertDialog.Builder(QDVSendDataActivity.this).
//						setMessage(String.format(getString(R.string.error_with_id), "406"))
//						.setCancelable(true)
//						.setPositiveButton(R.string.cancel, null).sh
// ow();
//				}
//			}).getFileDB();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm-ss");
		String fileNameString = dateFormat.format(new Date())+".db";
//		
//		//TODO for old version
//		File dbBackupFile = new File("/storage/sdcard1/LNotes/lnotes"+fileNameString);
//		boolean result = QDVFileUtils.copyFile(dbFile, dbBackupFile);
//		if (result){
//			Toast.makeText(this, "Save db ok:" + dbBackupFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
//		}
//		else 
//		{
//			Toast.makeText(this, "Save db ERROR:" + dbBackupFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
//		}
		
		try {
			Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("file/*");
			i.putExtra(Intent.EXTRA_TITLE, fileNameString);
			startActivityForResult(i, SELECTFILE_SAVE_DB_RESULT_CODE);
		}
		catch (Exception ignore) {
			new AlertDialog.Builder(this).setMessage(R.string.file_selector_not_found)
				.setCancelable(true).setNegativeButton(R.string.cancel, null).show();
		}		
	}

    private void restoreBackup () {
        if (((EditText)findViewById(R.id.editPassword)).toString().length()==0) {
            new AlertDialog.Builder(QDVBackupActivity.this).
                    setMessage("Введите пароль")
                    .setCancelable(true)
                    .setPositiveButton(R.string.action_ok, null).show();
            return;
        }
		try {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.setType("file/*");
			startActivityForResult(intent,SELECTFILE_RESTORE_DB_RESULT_CODE);
		}
		catch (Exception ignore) {
			new AlertDialog.Builder(this).setMessage(R.string.file_selector_not_found)
				.setCancelable(true).setNegativeButton(R.string.cancel, null).show();
		}		
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
			case SELECTFILE_RESTORE_DB_RESULT_CODE:
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
					File dbFile = new QDVMyBaseOpenHelper(this,  new DatabaseErrorHandler() {
							@Override
							public void onCorruption(SQLiteDatabase sqLiteDatabase) {
								new AlertDialog.Builder(QDVBackupActivity.this).
									setMessage(String.format(getString(R.string.error_with_id), "306"))
									.setCancelable(true)
									.setPositiveButton(R.string.cancel, null).show();
							}
						}).getFileDB();
					InputStream inputStream = null;
					OutputStream outputStream = null;
					boolean result = false;
					File backupFile = new File(dbFile.toString()+".bak");
                    File decryptedFile = new File(dbFile.toString()+".tmp");
					try {
                        String password = ((EditText)findViewById(R.id.editPassword)).toString();
                        if (password.length()==0) {
                            new AlertDialog.Builder(QDVBackupActivity.this).
                                    setMessage("Введите пароль")
                                    .setCancelable(true)
                                    .setPositiveButton(R.string.action_ok, null).show();
                            return;
                        }
						backupFile.delete();
                        decryptedFile.delete();
						dbFile.renameTo(backupFile);
						inputStream = cr.openInputStream(uri);
						outputStream = new FileOutputStream(decryptedFile);
						AESCrypt.decrypt(password, inputStream, outputStream);
                        outputStream.close();
                        outputStream = null;
						result = QDVFileUtils.copyFile(decryptedFile, dbFile, false);
						Log.d("Restore db","write data");
					} catch (Exception e) {
						Log.d("Restore db","FAILED TO WRITE", e);
					} finally {
						try
						{
							inputStream.close();
						}
						catch (IOException e)
						{
							
						}
					}
					
					Log.d("Restore db", "Restore db result:" + result + ", dbFile: " + dbFile.getAbsolutePath());
					
					if (result) {
						Toast.makeText(this, "База данных успешно восстановлена", Toast.LENGTH_LONG).show();
					}
					else {
						new AlertDialog.Builder(QDVBackupActivity.this).
							setMessage(String.format(getString(R.string.error_with_id), "302"))
							.setCancelable(true)
							.setPositiveButton(R.string.cancel, null).show();
						return;
					}
					
					Intent intent = new Intent(this, QDVNotesActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("needReloadDb", true);
					startActivity(intent);
				}
				break;
			case SELECTFILE_SAVE_DB_RESULT_CODE:
				if(resultCode == RESULT_OK){
					final ContentResolver cr = getContentResolver();
					Uri uri = data.getData();
					if (uri == null){
						new AlertDialog.Builder(QDVBackupActivity.this).
							setMessage(String.format(getString(R.string.error_with_id), "303"))
							.setCancelable(true)
							.setPositiveButton(R.string.cancel, null).show();
						return;
					}
					File dbFile = new QDVMyBaseOpenHelper(this,  new DatabaseErrorHandler() {
							@Override
							public void onCorruption(SQLiteDatabase sqLiteDatabase) {
								new AlertDialog.Builder(QDVBackupActivity.this).
									setMessage(String.format(getString(R.string.error_with_id), "306"))
									.setCancelable(true)
									.setPositiveButton(R.string.cancel, null).show();
							}
						}).getFileDB();
						
					OutputStream os = null;
					InputStream is = null;
					boolean result = false;
					try {
						os = cr.openOutputStream(uri);
						is = new FileInputStream(dbFile);
						String password = ((EditText)findViewById(R.id.editPassword)).toString();
                        if (password.length()==0) {
                            new AlertDialog.Builder(QDVBackupActivity.this).
                                    setMessage("Введите пароль")
                                    .setCancelable(true)
                                    .setPositiveButton(R.string.action_ok, null).show();
                            return;
                        }
						AESCrypt.encrypt(password, is, os);
                        result = true;
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
	
					if (result){
						Toast.makeText(this, "Резервная копия успешно сохранена", Toast.LENGTH_LONG).show();
					}
					else 
					{
						new AlertDialog.Builder(QDVBackupActivity.this).
							setMessage(String.format(getString(R.string.error_with_id), "300"))
							.setCancelable(true)
							.setPositiveButton(R.string.cancel, null).show();
						return;
					}
					
					Intent intent = new Intent(this, QDVNotesActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
				break;
		}
	}
}
