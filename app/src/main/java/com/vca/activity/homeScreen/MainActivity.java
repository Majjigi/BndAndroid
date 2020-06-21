package com.vca.activity.homeScreen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dropbox.core.v2.files.CreateFolderBatchLaunch;
import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;
import com.vca.R;
import com.vca.activity.InTrayActivity;
import com.vca.activity.ReportActivity;
import com.vca.activity.SettingActivity;
import com.vca.activity.UploadActivity;
import com.vca.utils.Constants;
import com.vca.utils.FileUtils;
import com.vca.utils.dropbox.CreateFileTask;
import com.vca.utils.dropbox.DeleteFileTask;
import com.vca.utils.dropbox.DownloadFileTask;
import com.vca.utils.dropbox.DropboxClientFactory;
import com.vca.utils.dropbox.ListFolderTask;
import com.vca.utils.dropbox.PicassoClient;
import com.vca.utils.dropbox.UploadFileTask;
import com.vca.utils.dropbox.UriHelpers;
import com.viethoa.DialogUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity implements HomeScreenView {
    private static final String TAG = "MainActivity";
    public Unbinder unbinder;
  /*  @BindView(R.id.upload_btn)
    FloatingActionButton mUploadBtn;
    @BindView(R.id.files_list)
    RecyclerView recyclerView;*/

    @BindView(R.id.upload_btn)
    public Button mUploadBtn;
    @BindView(R.id.report_btn)
    public Button mReportBtn;
    @BindView(R.id.stats_btn)
    public Button mStatsBtn;
    @BindView(R.id.inTray_btn)
    public Button mInTrayBtn;

    HomeScreenPresenter presenter;
    public final static String EXTRA_PATH = "FilesActivity_Path";
    private static final int PICKFILE_REQUEST_CODE = 1;
    private String mPath = "";
    private FilesAdapter mFilesAdapter;
    private FileMetadata mSelectedFile;
    private long exitTime;
    private final static int EXIT_TIME = 2000;
    public static Stack<String> filePathHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filePathHistory.push("");
        FileUtils.mkdir(Constants.LOCAL_Folder_ROOT, null);
        FileUtils.mkdir(Constants.LOCAL_Folder_UPLOADED_DOCUMENTS, null);
        unbinder = ButterKnife.bind(this);
        presenter = new HomeScreenPresenterImpl(this, this);
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "Upload Documents";
                String message = "Please select your option";
                String negativeButton = "Camera";
                String positiveButton = "Gallery";
                Dialog myDialog = DialogUtils.createDialogMessage(MainActivity.this, title, message,
                        negativeButton, positiveButton, false, new DialogUtils.DialogListener() {
                            @Override
                            public void onPositiveButton() {
                                performWithPermissions(FileAction.UPLOAD);
                            }

                            @Override
                            public void onNegativeButton() { // Camera
                                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                                startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
                            }
                        });

                if (myDialog != null && !myDialog.isShowing()) {
                    myDialog.setCanceledOnTouchOutside(true);
                    myDialog.show();
                }
            }
        });

        mInTrayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InTrayActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        mReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        PicassoClient.init(this, DropboxClientFactory.getClient());
        mFilesAdapter = new FilesAdapter(this, PicassoClient.getPicasso(), new FilesAdapter.Callback() {
            @Override
            public void onFolderClicked(FolderMetadata folder) {
                Log.d(TAG, "onFolderClicked: ");
                filePathHistory.add(folder.getPathLower());
                onRefreshFiles();
            }

            @Override
            public void onFileClicked(final FileMetadata file) {
                Log.d(TAG, "onFileClicked: ");
               /* mSelectedFile = file;
                performWithPermissions(FileAction.DOWNLOAD);*/
            }

            @Override
            public void onDeleteFileClicked(String filePath) {
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.setMessage("Deleting...");
                dialog.show();
                new DeleteFileTask(MainActivity.this, DropboxClientFactory.getClient(), new DeleteFileTask.Callback() {
                    @Override
                    public void onFileDeleted(DeleteResult result) {
                        Log.d(TAG, "onFileDeleted: " + result.getMetadata().toString());
                        Metadata deletedItem = result.getMetadata();
                        dialog.cancel();
                        if (deletedItem instanceof FolderMetadata) {
                            Toast.makeText(MainActivity.this, "Folder Deleted", Toast.LENGTH_SHORT).show();
                        } else if (deletedItem instanceof FileMetadata) {
                            Toast.makeText(MainActivity.this, "File Deleted", Toast.LENGTH_SHORT).show();
                        }
                        onRefreshFiles();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to Delete", Toast.LENGTH_SHORT).show();
                    }
                }).execute(filePath);
            }
        });
