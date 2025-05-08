package ell.one.clarix.data_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ell.one.clarix.R;
import ell.one.clarix.models.TutorModel;

public class TutorAdapter extends RecyclerView.Adapter<TutorAdapter.TutorViewHolder> {

    private final List<TutorModel> tutorList;
    private final OnTutorClickListener clickListener;

    // Constructor now accepts a click listener
    public TutorAdapter(List<TutorModel> tutorList, OnTutorClickListener clickListener) {
        this.tutorList = tutorList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutor, parent, false);
        return new TutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int position) {
        TutorModel tutor = tutorList.get(position);
        holder.nameText.setText(tutor.getName());
        holder.specializationText.setText("Specialization: " + tutor.getSpecialization());
        holder.rateText.setText("Rate: R " + tutor.getRate() + "/hr");
        holder.bioText.setText(tutor.getBio());

        // Set click listener for the entire card/item
        holder.itemView.setOnClickListener(v -> clickListener.onTutorClick(tutor));
    }

    @Override
    public int getItemCount() {
        return tutorList.size();
    }

    public static class TutorViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, specializationText, rateText, bioText;

        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tutorName);
            specializationText = itemView.findViewById(R.id.tutorSpecialization);
            rateText = itemView.findViewById(R.id.tutorRate);
            bioText = itemView.findViewById(R.id.tutorBio);
        }
    }

    // Custom interface to handle clicks
    public interface OnTutorClickListener {
        void onTutorClick(TutorModel tutor);
    }
}
