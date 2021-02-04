package com.vca.utils.dropbox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.vca.utils.FileUtils;
import com.vca.utils.TimeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Async task to upload a file to a directory
 */
public class UploadFileTask extends AsyncTask<List<String>, Integer, FileMetadata> {
    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);

        void onUploadProgressUpdate(int progress);

        void onError(Exception e);
    }

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    @Override
    protected FileMetadata doInBackground(List<String>... params) {
        List<String> uriList = params[0];
        Log.d("DEBUG", "doInBackground: uriList.size() : " + uriList.size());

        for (int i = 0; i < uriList.size(); i++) {
            publishProgress((i+1));
            String localUri = uriList.get(i);
            File localFile = UriHelpers.getFileForUri(mContext, Uri.parse(localUri));
            Log.d("DEBUG", "doInBackground: " + localFile.getName());

            if (localFile != null) {
                String remoteFolderPath = params[1].get(0);
                String localFileName = localFile.getName();
                try (InputStream inputStream = new FileInputStream(localFile)) {
                    mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + TimeUtil.getCurrentTime() + "." + FileUtils.getFileExtension(localFileName)).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
                } catch (DbxException | IOException e) {
                    mException = e;
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (values[0] == -1) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadProgressUpdate(values[0]);
        }
    }
}
