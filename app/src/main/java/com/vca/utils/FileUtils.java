package com.vca.utils;

import android.util.Log;

import com.vca.R;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    public static String getFileExtension(String fullName) {
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    public static boolean mkdir(String ROOT, String dirPath) {
        Log.d(TAG, "mkdir: ROOT " + ROOT);
        Log.d(TAG, "mkdir: dirPath " + dirPath);
        File storageDirectory;
        if (dirPath == null) {
            storageDirectory = new File(ROOT);
        } else {
            storageDirectory = new File(ROOT, dirPath);
        }
        if (!storageDirectory.exists()) {
            boolean status = storageDirectory.mkdir();
            if (status)
                return true;
            else
                return false;
        } else
            return false;
    }

    public static boolean isFileAvailableInLocal(String fileName) {
        for (File localFile : getAllFiles(Constants.LOCAL_Folder_UPLOADED_DOCUMENTS)) {
            Log.d(TAG, "isFileAvailableInLocal: "+localFile.getName());
            if (localFile.getName().equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    public static List<File> getAllFiles(String dirPath) {
        List<File> fileList = new ArrayList<>();
        File targetDirectory = new File(dirPath);
        if (targetDirectory.listFiles() != null) {
            for (File eachFile : targetDirectory.listFiles()) {
                Log.d("TAG", "getAllFiles to flash : " + eachFile.getName());
                fileList.add(eachFile);
            }
        }
        return fileList;
    }

}
