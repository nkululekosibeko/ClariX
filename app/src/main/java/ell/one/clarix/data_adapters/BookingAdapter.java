package ell.one.clarix.data_adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ell.one.clarix.R;
import ell.one.clarix.BookingModel;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<BookingModel> bookings;
    private final boolean isTutor;
    private final OnCancelClickListener cancelListener;
    private final OnStatusChangeListener statusChangeListener;

    public interface OnCancelClickListener {
        void onCancelClick(BookingModel booking);
    }

    public interface OnStatusChangeListener {
        void onStatusChange(BookingModel booking, String newStatus);
    }

    public BookingAdapter(List<BookingModel> bookings, boolean isTutor,
                          OnCancelClickListener cancelListener,
                          OnStatusChangeListener statusChangeListener) {
        this.bookings = bookings;
        this.isTutor = isTutor;
        this.cancelListener = cancelListener;
        this.statusChangeListener = statusChangeListener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking = bookings.get(position);

        // Set display values
        holder.name.setText(isTutor
                ? "Tutee: " + booking.getTuteeName()
                : "Tutor: " + booking.getTutorName());

        holder.slot.setText("Date: " + booking.getDate() +
                "\nTime: " + booking.getStartTime() + " - " + booking.getEndTime());

        holder.status.setText("Status: " + booking.getStatus());

        // Cancel button (tutee only & pending)
        if (!isTutor && "pending".equalsIgnoreCase(booking.getStatus())) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> {
                if (cancelListener != null) cancelListener.onCancelClick(booking);
            });
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }

        // Show Meeting Link for Tutee only if status is Confirmed or Completed
        if (!isTutor && booking.getMeetingLink() != null &&
                (booking.getStatus().equalsIgnoreCase("Confirmed")
                        || booking.getStatus().equalsIgnoreCase("Completed"))) {

            holder.meetingLink.setVisibility(View.VISIBLE);
            holder.meetingLink.setText("Join Meeting");
            holder.meetingLink.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(booking.getMeetingLink()));
                v.getContext().startActivity(intent);
            });

        } else {
            holder.meetingLink.setVisibility(View.GONE);
        }

        // Tutor: Status change dropdown
        if (isTutor) {
            holder.statusDropdown.setVisibility(View.VISIBLE);

            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                    holder.itemView.getContext(),
                    R.array.booking_status_options,
                    android.R.layout.simple_spinner_item
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.statusDropdown.setAdapter(spinnerAdapter);

            int selectedIndex = spinnerAdapter.getPosition(booking.getStatus());
            if (selectedIndex >= 0) {
                holder.statusDropdown.setSelection(selectedIndex, false);
            }

            holder.statusDropdown.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                    String selectedStatus = parent.getItemAtPosition(pos).toString();
                    if (!selectedStatus.equalsIgnoreCase(booking.getStatus())) {

                        if (selectedStatus.equalsIgnoreCase("Confirmed")) {
                            // Prompt for meeting link
                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Enter Meeting Link");

                            final EditText input = new EditText(holder.itemView.getContext());
                            input.setHint("https://meet.google.com/...");
                            input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                            builder.setView(input);

                            builder.setPositiveButton("Save", (dialog, which) -> {
                                String link = input.getText().toString().trim();
                                booking.setStatus("Confirmed");
                                booking.setMeetingLink(link);

                                if (statusChangeListener != null) {
                                    statusChangeListener.onStatusChange(booking, "Confirmed");
                                }
                                notifyItemChanged(holder.getAdapterPosition());
                            });

                            builder.setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.cancel();
                                int prevIndex = spinnerAdapter.getPosition(booking.getStatus());
                                holder.statusDropdown.setSelection(prevIndex, false);
                            });

                            builder.show();
                        } else {
                            booking.setStatus(selectedStatus);
                            booking.setMeetingLink(null);
                            if (statusChangeListener != null) {
                                statusChangeListener.onStatusChange(booking, selectedStatus);
                            }
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        } else {
            holder.statusDropdown.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView name, slot, status, meetingLink;
        Button cancelButton;
        Spinner statusDropdown;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.bookingTutorName);
            slot = itemView.findViewById(R.id.bookingSlot);
            status = itemView.findViewById(R.id.bookingStatus);
            cancelButton = itemView.findViewById(R.id.cancelBookingBtn);
            statusDropdown = itemView.findViewById(R.id.statusDropdown);
            meetingLink = itemView.findViewById(R.id.meetingLinkText);
        }
    }
}
