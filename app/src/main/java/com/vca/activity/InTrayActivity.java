package com.vca.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.vca.R;
import com.vca.activity.homeScreen.FilesAdapter;
import com.vca.activity.homeScreen.MainActivity;
import com.vca.utils.Constants;
import com.vca.utils.FileUtils;
import com.vca.utils.dropbox.DeleteFileTask;
import com.vca.utils.dropbox.DownloadFileTask;
import com.vca.utils.dropbox.DropboxClientFactory;
import com.vca.utils.dropbox.ListFolderTask;
import com.vca.utils.dropbox.PicassoClient;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class InTrayActivity extends AppCompatActivity {
    private static final String TAG = "InTrayActivity";
    public Unbinder unbinder;
    @BindView(R.id.files_list)
    RecyclerView recyclerView;
    private FilesAdapter mFilesAdapter;
    private FileMetadata mSelectedFile;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_tray);
        unbinder = ButterKnife.bind(this);
        getSupportActionBar().setTitle("Uploaded Documents");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mFilesAdapter = new FilesAdapter(this, PicassoClient.getPicasso(), new FilesAdapter.Callback() {
            @Override
            public void onFolderClicked(FolderMetadata folder) {
                Log.d(TAG, "onFolderClicked: ");
                /*filePathHistory.add(folder.getPathLower());
                onRefreshFiles();*/
            }

            @Override
            public void onFileClicked(final FileMetadata file) {
                mSelectedFile = file;
                if (mSelectedFile != null) {
                    if (FileUtils.isFileAvailableInLocal(Constants.LOCAL_Folder_UPLOADED_DOCUMENTS, file.getName())) {
                        openFile(new File(Constants.LOCAL_Folder_UPLOADED_DOCUMENTS + file.getName()));
                    } else {
                        downloadFile(mSelectedFile);
                    }
                } else {
                    Log.e(TAG, "No file selected to download.");
                }
            }

            @Override
            public void onDeleteFileClicked(String filePath) {
                final ProgressDialog dialog = new ProgressDialog(InTrayActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.setMessage("Deleting...");
                dialog.show();
                new DeleteFileTask(InTrayActivity.this, DropboxClientFactory.getClient(), new DeleteFileTask.Callback() {
                    @Override
                    public void onFileDeleted(DeleteResult result) {
                        Log.d(TAG, "onFileDeleted: " + result.getMetadata().toString());
                        Metadata deletedItem = result.getMetadata();
                        dialog.cancel();
                        if (deletedItem instanceof FolderMetadata) {
                            Toast.makeText(InTrayActivity.this, "Folder Deleted", Toast.LENGTH_SHORT).show();
                        } else if (deletedItem instanceof FileMetadata) {
                            Toast.makeText(InTrayActivity.this, "File Deleted", Toast.LENGTH_SHORT).show();
                        }
//                        onRefreshFiles();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(InTrayActivity.this, "Fail to Delete", Toast.LENGTH_SHORT).show();
                    }
                }).execute(filePath);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mFilesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading...");
        dialog.show();
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                if (dialog!= null){
                    dialog.dismiss();
                }
                mFilesAdapter.setFiles(result.getEntries());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to list folder.", e);
                if (dialog!= null){
                    dialog.dismiss();
                }
                Toast.makeText(InTrayActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(Constants.Folder_INTRAY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null)
            unbinder.unbind();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void downloadFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading");
        dialog.show();

        new DownloadFileTask(InTrayActivity.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();
                if (result != null) {
                    openFile(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to download file.", e);
                Toast.makeText(InTrayActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file, Constants.LOCAL_Folder_UPLOADED_DOCUMENTS);
    }

    private void openFile(File url) {
        Log.d(TAG, "openFile: " + url.getAbsolutePath());
        Uri uri;
        try {
            if (Build.VERSION.SDK_INT < 24)
                uri = Uri.fromFile(url);
            else {
                uri = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", url);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip")) {
                // ZIP file
                intent.setDataAndType(uri, "application/zip");
            } else if (url.toString().contains(".rar")) {
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed");
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                    url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }
}