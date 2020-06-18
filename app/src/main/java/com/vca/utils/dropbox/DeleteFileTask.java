package com.vca.utils.dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderBatchLaunch;
import com.dropbox.core.v2.files.DeleteResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Async task to upload a file to a directory
 */
public class DeleteFileTask extends AsyncTask<String, Void, DeleteResult> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onFileDeleted(DeleteResult result);

        void onError(Exception e);
    }

    public DeleteFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(DeleteResult result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onFileDeleted(result);
        }
    }

    @Override
    protected DeleteResult doInBackground(String... params) {
        String fileName = params[0];
        try {
            return mDbxClient.files().deleteV2(fileName);
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }
}