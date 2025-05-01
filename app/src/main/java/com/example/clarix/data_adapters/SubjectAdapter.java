package com.example.clarix.data_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.example.clarix.R;

public class SubjectAdapter extends ArrayAdapter<String> {
    private final List<String> teacherSubjects;
    private final List<String> subjects;
    private final List<String> selectedSubjects;

    public SubjectAdapter(Context context, List<String> subjects, List<String> teacherSubjects) {
        super(context, R.layout.subject_item_layout, subjects);
        this.subjects = subjects;
        this.teacherSubjects = teacherSubjects;
        this.selectedSubjects = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subject_item_layout, parent, false);
        }

        String subject = getItem(position);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        TextView textViewItem = convertView.findViewById(R.id.textViewItem);

        if (subject != null) {
            textViewItem.setText(subject);

            boolean isSelected = teacherSubjects.contains(subject);
            checkBox.setChecked(isSelected);

            if (isSelected) {
                selectedSubjects.add(subject);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedSubjects.add(subject);
                } else {
                    selectedSubjects.remove(subject);
                }
            });
        }

        return convertView;
    }
    public List<String> getSelectedSubjects() {
        return selectedSubjects;
    }
}
