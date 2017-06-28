package com.google.developer.taskmaker.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.developer.taskmaker.data.DatabaseContract.TaskColumns;

import java.util.ArrayList;
import java.util.Collections;

public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TABLE_TASKS = String.format("create table %s"
            +" (%s integer primary key autoincrement, %s text, %s integer, %s integer, %s integer)",
            DatabaseContract.TABLE_TASKS,
            TaskColumns._ID,
            TaskColumns.DESCRIPTION,
            TaskColumns.IS_COMPLETE,
            TaskColumns.IS_PRIORITY,
            TaskColumns.DUE_DATE
    );

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + DatabaseContract.TABLE_TASKS);

        onCreate(db);
    }
}
