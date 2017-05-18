package ru.q_dev.lnotes;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

//import org.apache.commons.codec.binary.Hex;

/**
 * Created by Vladimir Kudashov on 27.04.17.
 */

public class QDVSendDataActivity extends AppCompatActivity {

    static final int LNOTES_SEND_INTENT_ID = 1;
    static final int appType = 1;// 1-LNotes, 2-LNotes+

    private void startLnotesSyncWithUrl (String url){

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
        catch (ActivityNotFoundException e){
            Log.d("onLaunchWithUrlSheme", getString(R.string.error_lnotesync_not_found));
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(R.string.error_lnotesync_not_found)
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
            return;
        }
        catch (Exception e) {
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "200"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        finish();
    }

    private boolean copyFile(File fileFrom, File fileTo) {
        FileInputStream rfh = null;
        try {
            rfh = new FileInputStream(fileFrom);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (rfh == null) {
            return false;
        }
        FileOutputStream dbfh = null;
        if (dbfh == null) {
            try {
                dbfh = new FileOutputStream(fileTo, false);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                try {
                    rfh.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return false;
            }
        }

        byte[] copybuffer = new byte[2048];
        int count = 0;
        try {
            while ((count = rfh.read(copybuffer)) != -1) {
                dbfh.write(copybuffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                rfh.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                dbfh.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }
        try {
            rfh.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean removeFilesFromDir(File pathDir) {
        Log.d("removeFilesFromDir", "Remove files from dir: "+pathDir);

        if (!pathDir.exists() || !pathDir.isDirectory())
        {
            Log.d("removeFilesFromDir", "Dir not found");
            return false;
        }
        File[] files = pathDir.listFiles();
        boolean isError = false;
        for (int n = 0; n < files.length; n++) {
            File file = files[n];
            if (file.isDirectory())
            {
                removeFilesFromDir(file);
            }
            Log.d("removeFilesFromDir", "Do remove file: "+file.toString());
            if (!file.delete()) {
                isError = true;
                Log.d("removeFilesFromDir", "Error removing file");
            }
            else
            {
                Log.d("removeFilesFromDir", "File removed");
            }
        }
        return !isError;
    }

    private String encodeHex(byte[] bytes){
        String strHex = "";
        for (int i = 0; i <bytes.length; i++){
            strHex=strHex+String.format("%02X", bytes[i]).toLowerCase();
        }
        return  strHex;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_data_activity);
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle!=null) {
            String operationName = extrasBundle.getString("operationName");
            if (operationName!=null && operationName.equals("backupDB"))
            {
                Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onLaunchWithStartBackup();
                    }
                }, 2);
                return;
            }
        }


        final Uri uri = getIntent().getData();
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onLaunchWithUrlSheme(uri);
            }
        }, 2);
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

