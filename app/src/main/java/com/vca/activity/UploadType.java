package com.vca.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.sharing.FileAction;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;
import com.vca.R;
import com.vca.activity.homeScreen.FilesAdapter;
import com.vca.activity.homeScreen.MainActivity;
import com.vca.utils.Constants;
import com.vca.utils.dropbox.DownloadFileTask;
import com.vca.utils.dropbox.DropboxClientFactory;
import com.vca.utils.dropbox.UploadFileTask;
import com.viethoa.DialogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import okhttp3.OkHttpClient;

public class UploadType extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    String[] statutory = {"Select Any One", "Sales", "Purchase", "Expense", "Collection", "Payment"};
    private static final String TAG = "UploadType";

    public final static String EXTRA_PATH = "FilesActivity_Path";
    private static final int PICKFILE_REQUEST_CODE = 1;
    private String mPath = "";
    private FilesAdapter mFilesAdapter;
    private FileMetadata mSelectedFile;
    private long exitTime;
    private final static int EXIT_TIME = 2000;
    public static Stack<String> filePathHistory = new Stack<>();
    private List<String> scannedBitmaps = new ArrayList<>();

    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    private String filePath;
    public String gottoken;
    public String gotemail;
    public String selectedItem;
    private static final String ROOT_URL = "http://192.168.1.16:8000/android/api/fileupload";

    String imageData;
    File imageFile;
    String filename;
    Bitmap photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        gottoken = getIntent().getExtras().getString("sendtoken");
        gotemail = getIntent().getExtras().getString("sendemail");
        // Toast.makeText(UploadType.this, "^^^^^^^"+gottoken ,Toast.LENGTH_LONG).show();
        // Toast.makeText(UploadType.this, "^^^^^^^"+gotemail ,Toast.LENGTH_LONG).show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_type);

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, statutory);

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner

        spin.setAdapter(aa);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getItemAtPosition(i).equals("Select Any One")) {
            //do nothing
        } else {

            selectedItem = statutory[i];
            Toast.makeText(getApplicationContext(), statutory[i], Toast.LENGTH_LONG).show();

            scannedBitmaps.clear();
            String title = "Upload Documents";
            String message = "Please select your option";
            String negativeButton = "Camera";
            String positiveButton = "Gallery";
            Dialog myDialog = DialogUtils.createDialogMessage(UploadType.this, title, message,
                    negativeButton, positiveButton, false, new DialogUtils.DialogListener() {
                        @Override
                        public void onPositiveButton() {
                            performWithPermissions(UploadType.FileAction.UPLOAD);
                        }

                        @Override
                        public void onNegativeButton() { // Camera
                            Intent intent = new Intent(UploadType.this, ScanActivity.class);
                            intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                            startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
                        }
                    });

            if (myDialog != null && !myDialog.isShowing()) {
                myDialog.setCanceledOnTouchOutside(true);
                myDialog.show();
            }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void performWithPermissions(final UploadType.FileAction action) {
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

    private void requestPermissionsForAction(UploadType.FileAction action) {
        ActivityCompat.requestPermissions(
                this,
                action.getPermissions(),
                action.getCode()
        );
    }


    private boolean shouldDisplayRationaleForAction(UploadType.FileAction action) {
        for (String permission : action.getPermissions()) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermissionsForAction(UploadType.FileAction action) {
        for (String permission : action.getPermissions()) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void performAction(UploadType.FileAction action) {
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

        Intent filesIntent;
        filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
        filesIntent.setType("image/*");  //use image/* for photos, etc.
        filesIntent.putExtra("outputFormat",
                Bitmap.CompressFormat.JPEG.toString());

        Log.d("picked file is", String.valueOf(filesIntent));
        startActivityForResult(filesIntent, PICKFILE_REQUEST_CODE);
    }

    private void downloadFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading");
        dialog.show();

        new DownloadFileTask(UploadType.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
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
                Toast.makeText(UploadType.this,
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


    private enum FileAction {
        DOWNLOAD(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        UPLOAD(Manifest.permission.READ_EXTERNAL_STORAGE);

        private static final UploadType.FileAction[] values = values();

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

        public static UploadType.FileAction fromCode(int code) {
            if (code < 0 || code >= values.length) {
                throw new IllegalArgumentException("Invalid FileAction code: " + code);
            }
            return values[code];
        }
    }


//    private void uploadFile(List<String> fileUri) {
//        final ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        dialog.setCancelable(false);
//        dialog.setMessage("Uploading...");
//        dialog.show();
//        List<String> inTrayPath = new ArrayList<String>();
//
//        inTrayPath.add(Constants.Folder_INTRAY);
//
//        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
//            @Override
//            public void onUploadComplete(FileMetadata r) {
//                dialog.dismiss();
//                Toast.makeText(UploadType.this, "File Uploaded", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onUploadProgressUpdate(int progress) {
//                Log.d(TAG, "onUploadProgressUpdate: " + progress);
//                dialog.setMessage("Uploading...   " + progress + " of " + fileUri.size());
//            }
//
//            @Override
//            public void onError(Exception e) {
//                dialog.dismiss();
//                Log.e(TAG, "Failed to upload file.", e);
//                Toast.makeText(UploadType.this, "Failed to upload file", Toast.LENGTH_LONG).show();
//            }
//        }).execute(fileUri, inTrayPath);
//
//
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                //  imageData=data.toString();
                Uri picUri = data.getData();
                Log.d("picUri", String.valueOf(picUri));


                filePath = getPath(picUri);
                if (filePath != null) {

                    Log.d("filePath", String.valueOf(filePath));
                    //  Toast.makeText(UploadType.this, "filepath " + filePath, Toast.LENGTH_LONG).show();

                    imageFile = new File(filePath);
                    Log.d("imageFile", String.valueOf(imageFile));

                    filename = imageFile.getName();
                    Log.d("File name", String.valueOf(filename));

                    try {
                        photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendPost();

//                    Testing with volleyMultipart request

                    //uploadBitmap(photo);


                }

                // This is the result of a call to launchFilePicker
//                List<String> scannedImage = new ArrayList<>();
//
//                if (data.getClipData() == null) {
//                    scannedImage.add(data.getData().toString());
//                }
//                else {
//                    ClipData clipData = data.getClipData();
//                    for (int i = 0; i < clipData.getItemCount(); i++) {
//                        {
//                            scannedImage.add(clipData.getItemAt(i).getUri().toString());
//                        }
//                    }
//
//                 //   uploadFile(scannedImage);
//
//
//                }

            }
            if ((requestCode == ScanConstants.START_CAMERA_REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
                Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
                Log.d(TAG, "onActivityResult: " + uri);
                boolean doScanMore = data.getExtras().getBoolean(ScanConstants.SCAN_MORE);
                scannedBitmaps.add("" + uri);
                if (doScanMore) {
                    Intent intent = new Intent(this, ScanActivity.class);
                    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                    intent.putExtra("PAGE_NUM", scannedBitmaps.size() + 1);
                    startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
                } else {
                    Log.d(TAG, "onActivityResult: " + scannedBitmaps.size());
                    //uploadFile(scannedBitmaps);


                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }


//    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed: " + filePathHistory.size());
//        if (filePathHistory.size() == 1) {
//            if ((System.currentTimeMillis() - exitTime) > EXIT_TIME) {
//                Toast.makeText(getApplicationContext(), R.string.press_back_message, Toast.LENGTH_SHORT).show();
//                exitTime = System.currentTimeMillis();
//            } else {
//                finish();
//            }
//        } else {
//            filePathHistory.pop();
//            //onRefreshFiles();
//        }
//    }
    }

    private void uploadBitmap(Bitmap photo) {

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, ROOT_URL,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(new String(response.data));
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("GotError", "" + error.getMessage());
                }) {


            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".jpg", getFileDataFromDrawable(photo)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    private byte[] getFileDataFromDrawable(Bitmap photo) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void sendPost() {
        final ProgressDialog loading = new ProgressDialog(UploadType.this);
        loading.setMessage("Please Wait...");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        JSONObject object = new JSONObject();
        try {
            //input your API parameters
            object.put("org_email", gotemail);
            object.put("upload_type", selectedItem);
            object.put("upload_file", getStringImage(photo));
            object.put("token", gottoken);
            object.put("upload_file_name", filename);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Enter the correct url for your api service site
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, ROOT_URL, object,
                response -> {
                    Toast.makeText(UploadType.this, response.toString(), Toast.LENGTH_LONG).show();
                    Log.d("JSON", String.valueOf(response));
                    loading.dismiss();
                }
                , error -> {
            loading.dismiss();
            VolleyLog.d("Error", "Error: " + error.getMessage());
            Toast.makeText(UploadType.this, "error *************", Toast.LENGTH_LONG).show();
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);

    }

    private String getPath(Uri picUri) {
        Cursor cursor = getContentResolver().query(picUri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

}