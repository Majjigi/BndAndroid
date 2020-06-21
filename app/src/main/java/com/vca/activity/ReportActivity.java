package com.vca.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
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
import com.vca.utils.Constants;
import com.vca.utils.dropbox.DeleteFileTask;
import com.vca.utils.dropbox.DropboxClientFactory;
import com.vca.utils.dropbox.ListFolderTask;
import com.vca.utils.dropbox.PicassoClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = "ReportActivity";
    public Unbinder unbinder;
    @BindView(R.id.files_list)
    RecyclerView recyclerView;
    private FilesAdapter mFilesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        unbinder = ButterKnife.bind(this);
        getSupportActionBar().setTitle("Reports");
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
                Log.d(TAG, "onFileClicked: ");
               /* mSelectedFile = file;
                performWithPermissions(FileAction.DOWNLOAD);*/
            }

            @Override
            public void onDeleteFileClicked(String filePath) {
                final ProgressDialog dialog = new ProgressDialog(ReportActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.setMessage("Deleting...");
                dialog.show();
                new DeleteFileTask(ReportActivity.this, DropboxClientFactory.getClient(), new DeleteFileTask.Callback() {
                    @Override
                    public void onFileDeleted(DeleteResult result) {
                        Log.d(TAG, "onFileDeleted: " + result.getMetadata().toString());
                        Metadata deletedItem = result.getMetadata();
                        dialog.cancel();
                        if (deletedItem instanceof FolderMetadata) {
                            Toast.makeText(ReportActivity.this, "Folder Deleted", Toast.LENGTH_SHORT).show();
                        } else if (deletedItem instanceof FileMetadata) {
                            Toast.makeText(ReportActivity.this, "File Deleted", Toast.LENGTH_SHORT).show();
                        }
//                        onRefreshFiles();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(ReportActivity.this, "Fail to Delete", Toast.LENGTH_SHORT).show();
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
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                mFilesAdapter.setFiles(result.getEntries());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to list folder.", e);
                Toast.makeText(ReportActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(Constants.Folder_REPORTS);
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
}