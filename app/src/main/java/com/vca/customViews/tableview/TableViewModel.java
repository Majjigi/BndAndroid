/*
 * Copyright (c) 2018. Evren Coşkun
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.vca.customViews.tableview;

import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.vca.customViews.tableview.model.Cell;
import com.vca.customViews.tableview.model.ColumnHeader;
import com.vca.customViews.tableview.model.RowHeader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by evrencoskun on 4.02.2018.
 */

public class TableViewModel {

    // Constant size for dummy data sets
    private static final int COLUMN_SIZE = 3;
    private static final int ROW_SIZE = 10;

    public TableViewModel() {
        // initialize drawables

    }

    @NonNull
    private List<RowHeader> getSimpleRowHeaderList() {
        List<RowHeader> list = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            RowHeader header = new RowHeader(String.valueOf(i), getCurrentDate());
            list.add(header);
        }
        return list;
    }

    public static String getCurrentDate() {
        DateFormat df = new SimpleDateFormat("dd-MM-YYYY");
        String time = df.format(Calendar.getInstance().getTime());
        Log.d("prabhu", "getCurrentTime: df " + time);
        return time;
    }

    /**
     * This is a dummy model list test some cases.
     */
    @NonNull
    private List<ColumnHeader> getRandomColumnHeaderList() {
        List<ColumnHeader> list = new ArrayList<>();
        ColumnHeader header = new ColumnHeader("1", "Files Uploaded");
        ColumnHeader header1 = new ColumnHeader("2", "   In Progress   ");
        ColumnHeader header2 = new ColumnHeader("3", "    Rejected Files   ");
        list.add(header);
        list.add(header1);
        list.add(header2);

/*
        for (int i = 0; i < COLUMN_SIZE; i++) {
            String title = "column " + i;
            int nRandom = new Random().nextInt();
            if (nRandom % 4 == 0 || nRandom % 3 == 0 || nRandom == i) {
                title = "large column " + i;
            }

            ColumnHeader header = new ColumnHeader(String.valueOf(i), title);
            list.add(header);
        }*/

        return list;
    }

    /**
     * This is a dummy model list test some cases.
     */
    @NonNull
    private List<List<Cell>> getCellListForSortingTest() {
        List<List<Cell>> list = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            for (int j = 0; j < COLUMN_SIZE; j++) {
                Cell cell;
                // Create dummy id.
                String id = j + "-" + i;
                if (j == 0) {
                    cell = new Cell(id, "Prabhu");
                } else if (j == 1) {
                    cell = new Cell(id, "Kumara");
                } else {
                    cell = new Cell(id, "Swamy");
                }
                cellList.add(cell);
            }
            list.add(cellList);
        }
        return list;
    }

    @NonNull
    public List<List<Cell>> getCellList() {
        return getCellListForSortingTest();
    }

    @NonNull
    public List<RowHeader> getRowHeaderList() {
        return getSimpleRowHeaderList();
    }

    @NonNull
    public List<ColumnHeader> getColumnHeaderList() {
        return getRandomColumnHeaderList();
    }
}
