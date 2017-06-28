package com.google.developer.taskmaker.data;

import android.database.Cursor;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.developer.taskmaker.R;
import com.google.developer.taskmaker.view.TaskTitleView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    /* Callback for list item click events */
    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemToggled(boolean active, int position);
    }

    private ArrayList<Task> taskArrayList;

    /* ViewHolder for each task item */
    public class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TaskTitleView nameView;
        public TextView dateView;
        public ImageView priorityView;
        public CheckBox checkBox;

        public TaskHolder(View itemView) {
            super(itemView);

            nameView = (TaskTitleView) itemView.findViewById(R.id.text_description);
            dateView = (TextView) itemView.findViewById(R.id.text_date);
            priorityView = (ImageView) itemView.findViewById(R.id.priority);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == checkBox) {
                completionToggled(this);
            } else {
                postItemClick(this);
            }
        }
    }

    private Cursor mCursor;
    private OnItemClickListener mOnItemClickListener;

    public TaskAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void completionToggled(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemToggled(holder.checkBox.isChecked(), holder.getAdapterPosition());
        }
        holder.checkBox.setChecked(holder.checkBox.isChecked());
    }

    private void postItemClick(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
        }
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task, parent, false);

        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {

        //TODO: Bind the task data to the views


        if (mCursor.moveToFirst()) {
            mCursor.move(position);
            Task task = new Task(mCursor);
            holder.nameView.setText(task.description);
            //holder.nameView.setState(TaskTitleView.NORMAL);
            holder.nameView.setState(TaskTitleView.DONE);
            // Checking if the task is complete
            if (task.isComplete) {
                holder.nameView.setState(TaskTitleView.DONE);
                holder.nameView.setPaintFlags(holder.nameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            holder.checkBox.setChecked(task.isComplete);

            if (task.hasDueDate()) {
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                String date = df.format(task.dueDateMillis);
                holder.dateView.setText(date);
                // Checking for Overdue only if Due date exists
                if (System.currentTimeMillis() > task.dueDateMillis) {
                    holder.nameView.setState(TaskTitleView.OVERDUE);
                }
            } else {
                holder.dateView.setText(R.string.date_empty);
            }

            if (task.isPriority) {
                holder.priorityView.setImageResource(R.drawable.ic_priority);
            } else {
                holder.priorityView.setImageResource(R.drawable.ic_not_priority);
            }
        }


    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    /**
     * Retrieve a {@link Task} for the data at the given position.
     *
     * @param position Adapter item position.
     * @return A new {@link Task} filled with the position's attributes.
     */
    public Task getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return new Task(mCursor);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
