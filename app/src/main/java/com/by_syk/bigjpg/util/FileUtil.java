package com.by_syk.bigjpg.util;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by By_syk on 2017-06-08.
 */

public class FileUtil {
    public static boolean copyFile(File srcFile, File targetFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public static boolean record2Gallery(Context context, File newlyPicFile, boolean allInDir) {
        if (context == null || newlyPicFile == null || !newlyPicFile.exists()) {
            return false;
        }

        Log.d(C.LOG_TAG, "record2Gallery(): " + newlyPicFile + ", " + allInDir);

        if (C.SDK >= 19) {
            String[] filePaths;
            if (allInDir) {
                filePaths = newlyPicFile.getParentFile().list();
            } else {
                filePaths = new String[]{newlyPicFile.getPath()};
            }
            MediaScannerConnection.scanFile(context, filePaths, null, null);
        } else {
            if (allInDir) {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.fromFile(newlyPicFile.getParentFile())));
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(newlyPicFile)));
            }
        }

        return true;
    }

    @NonNull
    public static long getSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length();
    }

    @NonNull
    public static long getSize(String filePath) {
        if (filePath == null) {
            return 0;
        }
        return getSize(new File(filePath));
    }

    @NonNull
    public static String getReadableSize(Context context, File file) {
        if (context == null || file == null || !file.exists()) {
            return "";
        }
        return Formatter.formatFileSize(context, file.length());
    }

    @NonNull
    public static String getReadableSize(Context context, String filePath) {
        if (filePath == null) {
            return "";
        }
        return getReadableSize(context, new File(filePath));
    }
}
