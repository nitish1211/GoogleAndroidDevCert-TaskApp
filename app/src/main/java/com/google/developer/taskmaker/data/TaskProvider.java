package com.google.developer.taskmaker.data;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class TaskProvider extends ContentProvider {
    private static final String TAG = TaskProvider.class.getSimpleName();

    private static final int CLEANUP_JOB_ID = 43;

    private static final int TASKS = 100;
    private static final int TASKS_WITH_ID = 101;

    private TaskDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // content://com.google.developer.taskmaker/tasks
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_TASKS,
                TASKS);

        // content://com.google.developer.taskmaker/tasks/id
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_TASKS + "/#",
                TASKS_WITH_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new TaskDbHelper(getContext());
        manageCleanupJob();
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null; /* Not used */
    }

    @Nullable
    @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        //TODO: Implement task query
        //TODO: Expected "query all" Uri: content://com.google.developer.taskmaker/tasks
        //TODO: Expected "query one" Uri: content://com.google.developer.taskmaker/tasks/{id}
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseContract.TABLE_TASKS);
        switch (sUriMatcher.match(uri))
        {
            case TASKS:

                break;

            case TASKS_WITH_ID:
                qb.appendWhere( DatabaseContract.TaskColumns._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = qb.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //TODO: Implement new task insert
        //TODO: Expected Uri: content://com.google.developer.taskmaker/tasks
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowID;
        switch (sUriMatcher.match(uri))
        {
            case TASKS:
                rowID = db.insert(DatabaseContract.TABLE_TASKS,"", values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if(rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(Uri.parse(DatabaseContract.CONTENT_AUTHORITY+"/tasks"), rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //TODO: Implement existing task update
        //TODO: Expected Uri: content://com.google.developer.taskmaker/tasks/{id}
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (sUriMatcher.match(uri))
        {
            case TASKS:
                rowsUpdated = db.update(DatabaseContract.TABLE_TASKS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TASKS_WITH_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(DatabaseContract.TABLE_TASKS,
                            values,
                            DatabaseContract.TaskColumns._ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = db.update(DatabaseContract.TABLE_TASKS,
                            values,
                            DatabaseContract.TaskColumns._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                //Rows aren't counted with null selection
                selection = (selection == null) ? "1" : selection;
                break;
            case TASKS_WITH_ID:
                long id = ContentUris.parseId(uri);
                selection = String.format("%s = ?", DatabaseContract.TaskColumns._ID);
                selectionArgs = new String[]{String.valueOf(id)};
                break;
            default:
                throw new IllegalArgumentException("Illegal delete URI");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.delete(DatabaseContract.TABLE_TASKS, selection, selectionArgs);

        if (count > 0) {
            //Notify observers of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    /* Initiate a periodic job to clear out completed items */
    private void manageCleanupJob() {
        Log.d(TAG, "Scheduling cleanup job");
        JobScheduler jobScheduler = (JobScheduler) getContext()
                .getSystemService(Context.JOB_SCHEDULER_SERVICE);

        //Run the job approximately every hour
        long jobInterval = 3600000L;

        ComponentName jobService = new ComponentName(getContext(), CleanupJobService.class);
        JobInfo task = new JobInfo.Builder(CLEANUP_JOB_ID, jobService)
                .setPeriodic(jobInterval)
                .setPersisted(true)
                .build();

        if (jobScheduler.schedule(task) != JobScheduler.RESULT_SUCCESS) {
            Log.w(TAG, "Unable to schedule cleanup job");
        }
    }
}
