package com.google.developer.taskmaker;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.Task;
import com.google.developer.taskmaker.data.TaskAdapter;
import com.google.developer.taskmaker.data.TaskDbHelper;
import com.google.developer.taskmaker.data.TaskUpdateService;

public class MainActivity extends AppCompatActivity implements
        TaskAdapter.OnItemClickListener,
        View.OnClickListener {

    TaskAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new TaskAdapter(getContentResolver().query(DatabaseContract.CONTENT_URI,null,null,null,getSortOrder()));
        mAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        FloatingActionButton fab =(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent,0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.fab:
                Intent intent = new Intent(this, AddTaskActivity.class);
                startActivity(intent);
                break;
        }

    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {
        //TODO: Handle list item click event
        Intent intent = new Intent(this,TaskDetailActivity.class);
        intent.setData(Uri.parse(DatabaseContract.CONTENT_URI+"/"+mAdapter.getItemId(position)));
        startActivity(intent);
    }

    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, int position) {
        //TODO: Handle task item checkbox event
        Task task = mAdapter.getItem(position);
        Uri uri = Uri.parse(DatabaseContract.CONTENT_URI+"/"+mAdapter.getItemId(position));

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TaskColumns.DESCRIPTION,task.getDescription());
        values.put(DatabaseContract.TaskColumns.DUE_DATE,task.getDueDateMillis());
        Log.d("MainActivity","status "+active);
        if(active)
            values.put(DatabaseContract.TaskColumns.IS_COMPLETE,1);
        else
            values.put(DatabaseContract.TaskColumns.IS_COMPLETE,0);
        values.put(DatabaseContract.TaskColumns.IS_PRIORITY,task.isPriority());
        TaskUpdateService.updateTask(this, uri,values);
        //Delaying UI update till the service is done
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateList();
            }
        }, 50);

    }

    public void updateList()
    {
        Log.d("MainActivity","Updating list");
        mAdapter.swapCursor(getContentResolver().query(DatabaseContract.CONTENT_URI,null,null,null,getSortOrder()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    public String getSortOrder() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sort = sharedPreferences.getString(getResources().getString(R.string.pref_sortBy_key), getResources().getString(R.string.pref_sortBy_default));
        if(sort.equals(getResources().getString(R.string.pref_sortBy_default)))
            sort = DatabaseContract.DEFAULT_SORT;
        else
            sort = DatabaseContract.DATE_SORT;

        Log.d("MainActivity","Sort by : "+sort);
        return sort;
    }
}
