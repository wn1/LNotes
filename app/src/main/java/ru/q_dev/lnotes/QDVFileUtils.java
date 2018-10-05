package ru.q_dev.lnotes;

import java.io.*;
import android.util.*;

public class QDVFileUtils
{
	public static boolean copyFile(InputStream is, OutputStream os) {

        if (is == null) {
			Log.d("copyFile", "is == null");
            return false;
        }

        byte[] copybuffer = new byte[2048];
        int count = 0;
        try {
            while ((count = is.read(copybuffer)) != -1) {
                os.write(copybuffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
	
	public static boolean copyFile(File fileFrom, File fileTo) {
		return copyFile(fileFrom, fileTo, false);
	}
	
	public static boolean copyFile(File fileFrom, File fileTo, boolean appendFile) {
		FileOutputStream fos = null;
        if (fos == null) {
            try {
                fos = new FileOutputStream(fileTo, appendFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }
		
		FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileFrom);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fis == null) {
			Log.d("copyFile", "fis == null");
			try {
				fos.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
            return false;
        }
		
		boolean result = false;
        try {
            result = copyFile(fis, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		try {
			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			fis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
        return result;
    }
	
	public static boolean copyFile(File fileFrom, OutputStream fos) {

		FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileFrom);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fis == null) {
			Log.d("copyFile", "fis == null");
            return false;
        }

		boolean result = false;
        try {
            result = copyFile(fis, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }

		try {
			fis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
        return result;
    }

	public static boolean copyFile(InputStream is, File fileTo, boolean appendFile) {
		FileOutputStream fos = null;
        if (fos == null) {
            try {
                fos = new FileOutputStream(fileTo, appendFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

		boolean result = false;
        try {
            result = copyFile(is, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }

		try {
			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        return result;
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
