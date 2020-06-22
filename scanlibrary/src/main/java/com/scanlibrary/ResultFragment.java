package com.scanlibrary;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;

/**
 * Created by jhansi on 29/03/15.
 */
public class ResultFragment extends Fragment {
    private static final String TAG = ResultFragment.class.getSimpleName();
    private View view;
    private ImageView scannedImageView;
    private Button doneButton;
    private Button addButton;
    private Bitmap original;
    private Button originalButton;
    private Button MagicColorButton;
    private Button grayModeButton;
    private Button bwButton;
    private Bitmap transformed;
    private TextView pageNumber;
    private static ProgressDialogFragment progressDialogFragment;
    private String Temp_Path = Environment.getExternalStorageDirectory().getPath() + "/PureScanner/Temp/";

    public ResultFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.result_layout, null);
        init();
        setOptionSelected(1);
        return view;
    }

    private void init() {
        scannedImageView = (ImageView) view.findViewById(R.id.scannedImage);
        originalButton = (Button) view.findViewById(R.id.original);
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        MagicColorButton = (Button) view.findViewById(R.id.magicColor);
        MagicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        grayModeButton = (Button) view.findViewById(R.id.grayMode);
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        bwButton = (Button) view.findViewById(R.id.BWMode);
        bwButton.setOnClickListener(new BWButtonClickListener());
        Bitmap bitmap = getBitmap();
        setScannedImage(bitmap);
//        BWSelected();
        doneButton = (Button) view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new DoneButtonClickListener());
        addButton = (Button) view.findViewById(R.id.addBtn);
        addButton.setOnClickListener(new AddButtonClickListener());
        pageNumber = (TextView) view.findViewById(R.id.pageNumber);
        int pageNumberValue = getArguments().getInt(ScanConstants.PAGE_NUM);
        pageNumber.setText("" + pageNumberValue);
    }

    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            original = Utils.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        Uri uri = getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
        return uri;
    }

    public void setScannedImage(Bitmap scannedImage) {
        scannedImageView.setImageBitmap(scannedImage);
    }

    private class DoneButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.loading));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent data = new Intent();
                        Bitmap bitmap = transformed;
                        if (bitmap == null) {
                            bitmap = original;
                        }
                        Uri uri = Utils.getUri(getActivity(), bitmap);
                        data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                        data.putExtra(ScanConstants.SCAN_MORE, false);
                        getActivity().setResult(Activity.RESULT_OK, data);
                        original.recycle();
                        System.gc();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                                getActivity().finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private class AddButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.loading));
//            clearTempImages();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent data = new Intent();
                        Bitmap bitmap = transformed;
                        if (bitmap == null) {
                            bitmap = original;
                        }
                        Uri uri = Utils.getUri(getActivity(), bitmap);
                        data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                        data.putExtra(ScanConstants.SCAN_MORE, true);
                        getActivity().setResult(Activity.RESULT_OK, data);

                        original.recycle();
                        System.gc();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                                getActivity().finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void clearTempImages() {
        try {
            File tempFolder = new File(ScanConstants.IMAGE_PATH);
            for (File f : tempFolder.listFiles()) {
                if (f.getName().startsWith("IMG"))
                    f.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BWButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            BWSelected();
        }
    }

    private void BWSelected() {
        setOptionSelected(4);
        showProgressDialog(getResources().getString(R.string.applying_filter));
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    transformed = ((ScanActivity) getActivity()).getBWBitmap(original);
                } catch (final OutOfMemoryError e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            transformed = original;
                            scannedImageView.setImageBitmap(original);
                            e.printStackTrace();
                            dismissDialog();
                            BWSelected();
                        }
                    });
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scannedImageView.setImageBitmap(transformed);
                        dismissDialog();
                    }
                });
            }
        });
    }

    private class MagicColorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            setOptionSelected(2);
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getMagicColorBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }

    private class OriginalButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setOptionSelected(1);
            originalButton.setBackgroundColor(getResources().getColor(R.color.light_orange, null));
            try {
                showProgressDialog(getResources().getString(R.string.applying_filter));
                transformed = original;
                scannedImageView.setImageBitmap(original);
                dismissDialog();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                dismissDialog();
            }
        }
    }

    private void setOptionSelected(int i) {
        if (i == 1) {
            originalButton.setBackgroundColor(getResources().getColor(R.color.light_orange, null));
            MagicColorButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            grayModeButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            bwButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
        } else if (i == 2) {
            originalButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            MagicColorButton.setBackgroundColor(getResources().getColor(R.color.light_orange, null));
            grayModeButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            bwButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
        } else if (i == 3) {
            originalButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            MagicColorButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            grayModeButton.setBackgroundColor(getResources().getColor(R.color.light_orange, null));
            bwButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
        } else if (i == 4) {
            originalButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            MagicColorButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            grayModeButton.setBackgroundColor(getResources().getColor(R.color.transparent, null));
            bwButton.setBackgroundColor(getResources().getColor(R.color.light_orange, null));
        }
    }

    private class GrayButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            setOptionSelected(3);
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getGrayBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }

    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }
}