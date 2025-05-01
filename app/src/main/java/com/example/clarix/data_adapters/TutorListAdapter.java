package com.example.clarix.data_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import SandZ.Tutors.R;
import SandZ.Tutors.data.classes.TeacherClass;

public class TutorListAdapter extends ArrayAdapter<TeacherClass> {

    public TutorListAdapter(Context context, List<TeacherClass> teacherList) {
        super(context, R.layout.tutor_item_layout, teacherList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tutor_item_layout, parent, false);
        }

        TeacherClass teacher = getItem(position);
        ImageView imageView = convertView.findViewById(R.id.profile_picutre);
        TextView nameView = convertView.findViewById(R.id.linkView);
        TextView surnameView = convertView.findViewById(R.id.dateView);
        TextView priceView = convertView.findViewById(R.id.teacherName);
        TextView rateView = convertView.findViewById(R.id.studentName);

        if (teacher != null) {
            int image = teacher.getPicture();
            imageView.setImageResource(image);
            nameView.setText(teacher.getName());
            surnameView.setText(teacher.getSurname());
            priceView.setText(teacher.getStringPrice() + "zł/h");
            rateView.setText(teacher.getRateString());
        }

        return convertView;
    }
}

