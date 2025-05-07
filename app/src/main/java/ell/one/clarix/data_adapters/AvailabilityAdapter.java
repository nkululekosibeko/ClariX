package ell.one.clarix.data_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;
import ell.one.clarix.R;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(String dateKey);
    }

    private final List<Map<String, Object>> availabilityList;
    private final List<String> dateKeys;
    private final OnDeleteClickListener listener;

    public AvailabilityAdapter(List<Map<String, Object>> availabilityList, List<String> dateKeys, OnDeleteClickListener listener) {
        this.availabilityList = availabilityList;
        this.dateKeys = dateKeys;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, timeRange;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateText);
            timeRange = itemView.findViewById(R.id.timeText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_availability, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> entry = availabilityList.get(position);
        String date = (String) entry.get("date");
        String start = (String) entry.get("startTime");
        String end = (String) entry.get("endTime");

        holder.date.setText("Date: " + date);
        holder.timeRange.setText("Time: " + start + " - " + end);
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(dateKeys.get(position)));
    }

    @Override
    public int getItemCount() {
        return availabilityList.size();
    }
}