/*
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mFilesAdapter);*/
        mSelectedFile = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_setting:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void uploadFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading...");
        dialog.show();

        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();
                String message = result.getName() + " size " + result.getSize() + " modified " + DateFormat.getDateTimeInstance().format(result.getClientModified());
                Toast.makeText(MainActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                Log.e(TAG, "Failed to upload file.", e);
                Toast.makeText(MainActivity.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
            }
        }).execute(fileUri, Constants.Folder_INTRAY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null)
            unbinder.unbind();
    }

    private void performWithPermissions(final FileAction action) {
        if (hasPermissionsForAction(action)) {
            performAction(action);
            return;
        }

        if (shouldDisplayRationaleForAction(action)) {
            new AlertDialog.Builder(this)
                    .setMessage("This app requires storage access to download and upload files.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissionsForAction(action);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            requestPermissionsForAction(action);
        }
    }

    private boolean hasPermissionsForAction(FileAction action) {
        for (String permission : action.getPermissions()) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldDisplayRationaleForAction(FileAction action) {
        for (String permission : action.getPermissions()) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    private void requestPermissionsForAction(FileAction action) {
        ActivityCompat.requestPermissions(
                this,
                action.getPermissions(),
                action.getCode()
        );
    }

    @Override
    public void onLoginStarted() {

    }

    @Override
    public void onLoginError(String error) {

    }

    @Override
    public void onLoginSuccess() {

    }

    @Override
    public void onSignOutStarted() {

    }

    @Override
    public void onSignOut() {

    }

    @Override
    public void onRefreshFiles() {
      /*  Log.d(TAG, "onRefreshFiles: ");
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.show();

        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                dialog.dismiss();
                mFilesAdapter.setFiles(result.getEntries());
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                Log.e(TAG, "Failed to list folder.", e);
                Toast.makeText(MainActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(filePathHistory.peek());*/
    }

    private enum FileAction {
        DOWNLOAD(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        UPLOAD(Manifest.permission.READ_EXTERNAL_STORAGE);

        private static final FileAction[] values = values();

        private final String[] permissions;

        FileAction(String... permissions) {
            this.permissions = permissions;
        }

        public int getCode() {
            return ordinal();
        }

        public String[] getPermissions() {
            return permissions;
        }

        public static FileAction fromCode(int code) {
            if (code < 0 || code >= values.length) {
                throw new IllegalArgumentException("Invalid FileAction code: " + code);
            }
            return values[code];
        }
    }

    private void performAction(FileAction action) {
        switch (action) {
            case UPLOAD:
                launchFilePicker();
                break;
            case DOWNLOAD:
                if (mSelectedFile != null) {
                    downloadFile(mSelectedFile);
                } else {
                    Log.e(TAG, "No file selected to download.");
                }
                break;
            default:
                Log.e(TAG, "Can't perform unhandled file action: " + action);
        }
    }

    private void launchFilePicker() {
        // Launch intent to pick file for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    private void downloadFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading");
        dialog.show();

        new DownloadFileTask(MainActivity.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();

                if (result != null) {
                    viewFileInExternalApp(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to download file.", e);
                Toast.makeText(MainActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    private void viewFileInExternalApp(File result) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = result.getName().substring(result.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        intent.setDataAndType(Uri.fromFile(result), type);

        // Check for a handler first to avoid a crash
        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // This is the result of a call to launchFilePicker
                uploadFile(data.getData().toString());
            }
        }
        if ((requestCode == ScanConstants.START_CAMERA_REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Log.d(TAG, "onActivityResult: " + uri);
            uploadFile("" + uri);
           /* boolean doScanMore = data.getExtras().getBoolean(ScanConstants.SCAN_MORE);
            FileUriHelpers.getFileForUri(this, uri);


            scannedBitmaps.add(uri);
            if (doScanMore) {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                intent.putExtra("PAGE_NUM", scannedBitmaps.size() + 1);
                startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
            } else {
                uploadFile(uri);
                CreatePdf(scannedBitmaps);
            }*/
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + filePathHistory.size());
        if (filePathHistory.size() == 1) {
            if ((System.currentTimeMillis() - exitTime) > EXIT_TIME) {
                Toast.makeText(getApplicationContext(), R.string.press_back_message, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        } else {
            filePathHistory.pop();
            onRefreshFiles();
        }
    }
}
