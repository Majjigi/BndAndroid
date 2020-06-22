package com.vca.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.evrencoskun.tableview.TableView;
import com.vca.R;
import com.vca.customViews.tableview.TableViewAdapter;
import com.vca.customViews.tableview.TableViewListener;
import com.vca.customViews.tableview.TableViewModel;
import com.vca.customViews.tableview.model.Cell;
import com.vca.customViews.tableview.model.RowHeader;
import com.vca.utils.Constants;
import com.vca.utils.TimeUtil;
import com.vca.utils.dropbox.DropboxClientFactory;
import com.vca.utils.dropbox.ListFolderTask;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {
    private static final String TAG = "StatsActivity";
    private TableView mTableView;
    List<Metadata> inTrayList = new ArrayList<Metadata>();
    List<Metadata> inProgressList = new ArrayList<Metadata>();
    List<Metadata> rejectFileList = new ArrayList<Metadata>();
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        mTableView = findViewById(R.id.tableview);
        getSupportActionBar().setTitle("Statistics of Book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getFileCount();
    }

    private void getFileCount() {
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading...");
        dialog.show();
        getIntrayCount();
    }

    private void getIntrayCount() {
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                inTrayList = result.getEntries();
                Log.d(TAG, "onDataLoaded: " + inTrayList.size());
                for (Metadata data : inTrayList) {
                    Log.d(TAG, "onDataLoaded: " + data.getName());
                }
                getInProgressFileList();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to list folder.", e);
                if (dialog != null) {
                    dialog.dismiss();
                }
                Toast.makeText(StatsActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(Constants.Folder_INTRAY);
    }

    private void getInProgressFileList() {
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                inProgressList = result.getEntries();
                Log.d(TAG, "onDataLoaded: " + inTrayList.size());
                for (Metadata data : inTrayList) {
                    Log.d(TAG, "onDataLoaded: " + data.getName());
                }
                getRejectedFileList();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to list folder.", e);
                if (dialog != null) {
                    dialog.dismiss();
                }
                Toast.makeText(StatsActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(Constants.Folder_IN_PROGRESS);
    }

    private void getRejectedFileList() {
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                rejectFileList = result.getEntries();
                Log.d(TAG, "onDataLoaded: " + inTrayList.size());
                for (Metadata data : inTrayList) {
                    Log.d(TAG, "onDataLoaded: " + data.getName());
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
                initializeTableView();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to list folder.", e);
                if (dialog != null) {
                    dialog.dismiss();
                }
                Toast.makeText(StatsActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(Constants.Folder_RETURNED);
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

    private void initializeTableView() {
        // Create TableView View model class  to group view models of TableView
        TableViewModel tableViewModel = new TableViewModel();

        // Create TableView Adapter
        TableViewAdapter tableViewAdapter = new TableViewAdapter(tableViewModel);

        mTableView.setAdapter(tableViewAdapter);
        mTableView.setTableViewListener(new TableViewListener(mTableView));

        Log.d(TAG, "initializeTableView: " + inTrayList.size());
        Log.d(TAG, "initializeTableView: " + inProgressList.size());
        Log.d(TAG, "initializeTableView: " + rejectFileList.size());
        // Load the dummy data to the TableView
        tableViewAdapter.setAllItems(tableViewModel.getColumnHeaderList(), getSimpleRowHeaderList(), getCellListForSortingTest());
    }

    @NonNull
    private List<RowHeader> getSimpleRowHeaderList() {
        List<RowHeader> list = new ArrayList<>();
        RowHeader header = new RowHeader(TimeUtil.getCurrentDate(), TimeUtil.getCurrentDate());
        list.add(header);
        return list;
    }

    @NonNull
    private List<List<Cell>> getCellListForSortingTest() {
        List<List<Cell>> list = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            List<Cell> cellList = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                Cell cell;
                // Create dummy id.
                String id = j + "-" + i;
                if (j == 0) {
                    cell = new Cell(id, "" + (inTrayList.size() + inProgressList.size() + rejectFileList.size()));
                } else if (j == 1) {
                    cell = new Cell(id, "" + inProgressList.size());
                } else {
                    cell = new Cell(id, "" + rejectFileList.size());
                }
                cellList.add(cell);
            }
            list.add(cellList);
        }
        return list;
    }
}