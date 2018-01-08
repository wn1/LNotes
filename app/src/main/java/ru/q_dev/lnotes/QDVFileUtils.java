package ru.q_dev.lnotes;
import java.io.*;
import android.util.*;

public class QDVFileUtils
{
	public static boolean copyFile(File fileFrom, File fileTo) {
        FileInputStream rfh = null;
        try {
            rfh = new FileInputStream(fileFrom);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (rfh == null) {
			Log.d("copyFile", "rfh == null");
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

    public static boolean removeFilesFromDir(File pathDir) {
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
	
}