    private void onLaunchWithStartBackup () {
        File pathDB = new File(getFilesDir(), "senddb");
        File pathTemp = new File(pathDB, "tempdbparts");
        File dbFile = new QDVMyBaseOpenHelper(QDVSendDataActivity.this, null).getFileDB();
        Log.d("onLaunchWithStartBackup", "pathTemp: " + pathTemp);
        if (pathDB.mkdir()) {
            pathTemp.mkdir();
        }

        removeFilesFromDir(pathTemp);
        pathTemp.delete();

        if (pathTemp.exists()) {
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "101"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        if (!pathTemp.mkdir()) {
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "102"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "102.1"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        if (md == null) {
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "102.2"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        int maxByte = 250;
        FileInputStream rfh = null;
        try {
            rfh = new FileInputStream(dbFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "103"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        long sizeD = dbFile.length();
        if (sizeD <= 0) {
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "104"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        byte[] tData = new byte[maxByte];
        Log.d("onLaunchWithStartBackup", "dbData filesize: " + String.valueOf(sizeD));

        String tName = "";
        long n = 1;
        int from = 0;
        int dataLenght = 0;

        while (true) {
            String nm = "part"+String.valueOf(n);
            File fileNm = new File(pathTemp,nm);
            Log.d("onLaunchWithStartBackup", "fileNm: "+fileNm.toString());
            int cntRead = -1;
            try {
                cntRead = rfh.read(tData, 0, maxByte);
            } catch (IOException e) {
                e.printStackTrace();
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "105"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            }
            if (cntRead<0){
                break;
            }
            if (cntRead==0){
                continue;
            }

            md.update(tData, 0, cntRead);

            dataLenght = cntRead;

            Log.d("onLaunchWithStartBackup", "tData size:"+String.valueOf(cntRead));
            FileOutputStream wfh = null;
            try {
                wfh = new FileOutputStream(fileNm);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "106"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            }
            if (wfh == null){
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "107"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            }

            try {
                wfh.write(tData, 0, cntRead);
                tName = nm;
                wfh.close();
            } catch (IOException e) {
                e.printStackTrace();
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "108"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            }
            n = n + 1;
        }

        ((TextView) findViewById(R.id.progressTextView)).setText("1/"+String.valueOf(n));

        String hashOfDb = new String (encodeHex(md.digest()));
        if (hashOfDb==null ||hashOfDb.length() == 0){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "108.1"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        File fileNm = new File(pathTemp,"sha1");
        Log.d("onLaunchWithStartBackup", "fileNm: "+fileNm.toString());
        FileOutputStream wfh = null;
        try {
            wfh = new FileOutputStream(fileNm);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "108.2"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        if (wfh == null){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "108.3"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        try {
            wfh.write(hashOfDb.getBytes());
            wfh.close();
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "108.4"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }


        if (tName==null || tName.length()<=0){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "108.5"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
        }

        JSONObject data = new JSONObject();
        try {
            data.put("id", tName);
            data.put("data", new String(tData, 0, dataLenght, "ISO-8859-1"));
            data.put("st", true);
            data.put("operation", "senddb");
            JSONObject handler = new JSONObject();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmm-ss");
            handler.put("lnoteType", appType);
            handler.put("dateName", simpleDateFormat.format(new Date()));
            handler.put ("c" , n);
            handler.put ("i" , 1);
            data.put("handler", handler);
        } catch (JSONException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "109"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "109.1"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
        }

        String encoded = data.toString();
        if (encoded == null || encoded.length()<=0){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "110"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        long len = encoded.length();
        if (len>1980) {
            //Maximum length check
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "111"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        String sendUrl = "lnotesync://";
        try {
            sendUrl=sendUrl+urlEncode(encoded);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "112"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        Log.d("onLaunchWithStartBackup", sendUrl);
        Log.d("onLaunchWithStartBackup", "encoded len:" +len);

        startLnotesSyncWithUrl(sendUrl);
    }

    private void onLaunchWithUrlSheme (Uri uri){
        Log.d("onLaunchWithUrlSheme", "launchWidthUrl LNotes");
        Log.d("onLaunchWithUrlSheme", uri.toString());

        String launchURL = uri.toString();
        if (launchURL.length()<=12){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "300"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        String encoded = launchURL.substring(12);
        if (encoded==null){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "301"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        try {
            encoded = urlDecode(encoded);
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "302"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        Log.d("onLaunchWithUrlSheme", "req encoded len: "+encoded.length());
        JSONObject data = null;
        try {
            data = new JSONObject(encoded);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (data == null){
            return;
        }
        String reqType = data.optString("reqType");
        String operation = data.optString("operation");
        String v = data.optString("v");
        if (v==null || v.length()==0){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(R.string.need_update_lnotesync)
                    .setCancelable(true)
                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
            return;
        }
        Log.d ("onLaunchWithUrlSheme", "operation: "+operation);

        JSONObject dHandler = null;
        String strProgress = "";
        dHandler = data.optJSONObject("handler");
        if (dHandler!=null){
            if (dHandler.has("i"))
            {
                int i = dHandler.optInt("i");
                if (dHandler.has("c")){
                    int cnt = dHandler.optInt("c");
                    if (i > cnt){
                        i = cnt;
                    }
                }
                strProgress = String.valueOf(i);
            }
            if (dHandler.has("c"))
            {
                strProgress = strProgress+ "/" + String.valueOf(dHandler.optInt("c"));
            }
        }
        ((TextView)findViewById(R.id.progressTextView)).setText(strProgress);

        if ("returndb".equals(operation)) {
            File pathDB = new File(getFilesDir(),"retdb");
            File pathTemp = new File(pathDB, "tempdbparts");

            if (data.optBoolean("st")) {

                if (pathDB.mkdir()) {
                    pathTemp.mkdir();
                }

                removeFilesFromDir(pathTemp);
                pathTemp.delete();

                if (pathTemp.exists()) {
                    new AlertDialog.Builder(QDVSendDataActivity.this).
                            setMessage(String.format(getString(R.string.error_with_id), "1"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }
            }

            pathTemp.mkdir();

            if (!pathTemp.exists() || !pathTemp.isDirectory()) {
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "2"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            }

            if (!data.optBoolean("isend")) {
                String fileData = data.optString("data");
                JSONObject sdata = new JSONObject();
                try {
                sdata.put("reqType", "getfile");
                sdata.put("operation", "returndb");
                sdata.put("ch", true);
                JSONObject handler = data.optJSONObject("handler");
                if (handler == null) {
                    handler = new JSONObject();
                }
                sdata.put("handler",  handler);
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(QDVSendDataActivity.this).
                            setMessage(String.format(getString(R.string.error_with_id), "3"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }

                String dataId = data.optString("id");

                if (fileData!=null && dataId!=null && fileData.length() != 0 && dataId.length() != 0) {
                    Log.d("onLaunchWithUrlSheme", "pathTemp: "+pathTemp);
                    Log.d("onLaunchWithUrlSheme", "fileName: "+dataId);

                    File filePath = new File(pathTemp, dataId);
                    FileOutputStream wfh = null;
                    try {
                        wfh = new FileOutputStream(filePath, false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(QDVSendDataActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "4"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.cancel, null).show();
                        Log.d("onLaunchWithUrlSheme", "File write error" +filePath.toString());
                        return;
                    }

                    if (wfh==null){
                        new AlertDialog.Builder(QDVSendDataActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "5"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.cancel, null).show();
                        Log.d("onLaunchWithUrlSheme", "File write error" +filePath.toString());
                        return;
                    }

                    char [] bufferToWrite = fileData.toCharArray();
                    try {
                        for (int n = 0; n < bufferToWrite.length; n++){
                            wfh.write (bufferToWrite[n]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(QDVSendDataActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "6"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.cancel, null).show();
                        Log.d("onLaunchWithUrlSheme", "File write error" +filePath.toString());
                        try {
                            wfh.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        return;
                    }
                    try {
                        wfh.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bufferToWrite = null;
                    fileData = null;

                    JSONArray dataFiles = new JSONArray();
                    dataFiles.put(dataId);
                    try {
                        sdata.put("delFiles", dataFiles);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(QDVSendDataActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "7"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.cancel, null).show();
                        return;
                    }
                }
                encoded = null;
                encoded = sdata.toString();

                int len = encoded.length();
                if (len > 1980) {
                    //Maximum length check
                    new AlertDialog.Builder(QDVSendDataActivity.this).
                            setMessage(String.format(getString(R.string.error_with_id), "8"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }
                String sendUrl = "lnotesync://";
                try {
                    sendUrl = sendUrl + urlEncode(encoded);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(QDVSendDataActivity.this).
                            setMessage(String.format(getString(R.string.error_with_id), "9"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }
                Log.d("onLaunchWithUrlSheme", sendUrl);
                Log.d("onLaunchWithUrlSheme", "encoded len:" +len);

                startLnotesSyncWithUrl (sendUrl);
            }

            if (data.optBoolean("isend")) {
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(R.string.restore_db_confirm)
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(QDVSendDataActivity.this, QDVNotesActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setPositiveButton(R.string.action_restore, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                File pathDB = new File(getFilesDir(), "retdb");
                                if (!pathDB.exists()) {
                                    pathDB.mkdir();
                                }
                                if (!pathDB.exists() || !pathDB.isDirectory()) {
                                    new AlertDialog.Builder(QDVSendDataActivity.this).
                                            setMessage(String.format(getString(R.string.error_with_id), "10"))
                                            .setCancelable(true)
                                            .setPositiveButton(R.string.cancel, null).show();
                                    return;
                                }

                                File pathTemp = new File(pathDB, "tempdbparts");
                                pathDB = new File(pathDB, "data.db");


                                FileOutputStream dbfh = null;
                                long num = 1;
                                File filePath = null;
                                try {
                                    while (true) {
                                        filePath = new File(pathTemp, "part" + String.valueOf(num));
                                        FileInputStream rfh = null;
                                        try {
                                            rfh = new FileInputStream(filePath);
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }

                                        if (rfh == null) {
                                            break;
                                        }
                                        if (dbfh == null) {
                                            dbfh = new FileOutputStream(pathDB, false);
                                        }

                                        byte[] copybuffer = new byte[2048];
                                        int count = 0;
                                        while ((count = rfh.read(copybuffer)) != -1) {
                                            dbfh.write(copybuffer, 0, count);
                                        }
                                        rfh.close();
                                        num = num + 1;
                                        Log.d("onLaunchWithUrlSheme", "Move file to db: " + filePath.toString());

                                        if (filePath.delete()) {
                                            Log.d("onLaunchWithUrlSheme", "File removed");
                                        } else {
                                            Log.d("onLaunchWithUrlSheme", "Error removing file: " + filePath.toString());
                                        }
                                    }
                                    if (dbfh != null) {
                                        dbfh.close();
                                    }

                                    //Check hash checksum
                                    filePath = new File(pathTemp, "sha1");
                                    Log.d("onLaunchWithUrlSheme", "Sha file: " + filePath.toString());
                                    FileInputStream rfh = null;
                                    try {
                                        rfh = new FileInputStream(filePath);
                                    } catch (FileNotFoundException e) {
                                        Log.d("onLaunchWithUrlSheme", "File not found. Copyng without checksum.");
                                    }

                                    if (rfh != null) {
                                        FileInputStream dbfis = null;
                                        try {
                                            dbfis = new FileInputStream(pathDB);
                                        } catch (FileNotFoundException e1) {
                                            e1.printStackTrace();
                                            new AlertDialog.Builder(QDVSendDataActivity.this).
                                                    setMessage(String.format(getString(R.string.error_with_id), "10.2"))
                                                    .setCancelable(true)
                                                    .setPositiveButton(R.string.cancel, null).show();
                                            return;
                                        }
                                        if (dbfis == null) {
                                            new AlertDialog.Builder(QDVSendDataActivity.this).
                                                    setMessage(String.format(getString(R.string.error_with_id), "10.3"))
                                                    .setCancelable(true)
                                                    .setPositiveButton(R.string.cancel, null).show();
                                            return;
                                        }

                                        if (filePath.length()>1024) {
                                            new AlertDialog.Builder(QDVSendDataActivity.this).
                                                    setMessage(String.format(getString(R.string.error_with_id), "10.4"))
                                                    .setCancelable(true)
                                                    .setPositiveButton(R.string.cancel, null).show();
                                            return;
                                        }
                                        byte[] readbuffer = new byte[(int)filePath.length()];
                                        int count = 0 ;
                                        try {
                                            count = rfh.read(readbuffer);
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                            new AlertDialog.Builder(QDVSendDataActivity.this).
                                                    setMessage(String.format(getString(R.string.error_with_id), "10.5"))
                                                    .setCancelable(true)
                                                    .setPositiveButton(R.string.cancel, null).show();
                                            return;
                                        };
                                        if (count != filePath.length()) {
                                            new AlertDialog.Builder(QDVSendDataActivity.this).
                                                    setMessage(String.format(getString(R.string.error_with_id), "10.6"))
                                                    .setCancelable(true)
                                                    .setPositiveButton(R.string.cancel, null).show();
                                            return;
                                        }
                                        String sha = new String(readbuffer);
                                        Log.d("onLaunchWithUrlSheme", "sha db: "+sha);
                                        rfh.close();

                                        MessageDigest md = MessageDigest.getInstance("SHA-1");
                                        byte[] hashbuffer = new byte[1024];
                                        count = 0;
                                        while ((count = dbfis.read(hashbuffer)) != -1) {
                                            md.update(hashbuffer, 0, count);
                                        }
                                        dbfis.close();

                                        if (filePath.delete()) {
                                            Log.d("onLaunchWithUrlSheme", "File removed");
                                        } else {
                                            Log.d("onLaunchWithUrlSheme", "Error removing file: " + filePath.toString());
                                        }

                                        String hashOfDb = new String (encodeHex(md.digest()));
                                        if (!hashOfDb.equals(sha)) {
                                            new AlertDialog.Builder(QDVSendDataActivity.this).
                                                    setMessage(String.format(getString(R.string.error_with_id), "10.7"))
                                                    .setCancelable(true)
                                                    .setPositiveButton(R.string.cancel, null).show();
                                            return;
                                        }
                                    }
                                } catch (NoSuchAlgorithmException e1) {
                                    e1.printStackTrace();
                                    new AlertDialog.Builder(QDVSendDataActivity.this).
                                            setMessage(String.format(getString(R.string.error_with_id), "11.1"))
                                            .setCancelable(true)
                                            .setPositiveButton(R.string.cancel, null).show();
                                    return;
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                    new AlertDialog.Builder(QDVSendDataActivity.this).
                                            setMessage(String.format(getString(R.string.error_with_id), "11.2"))
                                            .setCancelable(true)
                                            .setPositiveButton(R.string.cancel, null).show();
                                    return;
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    new AlertDialog.Builder(QDVSendDataActivity.this).
                                            setMessage(String.format(getString(R.string.error_with_id), "11.3"))
                                            .setCancelable(true)
                                            .setPositiveButton(R.string.cancel, null).show();
                                    return;
                                }

                                boolean isError = false;

                                File[] files = pathTemp.listFiles();
                                if (files!=null) {
                                    for (int n = 0; n < files.length; n++) {
                                        File file = files[n];
                                        isError = true;
                                        Log.e("onLaunchWithUrlSheme", "Unknown file: " + file.toString());
                                    }
                                }
                                if (isError) {
                                    new AlertDialog.Builder(QDVSendDataActivity.this).
                                            setMessage(String.format(getString(R.string.error_with_id), "12"))
                                            .setCancelable(true)
                                            .setPositiveButton(R.string.cancel, null).show();

                                }

                                File dbFile = new QDVMyBaseOpenHelper(QDVSendDataActivity.this, null).getFileDB();
                                dbFile.delete();
                                copyFile(pathDB, dbFile);
                                removeFilesFromDir(pathTemp);
                                pathTemp.delete();
                                Log.d("onLaunchWithUrlSheme", "Restore db complite");
                                try {
                                    String packageName = getPackageManager().getPackageInfo(QDVSendDataActivity.this.getPackageName(), 0)
                                            .packageName;
                                    Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                                    intent.putExtra("needReloadDb", true);
                                    startActivity(intent);
                                    finish();
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
            }
            return;
        }

        if (!"getfile".equals(reqType)){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "13"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        JSONArray arrayDelFiles = data.optJSONArray("delFiles");
        File pathDB = new File(getFilesDir(),"senddb");
        File pathTemp = new File(pathDB, "tempdbparts");
        if (arrayDelFiles!=null) {
            for (int k = 0; k<arrayDelFiles.length(); k++)
            {
                String fileName = arrayDelFiles.optString(k);
                if (fileName!= null && fileName.length()>0){
                    File fileNm = new File (pathTemp, fileName);
                    Log.d("onLaunchWithUrlSheme", "In request do remove file: "+fileNm.toString());
                    if (fileNm.delete()){
                        Log.d("onLaunchWithUrlSheme","File removed");
                    }
                    else
                    {
                        Log.d("onLaunchWithUrlSheme", "File not removed");
                    }
                }
            }
        }

        File fileNm = null;
        boolean isEnd = true;
        File[] fileList = pathTemp.listFiles();
        if (fileList!=null) {
            for (int fileNum = 0; fileNum < fileList.length; fileNum++) {
                File file = fileList[fileNum];
                if (!file.isDirectory()) {
                    isEnd = false;
                    fileNm = file;
                    break;
                }
            }
        }

        JSONObject sdata = new JSONObject();
        try {
            sdata.put("isend", isEnd);
            sdata.put("operation", "senddb");
            JSONObject dataHandler = data.optJSONObject("handler");
            if (dataHandler!=null){
                sdata.put("handler", dataHandler);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "14"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        if (!isEnd) {
            File filePath = fileNm;
            FileInputStream rfh = null;
            try {
                rfh = new FileInputStream(filePath);
                if (rfh == null) {
                    new AlertDialog.Builder(QDVSendDataActivity.this).
                            setMessage(String.format(getString(R.string.error_with_id), "15"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }
                long countBytes = filePath.length();
                if (countBytes <= 0) {
                    new AlertDialog.Builder(QDVSendDataActivity.this).
                            setMessage(String.format(getString(R.string.error_with_id), "16"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }
                if (countBytes > Integer.MAX_VALUE) {
                    new AlertDialog.Builder(QDVSendDataActivity.this).
                            setMessage(String.format(getString(R.string.error_with_id), "17"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                    return;
                }
                int iCountBytes = (int) countBytes;
                byte fileData[] = new byte[iCountBytes];
                rfh.read(fileData, 0, iCountBytes);
                rfh.close();
                sdata.put("data", new String(fileData, "ISO-8859-1"));
                sdata.put("id", fileNm.getName());
                fileData = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "18"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "19"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            } catch (JSONException e) {
                e.printStackTrace();
                new AlertDialog.Builder(QDVSendDataActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "20"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
                return;
            }
        }

        String sdataStr = sdata.toString();
        if (sdataStr==null || sdataStr.length() <=0){
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "21"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }

        encoded = sdataStr;


        int len = encoded.length();
        if (len > 1980) {
            //Maximum length check
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "22"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        String sendUrl = "lnotesync://";
        len = sendUrl.length();
        try {
            sendUrl = sendUrl+urlEncode(encoded);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            new AlertDialog.Builder(QDVSendDataActivity.this).
                    setMessage(String.format(getString(R.string.error_with_id), "23"))
                    .setCancelable(true)
                    .setPositiveButton(R.string.cancel, null).show();
            return;
        }
        Log.d("onLaunchWithUrlSheme", "Send: "+sendUrl);
        Log.d("onLaunchWithUrlSheme", "encoded len:" +len);

        startLnotesSyncWithUrl (sendUrl);
    }

    public String hexToString(String txtInHex)
    {
        byte [] txtInByte = new byte [txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2)
        {
            txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }

    private String urlDecode (String str) throws UnsupportedEncodingException {
        Log.d("onLaunchWithUrlSheme", "Decode from: "+str);
        str = URLDecoder.decode(str, "ISO-8859-1");
        Log.d("onLaunchWithUrlSheme", "Decode result: "+str);
        return str;
    }

    private String urlEncode (String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "ISO-8859-1");
    }
}
