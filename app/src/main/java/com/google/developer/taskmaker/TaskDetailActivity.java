package com.google.developer.taskmaker;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.developer.taskmaker.data.Task;
import com.google.developer.taskmaker.data.TaskUpdateService;
import com.google.developer.taskmaker.reminders.AlarmScheduler;
import com.google.developer.taskmaker.view.DatePickerFragment;
import com.google.developer.taskmaker.view.TaskTitleView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener {
    Uri taskUri = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        //Task must be passed to this activity as a valid provider Uri
        taskUri = getIntent().getData();

        TaskTitleView description = (TaskTitleView) findViewById(R.id.task_description);
        TextView due = (TextView) findViewById(R.id.task_due_date);
        ImageView priorityImage = (ImageView) findViewById(R.id.priority_indicator_image);

        //TODO: Display attributes of the provided task in the UI
        Cursor cursor = getContentResolver().query(taskUri,null,null,null,null);
        if(cursor.moveToFirst())
        {
            Task task = new Task(cursor);
            description.setText(task.getDescription());
            description.setState(TaskTitleView.NORMAL);
            if(task.hasDueDate())
            {
                DateFormat df = new SimpleDateFormat("MM/dd/YYYY");
                String date= df.format(task.getDueDateMillis());
                due.setText(date);
            }
            else {
                due.setText(R.string.date_empty);
            }

            if(System.currentTimeMillis() > task.getDueDateMillis())
                description.setState(TaskTitleView.OVERDUE);
            if(task.isComplete())
                description.setState(TaskTitleView.DONE);
            if(task.isPriority())
                priorityImage.setImageResource(R.drawable.ic_priority);
            else
                priorityImage.setImageResource(R.drawable.ic_not_priority);
        }

        cursor.close();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case R.id.action_delete:
                TaskUpdateService.deleteTask(this,taskUri);
                finish();
                break;
            case R.id.action_reminder:
                DatePickerFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "datePicker");
                break;
        }

            return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //TODO: Handle date selection from a DatePickerFragment
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        Log.d("TaskDetails","setting alarm ...");
        setAlarm(c.getTimeInMillis());

    }

    public void setAlarm(long alarmTime)
    {
        AlarmScheduler alarmScheduler = new AlarmScheduler();
        alarmScheduler.scheduleAlarm(this, alarmTime,taskUri);
    }
}
