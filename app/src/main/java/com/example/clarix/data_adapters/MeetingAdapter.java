package com.example.clarix.data_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import SandZ.Tutors.R;
import SandZ.Tutors.data.classes.Meeting;

public class MeetingAdapter extends ArrayAdapter<Meeting> {

    public MeetingAdapter(Context context, List<Meeting> meetingList) {
        super(context, R.layout.meeting_item_layout, meetingList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.meeting_item_layout, parent, false);
        }
        Meeting meeting = getItem(position);
        TextView dateView = convertView.findViewById(R.id.dateView);
        TextView teacherName = convertView.findViewById(R.id.teacherName);
        TextView studentName = convertView.findViewById(R.id.studentName);

        if (meeting != null) {
            dateView.setText(meeting.getDateString());
            teacherName.setText(meeting.getTeacher());
            studentName.setText(meeting.getStudent());
        }

        return convertView;
    }
}

