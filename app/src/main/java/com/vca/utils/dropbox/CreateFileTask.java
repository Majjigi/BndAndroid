package com.vca.utils.dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderBatchLaunch;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Async task to upload a file to a directory
 */
public class CreateFileTask extends AsyncTask<String, Void, CreateFolderBatchLaunch> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onFilesCreated(CreateFolderBatchLaunch result);

        void onError(Exception e);
    }

    public CreateFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(CreateFolderBatchLaunch result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onFilesCreated(result);
        }
    }

    @Override
    protected CreateFolderBatchLaunch doInBackground(String... params) {
        String fileName = params[0];
        try {
            List<String> fileList = new ArrayList<>();
            fileList.add("/2018");
            fileList.add("/2019");
            fileList.add("/2020");
            fileList.add("/2021/01 General Ledger");
            fileList.add("/2021/02 Accounts Receivable");
            fileList.add("/2021/03 Accounts Payable");
            fileList.add("/2021/04 Banking");
            fileList.add("/2021/05 Investments");
            fileList.add("/2021/06 Loan & Other Capital");
            fileList.add("/2021/07 Compliances");
            fileList.add("/2021/08 Financials");
            fileList.add("/2021/09 Reports & MIS");
            fileList.add("/Masters/Bank");
            fileList.add("/Masters/Director KYC");
            fileList.add("/Masters/ESIC");
            fileList.add("/Masters/GST");
            fileList.add("/Masters/PAN");
            fileList.add("/Masters/Patents");
            fileList.add("/Masters/Professional Tax");
            fileList.add("/Masters/Provident Fund");
            fileList.add("/Masters/ROC & MCA");
            fileList.add("/Masters/Shop & Establishment");
            fileList.add("/Masters/Patents");
            fileList.add("/Masters/TAN");
            fileList.add("/Masters/Trademarks");
            return mDbxClient.files().createFolderBatch(fileList);
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }
}